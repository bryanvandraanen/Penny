package main.java.penny.constants;

/**
 * Command-line interface constants.  For program execution using a shell/command-line, represent the major
 * and minor command-line arguments that can be passed to the program to enable different program usages.
 */
public class CLIConstants {

    // MAJOR COMMANDS
    /** Command corresponding to live market scanning and classification execution */
    public static final String LIVE_SCAN_COMMAND = "--live";

    /** Command corresponding to the help text describing the usage of the program execution */
    public static final String HELP_COMMAND = "--help";

    /** Command corresponding to the stock database loading and analysis execution */
    public static final String ANALYZE_COMMAND = "--analyze";

    /** Command corresponding to the contrived market data program simulation */
    public static final String SPOOF_COMMAND = "--spoof";

    // MINOR COMMANDS
    /**
     * Live command - indicates that the most active dollar volume stocks should be scanned rather
     * than the entire OTC
     */
    public static final String PUMP_SCANNER_COMMAND = "-mostactive";

    /**
     * Live/Spoof command - indicates that the classification results should be output to the log specified
     * following this argument
     */
    public static final String OUTPUT_TO_LOG_COMMAND = "-log";

    /**
     * Live/Spoof command - serializes all scanned stocks to the constant database directory
     * (specified in SerializationConstants)
     */
    public static final String SERIALIZE_COMMAND = "-serialize";

    /**
     * Spoof command - delivers the contrived market data in parallel rather than sequentially
     */
    public static final String SPOOF_DELIVER_IN_PARALLEL_COMMAND = "-parallel";

    /**
     * Analysis command - appends the analyzed stocks in CSV format to the CSV file specified following
     * this argument
     */
    public static final String APPEND_TO_CSV_FILE = "-csv";
}
