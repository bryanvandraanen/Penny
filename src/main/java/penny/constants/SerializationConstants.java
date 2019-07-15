package main.java.penny.constants;

import java.text.SimpleDateFormat;

/**
 * Serialization Constants.  Constants dictate the "database" location in the local file system and the serialization
 * protocol to follow including directory format, serialization extension, etc.
 */
public class SerializationConstants {

    /** Root directory containing the database in the local file system */
    public static String DATABASE_ROOT_DIR = "D:/";

    /** Name of the database directory in the root directory */
    public static String DATABASE_DIR = "StockTickDB/";

    /** Default serialization extension expected for serialized stock tick information */
    public static String SERIALIZATION_EXTENSION = ".ser";

    /** Directory format structure for the month directory housing all serialization for daily results from that month */
    public static SimpleDateFormat MONTH_DIR_FORMAT = new SimpleDateFormat("MMMyyyy");

    /** Directory format structure for the day directory within a given month directory housing daily serialized stock ticks */
    public static SimpleDateFormat DAY_DIR_FORMAT = new SimpleDateFormat("dd");

    /** Date format style for display of the date which stock information was received originally */
    public static SimpleDateFormat LOADED_TICK_DATE_FORMAT = new SimpleDateFormat("M/d/yyyy");
}
