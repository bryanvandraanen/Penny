package main.java.penny.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * FileOutputEcho represents a singleton logger that echoes all console output to a collection of maintained files.
 *
 * FileOutputEcho allows for simple logging through intuitive, standard print statements to system console output.  The
 * logger can accommodate numerous independent output files at once and offers selective control on when logging
 * is enabled and disabled throughout execution.  While this logger is unified across all files, suspending
 * logging is not a thread-safe action.
 */
public class FileOutputEcho {

    /** The singleton output logger echoing system console output */
    private static FileOutputEcho out;

    /** Map from file names to output writers of all output files currently replicating system output */
    private Map<String, FileWriter> outFiles;

    /** Flag indicating whether logging is currently paused */
    private boolean isPaused;

    /**
     * Constructs a new FileOutputEcho that is not paused and is attached to the standard system console output
     * with no active output files to log to.
     */
    private FileOutputEcho() {
        this.outFiles = new HashMap<String, FileWriter>();
        this.isPaused = false;

        // Construct a PrintStream that echoes standard system output to available log files.
        PrintStream fileOutputStream = new PrintStream(System.out) {
            @Override
            public void println() {
                this.println("");
            }

            @Override
            public void println(boolean x) {
                this.println(Boolean.toString(x));
            }

            @Override
            public void println(char x) {
                this.println(Character.toString(x));
            }

            @Override
            public void println(int x) {
                this.println(Integer.toString(x));
            }

            @Override
            public void println(long x) {
                this.println(Long.toString(x));
            }

            @Override
            public void println(float x) {
                this.println(Float.toString(x));
            }

            @Override
            public void println(double x) {
                this.println(Double.toString(x));
            }

            @Override
            public void println(char x[]) {
                this.println(Arrays.toString(x));
            }

            @Override
            public void println(Object x) {
                this.println(x.toString());
            }

            @Override
            public void println(String x) {
                this.print(x);
                this.print(System.lineSeparator());
            }

            @Override
            public void print(String x) {
                super.print(x);

                if (!isPaused()) {
                    for (String fileName : outFiles.keySet()) {
                        FileWriter outFile = outFiles.get(fileName);
                        try {
                            outFile.write(x);
                        } catch (IOException e) {
                            super.println("Failed to write to file: " + fileName);
                        }
                    }
                }
            }
        };

        System.setOut(fileOutputStream);
    }

    /**
     * Returns the singleton instance of the FileOutputEcho.
     */
    public static FileOutputEcho getInstance() {
        if (out == null) {
            out = new FileOutputEcho();
        }

        return out;
    }

    /**
     * Adds the output file to this FileOutputEcho for logging.
     *
     * @param fileName The name of the file to attach to this logger and echo system console output to
     * @throws IOException If the output file cannot be added to this logger
     */
    public void addOutputFile(String fileName) throws IOException {
        FileWriter outFile = new FileWriter(fileName);
        this.outFiles.put(fileName, outFile);
    }

    /**
     * Closes the output file in this FileOutputEcho stopping any future logging to the file specified.
     *
     * @param fileName The name of the file to cease echoing system console output to
     * @throws IOException If the output file cannot be closed in this logger
     */
    public void closeOutputFile(String fileName) throws IOException {
        if (this.outFiles.containsKey(fileName)) {
            FileWriter outFile = this.outFiles.get(fileName);
            this.outFiles.remove(fileName);

            outFile.close();
        }
    }

    /**
     * Closes all output files in this FileOutputEcho stopping any future logging of system console output
     * to all files maintained by this FileOutputEcho.
     */
    public void closeOutputFiles() {
        for (String fileName : this.outFiles.keySet()) {
            try {
                this.closeOutputFile(fileName);
            } catch (IOException e) {
                // Simply continue with closing other output files
            }
        }
    }

    /**
     * Pauses the system console output logging to all files maintained by this FileOutputEcho.  In other words,
     * temporarily stops echoing system output to the files maintained by this FileOutputEcho.  This operation
     * is not toggleable - see resumeFileOutputEcho for continuing file logging.
     */
    public void pauseFileOutputEcho() {
        this.isPaused = true;
    }

    /**
     * Resumes the system console output logging to all files maintained by this FileOutputEcho.  In other words,
     * removes any suspension on echoing system output to files maintained by this FileOutputEcho.  This operation
     * is not toggleable - see pauseFileOutputEcho for pausing file logging.
     */
    public void resumeFileOutputEcho() {
        this.isPaused = false;
    }

    /**
     * Returns true if system console output echoing is currently suspended for all files maintained by this
     * FileOutputEcho, and false otherwise.
     */
    public boolean isPaused() {
        return this.isPaused;
    }
}
