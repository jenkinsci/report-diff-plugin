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

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.Job;
import hudson.model.Run;

import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import jenkins.model.RunAction2;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.StaplerProxy;


public class RpmsReportAction implements Action, StaplerProxy, SimpleBuildStep.LastBuildAction, RunAction2 {

    private static final Logger LOG = Logger.getLogger(RpmsReportAction.class.getName());

    private Run<?, ?> run;
    /**
     * Field name used before pipeline support (up to report-diff 4.6). Kept only so XStream can still
     * read old build.xml files ({@code <build class="build" reference="../../.."/>}); its value is
     * migrated to {@link #run} and it is never written again.
     */
    @Deprecated
    private AbstractBuild<?, ?> build;
    private transient RpmsReportPublisher publisher;

    public RpmsReportAction(Run<?, ?> run) {
        this.run = run;
    }
    
    public RpmsReportAction(Run<?, ?> run, RpmsReportPublisher publisher) {
        this.run = run;
        this.publisher = publisher;
    }

    @Override
    public void onAttached(Run<?, ?> r) {
        this.run = r;
    }

    @Override
    public void onLoad(Run<?, ?> r) {
        // builds stored by older versions have the run in the legacy "build" field (or nowhere);
        // Jenkins always tells us which run we belong to, so trust it
        this.run = r != null ? r : build;
        this.build = null;
    }

    public RpmsReportPublisher getPublisher() {
        if (publisher != null) {
            return publisher;
        }
        Job<?, ?> job = run.getParent();
        if (job instanceof AbstractProject) {
            AbstractProject<?, ?> project = (AbstractProject<?, ?>) job;
            DescribableList<Publisher, Descriptor<Publisher>> l = project.getPublishersList();
            for (Publisher p : l.toArray(new Publisher[0])) {
                if (p instanceof RpmsReportPublisher) {
                    publisher = (RpmsReportPublisher) p;
                    return publisher;
                }
            }
        }
        return null;
    }

    @Override
    public String getIconFileName() {
        return "clipboard.png";
    }

    @Override
    public String getDisplayName() {
        return DefaultStrings.MAIN_TITLE_REPORT;
    }

    @Override
    public String getUrlName() {
        return DefaultStrings.RPMS_URL;
    }


    public String getDiffUrlName() {
        String thisId = "0";
        String prevId = "0";
        if (run != null) {
            thisId = run.getId();
            Run<?, ?> bb = run.getPreviousNotFailedBuild();
            if (bb != null) {
                prevId = bb.getId();
            }
        }
        return DefaultStrings.PATCH_URL + "/" + DefaultStrings.DIFF_COMPUTED_URL +
                "?from=" + thisId + "&to=" + prevId + "&ids=.*&context=-1";
    }


    @Override
    public RpmsReport getTarget() {
        return new RpmsReport(getPublisher(), run);
    }


    @Override
    public Collection<? extends Action> getProjectActions() {
        // Jenkins core collects project actions of all LastBuildActions of the last successful build
        // in one loop (SimpleBuildStep.LastBuildActionFactory). Any exception thrown here would drop
        // project actions (and thus charts) of all other plugins too, so never let it escape.
        try {
            if (run == null) {
                LOG.log(Level.WARNING, "RpmsReportAction has no run attached, no project action created");
                return Collections.emptyList();
            }
            Job<?, ?> job = run.getParent();
            return Collections.singleton(new RpmsReportProjectAction(job));
        } catch (RuntimeException | LinkageError ex) {
            LOG.log(Level.WARNING, "Failed to create rpms project action for " + run, ex);
            return Collections.emptyList();
        }
    }

}
