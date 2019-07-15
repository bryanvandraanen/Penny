package main.java.penny.util;

import main.java.penny.constants.ScannerConstants;
import main.java.penny.marketdata.StockTick;
import main.java.penny.marketdata.StockTickResults;
import main.java.penny.models.Classifier;
import main.java.penny.models.PumpClassifier;
import main.java.penny.models.classification.ClassificationContainer;
import main.java.penny.models.classification.ClassificationFilter;
import main.java.penny.models.classification.ClassificationResult;
import main.java.penny.models.classification.ClassifiedStockTick;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * CommandUtil represents a utility class that contains functions for performing all common command-line
 * program execution requests.  These requests include classifying, serializing, deserializing, outputting, and
 * analyzing stock data.
 */
public class CommandUtil {

    /**
     * Classifies the stock ticks provided using a PumpClassifier filtering out any stock ticks
     * that do not satisfy the classification filter.
     *
     * @return A List of ClassifiedStockTicks containing all the stock ticks and their associated classifications.
     */
    public static List<ClassifiedStockTick> classify(Collection<StockTick> ticks,
                                                     ClassificationFilter filter) throws Exception {
        ClassificationContainer classifications = new ClassificationContainer();

        if (filter != null) {
            ticks = filter.filter(ticks);
        }

        Classifier pumpClassifier = new PumpClassifier();

        for (StockTick tick : ticks) {
            ClassificationResult classification = pumpClassifier.classify(tick);
            classifications.addStockTick(tick, classification);
        }

        return classifications.getClassifiedPumpStockTicks();
    }

    /**
     * Serializes all the stock ticks provided to the default database file location.
     */
    public static void serialize(Collection<StockTick> ticks) {
        List<String> uniqueTickers = new ArrayList<String>();
        ticks.forEach((tick) -> uniqueTickers.add(tick.getTicker()));

        ProgressBar progressBar = new ProgressBar.ProgressBarBuilder()
                .withTitle("Serialize")
                .withStartSymbol("|")
                .withProgressToken("#")
                .withGapToken(" ")
                .withEndSymbol("|")
                .withNumberOfBars(Math.min(uniqueTickers.size(), ScannerConstants.PROGRESS_NUMBER_OF_BARS))
                .withTotal(uniqueTickers.size())
                .withUniqueTokens(uniqueTickers)
            .build();

        for (StockTick tick : ticks) {
            SerializationUtil.serializeStockTick(tick);
            progressBar.increment();
            progressBar.display();
        }
    }

    /**
     * Deserializes the stock ticks specified from the files provided and copies the stock data to the
     * StockTickResults specified.  If no tickers and provided to analyze, includes all deserialized stock ticks
     * in the stock tick results.
     */
    public static void deserialize(File[] files, StockTickResults results, Set<String> analyzeTickers) {
        ProgressBar progressBar = new ProgressBar.ProgressBarBuilder()
                .withTitle("Deserialize")
                .withStartSymbol("|")
                .withProgressToken("#")
                .withGapToken(" ")
                .withEndSymbol("|")
                .withNumberOfBars(Math.min(files.length, ScannerConstants.PROGRESS_NUMBER_OF_BARS))
                .withTotal(files.length)
            .build();

        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            StockTick tick = SerializationUtil.deserializeStockTick(file);

            progressBar.increment();
            progressBar.display();

            if (analyzeTickers == null || analyzeTickers.isEmpty() || analyzeTickers.contains(tick.getTicker())) {
                results.copyStockTick(i, tick);
            }
        }
    }

    /**
     * Outputs all the provided classified stock ticks to the console.  If only positive results are indicated as
     * wanted output, only prints stock ticks with positive classifications to the console.
     */
    public static void output(List<ClassifiedStockTick> results, boolean onlyPositiveResults) {
        System.out.println("Pump Ticker: Classification Percentage");
        for (ClassifiedStockTick tick : results) {
            if (!onlyPositiveResults || tick.getClassification().isPositive()) {
                System.out.println(tick);
            }
        }
    }

    /**
     * Analyzes the stock tickers specified from the stock tick results provided by outputting the
     * ticker information to the console.
     */
    public static void analyze(Set<String> analyzeTickers, StockTickResults results) {
        for (String ticker : analyzeTickers) {
            StockTick tick = results.getStockTick(ticker);

            if (tick == null) {
                System.out.println("Ticker not found: " + ticker);
            } else {
                System.out.println(tick);
            }
        }
    }

    /**
     * Appends the stock tick and their classification percentages to the CSV file provided.
     * If the information cannot be appended to the CSV file, an IOException is thrown.  Appends only the
     * stock tick data associated with stocks that are found in the set of tickers given.
     */
    public static void appendCSV(File csvAppendFile, List<ClassifiedStockTick> classifications,
                                 Set<String> tickers) throws IOException {
        FileWriter writer = new FileWriter(csvAppendFile, true /* Append */);
        for (ClassifiedStockTick classified : classifications) {
            StockTick tick = classified.getStockTick();

            if (tickers.isEmpty() || tickers.contains(tick.getTicker())) {
                writer.write(CSVUtil.classifiedStockTickToCSV(classified));
                writer.write(System.lineSeparator());
            }
        }

        writer.close();
    }
}
