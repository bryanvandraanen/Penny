package main.java.penny.models.classification;

import main.java.penny.marketdata.StockTick;

/**
 * ClassifiedStockTick represents a wrapper class for an existing StockTick that associates a classification
 * result with the stock.
 */
public class ClassifiedStockTick {

    /** The stock tick associated with this classification */
    private StockTick tick;

    /** The classification of the wrapped stock tick */
    private ClassificationResult classification;

    /**
     * Constructs a new ClassifiedStockTick associating the stock tick provided with the classification specified.
     *
     * @param tick The stock tick to associate with this classification
     * @param classification The classification to associate with this stock tick
     */
    public ClassifiedStockTick(StockTick tick, ClassificationResult classification) {
        this.tick = tick;
        this.classification = classification;
    }

    /**
     * Returns the stock tick associated with this classification.
     */
    public StockTick getStockTick() {
        return this.tick;
    }

    /**
     * Returns the classification associated with this stock tick.
     */
    public ClassificationResult getClassification() {
        return this.classification;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(this.tick.getTicker());
        builder.append(": ");
        builder.append(this.classification.getPercentage());

        return builder.toString();
    }
}
