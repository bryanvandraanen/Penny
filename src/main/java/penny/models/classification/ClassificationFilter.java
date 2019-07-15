package main.java.penny.models.classification;

import main.java.penny.marketdata.PumpStockTick;
import main.java.penny.marketdata.StockTick;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * ClassificationFilter represents a filter prior to classifying data instances for particular stocks with a model.
 * The filter takes a series of StockTicks and returns all the StockTicks that satisfy a particular predicate based
 * on user-defined supported heuristics.
 *
 * For example, depending on the available data a model is trained with, StockTicks with extremely low prices or
 * dollar volumes may exhibit drastic percentage increases that are falsely viewed as positive model indicators
 * when they are obviously do not exhibit the actual target class (i.e. 0.0001 to 0.0002 change in price).
 */
public class ClassificationFilter {

    /** The minimum price (inclusive) allowed to include for classification */
    private final double minimumPrice;

    /** The minimum dollar volume (inclusive) allowed to include for classification */
    private final double minimumDollarVolume;

    /**
     * Constructs a new ClassificationFilter allowing only for StockTicks trading with the minimum price and dollar
     * volume specified.
     */
    private ClassificationFilter(double minimumPrice, double minimumDollarVolume) {
        this.minimumPrice = minimumPrice;
        this.minimumDollarVolume = minimumDollarVolume;
    }

    /**
     * Filters the provided iterable collection of StockTicks based on the instance-defined predicate
     * and returns a new collection of the same StockTicks that satisfy the predicate.
     *
     * @param ticks The StockTicks to filter following the predicate defined in this ClassificationFilter
     * @return A new List of StockTicks fromt he provided stock ticks that satisfied the predicate
     */
    public List<StockTick> filter(Iterable<StockTick> ticks) {
        return filter(ticks.iterator());
    }

    /**
     * Iterates through all the provided StockTicks and selects only the StockTicks that satisfy the
     * instance-defined predicate to provide as a new collection of the same StockTicks.
     *
     * @param ticks The iterator of StockTicks to filter following the predicate defined in this ClassificationFilter
     * @return A new List of StockTicks from the provided iterator that satisfied the predicate
     */
    private List<StockTick> filter(Iterator<StockTick> ticks) {
        List<StockTick> filtered = new ArrayList<StockTick>();

        while (ticks.hasNext()) {
            StockTick tick = ticks.next();

            PumpStockTick pumpTick = new PumpStockTick(tick);

            if (satisfiesFilter(pumpTick)) {
                filtered.add(tick);
            }
        }

        return filtered;
    }

    /**
     * Returns true if the StockTick provided satisfies the instance-defined predicate of this ClassificationFilter,
     * and false otherwise.
     */
    private boolean satisfiesFilter(PumpStockTick tick) {
        return tick.isComplete() &&
               tick.getLastPrice() >= this.minimumPrice &&
               tick.getVolumeUSD() >= this.minimumDollarVolume;
    }

    /**
     * ClassificationFilterBuilder represents a builder for configuring and creating a new instance of
     * ClassificationFilter.
     */
    public static class ClassificationFilterBuilder {

        /** The current configured minimum price */
        private double minimumPrice;

        /** The current configured minimum dollar volume */
        private double minimumDollarVolume;

        /**
         * Creates a new ClassificationFilterBuilder with no restrictions on the minimum price or minimum
         * dollar volume of stocks.
         */
        public ClassificationFilterBuilder() {
            this.minimumPrice = -1.0;
            this.minimumDollarVolume = -1.0;
        }

        /**
         * Sets the minimum price of the to-be-built ClassificationFilter to the price specified.
         * Configures the current minimum price to the value provided to be used when building
         * and finalizing the new ClassificationFilter.
         *
         * @param price The minimum allowed price of a stock tick to accept when filtering.  Any stock tick
         *              of a strictly lesser price is excluded
         * @return This ClassificationFilterBuilder for chaining
         */
        public ClassificationFilterBuilder withMinimumPrice(double price) {
            this.minimumPrice = price;
            return this;
        }

        /**
         * Sets the minimum dollar volume of the to-be-built ClassificationFilter to the amount specified.
         * Configures the current minimum dollar volume to the value provided to be used when building
         * and finalized the new ClassificationFilter.
         *
         * @param dollarVolume The minimum allowed dollar volume of a stock tick to accept when filtering.  Any
         *                     stock tick of a strictly lesser price is excluded
         * @return This ClassificationFilterBuilder for chaining
         */
        public ClassificationFilterBuilder withMinimumVolumeUSD(double dollarVolume) {
            this.minimumDollarVolume = dollarVolume;
            return this;
        }

        /**
         * Builds a new instance of ClassificationFilter based on the configured values previously defined
         * in this builder.
         *
         * @return A ClassificationFilter with the specified and configured values from this builder.
         */
        public ClassificationFilter build() {
            return new ClassificationFilter(this.minimumPrice, this.minimumDollarVolume);
        }

    }
}
