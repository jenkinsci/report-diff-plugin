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

import java.io.IOException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;


public class RpmsReportPublisher extends Recorder {

    private String command;
    private String maintitle;
    private String nochanges;
    private String updatedlines;
    private String addedlines;
    private String removedlines;
    private String errortitle;
    private String addedlineslong;
    private String removedlineslong;
    private String alllineslong;
    private String addedlinesshort;
    private String removedlinesshort;
    private String alllinesshort;

    @DataBoundConstructor
    public RpmsReportPublisher(String command, String maintitle, String nochanges, String updatedlines, String addedlines, String removedlines, String errortitle, String addedlineslong,
                               String removedlineslong, String alllineslong, String addedlinesshort, String removedlinesshort, String alllinesshort) {
        this.command = command;
        this.maintitle = maintitle;
        this.nochanges = nochanges;
        this.updatedlines = updatedlines;
        this.addedlines = addedlines;
        this.removedlines = removedlines;
        this.errortitle = errortitle;
        this.addedlineslong = addedlineslong;
        this.removedlineslong = removedlineslong;
        this.alllineslong = alllineslong;
        this.addedlinesshort = addedlinesshort;
        this.removedlinesshort = removedlinesshort;
        this.alllinesshort = alllinesshort;
    }


    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        return new RpmsReportPublisherImpl(command).perform(build, launcher, listener);
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

    public String getMaintitle() {
        return DefaultStrings.get(maintitle, DefaultStrings.MAIN_TITLE);
    }

    public String getNochanges() {
        return DefaultStrings.get(nochanges, DefaultStrings.NO_CHANGES);
    }

    public String getUpdatedlines() {
        return DefaultStrings.get(updatedlines, DefaultStrings.UPDATED_LINES);
    }

    public String getAddedlines() {
        return DefaultStrings.get(addedlines, DefaultStrings.ADDED_LINES);
    }

    public String getRemovedlines() {
        return DefaultStrings.get(removedlines, DefaultStrings.REMOVED_LINES);
    }

    public String getErrortitle() {
        return DefaultStrings.get(errortitle, DefaultStrings.ERROR_TITLE);
    }

    public String getAddedlineslong() {
        return DefaultStrings.get(addedlineslong, DefaultStrings.ADDED_LINES_LONG);
    }

    public String getRemovedlineslong() {
        return DefaultStrings.get(removedlineslong, DefaultStrings.REMOVED_LINES_LONG);
    }

    public String getAlllineslong() {
        return DefaultStrings.get(alllineslong, DefaultStrings.ALL_LINES_LONG);
    }

    public String getAddedlinesshort() {
        return DefaultStrings.get(addedlinesshort, DefaultStrings.ADDED_LINES_SHORT);
    }

    public String getRemovedlinesshort() {
        return DefaultStrings.get(removedlinesshort, DefaultStrings.REMOVED_LINES_SHORT);
    }

    public String getAlllinesshort() {
        return DefaultStrings.get(alllinesshort, DefaultStrings.ALL_LINES_SHORT);
    }

    @DataBoundSetter
    public void setCommand(String command) {
        this.command = command;
    }

    @DataBoundSetter
    public void setMaintitle(String maintitle) {
        this.maintitle = maintitle;
    }

    @DataBoundSetter
    public void setNochanges(String nochanges) {
        this.nochanges = nochanges;
    }

    @DataBoundSetter
    public void setUpdatedlines(String updatedlines) {
        this.updatedlines = updatedlines;
    }

    @DataBoundSetter
    public void setAddedlines(String addedlines) {
        this.addedlines = addedlines;
    }

    @DataBoundSetter
    public void setRemovedlines(String removedlines) {
        this.removedlines = removedlines;
    }

    @DataBoundSetter
    public void setErrortitle(String errortitle) {
        this.errortitle = errortitle;
    }

    @DataBoundSetter
    public void setAddedlineslong(String addedlineslong) {
        this.addedlineslong = addedlineslong;
    }

    @DataBoundSetter
    public void setRemovedlineslong(String removedlineslong) {
        this.removedlineslong = removedlineslong;
    }

    @DataBoundSetter
    public void setAlllineslong(String alllineslong) {
        this.alllineslong = alllineslong;
    }

    @DataBoundSetter
    public void setAddedlinesshort(String addedlinesshort) {
        this.addedlinesshort = addedlinesshort;
    }

    @DataBoundSetter
    public void setRemovedlinesshort(String removedlinesshort) {
        this.removedlinesshort = removedlinesshort;
    }

    @DataBoundSetter
    public void setAlllinesshort(String alllinesshort) {
        this.alllinesshort = alllinesshort;
    }

    @Extension
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();

    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public String getDisplayName() {
            return DefaultStrings.MAIN_TITLE_REPORT;
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

    }

}
