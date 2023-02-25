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

import java.util.List;

public class RpmsReportSingle {

    private final String stderr;
    private final List<String> newRpms;
    private final List<String> removedRpms;
    private final List<String> allRpms;
    private RpmsReportOneRecord publisher;

    public RpmsReportSingle(RpmsReportOneRecord publisher, String stderr, List<String> newRpms, List<String> removedRpms, List<String> allRpms) {
        this.publisher = publisher;
        this.stderr = stderr;
        this.newRpms = newRpms;
        this.removedRpms = removedRpms;
        this.allRpms = allRpms;
    }

    public RpmsReportOneRecord getPublisher() {
        return publisher;
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

    public String getErrorHeader() {
        return publisher.getErrortitle();
    }

    public String getAddedHeader() {
        return publisher.getAddedlineslong();
    }

    public String getRemovedHeader() {
        return publisher.getRemovedlineslong();
    }

    public String getAllHeader() {
        return publisher.getAlllineslong();
    }


}
