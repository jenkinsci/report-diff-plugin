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
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebMethod;
import org.kohsuke.stapler.verb.GET;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
        if (interesting == null || interesting.length == 0 || interesting[0].isEmpty()) {
            interesting = new String[]{".*"};
        }
        String sContext = req.getParameter("context");
        if (sContext == null || sContext.isEmpty()) {
            sContext = "-1";
        }
        long context = -1;
        try {
            context = Long.parseLong(sContext);
        } catch (NumberFormatException e) {
            context = -1;
        }

        String sFrom = req.getParameter("from");
        String sTo = req.getParameter("to");

        if (sFrom == null || sTo == null || sFrom.isEmpty() || sTo.isEmpty()) {
            res.setStatus(404);
            out.print("expected from and to arguments at least");
            out.flush();
            return;
        }

        try {
            long from = Long.parseLong(sFrom);
            long to = Long.parseLong(sTo);
        } catch (NumberFormatException e) {
            res.setStatus(404);
            out.print("from and to are supposed to be numbers");
            out.flush();
            return;
        }

        long from = Long.parseLong(sFrom);
        long to = Long.parseLong(sTo);

        if (from <= 0 || to <= 0) {
            res.setStatus(404);
            out.print("from and to have to be positive");
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
                List<String> resultLines = comapre(bFrom.getId(), bTo.getId(), dataFrom, dataTo, interesting, context);
                for (String resultLine : resultLines) {
                    out.println(resultLine);
                }
            }
        }
        out.flush();
    }

    private List<String> comapre(String id1, String id2,
                                 RpmsReport data1, RpmsReport data2,
                                 String[] interesting, long contex) {
        List<RpmsReportSingle> files1 = data1.getIndex();
        List<RpmsReportSingle> files2 = data2.getIndex();
        Set<String> idsu = new HashSet<>();
        idsu.addAll(files1.stream().map(r -> r.getPublisher().getId()).collect(Collectors.toList()));
        idsu.addAll(files2.stream().map(r -> r.getPublisher().getId()).collect(Collectors.toList()));
        List<String> ids = new ArrayList<>(idsu);
        Collections.sort(ids);

        List<String> resultLines = new ArrayList<>();
        for (String id : ids) {
            boolean wished = false;
            for (String s : interesting) {
                if (id.matches(s)) {
                    wished = true;
                    break;
                }
            }
            if (!wished) {
                continue;
            }
            List<String> thisComaprsion1 = new ArrayList<>(0);
            List<String> thisComaprsion2 = new ArrayList<>(0);
            for (RpmsReportSingle report : files1) {
                if (report.getPublisher().getId().equals(id)) {
                    if (report.getAllRpms() != null) {
                        thisComaprsion1 = report.getAllRpms();
                    }
                }
            }
            for (RpmsReportSingle report : files2) {
                if (report.getPublisher().getId().equals(id)) {
                    if (report.getAllRpms() != null) {
                        thisComaprsion2 = report.getAllRpms();
                    }
                }
            }
            Patch<String> diff = DiffUtils.diff(thisComaprsion2, thisComaprsion1);
            int usedContext = (int) contex;
            if (contex < 0) {
                usedContext = Math.max(thisComaprsion2.size(), thisComaprsion1.size());
            }
            List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(
                    id2 + "/" + id, id1 + "/" + id,
                    thisComaprsion2, diff,
                    usedContext);
            if (unifiedDiff.isEmpty()) {
                resultLines.add("--- " + id2 + "/" + id);
                resultLines.add("+++ " + id1 + "/" + id);
                resultLines.add("@@ " + id2 + "/" + id + " and " + id1 + "/" + id + " are identical @@");
                if (contex < 0) {
                    resultLines.addAll(thisComaprsion1);
                } else {
                    resultLines.add(" ");
                }
            } else {
                resultLines.addAll(unifiedDiff);
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
