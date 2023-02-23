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

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.IOException;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;


public class RpmsReportOneDiff extends Recorder {

    private String command = null;
    private String id = null;
    private String maintitle = DefaultStrings.MAIN_TITLE.get("init");
    private String nochanges = DefaultStrings.NO_CHANGES.get("init");
    private String updatedlines = DefaultStrings.UPDATED_LINES.get("init");
    private String addedlines = DefaultStrings.ADDED_LINES.get("init");
    private String removedlines = DefaultStrings.REMOVED_LINES.get("init");
    private String errortitle = DefaultStrings.ERROR_TITLE.get("init");
    private String addedlineslong = DefaultStrings.ADDED_LINES_LONG.get("init");
    private String removedlineslong = DefaultStrings.REMOVED_LINES_LONG.get("init");
    private String alllineslong = DefaultStrings.ALL_LINES_LONG.get("init");
    private String addedlinesshort = DefaultStrings.ADDED_LINES_SHORT.get("init");
    private String removedlinesshort = DefaultStrings.REMOVED_LINES_SHORT.get("init");
    private String alllinesshort = DefaultStrings.ALL_LINES_SHORT.get("init");
    private String lastProject = null;

    @DataBoundConstructor
    public RpmsReportOneDiff(String command, String id, String maintitle, String nochanges, String updatedlines, String addedlines, String removedlines, String errortitle, String addedlineslong,
            String removedlineslong, String alllineslong, String addedlinesshort, String removedlinesshort, String alllinesshort) {
        this.command = command;
        this.id = id;
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
        setAll();
    }

    public void setLastProject(String lastProject) {
        this.lastProject = lastProject;
    }

    public String getLastProject() {
        return lastProject;
    }

    public void setAll() {
        if (lastProject != null) {
            DefaultStrings.MAIN_TITLE.set(lastProject, maintitle);
            DefaultStrings.NO_CHANGES.set(lastProject, nochanges);
            DefaultStrings.UPDATED_LINES.set(lastProject, updatedlines);
            DefaultStrings.ADDED_LINES.set(lastProject, addedlines);
            DefaultStrings.REMOVED_LINES.set(lastProject, removedlines);
            DefaultStrings.ERROR_TITLE.set(lastProject, errortitle);
            DefaultStrings.ADDED_LINES_LONG.set(lastProject, addedlineslong);
            DefaultStrings.REMOVED_LINES_LONG.set(lastProject, removedlineslong);
            DefaultStrings.ADDED_LINES_LONG.set(lastProject, alllineslong);
            DefaultStrings.ADDED_LINES_SHORT.set(lastProject, addedlinesshort);
            DefaultStrings.REMOVED_LINES_SHORT.set(lastProject, removedlinesshort);
            DefaultStrings.ADDED_LINES_SHORT.set(lastProject, alllinesshort);
        }
    }

    public String getCommand() {
        return command;
    }

    public String getId() {
        return id;
    }

    public String getMaintitle() {
        return maintitle;
    }

    public String getNochanges() {
        return nochanges;
    }

    public String getUpdatedlines() {
        return updatedlines;
    }

    public String getAddedlines() {
        return addedlines;
    }

    public String getRemovedlines() {
        return removedlines;
    }

    public String getErrortitle() {
        return errortitle;
    }

    public String getAddedlineslong() {
        return addedlineslong;
    }

    public String getRemovedlineslong() {
        return removedlineslong;
    }

    public String getAlllineslong() {
        return alllineslong;
    }

    public String getAddedlinesshort() {
        return addedlinesshort;
    }

    public String getRemovedlinesshort() {
        return removedlinesshort;
    }

    public String getAlllinesshort() {
        return alllinesshort;
    }

    @DataBoundSetter
    public void setCommand(String command) {
        this.command = command;
        setAll();
    }

    public void setId(String id) {
        this.id = id;
        setAll();
    }

    @DataBoundSetter
    public void setMaintitle(String maintitle) {
        this.maintitle = maintitle;
        setAll();
    }

    @DataBoundSetter
    public void setNochanges(String nochanges) {
        this.nochanges = nochanges;
        setAll();
    }

    @DataBoundSetter
    public void setUpdatedlines(String updatedlines) {
        this.updatedlines = updatedlines;
        setAll();
    }

    @DataBoundSetter
    public void setAddedlines(String addedlines) {
        this.addedlines = addedlines;
        setAll();
    }

    @DataBoundSetter
    public void setRemovedlines(String removedlines) {
        this.removedlines = removedlines;
        setAll();
    }

    @DataBoundSetter
    public void setErrortitle(String errortitle) {
        this.errortitle = errortitle;
        setAll();
    }

    @DataBoundSetter
    public void setAddedlineslong(String addedlineslong) {
        this.addedlineslong = addedlineslong;
        setAll();
    }

    @DataBoundSetter
    public void setRemovedlineslong(String removedlineslong) {
        this.removedlineslong = removedlineslong;
        setAll();
    }

    @DataBoundSetter
    public void setAlllineslong(String alllineslong) {
        this.alllineslong = alllineslong;
        setAll();
    }

    @DataBoundSetter
    public void setAddedlinesshort(String addedlinesshort) {
        this.addedlinesshort = addedlinesshort;
        setAll();
    }

    @DataBoundSetter
    public void setRemovedlinesshort(String removedlinesshort) {
        this.removedlinesshort = removedlinesshort;
        setAll();
    }

    @DataBoundSetter
    public void setAlllinesshort(String alllinesshort) {
        this.alllinesshort = alllinesshort;
        setAll();
    }

}
