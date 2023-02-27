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


import hudson.model.AbstractBuild;

import java.util.ArrayList;
import java.util.List;

public class RpmsReport {

    private final RpmsReportPublisher publisher;
    private final List<RpmsReportActionOneSummary> reports;

    public RpmsReport(RpmsReportPublisher publisher, AbstractBuild<?, ?> build) {
        this.publisher = publisher;
        reports = new ArrayList<>();
        for(RpmsReportOneRecord record: this.publisher.getConfigurations()) {
            RpmsReportActionOneSummary r = new RpmsReportActionOneSummary(build, record);
            reports.add(r);
        }
    }

    public List<RpmsReportActionOneSummary> getReports() {
        return reports;
    }
}
