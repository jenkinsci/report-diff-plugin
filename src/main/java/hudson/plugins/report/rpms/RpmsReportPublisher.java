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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import static hudson.plugins.report.rpms.Constants.RPMS_COMMAND_STDERR;
import static hudson.plugins.report.rpms.Constants.RPMS_LIST_FILES;

public class RpmsReportPublisher extends Recorder {

    private String command;

    @DataBoundConstructor
    public RpmsReportPublisher(String command) {
        this.command = command;
    }

    private RpmsReport executeCommand() throws IOException, InterruptedException {
        if (command == null || command.trim().isEmpty()) {
            return new RpmsReport("No command specified", null);
        }
        Process process = new ProcessBuilder(command.trim().split(" ")).start();
        OutputReader stdoutReader = new OutputReader(process.getInputStream()).start();
        OutputReader stderrReader = new OutputReader(process.getErrorStream()).start();
        process.waitFor(60, TimeUnit.SECONDS);
        String stdout = stdoutReader.getResult();
        if (stdout != null) {
            String[] lines = stdout.trim().split("\n");
            List<String> list = Arrays.stream(lines).filter(s -> s != null && s.length() > 0).sorted().collect(Collectors.toList());
            return new RpmsReport(stderrReader.getResult(), list);
        }
        return new RpmsReport(stderrReader.getResult(), null);
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        RpmsReport report = executeCommand();
        Files.write(new File(build.getRootDir(), RPMS_LIST_FILES).toPath(), report.getRpms(), Charset.forName("UTF-8"), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        Files.write(new File(build.getRootDir(), RPMS_COMMAND_STDERR).toPath(), report.getStderr().getBytes("UTF-8"), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
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
                result = sb.toString();
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
}
