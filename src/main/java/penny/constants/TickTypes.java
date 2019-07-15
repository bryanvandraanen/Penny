package main.java.penny.constants;

/**
 * TickTypes represents a series of static integer values identifying the meaning of tick values received from
 * the Broker.  This represents a simplification of the built-in Broker TickType and contains only the current
 * collection of ticks required for prediction and requested in market data inquiries.
 */
public class TickTypes {
    // Realtime Market Data
    /** Bid order size */
    public static final int BID_SIZE = 0;

    /** Bid price */
    public static final int BID = 1;

    /** Ask price */
    public static final int ASK = 2;

    /** Ask order size */
    public static final int ASK_SIZE = 3;

    /** Last price */
    public static final int LAST = 4;

    /** Last order size */
    public static final int LAST_SIZE = 5;

    /** High price of the day */
    public static final int HIGH = 6;

    /** Low price of the day */
    public static final int LOW = 7;

    /** Total daily volume */
    public static final int VOLUME = 8;

    /** Closing price */
    public static final int CLOSE = 9;

    /** Opening price */
    public static final int OPEN = 14;

    // Generic Tick String: "165"
    /** 13 week low */
    public static final int LOW_13_WEEKS = 15;

    /** 13 week high */
    public static final int HIGH_13_WEEKS = 16;

    /** 26 week low */
    public static final int LOW_26_WEEKS = 17;

    /** 26 week high */
    public static final int HIGH_26_WEEKS = 18;

    /** 52 week low */
    public static final int LOW_52_WEEKS = 19;

    /** 52 week high */
    public static final int HIGH_52_WEEKS = 20;

    /** Average volume */
    public static final int AVERAGE_VOLUME = 21;

    /** Required tick type values to have what's considered "complete" information about the stock */
    public static Integer[] requiredTicks = {VOLUME, AVERAGE_VOLUME, HIGH_13_WEEKS,
                                             LOW_13_WEEKS, OPEN, LAST, HIGH, LOW};

    /**
     * Returns the tick type specified as its equivalent String interpretation.
     */
    public static String asString(int tickType) {
        switch (tickType) {
            case BID_SIZE:
                return "Bid Size";
            case BID:
                return "Bid";
            case ASK:
                return "Ask";
            case ASK_SIZE:
                return "Ask Size";
            case OPEN:
                return "Open";
            case LAST:
                return "Last";
            case LAST_SIZE:
                return "Last Size";
            case HIGH:
                return "High";
            case LOW:
                return "Low";
            case VOLUME:
                return "Volume";
            case CLOSE:
                return "Close";
            case LOW_13_WEEKS:
                return "Low 13 Weeks";
            case HIGH_13_WEEKS:
                return "High 13 Weeks";
            case LOW_26_WEEKS:
                return "Low 26 Weeks";
            case HIGH_26_WEEKS:
                return "High 26 Weeks";
            case LOW_52_WEEKS:
                return "Low 52 Weeks";
            case HIGH_52_WEEKS:
                return "High 52 Weeks";
            case AVERAGE_VOLUME:
                return "Average Volume";
            default:
                return "Undefined";
        }
    }
}
