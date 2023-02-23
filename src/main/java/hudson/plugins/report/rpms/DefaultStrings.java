package hudson.plugins.report.rpms;

import java.util.HashMap;
import java.util.Map;

public class DefaultStrings {

    static class DynamicProvider {

        private final String defaultValue;

        public DynamicProvider(String defaultValue) {
            this.defaultValue = defaultValue;
        }

        private final Map<String, String> valuesPerProject = new HashMap<>();

        public String get(String project) {
            String r =  valuesPerProject.getOrDefault(project, defaultValue);
            if (r.trim().isEmpty()) {
                return defaultValue;
            } else {
                return r;
            }
        }

        public void set(String project, String value) {
            valuesPerProject.put(project, value);
        }

    }

    public static final DynamicProvider MAIN_TITLE = new DynamicProvider("Diff chart"); //"RPM Changes";
    public static final DynamicProvider NO_CHANGES = new DynamicProvider("No Changes"); //"No RPM Changes";
    public static final DynamicProvider UPDATED_LINES = new DynamicProvider("Changed lines"); //"RPMs updated";
    public static final DynamicProvider ADDED_LINES = new DynamicProvider("Added lines"); //"RPMs installed";
    public static final DynamicProvider REMOVED_LINES = new DynamicProvider("Removed lines"); //"RPMs removed";
    public static final DynamicProvider ERROR_TITLE = new DynamicProvider("Command stderr output");
    public static final DynamicProvider ADDED_LINES_LONG = new DynamicProvider("New lines:"); //"Newly installed RPMs:";
    public static final DynamicProvider REMOVED_LINES_LONG = new DynamicProvider("Removed lines:"); //Removed RPMs:";
    public static final DynamicProvider ALL_LINES_LONG = new DynamicProvider("All lines:"); //"All RPMs:";
    public static final DynamicProvider ADDED_LINES_SHORT = new DynamicProvider("Added"); //"Installed";
    public static final DynamicProvider REMOVED_LINES_SHORT = new DynamicProvider("Deleted"); //"Removed";
    public static final DynamicProvider ALL_LINES_SHORT = new DynamicProvider("All"); //"Total";

    //this one is immutable:(
    public static final String MAIN_TITLE_REPORT = "Diff chart report"; //"Installed RPMs report";
}
