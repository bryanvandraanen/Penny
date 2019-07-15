package main.java.penny.constants;

/**
 * Classification constants.  Specifically, includes heuristics used for improving classification in practice
 * including the classification threshold (balancing precision and recall of the model), and various filters to
 * exclude obvious stock ticks that are not in the domain of real pump and dumps.
 */
public class ClassificationConstants {

    /** The classification threshold used for identifying active stocks */
    public static double CLASSIFICATION_THRESHOLD = 0.5;

    /** The minimum trading price required to be eligible to classify a particular stock tick */
    public static double MIN_PRICE_FILTER = 0.1;

    /** The minimum dollar volume required to be eligible to classify a particular stock tick */
    public static double MIN_VOLUME_USD_FILTER = 10000;
}
