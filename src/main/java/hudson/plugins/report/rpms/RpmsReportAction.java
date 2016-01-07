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

import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.Job;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.StaplerProxy;

import static hudson.plugins.report.rpms.Constants.RPMS_ALL;
import static hudson.plugins.report.rpms.Constants.RPMS_COMMAND_STDERR;
import static hudson.plugins.report.rpms.Constants.RPMS_NEW;
import static hudson.plugins.report.rpms.Constants.RPMS_REMOVED;

public class RpmsReportAction implements Action, StaplerProxy, SimpleBuildStep.LastBuildAction {

    private final AbstractBuild<?, ?> build;

    public RpmsReportAction(AbstractBuild<?, ?> build) {
        this.build = build;
    }

    @Override
    public String getIconFileName() {
        return "clipboard.png";
    }

    @Override
    public String getDisplayName() {
        return "Installed RPMs";
    }

    @Override
    public String getUrlName() {
        return "rpms";
    }

    @Override
    public RpmsReport getTarget() {
        List<String> newRpms = readFile(RPMS_NEW);
        List<String> removedRpms = readFile(RPMS_REMOVED);
        List<String> allRpms = readFile(RPMS_ALL);
        List<String> stderrs = readFile(RPMS_COMMAND_STDERR);
        return new RpmsReport(
                stderrs == null ? null : stderrs.stream().findFirst().orElse(null),
                newRpms,
                removedRpms,
                allRpms);
    }

    private List<String> readFile(String fileName) {
        File file = new File(build.getRootDir(), fileName);
        if (file.exists() && file.isFile() && file.canRead()) {
            try {
                return Files
                        .lines(file.toPath(), StandardCharsets.UTF_8)
                        .collect(Collectors.toList());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return null;
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        Job<?, ?> job = build.getParent();
        if (/* getAction(Class) produces a StackOverflowError */!Util.filter(job.getActions(), RpmsReportProjectAction.class).isEmpty()) {
            // JENKINS-26077: someone like XUnitPublisher already added one
            return Collections.emptySet();
        }
        return Collections.singleton(new RpmsReportProjectAction(job));
    }

}
