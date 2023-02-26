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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;


public class RpmsReportPublisher extends Recorder {

    private List<RpmsReportOneRecord> configurations;
    private int buildstopast;
    private static final Logger LOGGER = Logger.getLogger(RpmsReportPublisher.class.getName());

    @DataBoundConstructor
    public RpmsReportPublisher(int buildstopast, List<RpmsReportOneRecord> configurations) {
        this.configurations = configurations;
        this.buildstopast = buildstopast;
    }


    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        boolean result = true;
        for (RpmsReportOneRecord configuration : getConfigurations()) {
            boolean run = new RpmsReportPublisherImpl(configuration.getCommand(), configuration.getId()).perform(build, launcher, listener);
            result = run && result;
        }
        RpmsReportAction action = new RpmsReportAction(build);
        build.addAction(action);
        return result;
    }


    public void validateId() {
        FormValidation r = doCheckConfigurations(configurations);
        if (r.kind != FormValidation.Kind.OK) {
            LOGGER.log(Level.SEVERE, r.getMessage());
        }
    }

    /**
     * It seems that jenkins is unable to verify repeatble element. So we cal this manually by validate if
     *
     * @param configurations
     * @return
     * @throws IOException
     * @throws ServletException
     */
    public FormValidation doCheckConfigurations(@QueryParameter List<RpmsReportOneRecord> configurations) {
        Set<String> uniq = new HashSet<>();
        List<String> uniqRepor = new ArrayList<>(configurations.size());
        for (RpmsReportOneRecord configuration : configurations) {
            String value = configuration.getId();
            if (value == null || value.trim().isEmpty()) {
                return FormValidation.error("ID must not be empty (and must be unique)");
            }
            if (!value.matches("[a-zA-Z]+")) {
                return FormValidation.error("ID must be just letters a-zA-Z (and must be unique). Is " + value);
            }
            uniq.add(value);
            uniqRepor.add(value);
        }
        if (uniq.size() != configurations.size()) {
            return FormValidation.error("IDs must be unique. Are:" + uniqRepor.toString());
        }
        return FormValidation.ok();

    }

    @Override
    public BuildStepDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    public List<RpmsReportOneRecord> getConfigurations() {
        validateId();
        return configurations;
    }

    public int getBuildstopast() {
        return buildstopast <= 0 ? 10 : buildstopast;
    }

    @DataBoundSetter
    public void setConfigurations(List<RpmsReportOneRecord> configurations) {
        this.configurations = configurations;
        validateId();
    }

    @DataBoundSetter
    public void setBuildstopast(int buildstopast) {
        this.buildstopast = buildstopast;
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
