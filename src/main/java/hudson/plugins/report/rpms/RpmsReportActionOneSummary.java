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


import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class RpmsReportActionOneSummary {


    private final String stderr;
    private final List<String> newRpms;
    private final List<String> removedRpms;
    private final List<String> allRpms;

    private final RpmsReportOneRecord publisher;
    private final AbstractBuild<?, ?> build;

    public RpmsReportActionOneSummary(AbstractBuild<?, ?> build, RpmsReportOneRecord publisher) {
        this.publisher = publisher;
        this.build = build;
        newRpms = readFile(Constants.getNEW(getID()));
        removedRpms = readFile(Constants.getREMOVED(getID()));
        allRpms = readFile(Constants.getALL(getID()));
        List<String> lstderrs = readFile(Constants.getCOMMAND_STDERR(getID()));
        stderr = lstderrs == null ? null : lstderrs.stream().findFirst().orElse(null);

    }

    private RpmsReportOneRecord getPublisher() {
        return publisher;
    }


    public String getDisplayName() {
        return getPublisher().getMaintitle();
    }

    public String getNoChanges() {
        return getPublisher().getNochanges();
    }

    public String getChanged() {
        return getPublisher().getUpdatedlines();
    }

    public String getAdded() {
        return getPublisher().getAddedlines();
    }

    public String getRemoved() {
        return getPublisher().getRemovedlines();
    }

    public String getID() {
        return getPublisher().getId();
    }


    private List<String> readFile(String fileName) {
        File file = new File(build.getRootDir(), fileName);
        if (file.exists() && file.isFile() && file.canRead()) {
            try (Stream<String> stream = Files.lines(file.toPath(), StandardCharsets.UTF_8)) {
                return stream.collect(Collectors.toList());
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return null;
    }

    public String getStderr() {
        return stderr;
    }

    public List<String> getNewRpms() {
        return newRpms;
    }

    public List<String> getRemovedRpms() {
        return removedRpms;
    }

    public List<String> getAllRpms() {
        return allRpms;
    }

    public int getNewRpmsSize() {
        return newRpms == null ? -1 : +newRpms.size();
    }

    public int getRemovedRpmsSize() {
        return removedRpms == null ? -1 : removedRpms.size();
    }

    public int getAllRpmsSize() {
        return allRpms == null ? -1 : allRpms.size();
    }
}

