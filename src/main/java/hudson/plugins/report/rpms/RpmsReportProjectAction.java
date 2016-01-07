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
import java.util.Collections;
import java.util.List;

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
        return "RPM changes";
    }

    @Override
    public String getUrlName() {
        return "rpms";
    }

    public List<String> getNewRpms() {
        return readFile(RPMS_NEW);
    }

    public List<String> getRemovedRpms() {
        return readFile(RPMS_REMOVED);
    }

    private List<String> readFile(String fileName) {
        try {
            Run build = job.getLastBuild();
            if (build != null) {
                File file = new File(build.getRootDir(), fileName);
                if (file.exists() && file.isFile() && file.canRead()) {
                    return Files.readAllLines(file.toPath());
                }
            }
        } catch (Exception ignore) {
        }
        return Collections.emptyList();
    }

}
