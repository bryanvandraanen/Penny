package main.java.penny.marketdata;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.ib.client.Contract;

import main.java.penny.constants.TickTypes;

/**
 * StockTick represents the tick price and size values associated with a particular stock ticker.  In other words, a
 * StockTick captures the market data information associated with a stock ticker.  A StockTick is considered "complete"
 * if it contains a value for every required tick type.
 */
public class StockTick implements Serializable {

    private static final long serialVersionUID = -5833141718388638175L;

    /** Map from TickType to associated value (price/size) for this stock; */
    private Map<Integer, Number> ticks;

    /** Stock ticker associated with this tick data */
    private String ticker;

    /** Required TickTypes not yet present in this StockTick */
    private Set<Integer> missingTicks;

    /**
     * Constructs a new StockTick for tick data associated with the ticker provided.  Initializes a
     * StockTick with an empty collection of tick data.
     *
     * @param ticker The ticker associated with the tick data
     */
    public StockTick(String ticker) {
        this.ticks = new ConcurrentHashMap<Integer, Number>();
        this.ticker = ticker;
        this.missingTicks = new HashSet<Integer>(Arrays.asList(TickTypes.requiredTicks));
    }

    /**
     * Copies the provided StockTick and creates a new StockTick with the same ticker and identical tick data.
     *
     * @param tick The StockTick to copy
     */
    protected StockTick(StockTick tick) {
        this(tick.getTicker());
        for (Integer tickType : tick.ticks.keySet()) {
            this.addTick(tickType, tick.getTick(tickType));
        }
    }

    /**
     * Adds the tick data to this StockTick.  If the tick type already exists in this stock tick, overwrites the
     * data with the new value.
     *
     * @param tickType Integer tick type corresponding to the stock data provided
     * @param value Value associated with the tick type provided; value must not be null
     */
    public void addTick(int tickType, Number value) {
        this.ticks.put(tickType, value);
        this.missingTicks.remove(tickType);
    }

    /**
     * Retrieves the value associated to the tick type specified for this StockTick.
     *
     * @param tickType Integer tick type corresponding to the stock data to retrieve
     * @return The value (as defined by the provided tick type, i.e. price, size) for this StockTick.
     *         If no data exists under the tick type specified in this StockTick, null is returned instead.
     */
    public Number getTick(int tickType) {
        if (!this.hasTick(tickType)) {
            return null;
        }

        return this.ticks.get(tickType);
    }

    /**
     * Checks whether this stock tick has market data for the tick type requested.
     *
     * @param tickType Integer tick type corresponding to the stock data to verify
     * @return True if this StockTick has tick data corresponding to the tick type specified and false otherwise
     */
    public boolean hasTick(int tickType) {
        return this.ticks.containsKey(tickType);
    }

    /**
     * Retrieves the stock ticker associated with this StockTick.
     *
     * @return The String ticker associated with the stock tick data of this StockTick
     */
    public String getTicker() {
        return this.ticker;
    }

    /**
     * Returns true if this StockTick has data for all the required tick types, and false otherwise.
     */
    public boolean isComplete() {
        return this.missingTicks.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof StockTick)) {
            return false;
        }

        StockTick st = (StockTick) other;

        return this.getTicker().equals(st.getTicker());
    }

    @Override
    public int hashCode() {
        return this.getTicker().hashCode();
    }

    /**
     * Standard toString definition for this StockTick.  Provides the symbol and all available stock tick data values
     * found in this StockTick.  Returns a String in the form of:
     * "Symbol: BRAB
     * Field: 1, Value: 4
     * Field: 2, Value: 2
     * ..."
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Set<Integer> fields = new TreeSet<Integer>(this.ticks.keySet());

        if (this.getTicker() != null) {
            builder.append("Symbol: ");
            builder.append(this.getTicker());
            builder.append(System.lineSeparator());
        }

        for (Integer field : fields) {
            builder.append(TickTypes.asString(field));
            builder.append(": ");
            builder.append(this.ticks.get(field));
            builder.append(System.lineSeparator());
        }

        return builder.toString();
    }
}
