package main.java.penny.marketdata;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * StockScannerFilter represents a stock filter prior to requesting market data for particular stocks.
 * The filter takes a series of tickers and returns all the stock tickers that satisfy a particular
 * predicate based on user-defined supported heuristics.
 *
 * For example, often times, stock patterns including pump and dump penny stocks are almost always
 * inapplicable to tickers longer than 4 in length - this filter helps sift through those stocks prior to
 * waiting to retrieve market data.
 */
public class StockScannerFilter {

    /** The maximum ticker length (inclusive) allowed to include and scan tickers */
    private final int maximumTickerLength;

    /**
     * Constructs a new StockScannerFilter allowing only for stocks with the maximum ticker length specified.
     */
    private StockScannerFilter(int maximumTickerLength) {
        this.maximumTickerLength = maximumTickerLength;
    }

    /**
     * Filters the provided iterable collection of tickers based on the instance-defined predicate and
     * returns a new collection of tickers that satisfy the predicate.
     *
     * @param tickers The tickers to filter following the predicate defined in this StockScannerFilter
     * @return A new List of tickers from the provided stock tickers that satisfied the predicate
     */
    public List<String> filter(Iterable<String> tickers) {
        return filter(tickers.iterator());
    }

    /**
     * Iterates through all the provided tickers and selects only the tickers that satisfy the
     * instance-defined predicate to provide as a new collection of tickers.
     *
     * @param tickers The iterator of tickers to filter following the predicate defined in this StockScannerFilter
     * @return A new List of tickers from the provided iterator that satisfied the predicate
     */
    private List<String> filter(Iterator<String> tickers) {
        List<String> filtered = new ArrayList<String>();

        while (tickers.hasNext()) {
            String ticker = tickers.next();

            if (satisfiesFilter(ticker)) {
                filtered.add(ticker);
            }
        }

        return filtered;
    }

    /**
     * Returns true if the ticker provided satisfies the instance-defined predicate of this StockScannerFilter,
     * and false otherwise.
     */
    private boolean satisfiesFilter(String ticker) {
        return ticker.length() <= this.maximumTickerLength;
    }

    /**
     * StockScannerFilterBuilder represents a builder for configuring and creating a new instance of
     * StockScannerFilter.
     */
    public static class StockScannerFilterBuilder {

        /** The current configured maximum ticker length */
        private int maximumTickerLength;

        /**
         * Creates a new StockScannerFilterBuilder with no restriction on maximum ticker length of stocks
         */
        public StockScannerFilterBuilder() {
            this.maximumTickerLength = Integer.MAX_VALUE;
        }

        /**
         * Sets the maximum ticker length of the to-be-built StockScannerFilter to the length specified.
         * Configures the current maximum ticker length to the value provided to be used when building
         * and finalizing a new StockScannerFilter.
         *
         * @param maximumTickerLength The maximum allowed length of a stock ticker to accept when filtering.  Any ticker
         *                            of a strictly longer length is excluded
         * @return This StockScannerFilterBuilder for chaining
         */
        public StockScannerFilterBuilder withMaximumTickerLength(int maximumTickerLength) {
            this.maximumTickerLength = maximumTickerLength;
            return this;
        }

        /**
         * Builds a new instance of StockScannerFilter based on the configured values previously defined in this builder.
         *
         * @return A StockScannerFilter with the specified and configured values from this builder.
         */
        public StockScannerFilter build() {
            return new StockScannerFilter(this.maximumTickerLength);
        }
    }
}
