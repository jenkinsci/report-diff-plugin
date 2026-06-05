package io.jenkins.plugins.report.genericdiff;

import hudson.FilePath;
import hudson.model.Run;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class RpmsReportPublisherImpl {

    private final String command;
    private final String id;

    public RpmsReportPublisherImpl(String command, String id) {
        this.command = command;
        this.id = id;
    }

    public static class Result {
        int total = 0;
        int changes = 0;
        boolean broken = false;

        public boolean isEmpty() {
            return total == 0;
        }

        public boolean isChanged() {
            return changes != 0;
        }

        public boolean isBroken() {
            return broken;
        }
    }

    public Result perform(Run<?, ?> run, FilePath workspace) throws InterruptedException, IOException {
        if (run == null) { //spotbugs issue, otherwise never hit
            throw new IOException("No build found");
        }
        Result r = new Result();
        try {
            if (workspace == null) { //spotbugs issue, otherwise never hit
                throw new IOException("No workspace found");
            }
            List<String> rpms = workspace.act(new CommandCallable(command));
            Files.write(new File(run.getRootDir(), Constants.getALL(id)).toPath(), rpms, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            r.total=rpms.size();
            Run<?, ?> previousBuild = run.getPreviousBuild();
            if (previousBuild != null) {
                File prevRpmsFile = new File(previousBuild.getRootDir(), Constants.getALL(id));
                if (prevRpmsFile.exists() && prevRpmsFile.isFile() && prevRpmsFile.canRead()) {
                    Set<String> prevRpms = new HashSet(Files.readAllLines(prevRpmsFile.toPath()));

                    // rpms list is already saved to file, we can modify it now:
                    Iterator<String> rpmsIterator = rpms.iterator();
                    while (rpmsIterator.hasNext()) {
                        String rpm = rpmsIterator.next();
                        if (prevRpms.remove(rpm)) {
                            rpmsIterator.remove();
                        }
                    }

                    List<String> removedRpms = prevRpms.stream().sorted().collect(Collectors.toList());
                    Files.write(new File(run.getRootDir(), Constants.getNEW(id)).toPath(), rpms, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
                    r.changes+=rpms.size();
                    Files.write(new File(run.getRootDir(), Constants.getREMOVED(id)).toPath(), removedRpms, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
                    r.changes+=removedRpms.size();

                }
            }

        } catch (OperationFailedException ex) {
            Files.write(new File(run.getRootDir(), Constants.getCOMMAND_STDERR(id)).toPath(), Arrays.asList(ex.getMessage()), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            r.broken = true;
        }
        return r;
    }
}
