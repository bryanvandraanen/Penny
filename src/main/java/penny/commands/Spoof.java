package main.java.penny.commands;

import main.java.penny.Broker;
import main.java.penny.constants.CLIConstants;
import main.java.penny.constants.ClassificationConstants;
import main.java.penny.constants.ScannerConstants;
import main.java.penny.marketdata.StockScanner;
import main.java.penny.marketdata.StockScannerFilter;
import main.java.penny.marketdata.StockTick;
import main.java.penny.mock.MockBroker;
import main.java.penny.models.classification.ClassificationFilter;
import main.java.penny.models.classification.ClassifiedStockTick;
import main.java.penny.util.CommandUtil;
import main.java.penny.util.FileOutputEcho;
import main.java.penny.util.OTCTickersReader;

import java.io.IOException;
import java.util.*;

/**
 * Demonstration execution and program usage of Penny.
 *
 * Spoof executes an example version of live Penny program execution.  Spoof leverages simulated market
 * data on actual OTC Market tickers resulting in arbitrary classifications.  This program usage supports
 * optional arguments that include parallel market data delivery, outputting classifications to a log, and
 * serializing stock tick data to disk.
 */
public class Spoof {

    /**
     * Executes this command with the provided (minor) arguments.  Throws an Exception if this command
     * fails to execute.
     */
    public static void execute(String[] args) throws Exception {
        // Initialize commands and extra optional minor commands
        Set<String> commands = new HashSet<String>();
        init(args, commands);

        // Initialize Broker to deliver in parallel if specified, otherwise deliver sequentially
        if (commands.contains(CLIConstants.SPOOF_DELIVER_IN_PARALLEL_COMMAND)) {
            MockBroker.init(true /* Deliver in Parallel */);
        } else {
            MockBroker.init();
        }

        FileOutputEcho.getInstance().pauseFileOutputEcho();

        // Get all OTC Market tickers and filter them accordingly
        List<String> tickers = OTCTickersReader.getOTCTickers();
        StockScannerFilter filter = new StockScannerFilter.StockScannerFilterBuilder()
                .withMaximumTickerLength(ScannerConstants.MAX_TICKER_LENGTH_FILTER).build();

        // Scan and request market data for all stock tickers
        StockScanner.scan(tickers, filter);

        ClassificationFilter classificationFilter = new ClassificationFilter.ClassificationFilterBuilder()
                    .withMinimumPrice(ClassificationConstants.MIN_PRICE_FILTER)
                    .withMinimumVolumeUSD(ClassificationConstants.MIN_VOLUME_USD_FILTER)
                .build();

        Collection<StockTick> ticks = Broker.getInstance().getMarketData().getStockTickResults().getStockTicks();

        // Classify all scanned stock ticks and filter them accordingly
        List<ClassifiedStockTick> results = CommandUtil.classify(ticks, classificationFilter);

        // Resume file output echo now that we have results
        FileOutputEcho.getInstance().resumeFileOutputEcho();
        CommandUtil.output(results, true /* Only positive results */);
        System.out.println("Recall the data associated with these symbols is currently contrived!");
        System.out.println("The number of positive pump and dump classifications is unrealistic and" +
                " is not representative of the actual stocks.");

        FileOutputEcho.getInstance().closeOutputFiles();

        // Serialize the stock information to the default database location if specified
        if (commands.contains(CLIConstants.SERIALIZE_COMMAND)) {
            CommandUtil.serialize(Broker.getInstance().getMarketData().getStockTickResults().getStockTicks());
        }

        Broker.getInstance().getMarketData().shutdownTimeoutProcess();
    }

    /**
     * Initializes this program execution extracting and configuring the arguments accordingly.  Updates the
     * set of commands provided to include the indicated arguments.
     */
    private static void init(String[] args, Set<String> commands) {
        for (int i = 0; i < args.length; i++) {
            String command = args[i];
            switch (command) {
                case CLIConstants.SPOOF_DELIVER_IN_PARALLEL_COMMAND:
                    commands.add(CLIConstants.SPOOF_DELIVER_IN_PARALLEL_COMMAND);
                    break;
                case CLIConstants.OUTPUT_TO_LOG_COMMAND:
                    String fileName = args[i + 1];
                    try {
                        FileOutputEcho.getInstance().addOutputFile(fileName);
                    } catch (IOException e) {
                        System.out.println("Unable to output to file: " + fileName);
                    }
                    i++;
                    break;
                case CLIConstants.SERIALIZE_COMMAND:
                    commands.add(CLIConstants.SERIALIZE_COMMAND);
                    break;
            }
        }
    }
}
