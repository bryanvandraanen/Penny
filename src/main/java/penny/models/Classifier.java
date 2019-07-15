package main.java.penny.models;

import main.java.penny.marketdata.StockTick;
import main.java.penny.models.classification.ClassificationResult;
import weka.core.Instance;

import java.util.List;

/**
 * Classifier represents an interface for classifying individual StockTicks and building live
 * actual data instances for a provided StockTick.
 */
public interface Classifier {

    /**
     * Classifies the StockTick provided as either a positive pump and dump stock or not.  If the StockTick
     * provided is not complete (does not have the required necessary tick data), indicates that the
     * classification is invalid instead.
     *
     * @param tick The StockTick to classify as either a pump and dump stock or not
     * @return A ClassificationResult containing the classification and the classification percentage
     *         representing the 'probability' that the StockTick is a pump and dump
     */
    public ClassificationResult classify(StockTick tick);

    /**
     * Builds an actual data instance of the StockTick provided.  A StockTick data instance corresponds to a
     * StockTick data point with all required features that this classifier expects.
     *
     * @param tick The StockTick to build an actual data instance from
     * @return A new data instance containing the required model features based on the StockTick provided.  If
     *         a data instance cannot be created, null is returned instead
     */
    public Instance buildInstanceOf(StockTick tick);

}
