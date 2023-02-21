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

import hudson.model.Action;
import hudson.model.Job;
import hudson.model.Run;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static hudson.plugins.report.rpms.Constants.RPMS_ALL;
import static hudson.plugins.report.rpms.Constants.RPMS_NEW;
import static hudson.plugins.report.rpms.Constants.RPMS_REMOVED;

public class RpmsReportProjectAction implements Action {

    private final Job<?, ?> job;

    public RpmsReportProjectAction(Job<?, ?> job) {
        this.job = job;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return DefaultStrings.MAIN_TITLE;
    }

    @Override
    public String getUrlName() {
        return "rpms";
    }

    public List<RpmsReportChartPoint> getChartData() {
        List<RpmsReportChartPoint> list = new ArrayList<>();
        for (Run run : job.getBuilds()) {

            File allFile = new File(run.getRootDir(), RPMS_ALL);
            if (!checkFile(allFile)) {
                continue;
            }

            list.add(new RpmsReportChartPoint(
                    run.getNumber(),
                    run.getDisplayName(),
                    fileLines(new File(run.getRootDir(), RPMS_NEW)),
                    fileLines(new File(run.getRootDir(), RPMS_REMOVED)),
                    fileLines(allFile)));

            if (list.size() == 10) {
                break;
            }
        }
        Collections.reverse(list);
        return list;
    }

    private boolean checkFile(File f) {
        return f.exists() && f.isFile() && f.canRead();
    }

    private int fileLines(File f) {
        if (checkFile(f)) {
            try {
                int number = Files.readAllLines(f.toPath()).size();
                return number;
            } catch (Exception ignore) {
            }
        }
        return 0;
    }

    public String getChartInstalled() {
        return DefaultStrings.ADDED_LINES_SHORT;
    }

    public String getChartTotal() {
        return DefaultStrings.ALL_LINES_SHORT;
    }

    public String getChartRemoved() {
        return DefaultStrings.REMOVED_LINES_SHORT;
    }

}
