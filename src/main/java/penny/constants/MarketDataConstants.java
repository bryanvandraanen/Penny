package main.java.penny.constants;

import java.util.HashSet;
import java.util.Set;

/**
 * Market data constants.  Constants dictating market data delivery and
 */
public class MarketDataConstants {

    /** Type of market data currently requested by broker - 1 real-time market data; 3 delayed market data */
    public static final int MARKET_DATA_TYPE = 1;

    public static final String BROKER_CONNECTION_IP = "127.0.0.1";

    public static final int BROKER_CONNECTION_PORT = 7496;

    /** Maximum number of active market data requests allowed by broker */
    public static final int MAX_CONCURRENT_MARKET_DATA_REQUESTS = 50;

    /** Default market data request timeout in milliseconds */
    public static final int MARKET_DATA_TIMEOUT_MILLIS = 1000;

    /** Error codes to unlock locks associated market data requests */
    public static final Set<Integer> ERROR_CODE_UNLOCKS = initializeErrorCodeUnlocks();

    /** Broker API tick type String indicating which tick values to receive from market data */
    public static final String TICK_STRING = "165";

    /** Stock security type */
    public static final String SECURITY_TYPE = "STK";

    /** Stock exchange routing */
    public static final String EXCHANGE = "SMART";

    /** Stock currency */
    public static final String CURRENCY = "USD";

    /**
     * Initializes the market data error codes that unlock locks associated to the errored market data requests.
     */
    private static Set<Integer> initializeErrorCodeUnlocks() {
        Set<Integer> errorCodeUnlocks = new HashSet<Integer>();
        errorCodeUnlocks.add(200); /* No security definition found */
        errorCodeUnlocks.add(354); /* Market data not subscribed */
        return errorCodeUnlocks;
    }
}
