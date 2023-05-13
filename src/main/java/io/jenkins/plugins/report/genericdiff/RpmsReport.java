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

import java.util.ArrayList;
import java.util.List;

public class RpmsReport {

    private final RpmsReportPublisher publisher;
    private final List<RpmsReportActionOneSummary> summary;
    private final List<RpmsReportSingle> index;
    private final AbstractBuild<?, ?> build;

    public RpmsReport(RpmsReportPublisher publisher, AbstractBuild<?, ?> build) {
        this.publisher = publisher;
        this.build = build;
        summary = new ArrayList<>();
        index = new ArrayList<>();
        for(RpmsReportOneRecord record: this.publisher.getConfigurations()) {
            RpmsReportActionOneSummary summary1 = new RpmsReportActionOneSummary(build, record);
            RpmsReportSingle index1 = new RpmsReportSingle(record, summary1.getStderr(), summary1.getNewRpms(), summary1.getRemovedRpms(), summary1.getAllRpms());
            summary.add(summary1);
            index.add(index1);
        }
    }

    public List<RpmsReportActionOneSummary> getSummary() {
        return summary;
    }

    public List<RpmsReportSingle> getIndex() {
        return index;
    }

    public String getDisplayName() {
        return DefaultStrings.MAIN_TITLE_REPORT;
    }

    public String getRun() {
        return build.getId();
    }

    public int getBuildNumber() {
        return build.getNumber();
    }

    public String getPreviousLink() {
        return "../../" + (getBuildNumber() - 1) + "/"+DefaultStrings.RPMS_URL;
    }

    public String getPreviousLinkName() {
        return " << " + (getBuildNumber() - 1) + " << ";
    }

    public String getNextLink() {
        return "../../" + (getBuildNumber() + 1) + "/"+DefaultStrings.RPMS_URL;
    }

    public String getNextLinkName() {
        return " >> " + (getBuildNumber() + 1) + " >> ";
    }

}
