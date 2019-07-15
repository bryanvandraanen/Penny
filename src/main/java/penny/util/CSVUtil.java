package main.java.penny.util;

import main.java.penny.constants.TickTypes;
import main.java.penny.marketdata.StockTick;
import main.java.penny.models.classification.ClassifiedStockTick;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import java.io.File;

import static main.java.penny.constants.SerializationConstants.LOADED_TICK_DATE_FORMAT;

/**
 * CSVUtil represents a utility class that contains functions that convert StockTicks to valid CSV lines based
 * on the tick values found in the StockTicks.
 *
 * StockTick CSV lines follow the general form:
 * ticker,classification,open,high,low,last,volume,average volume,13 week high,13 week low,date
 */
public class CSVUtil {

    /**
     * Takes a ClassifiedStockTick and returns a CSV line in the form:
     * ticker,classification,open,high,low,last,volume,average volume,13 week high,13 week low,date
     *
     * @param tick Stock tick and associated classification to convert to CSV line
     * @return String CSV line following the format above for the provided ClassifiedStockTick
     */
    public static String classifiedStockTickToCSV(ClassifiedStockTick tick) {
        StringBuilder builder = new StringBuilder();

        appendCSV(builder, tick.getStockTick().getTicker());

        if (tick.getClassification().isPositive()) {
            appendCSV(builder, Integer.toString(1));
        } else {
            appendCSV(builder, Integer.toString(0));
        }

        builder.append(stockTickToCSV(tick.getStockTick(), false /* Should not include ticker */));

        return builder.toString();
    }

    /**
     * Takes a StockTick and returns a CSV line in the form:
     * ticker,open,high,low,last,volume,average volume,13 week high,13 week low,date
     *
     * @param tick Stock tick and associated classification to convert to CSV line
     * @param shouldIncludeTicker True if the output CSV should include the initial ticker,
     *                            false if it should be excluded
     * @return String CSV line following the format above for the provided StockTick
     */
    public static String stockTickToCSV(StockTick tick, boolean shouldIncludeTicker) {
        StringBuilder builder = new StringBuilder();
        if (shouldIncludeTicker) {
            appendCSV(builder, tick.getTicker());
        }

        appendTickCSV(builder, tick, TickTypes.OPEN);
        appendTickCSV(builder, tick, TickTypes.HIGH);
        appendTickCSV(builder, tick, TickTypes.LOW);
        appendTickCSV(builder, tick, TickTypes.LAST);
        appendTickCSV(builder, tick, TickTypes.VOLUME);
        appendTickCSV(builder, tick, TickTypes.AVERAGE_VOLUME);
        appendTickCSV(builder, tick, TickTypes.HIGH_13_WEEKS);
        appendTickCSV(builder, tick, TickTypes.LOW_13_WEEKS);
        builder.append(LOADED_TICK_DATE_FORMAT.format(StockCalendar.getInstance().getTime()));

        return builder.toString();
    }

    /**
     * Converts the CSV file specified to the Weka ARFF file output path location.
     * @param csvFilePath File path of the CSV file to convert to ARFF; assumes the CSV file is not malformed
     * @param arffFilePath Output file path location of the ARFF file to create
     * @throws Exception If the CSV file cannot be converted to the ARFF file
     */
    public static void csvToArff(String csvFilePath, String arffFilePath) throws Exception {
        CSVLoader loader = new CSVLoader();
        loader.setSource(new File(csvFilePath));
        Instances data = loader.getDataSet();

        ArffSaver saver = new ArffSaver();
        saver.setInstances(data);
        saver.setFile(new File(arffFilePath));
        saver.writeBatch();
    }

    /**
     * Appends the value of the tick found in the provided StockTick for the tick type specified to the builder
     * in CSV form.
     */
    private static void appendTickCSV(StringBuilder builder, StockTick tick, int tickType) {
        if (tick.hasTick(tickType)) {
            appendCSV(builder, tick.getTick(tickType).toString());
        }
    }

    /**
     * Appends all the strings provided to the builder in CSV form.
     */
    private static void appendCSV(StringBuilder builder, String... strings) {
        for (String string : strings) {
            builder.append(string);
            builder.append(",");
        }
    }
}
