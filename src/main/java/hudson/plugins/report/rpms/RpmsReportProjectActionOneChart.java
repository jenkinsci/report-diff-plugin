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

import hudson.model.Job;
import hudson.model.Run;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class RpmsReportProjectActionOneChart {

    private final RpmsReportOneRecord publisher;
    private final RpmsReportPublisher parent;
    private final Job<?, ?> job;

    public RpmsReportProjectActionOneChart(RpmsReportOneRecord publisher, RpmsReportPublisher parent, Job<?, ?> job) {
        this.publisher = publisher;
        this.parent = parent;
        this.job = job;
    }



    public String getDisplayName() {
        return publisher.getMaintitle();
    }


    public List<RpmsReportChartPoint> getChartData() {
        List<RpmsReportChartPoint> list = new ArrayList<>();
        for (Run run : job.getBuilds()) {

            File allFile = new File(run.getRootDir(), Constants.getALL(publisher.getId()));
            if (!checkFile(allFile)) {
                continue;
            }

            list.add(new RpmsReportChartPoint(
                    run.getNumber(),
                    run.getDisplayName(),
                    fileLines(new File(run.getRootDir(), Constants.getNEW(publisher.getId()))),
                    fileLines(new File(run.getRootDir(), Constants.getREMOVED(publisher.getId()))),
                    fileLines(allFile)));

            if (list.size() == parent.getBuildstopast()) {
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
        return publisher.getAddedlinesshort();
    }

    public String getChartTotal() {
        return publisher.getAlllinesshort();
    }

    public String getChartRemoved() {
        return publisher.getRemovedlinesshort();
    }

    public String getId() {
        return publisher.getId();
    }

}
