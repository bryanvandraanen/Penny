package main.java.penny.util;

import java.util.Calendar;

/**
 * StockCalendar represents the unified calendar associated with market data information.
 */
public class StockCalendar {

    /** Singleton Calendar */
    private static Calendar calendar;

    /**
     * Returns the singleton instance of the current unified Calendar.
     */
    public static Calendar getInstance() {
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }

        return calendar;
    }
}
