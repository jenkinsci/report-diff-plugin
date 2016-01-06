/*
 * The MIT License
 *
 * Copyright 2016 user.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package hudson.plugins.report.rpms;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import static hudson.plugins.report.rpms.Constants.RPMS_ALL;
import static hudson.plugins.report.rpms.Constants.RPMS_COMMAND_STDERR;
import static hudson.plugins.report.rpms.Constants.RPMS_NEW;
import static hudson.plugins.report.rpms.Constants.RPMS_REMOVED;

public class RpmsReportPublisher extends Recorder {

    private String command;

    @DataBoundConstructor
    public RpmsReportPublisher(String command) {
        this.command = command;
    }

    private List<String> executeCommand() throws OperationFailedException {
        if (command == null || command.trim().isEmpty()) {
            throw new OperationFailedException("No command specified");
        }
        try {
            Process process = new ProcessBuilder(command.trim().split(" ")).start();
            OutputReader stdoutReader = new OutputReader(process.getInputStream()).start();
            OutputReader stderrReader = new OutputReader(process.getErrorStream()).start();
            if (!process.waitFor(60, TimeUnit.SECONDS)) {
                throw new OperationFailedException("Command time out");
            }
            if (stderrReader.getResult() != null) {
                throw new OperationFailedException(stderrReader.getResult());
            }
            String stdout = stdoutReader.getResult();
            if (stdout == null) {
                throw new OperationFailedException("Command produced no output");
            }
            List<String> list = StringSpliterator.splitString(stdout, "\n")
                    .filter(s -> s != null && s.length() > 0)
                    .sorted()
                    .collect(Collectors.toList());
            return list;
        } catch (OperationFailedException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new OperationFailedException(ex.toString());
        }
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        try {

            List<String> rpms = executeCommand();
            Files.write(new File(build.getRootDir(), RPMS_ALL).toPath(), rpms, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

            AbstractBuild previousBuild = build.getPreviousBuild();
            if (previousBuild != null) {
                File prevRpmsFile = new File(previousBuild.getRootDir(), RPMS_ALL);
                if (prevRpmsFile.exists() && prevRpmsFile.isFile() && prevRpmsFile.canRead()) {
                    Set<String> prevRpms = new HashSet(Files.readAllLines(prevRpmsFile.toPath()));

                    // rpms list is already saved to file, we can modify it now:
                    Iterator<String> rpmsIterator = rpms.iterator();
                    while (rpmsIterator.hasNext()) {
                        String rpm = rpmsIterator.next();
                        if (prevRpms.remove(rpm)) {
                            rpmsIterator.remove();
                        }
                    }

                    List<String> removedRpms = prevRpms.stream().sorted().collect(Collectors.toList());

                    Files.write(new File(build.getRootDir(), RPMS_NEW).toPath(), rpms, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
                    Files.write(new File(build.getRootDir(), RPMS_REMOVED).toPath(), removedRpms, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

                }
            }

        } catch (OperationFailedException ex) {
            Files.write(new File(build.getRootDir(), RPMS_COMMAND_STDERR).toPath(), Arrays.asList(ex.getMessage()), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        }
        RpmsReportAction action = new RpmsReportAction(build);
        build.addAction(action);
        return true;
    }

    @Override
    public BuildStepDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    public String getCommand() {
        return command;
    }

    @DataBoundSetter
    public void setCommand(String command) {
        this.command = command;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public String getDisplayName() {
            return "Installed RPMs report";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

    }

    private class OutputReader implements Runnable {

        private final InputStream stream;
        private String result;

        public OutputReader(InputStream stream) {
            this.stream = stream;
        }

        public String getResult() {
            return result;
        }

        @Override
        public void run() {
            try (InputStreamReader in = new InputStreamReader(stream, "UTF-8")) {
                StringBuilder sb = new StringBuilder();
                char[] buffer = new char[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    sb.append(buffer, 0, read);
                }
                if (sb.length() > 0) {
                    result = sb.toString();
                }
            } catch (Exception ex) {
                result = ex.toString();
            }
        }

        public OutputReader start() {
            Thread t = new Thread(this);
            t.setPriority(Thread.MIN_PRIORITY);
            t.setName("RPMs Report - Output Reader - '" + command + "'");
            t.start();
            return this;
        }

    }

    private class OperationFailedException extends Exception {

        public OperationFailedException(String message) {
            super(message);
        }

    }
}
