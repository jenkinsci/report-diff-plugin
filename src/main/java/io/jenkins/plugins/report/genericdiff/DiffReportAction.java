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


import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.GET;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import hudson.model.AbstractBuild;
import hudson.model.Action;
import jenkins.tasks.SimpleBuildStep;

public class DiffReportAction implements SimpleBuildStep.LastBuildAction {

    private final AbstractBuild<?, ?> build;

    public DiffReportAction(AbstractBuild<?, ?> build) {
        this.build = build;
    }

    private RpmsReportPublisher getRpmsReport(AbstractBuild<?, ?> build) {
        final RpmsReportPublisher data;
        Optional<RpmsReportPublisher> publisher =
                (build.getProject().getPublishersList().stream().filter(p -> p instanceof RpmsReportPublisher).map(a -> (RpmsReportPublisher) a).findFirst());
        if (publisher.isPresent()) {
            data = publisher.get();
        } else {
            data = null;
        }
        return data;
    }

    public String getDisplayName() {
        return DefaultStrings.DIFF_TITLE_REPORT;
    }

    @GET
    @WebMethod(name = DefaultStrings.DIFF_COMPUTED_URL)
    public void doSearchOffset(StaplerRequest req, StaplerResponse res) throws IOException {
        res.setContentType("text/plain");
        res.setCharacterEncoding("UTF-8");
        PrintWriter out = res.getWriter();

        String[] interesting = req.getParameterValues("ids");
        if (interesting == null) {
            interesting = new String[]{".*"};
        }
        try {
            long from = Long.parseLong(req.getParameter("from"));
            long to = Long.parseLong(req.getParameter("to"));
        } catch (NumberFormatException e) {
            res.setStatus(404);
            out.print("Couldn't parse input");
            out.flush();
            return;
        }

        long from = Long.parseLong(req.getParameter("from"));
        long to = Long.parseLong(req.getParameter("to"));

        if (from <= 0 || to <= 0) {
            res.setStatus(404);
            out.print("Invalid parameters");
        } else {
            AbstractBuild<?, ?> bFrom = this.build.getProject().getBuild(from + "");
            AbstractBuild<?, ?> bTo = this.build.getProject().getBuild(to + "");
            RpmsReportPublisher pFrom = getRpmsReport(bFrom);
            RpmsReportPublisher pTo = getRpmsReport(bTo);
            if (pFrom == null && pTo == null) {
                res.setStatus(404);
                out.print("builds " + from + " nor " + to + " have RpmsReportPublisher");
            } else if (pFrom == null) {
                res.setStatus(404);
                out.print("build " + from + " do not have have RpmsReportPublisher");
            } else if (pTo == null) {
                res.setStatus(404);
                out.print("build " + to + " do not have have RpmsReportPublisher");
            } else {
                RpmsReport dataFrom = new RpmsReport(pFrom, bFrom);
                RpmsReport dataTo = new RpmsReport(pTo, bTo);
                List<String> resultLines = comapre(dataFrom, dataTo, interesting);
                for (String resultLine : resultLines) {
                    out.println(resultLine);
                }
            }
        }
        out.flush();
    }

    private List<String> comapre(RpmsReport data1, RpmsReport data2, String[] interesting) {
        List<String> resultLines = new ArrayList<>();
        List<RpmsReportSingle> files1 = data1.getIndex();
        //first gather all ids, then iterate through both sets.
        //if some of them do not have that id, then all + or -...
        // and also if it mathes any of the interesting regexes...
        for (RpmsReportSingle report : files1) {
            if (report.getAllRpms() != null) {
                resultLines.add(report.getPublisher().getId());
                resultLines.addAll(report.getAllRpms());
//            Patch<String> diff = DiffUtils.diff(l0, l1);
//            List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(name0, name1, l0, diff, Math.max(lo
//            .size, l1.size));
            }
        }
        List<RpmsReportSingle> files2 = data2.getIndex();
        for (RpmsReportSingle report : files2) {
            if (report.getAllRpms() != null) {
                resultLines.add(report.getPublisher().getId());
                resultLines.addAll(report.getAllRpms());
//            Patch<String> diff = DiffUtils.diff(l0, l1);
//            List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(name0, name1, l0, diff, Math.max(lo
//            .size, l1.size));
            }
        }
        return resultLines;
    }

    @Override
    public Collection<? extends Action> getProjectActions() {
        ArrayList<Action> list = new ArrayList<>();
        return list;
    }

    @Override
    public String getIconFileName() {
        return "clipboard.png";
    }

    @Override
    public String getUrlName() {
        return DefaultStrings.PATCH_URL;
    }

    public String getThisBuild() {
        if (build == null) {
            return "0";
        } else {
            return build.getId();
        }
    }

    public String getPreviousBuild() {
        if (build == null) {
            return "0";
        } else {
            AbstractBuild<?, ?> previouSbuild = build.getPreviousNotFailedBuild();
            if (previouSbuild == null) {
                return "0";
            } else {
                return previouSbuild.getId();
            }
        }
    }

    public List<String> getDiffIds() {
        if (build == null) {
            return Arrays.asList(".*");
        }
        RpmsReportPublisher pFrom = getRpmsReport(build);
        if (pFrom == null) {
            return Arrays.asList(".*");
        }
        List<String> r = new ArrayList<>();
        for (RpmsReportOneRecord on : pFrom.getConfigurations()) {
            r.add(on.getId());
        }
        return r;
    }


}
