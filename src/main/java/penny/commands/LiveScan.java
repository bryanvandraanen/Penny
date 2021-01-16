package main.java.penny.commands;

import main.java.penny.Broker;
import main.java.penny.concurrent.Lock;
import main.java.penny.concurrent.LockManager;
import main.java.penny.constants.CLIConstants;
import main.java.penny.constants.ClassificationConstants;
import main.java.penny.marketdata.*;
import main.java.penny.models.classification.ClassificationFilter;
import main.java.penny.models.classification.ClassifiedStockTick;
import main.java.penny.util.CommandUtil;
import main.java.penny.util.FileOutputEcho;
import main.java.penny.util.OTCTickersReader;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Primary execution and program usage of Penny.
 *
 * LiveScan executes Penny with real-time market data and direct communication with the broker to receive and
 * classify OTC Market stocks as pump and dumps.  This program usage supports optional arguments that include
 * narrowing the scan to only the 50 most active stocks (based on dollar volume), outputting classifications to a
 * log, and serializing stock tick data to disk.
 */
public class LiveScan {

    /**
     * Executes this command with the provided (minor) arguments.  Throws an Exception if this command
     * fails to execute.
     */
    public static void execute(String[] args) throws Exception {
        // Initialize commands and extra optional minor commands
        Broker.init();
        Set<String> commands = new HashSet<String>();
        init(args, commands);

        FileOutputEcho.getInstance().pauseFileOutputEcho();

        // For the live scan, do not timeout market data requests because should be able to get data for all tickers
        // in a reasonable period of time
        // Broker.getInstance().getMarketData().shutdownTimeoutProcess();

        // Configure the tickers to scan depending on whether only the most active stock tickers are wanted
        // or the entire OTC Market
        Collection<String> tickers;
        if (commands.contains(CLIConstants.PUMP_SCANNER_COMMAND)) {
            // Request the most active pump and dump scanner and wait until the scanner has completed
            PumpScanner.requestScanner(0);
            Lock lock = LockManager.getInstance().getLock(0);
            lock.lock(); // Once we can acquire the lock here, we know the scanner has completed
            tickers = Broker.getInstance().getMarketData().getTickers();
            lock.unlock();
        } else {
            tickers = OTCTickersReader.getOTCTickers();
        }
        StockScannerFilter filter = new StockScannerFilter.StockScannerFilterBuilder()
                .withMaximumTickerLength(4)
                .build();

        // Scan and request market data for the specified and filtered stock tickers.
        // StockScanner.scan(tickers, filter);
        StockScanner.scanSequential(tickers, filter);
        Broker.getInstance().getMarketData().waitForActiveDataToDeliver();

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

        FileOutputEcho.getInstance().closeOutputFiles();

        // Serialize the stock information to the default database location if specified
        if (commands.contains(CLIConstants.SERIALIZE_COMMAND)) {
            CommandUtil.serialize(ticks);
        }

        Broker.getInstance().getMarketData().shutdownTimeoutProcess();
        Broker.getInstance().getClient().eDisconnect();
    }

    /**
     * Initializes this program execution extracting and configuring the arguments accordingly.  Updates the
     * set of commands provided to include the indicated arguments.
     */
    private static void init(String[] args, Set<String> commands) {
        for (int i = 0; i < args.length; i++) {
            String command = args[i];
            switch (command) {
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
                case CLIConstants.PUMP_SCANNER_COMMAND:
                    commands.add(CLIConstants.PUMP_SCANNER_COMMAND);
                    break;
            }
        }
    }
}
