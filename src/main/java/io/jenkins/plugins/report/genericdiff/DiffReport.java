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


import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.patch.Patch;


import java.util.List;

import hudson.model.AbstractBuild;

public class DiffReport {

    private final AbstractBuild<?, ?> build;
    private final RpmsReport data;

    public DiffReport(RpmsReportPublisher publisher, AbstractBuild<?, ?> build) {
        this.build = build;
        data = new RpmsReport(publisher, build);
        List<RpmsReportSingle> files = data.getIndex();
        for(RpmsReportSingle report: files) {
            Patch<String> diff = DiffUtils.diff(l0, l1);
            List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(name0, name1, l0, diff, Math.max(lo.size, l1.size));
        }
    }

    public String getDisplayName() {
        return DefaultStrings.DIFF_TITLE_REPORT;
    }

    public String getRun() {
        return build.getId();
    }

    public int getBuildNumber() {
        return build.getNumber();
    }

    public String getPreviousLink() {
        return "../../" + (getBuildNumber() - 1) + "/"+DefaultStrings.PATCH_URL;
    }

    public String getPreviousLinkName() {
        return " << " + (getBuildNumber() - 1) + " << ";
    }

    public String getNextLink() {
        return "../../" + (getBuildNumber() + 1) + "/"+DefaultStrings.PATCH_URL;
    }

    public String getNextLinkName() {
        return " >> " + (getBuildNumber() + 1) + " >> ";
    }

}
