package io.jenkins.plugins.report.genericdiff;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class RpmsReportOneRecordTest {

    @Test
    void sanitizeIdTest() {
        Assertions.assertEquals("aaa", RpmsReportOneRecord.sanitizeId("aaa"));
        Assertions.assertEquals("aaaddd", RpmsReportOneRecord.sanitizeId("aaa ddd"));
        Assertions.assertEquals("aaaddd", RpmsReportOneRecord.sanitizeId("-aaa-ddd-"));
        Assertions.assertEquals("scriptalertaaaascript", RpmsReportOneRecord.sanitizeId("<script>alert(\"aaaa\")</script>"));
    }
}