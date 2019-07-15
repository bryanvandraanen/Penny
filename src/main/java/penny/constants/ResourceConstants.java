package main.java.penny.constants;

/**
 * Resource constants.  Refer to locations and names of various program resources including models, usage, and
 * OTC ticker information.
 */
public class ResourceConstants {

    /** Root directory of resource files */
    public static final String RESOURCE_FILE_PATH = "src/resources/";

    /** Complete path from root resource directory to the machine learning model directory location */
    public static final String MODEL_FILE_PATH = RESOURCE_FILE_PATH + "models/";

    /** Complete path from root resource directory to data file locations */
    public static final String DATA_FILE_PATH = RESOURCE_FILE_PATH + "data/";

    /** Name of file containing list of all OTC Market penny stock tickers */
    public static final String OTC_TICKER_FILE = "OTCBB.txt";

    /** Name of usage file explaining various program usages and command-line arguments */
    public static final String USAGE_FILE = "usage.txt";

}
