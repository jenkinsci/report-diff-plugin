package io.jenkins.plugins.report.genericdiff;

import java.util.HashMap;
import java.util.Map;

public class DefaultStrings {


    public static String get(String value, String defaultValue) {
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        } else {
            return value;
        }
    }


    public static final String MAIN_TITLE = "Diff chart"; //"RPM Changes";
    public static final String NO_CHANGES = "No Changes"; //"No RPM Changes";
    public static final String UPDATED_LINES = "Changed lines"; //"RPMs updated";
    public static final String ADDED_LINES = "Added lines"; //"RPMs installed";
    public static final String REMOVED_LINES = "Removed lines"; //"RPMs removed";
    public static final String ERROR_TITLE = "Command stderr output";
    public static final String ADDED_LINES_LONG = "New lines:"; //"Newly installed RPMs:";
    public static final String REMOVED_LINES_LONG = "Removed lines:"; //Removed RPMs:";
    public static final String ALL_LINES_LONG = "All lines:"; //"All RPMs:";
    public static final String ADDED_LINES_SHORT = "Added"; //"Installed";
    public static final String REMOVED_LINES_SHORT = "Deleted"; //"Removed";
    public static final String ALL_LINES_SHORT = "All"; //"Total";

    //no setup-able plugin title
    public static final String MAIN_TITLE_REPORT = "Diff chart report"; //"Installed RPMs report";
}
