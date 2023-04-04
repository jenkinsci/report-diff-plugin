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
package io.jenkins.plugins.report.genericdiff;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
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


public class RpmsReportPublisher extends Recorder {

    private boolean moveunstable;
    private boolean movefailed;
    private boolean moveempty;
    private List<RpmsReportOneRecord> configurations;
    private int buildstopast;
    private static final Logger LOGGER = Logger.getLogger(RpmsReportPublisher.class.getName());

    @DataBoundConstructor
    public RpmsReportPublisher(int buildstopast, boolean moveunstable, boolean movefailed, boolean moveempty, List<RpmsReportOneRecord> configurations) {
        this.configurations = configurations;
        this.buildstopast = buildstopast;
        this.moveunstable = moveunstable;
        this.movefailed = movefailed;
        this.moveempty = moveempty;
    }


    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        boolean result = true;
        List<String> changeditems = new ArrayList<>();
        List<String> failedItems = new ArrayList<>();
        List<String> emptyItems = new ArrayList<>();
        for (RpmsReportOneRecord configuration : getConfigurations()) {
            RpmsReportPublisherImpl.Result run = new RpmsReportPublisherImpl(configuration.getCommand(), configuration.getId()).perform(build);
            if (run.isChanged()) {
                changeditems.add(configuration.getId() + "(" + configuration.getCommand() + ")");
            }
            if (run.isBroken()) {
                failedItems.add(configuration.getId() + "(" + configuration.getCommand() + ")");
            } else {
                if (run.isEmpty()) {
                    emptyItems.add(configuration.getId() + "(" + configuration.getCommand() + ")");
                }
            }
        }
        RpmsReportAction action = new RpmsReportAction(build);
        build.addAction(action);
        FormValidation fv = doCheckConfigurations(configurations);
        listener.getLogger().println("[diff plugin] ID validation " + fv.kind + " " + fv.getMessage());
        listener.getLogger().println("[diff plugin] found changes in: " + changeditems);
        listener.getLogger().println("[diff plugin] possible error in: " + failedItems);
        listener.getLogger().println("[diff plugin] empty readings: " + emptyItems);
        if (isMoveunstable() && changeditems.size() > 0) {
            build.setResult(Result.UNSTABLE);
        }
        if (isMovefailed() && failedItems.size() > 0) {
            build.setResult(Result.FAILURE);
        }
        if (isMoveempty() && emptyItems.size() > 0) {
            build.setResult(Result.FAILURE);
        }
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
     */
    public FormValidation doCheckConfigurations(@QueryParameter List<RpmsReportOneRecord> configurations) {
        Set<String> uniq = new HashSet<>();
        List<String> uniqRepor = new ArrayList<>(configurations.size());
        for (RpmsReportOneRecord configuration : configurations) {
            String value = configuration.getId();
            if (value == null || value.trim().isEmpty()) {
                return FormValidation.error("ID must not be empty (and must be unique)");
            }
            if (!value.matches("[" + RpmsReportOneRecord.ID_BASE + "]+")) {
                return FormValidation.error("ID must be just letters " + RpmsReportOneRecord.ID_BASE+ " (and must be unique). Is " + value);
            }
            uniq.add(value);
            uniqRepor.add(value);
        }
        if (uniq.size() != configurations.size()) {
            return FormValidation.error("IDs must be unique. Are:" + uniqRepor.toString());
        }
        return FormValidation.ok();
    }


    public FormValidation doValidateIds(@QueryParameter List<RpmsReportOneRecord> configurations) {
        try {
            return doCheckConfigurations(configurations);
        } catch (Exception e) {
            return FormValidation.error("Client error : " + e.getMessage());
        }
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

    public boolean isMovefailed() {
        return movefailed;
    }

    public boolean isMoveunstable() {
        return moveunstable;
    }

    public boolean isMoveempty() {
        return moveempty;
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

    @DataBoundSetter
    public void setMoveunstable(boolean moveunstable) {
        this.moveunstable = moveunstable;
    }

    @DataBoundSetter
    public void setMovefailed(boolean movefailed) {
        this.movefailed = movefailed;
    }

    @DataBoundSetter
    public void setMoveempty(boolean moveempty) {
        this.moveempty = moveempty;
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
