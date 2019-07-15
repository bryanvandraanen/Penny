package main.java.penny.models.classification;

import main.java.penny.marketdata.StockTick;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ClassificationContainer represents a container of classified stock tick results.  The ClassificationContainer
 * merges stock ticks and their classifications into individual instances and accommodates concurrent population
 * of classification results.
 */
public class ClassificationContainer {

    /** The classified stock tick results */
    private final Set<ClassifiedStockTick> results;

    /**
     * Constructs a new ClassificationContainer with an empty collection of classified stock tick results.
     */
    public ClassificationContainer() {
        this.results = ConcurrentHashMap.newKeySet();
    }

    /**
     * Merges the stock tick and classification result provided and adds the compiled stock tick to the
     * classification results in this container.  If the stock already exists in this container, overwrites
     * the information with the new merged stock tick classification result.
     *
     * @param tick The stock tick to add to these results
     * @param result The classification of the provided stock tick to associate with in these results
     */
    public void addStockTick(StockTick tick, ClassificationResult result) {
        this.results.add(new ClassifiedStockTick(tick, result));
    }

    /**
     * Retrieves the list of classified stock ticks sorted in decreasing order of classification percentage.
     * That is, returns all the classified stock ticks in this container ordered starting with the most
     * confident positive entries to the most confident negative entries.
     *
     * @return A List of classified stock ticks ordered in descending order of confidence from positive
     *         classification to negative classification (least positive)
     */
    public List<ClassifiedStockTick> getClassifiedPumpStockTicks() {
        List<ClassifiedStockTick> output = new ArrayList<ClassifiedStockTick>();
        output.addAll(this.results);

        output.sort((first, second) -> Double.compare(second.getClassification().getPercentage(),
                first.getClassification().getPercentage()));

        return output;
    }
}
