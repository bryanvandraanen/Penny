package main.java.penny.constants;

/**
 * Scanner constants.  Details scanner subscription information and scanner heuristics used to optimize stock
 * search results when scanning market.
 */
public class ScannerConstants {

    /**
     * Maximum ticker length (inclusive) to filter by prior to requesting market data.
     * Almost always, effective pump and dump stocks are not companies with tickers that include a 5th letter.
     */
    public static final int MAX_TICKER_LENGTH_FILTER = 4;

    /** Default number of progress bars to include while scanning, etc. to display progress */
    public static final int PROGRESS_NUMBER_OF_BARS = 50;

    /** For the most active dollar volume scanner subscription, the maximum trading price to consider */
    public static final double PUMP_SCANNER_BELOW_PRICE = 10.00;

    /** For the most active dollar volume scanner subscription, the minimum trading price to consider */
    public static final double PUMP_SCANNER_ABOVE_PRICE = 0.4;

    /** For the most active dollar volume scanner subscription, the maximum market capitalization of stocks to consider */
    public static final double PUMP_SCANNER_MARKET_CAP_BELOW = 100000000;

    /** Broker API scanner code to request the most active dollar volume stocks */
    public static final String PUMP_SCANNER_SCAN_CODE = "MOST_ACTIVE_USD";

    /** The market location of stocks to request in the Broker scanner (i.e. Pink Sheets aka OTC Market) */
    public static final String PINK_SHEET_LOCATION_CODE = "STK.PINK";
}
