package main.java.penny.models;

import java.io.FileInputStream;

import main.java.penny.constants.Classification;
import main.java.penny.constants.ResourceConstants;
import main.java.penny.marketdata.PumpStockTick;
import main.java.penny.marketdata.StockTick;
import main.java.penny.models.classification.ClassificationResult;

import weka.classifiers.functions.Logistic;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Standardize;

import static main.java.penny.constants.ClassificationConstants.CLASSIFICATION_THRESHOLD;

/**
 * PumpClassifier represents a penny stock pump and dump stock classifier.  The classifier is designed to identify
 * pump and dump stock promotions on the first day the stock promotion starts.  PumpClassifier contains 4 principal
 * components based off stock manipulation indicators and signs.  These features consist of the daily dollar volume,
 * average dollar volume (over the past 90 days), the daily percent change, and the daily price range.
 *
 * Pump and dumps almost always show unusually high trading activity (exhibited through dollar volume) compared to the
 * previous trading activity for the stock.  Additionally, pump and dumps show a staggering increase in price on the
 * first day that is stabilized and maintained for the duration of the market hours.  The limitation of these components
 * is that it does not consider intra-day signals (such as the previously mentioned manipulated/stabilized stock price).
 */
public class PumpClassifier implements Classifier {

    /** File location of the previously trained logistic regression model */
    public static final String LOGISTIC_REGRESSION_MODEL = ResourceConstants.MODEL_FILE_PATH + "PumpClassifier.model";

    /** File location of the training data used to build this model */
    public static final String TRANING_DATA = ResourceConstants.DATA_FILE_PATH + "PennyStockPrincipalComponents.arff";

    /** Indices of features found in the original training data */
    // Assumes this is the order attributes appear in when model was constructed
    private static final int NUM_ATTRIBUTES = 5; // Includes "class" aka positive/negative classification as attribute
    private static final int VOLUME_USD_INDEX = 1;
    private static final int AVG_VOLUME_USD_INDEX = 2;
    private static final int PERCENT_CHANGE_INDEX = 3;
    private static final int DAY_RANGE_INDEX = 4;

    /** The logistic regression pump and dump classifier model */
    private Logistic model;

    /** The training dataset used to build this classifier */
    private Instances dataset;

    /** The transformations applied to the training data to normalize data instances between 0 and 1 */
    private Standardize standardize;

    /**
     * Constructs a new PumpClassifier loading the logistic regression model and training data from their
     * default file locations.
     *
     * @throws Exception If the model or training data cannot be loaded and the classifier cannot be created
     */
    public PumpClassifier() throws Exception {
        DataSource trainingData = new DataSource(new FileInputStream(TRANING_DATA));
        Instances dataset = trainingData.getDataSet();
        dataset.setClassIndex(0);

        this.standardize = new Standardize();
        this.standardize.setInputFormat(dataset);

        dataset = Filter.useFilter(dataset, this.standardize);

        this.model = readLogisticModel(); // Use model created with Weka GUI
        this.dataset = dataset; // Need this updated dataset for standardization
    }

    @Override
    public ClassificationResult classify(StockTick tick) {
        // Build an actual data instance from the provided stock tick
        Instance instance = buildInstanceOf(tick);
        if (instance == null) {
            return ClassificationResult.INVALID;
        }

        // Normalize the instance values the same as the training data
        try {
            instance = normalize(instance);
        } catch (Exception e) {
            return ClassificationResult.INVALID;
        }

        if (instance == null) {
            return ClassificationResult.INVALID;
        }

        ClassificationResult result;
        try {
            // Classify the instance and get the probability that the instance is a pump (index 1 class)
            double classification = this.model.classifyInstance(instance);
            double percentage = this.model.distributionForInstance(instance)[1];

            // Associate a positive or negative result depending on if the probability exceeds the threshold
            result = new ClassificationResult(percentage);
        } catch (Exception e) {
            result = ClassificationResult.INVALID;
        }

        return result;
    }

    @Override
    public Instance buildInstanceOf(StockTick tick) {
        // If this stock tick is not complete, we cannot build a data instance; return null
        if (!tick.isComplete()) {
            return null;
        }
        PumpStockTick pumpTick = new PumpStockTick(tick);

        // Extract the necessary features from this stock tick
        double[] attributeValues = new double[NUM_ATTRIBUTES];

        attributeValues[VOLUME_USD_INDEX] = pumpTick.getVolumeUSD();
        attributeValues[AVG_VOLUME_USD_INDEX] = pumpTick.getAverageVolumeUSD();
        attributeValues[PERCENT_CHANGE_INDEX] = pumpTick.getPercentChange();
        attributeValues[DAY_RANGE_INDEX] = pumpTick.getDayRange();

        Instance instance = new DenseInstance(1.0, attributeValues);

        Instances dataset = getDatasetCopy();
        dataset.add(instance);
        return dataset.instance(dataset.numInstances() - 1);
    }

    @Override
    public String toString() {
        return this.model.toString();
    }

    /**
     * Returns a copy of the training dataset used to build this classifier.
     */
    private Instances getDatasetCopy() {
        return new Instances(this.dataset);
    }

    /**
     * Normalizes the provided instance based on the training data of this classifier between 0 and 1.
     * Returns a new data instance representing the normalized values of the original.  If the instance
     * cannot be normalized, returns null instead.
     *
     * @param instance The data instance to normalize according to how this model was normalized
     * @return A new instance with normalized feature values
     * @throws Exception If the instance fails to normalize
     */
    private Instance normalize(Instance instance) throws Exception {
        this.standardize.input(instance);
        return this.standardize.output();
    }

    /**
     * Reads the logistic regression model from the default logistic regression model file location.
     *
     * @return The logistic regression model read from the default file path
     * @throws Exception If the logistic classifier cannot be loaded
     */
    private static Logistic readLogisticModel() throws Exception {
        return (Logistic) SerializationHelper.read(new FileInputStream(LOGISTIC_REGRESSION_MODEL));
    }
}
