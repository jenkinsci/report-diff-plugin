package hudson.plugins.report.rpms;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;

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

    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        if (build == null ) { //spotbugs issue, otherwise never hit
            throw new IOException("No build found");
        }
        try {
            FilePath ws = build.getWorkspace();
            if (ws == null ) { //spotbugs issue, otherwise never hit
                throw new IOException("No workspace found");
            }
            List<String> rpms = ws.act(new CommandCallable(command));
            Files.write(new File(build.getRootDir(), Constants.getALL(id)).toPath(), rpms, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

            AbstractBuild previousBuild = build.getPreviousBuild();
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

                    Files.write(new File(build.getRootDir(), Constants.getNEW(id)).toPath(), rpms, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
                    Files.write(new File(build.getRootDir(), Constants.getREMOVED(id)).toPath(), removedRpms, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);

                }
            }

        } catch (OperationFailedException ex) {
            Files.write(new File(build.getRootDir(), Constants.getCOMMAND_STDERR(id)).toPath(), Arrays.asList(ex.getMessage()), StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        }
        return true;
    }
}
