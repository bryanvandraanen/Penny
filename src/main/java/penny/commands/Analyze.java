package main.java.penny.commands;

import main.java.penny.constants.CLIConstants;
import main.java.penny.marketdata.*;
import main.java.penny.models.classification.ClassifiedStockTick;
import main.java.penny.util.CommandUtil;
import main.java.penny.util.StockCalendar;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static main.java.penny.constants.SerializationConstants.*;

/**
 * Analysis execution of previously serialized stock results of Penny.
 *
 * Analyze executes Penny in a reviewing mode capable of displaying and assessing the values of previously
 * scanned stocks and their tick information.  Allows for reclassification of historically scanned stocks
 * and possible updates to existing datasets.  This program usage supports optional arguments that include
 * appending loaded stock tick data in CSV format to an output data file.
 */
public class Analyze {

    /**
     * Executes this command with the provided (minor) arguments.  Throws an Exception if this command
     * fails to execute.
     */
    public static void execute(String[] args) throws Exception {
        // Initialize tickers to analyze and other optional minor commands
        Set<String> analyzeTickers = new HashSet<String>();
        File csvAppendFile = init(args, analyzeTickers);

        // Initialize a local market data to house all loaded stock tick information
        MarketData marketData = new MarketData();

        // Load the serialized stock tick directory from the default file paths expected
        Date date = StockCalendar.getInstance().getTime();
        String monthDir = MONTH_DIR_FORMAT.format(date);
        String dayDir = DAY_DIR_FORMAT.format(date);

        Path path = Paths.get(DATABASE_ROOT_DIR, DATABASE_DIR, monthDir, dayDir);

        File dir = new File(path.toString());
        File[] files = dir.listFiles();

        StockTickResults results = marketData.getStockTickResults();

        // Load all the stock tick results from the date specified into these stock tick results
        CommandUtil.deserialize(files, results, analyzeTickers);

        Collection<StockTick> ticks = results.getStockTicks();

        // Classify all loaded stock ticks without filter
        List<ClassifiedStockTick> classifications = CommandUtil.classify(ticks, null /* No filter */);

        // If no explicit stock symbols were indicated in the CLI arguments, output all classifications instead
        boolean onlyPositiveResults = analyzeTickers.isEmpty();
        CommandUtil.output(classifications, onlyPositiveResults);

        // Only analyze the specific tick values of tickers if they were explicitly provided
        if (!analyzeTickers.isEmpty()) {
            CommandUtil.analyze(analyzeTickers, results);
        }

        // Append the results to the CSV file provided if specified
        if (csvAppendFile != null) {
            CommandUtil.appendCSV(csvAppendFile, classifications, analyzeTickers);
        }
    }

    /**
     * Initializes this program execution extracting and configuring the arguments accordingly.  Updates the
     * tickers to analyze accordingly with all the argument-specified stock tickers  Additionally, returns the
     * output CSV file if specified in the provided arguments, or null if no file location is provided.
     */
    private static File init(String[] args, Set<String> analyzeTickers) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Expected date in format mm/dd/yyyy when analyzing.");
        }

        String stringDate = args[0];
        String[] split = stringDate.split("/");

        if (split.length != 3) {
            throw new IllegalArgumentException("Expected date in format mm/dd/yyyy when analyzing.");
        }

        // Configure the 'current' date of the unified calendar based on the month, day, and year
        int month = Integer.parseInt(split[0]);
        int day = Integer.parseInt(split[1]);
        int year = Integer.parseInt(split[2]);

        Date date = new GregorianCalendar(year, month - 1, day).getTime();
        StockCalendar.getInstance().setTime(date);

        File file = null;
        for (int i = 1; i < args.length; i++) {
            String command = args[i];
            // If this is a minor command, extract the command accordingly.
            // Otherwise, this is assumed to be a stock ticker and is added
            // to the ticker sto analyze.
            if (command.startsWith("-")) {
                switch (command) {
                    case CLIConstants.APPEND_TO_CSV_FILE:
                        String csvFile = args[i + 1];
                        i++;
                        file = new File(csvFile);
                        break;
                }
            } else {
                analyzeTickers.add(command);
            }
        }

        return file;
    }
}
