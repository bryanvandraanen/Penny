package main.java.penny.models.classification;

import main.java.penny.constants.Classification;

import static main.java.penny.constants.ClassificationConstants.CLASSIFICATION_THRESHOLD;

/**
 * ClassificationResult represents a model classification.  Classifications take on two main classes:
 * positive or negative.  Additionally, if a classification is not possible (i.e. required data is missing),
 * an invalid classification is accommodated.
 */
public class ClassificationResult {

    /** Static invalid classification constant */
    public static final ClassificationResult INVALID = new ClassificationResult(-1);

    /** The class (positive/negative/invalid) of this classification */
    private final Classification classification;

    /** The percentage (probability) of this classification between 0 (negative) and 1 (positive) (unless invalid) */
    private final double percentage;

    /**
     * Constructs a new ClassificationResult with the percentage (probability) specified.  If the percentage is
     * above the default defined classification threshold, a positive classification is given, otherwise negative.
     * If the percentage is negative and is not valid, an invalid classification is indicated instead.
     *
     * @param percentage The percent probability of this classification between 0 or 1; if the percentage is not
     *                   within this range an invalid classification is given instead.
     */
    public ClassificationResult(double percentage) {
        if (percentage < 0 || percentage > 1) {
            this.classification = Classification.Invalid;
        } else if (percentage > CLASSIFICATION_THRESHOLD) {
            this.classification = Classification.Positive;
        } else {
            this.classification = Classification.Negative;
        }
        this.percentage = percentage;
    }

    /**
     * Returns true if this classification is a positive classification, and false otherwise.
     */
    public boolean isPositive() {
        return this.classification == Classification.Positive;
    }

    /**
     * Returns true if this classification is a negative classification, and false otherwise.
     */
    public boolean isNegative() {
        return this.classification == Classification.Negative;
    }

    /**
     * Returns true if this classification is an invalid classification, and false otherwise.
     */
    public boolean isInvalid() {
        return this.classification == Classification.Invalid;
    }

    /**
     * Returns the percentage (probability) of this classification between 0 and 1.  If the classification is invalid
     * behavior is undefined.
     */
    public double getPercentage() {
        return this.percentage;
    }

    @Override
    public String toString() {
        return this.classification + ": " + this.percentage;
    }
}
