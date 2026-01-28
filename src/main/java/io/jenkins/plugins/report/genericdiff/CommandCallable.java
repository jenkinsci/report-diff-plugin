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
package io.jenkins.plugins.report.genericdiff;

import hudson.FilePath.FileCallable;
import hudson.remoting.VirtualChannel;
import jenkins.MasterToSlaveFileCallable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandCallable extends MasterToSlaveFileCallable<List<String>> {

    private static final Logger LOG = LoggerFactory.getLogger(CommandCallable.class);
    private final String command;

    public CommandCallable(String command) {
        if (command == null || command.trim().isEmpty()) {
            throw new IllegalArgumentException("No command specified");
        }
        this.command = command;
    }

    /*
     * Called when job finishes, cache diff and full listing for future diffing
     */
    @Override
    public List<String> invoke(File f, VirtualChannel vchannel) throws IOException, InterruptedException {
        try {
            String stdout = null;
            File kandidateFile = new File(f, command.trim());
            if (kandidateFile.exists()) {
                LOG.info("Reading " + command.trim() + " (" + kandidateFile.getAbsolutePath() + ")");
                stdout = FileToString(kandidateFile);
            } else if (mayBeArchive(kandidateFile, ".zip") != null) {
                LOG.info("Getting from Zip " + command.trim());
                stdout = FileFromZipToString(kandidateFile);
            } else {
                LOG.info("Executing `" + command.trim() + "` in" + f.toString() + " (" + f.getAbsolutePath() + ")");
                Process process = new ProcessBuilder(command.trim().split(" "))
                        .directory(f)
                        .start();
                OutputReader stdoutReader = new OutputReader(command, process.getInputStream()).start();
                OutputReader stderrReader = new OutputReader(command, process.getErrorStream()).start();
                if (!process.waitFor(60, TimeUnit.SECONDS)) {
                    throw new OperationFailedException("Command time out");
                }
                LOG.info("Returned " + process.exitValue());
                Thread.sleep(10); //giving time to pipes to finish
                try {
                    process.getInputStream().close();
                } catch (Exception ex) {
                }
                try {
                    process.getErrorStream().close();
                } catch (Exception ex) {
                }
                Thread.sleep(10); //giving time to pipes to finish
                LOG.info("sout read " + stdoutReader.getBytes());
                LOG.info("serr read " + stderrReader.getBytes());
                if (stderrReader.getResult() != null) {
                    throw new OperationFailedException(stderrReader.getResult());
                }
                stdout = stdoutReader.getResult();
            }
            if (stdout == null) {
                throw new OperationFailedException("Command produced no output");
            }
            List<String> list = StringSpliterator.splitString(stdout, "\n")
                    .filter(s -> s != null && s.length() > 0)
                    .sorted()
                    .collect(Collectors.toList());
            return list;
        } catch (OperationFailedException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new OperationFailedException(ex.toString());
        }
    }

    private String FileToString(File filePath) throws IOException {
        try (FileInputStream fis = new FileInputStream(filePath)) {
            return readStream(fis);
        }
    }

    private File mayBeArchive(File filePath, String suffix) throws IOException {
        while (filePath.getParent() != null) {
            filePath = filePath.getParentFile();
            if (filePath.exists() && filePath.isFile() && filePath.toString().endsWith(suffix)) {
                return filePath;
            }
        }
        return null;
    }

    private String FileFromZipToString(File filePath) throws IOException {
        File zipFilePath = mayBeArchive(filePath, ".zip");
        if (zipFilePath == null) {
            throw new IOException("No file found on path");
        }
        String zipItem = filePath.toString().replace(zipFilePath.toString(), "");
        while (zipItem.startsWith("/") || zipItem.startsWith("\\")) {
            zipItem = zipItem.substring(1);
        }
        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().equals(zipItem)) {
                    try (InputStream is = zipFile.getInputStream(entry)) {
                        return readStream(is);
                    }
                }
            }
        }
        throw new IOException("Item " + zipItem + " not found in " + zipFilePath);
    }

    public static String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder(512);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            while (true) {
                String s = br.readLine();
                if (s == null) {
                    break;
                }
                sb.append(s).append("\n");
            }
        }
        return sb.toString();
    }

    private static class OutputReader implements Runnable {

        private final String command;
        private final InputStream stream;
        private String result;
        private int bytes = 0;

        public OutputReader(String command, InputStream stream) {
            this.command = command;
            this.stream = stream;
        }

        public String getResult() {
            return result;
        }

        public int getBytes() {
            return bytes;
        }
        

        @Override
        public void run() {
            try (InputStreamReader in = new InputStreamReader(stream, "UTF-8")) {
                StringBuilder sb = new StringBuilder();
                char[] buffer = new char[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    bytes+=read;
                    sb.append(buffer, 0, read);
                }
                if (sb.length() > 0) {
                    result = sb.toString();
                }
            } catch (Exception ex) {
                result = ex.toString();
            }
        }

        public OutputReader start() {
            Thread t = new Thread(this);
            t.setPriority(Thread.MIN_PRIORITY);
            t.setName("RPMs Report - Output Reader - '" + command + "'");
            t.start();
            return this;
        }

    }

}
