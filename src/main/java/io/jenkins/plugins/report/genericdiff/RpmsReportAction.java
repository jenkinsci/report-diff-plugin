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
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.Job;

import java.util.Collection;
import java.util.Collections;

import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.StaplerProxy;


public class RpmsReportAction implements Action, StaplerProxy, SimpleBuildStep.LastBuildAction {

    private final AbstractBuild<?, ?> build;

    public RpmsReportAction(AbstractBuild<?, ?> build) {
        this.build = build;
    }

    private RpmsReportPublisher getPublisher() {
        DescribableList<Publisher, Descriptor<Publisher>> l = build.getProject().getPublishersList();
        for (Publisher p : l.toArray(new Publisher[0])) {
            if (p instanceof RpmsReportPublisher) {
                return (RpmsReportPublisher) p;
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


    @Override
    public RpmsReport getTarget() {
        return new RpmsReport(getPublisher(), build);
    }



    @Override
    public Collection<? extends Action> getProjectActions() {
        Job<?, ?> job = build.getParent();
        return Collections.singleton(new RpmsReportProjectAction(job));
    }

}
