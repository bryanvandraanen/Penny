package main.java.penny.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;

import main.java.penny.marketdata.StockTick;

import static main.java.penny.constants.SerializationConstants.*;

/**
 * SerializationUtil represents a utility class for serializing and deserializing stock ticks from the local
 * file system.  The utility functions assume stock ticks are organized under directories that correspond to
 * the dates which the stock data was collected.  This directory structure is defined within the
 * SerializationConstants that outlines how stock data is grouped under months and days.
 */
public class SerializationUtil {

    /**
     * Serializes the provided stock tick to the local file system following the directory structure
     * defined by the SerializationConstants.  Reports to standard output if the file is not able to be serialized.
     *
     * @param tick The StockTick to serialize to the database (file system) under the appropriate StockCalendar date
     */
    public static void serializeStockTick(StockTick tick) {
        Calendar cal = StockCalendar.getInstance();

        String monthDir = MONTH_DIR_FORMAT.format(cal.getTime());
        String dayDir = DAY_DIR_FORMAT.format(cal.getTime());

        Path path = Paths.get(DATABASE_ROOT_DIR, DATABASE_DIR, monthDir, dayDir);

        serialize(path.toString(), tick.getTicker(), tick);
    }

    /**
     * Deserializes the stock tick found in the file provided.  Assumes the file was serialized previously with
     * the default serialization extension.  Reports to standard output if the file is not able to be deserialized.
     *
     * @param serialized The serialized stock tick file to extract
     * @return The deserialized StockTick stored in the serialized file provided
     */
    public static StockTick deserializeStockTick(File serialized) {
        return (StockTick) deserialize(serialized);
    }

    /**
     * Deserializes the stock ticker from the date provided.  The date defines the directory structure
     * to inspect and extract the serialized stock from.  Assumes stock ticks were serialized previously with the
     * default serialization extension.  Reports to standard output if the file is not able to be deserialized.
     *
     * @param date The date of the serialized stock data to deserialize from the local file system
     * @param symbol The stock ticker of the stock to deserialize on the particular date
     * @return The deserialized StockTick stored in the local file system under the date-defined directory structure
     */
    public static StockTick deserializeStockTick(Date date, String symbol) {
        String monthDir = MONTH_DIR_FORMAT.format(date);
        String dayDir = DAY_DIR_FORMAT.format(date);

        Path path = Paths.get(DATABASE_ROOT_DIR, DATABASE_DIR, monthDir, dayDir, symbol + SERIALIZATION_EXTENSION);
        File file = new File(path.toString());

        return (StockTick) deserialize(file);
    }

    /**
     * Serializes the object under the file name specified in the given file path.  Serializes stock ticks with the
     * default serialization extension.  Reports to standard output if the file is not able to be serialized.
     *
     * @param filePath The file path to save the serialized file to
     * @param fileName The name of the file (without the file extension) to serialize the object to
     * @param toSerialize The object to serialize to the file specified along the file path given
     */
    private static void serialize(String filePath, String fileName, Object toSerialize) {
        try {
            File file = new File(filePath);
            file.mkdirs();

            Path fullPath = Paths.get(filePath, fileName + SERIALIZATION_EXTENSION);

            FileOutputStream serializeStream = new FileOutputStream(fullPath.toString());
            ObjectOutputStream out = new ObjectOutputStream(serializeStream);
            out.writeObject(toSerialize);

            out.close();
            serializeStream.close();
        } catch (IOException e) {
            System.out.println("Unable to serialize object: " + fileName);
        }
    }

    /**
     * Deserializes the object found in the file provided.  Assumes the file was serialized previously with
     * the default serialization extension.  Reports to standard output if the file is not able to be deserialized.
     *
     * @param serialized The serialized object file to extract
     * @return The deserialized Object stored in the serialized file provided
     */
    private static Object deserialize(File serialized) {
        if (!serialized.getName().endsWith(SERIALIZATION_EXTENSION)) {
            return null;
        }

        try {
            FileInputStream deserializeStream = new FileInputStream(serialized);
            ObjectInputStream in = new ObjectInputStream(deserializeStream);

            Object deserialized = in.readObject();

            deserializeStream.close();
            in.close();

            return deserialized;
        } catch (IOException e) {
            System.out.println("Unable to deserialize object: " + serialized.getName());
        } catch (ClassNotFoundException c) {
            System.out.println("Unable to deserialize object - class not found: " + serialized.getName());
        }

        return null;
    }
}
