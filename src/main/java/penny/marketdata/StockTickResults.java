package main.java.penny.marketdata;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import com.ib.client.Contract;

/**
 * StockTickResults represents a collection of stock tick data results associated with market data tick requests.
 * Results can be accessed either by the stock ticker or integer market data identifier.  StockTickResults
 * provides a robust central location for stock tick data that accommodates concurrent updates and queries.
 */
public class StockTickResults {

    /** Map from integer market data identifier to StockTick data */
    private ConcurrentHashMap<Integer, StockTick> results;

    /** Map from String stock ticker to associated integer market data identifier */
    private ConcurrentHashMap<String, Integer> tickers;

    /**
     * Constructs a new StockTickResults with an empty collection of market data and stock tick data results.
     */
    public StockTickResults() {
        this.results = new ConcurrentHashMap<Integer, StockTick>();
        this.tickers = new ConcurrentHashMap<String, Integer>();
    }

    /**
     * Adds a new stock tick associated to the market data request identifier and the contract specified.  If the
     * same stock ticker has results from a previous market data request, the stock data is now only associated with
     * the new market data identifier (tickId).
     *
     * @param tickId Integer corresponding to the market data identifier for stock data updates
     * @param contract Stock contract associated to the market data subscription
     */
    public void addStockTick(int tickId, Contract contract) {
        StockTick tick;
        // If we've already added this StockTick, we want to modify the same StockTick instance even with the new tickId
        if (this.hasTicker(contract.symbol())) {
            int existingTickID = this.tickers.get(contract.symbol());
            tick = this.results.get(existingTickID);
        } else {
            tick = new StockTick(contract.symbol());
        }

        this.results.put(tickId, tick);
        this.tickers.put(contract.symbol(), tickId);
    }

    /**
     * Adds the individual tick data to the stock associated with the market data identifier provided.  Updates
     * and overwrites any previous data for that stock associated with the same tick type.
     *
     * @param tickId Integer market data identifier associated with this tick update
     * @param tickType Integer tick type corresponding to the stock data to update
     * @param value Value of the stock for this particular tick type
     * @return True if the market data identifier provided exists in these stock results and the tick data associated
     *         with the stock was able to be updated, and false otherwise
     */
    public boolean addTickResult(int tickId, int tickType, Number value) {
        if (!this.hasStockTick(tickId)) {
            return false;
        }
        StockTick tick = this.results.get(tickId);
        tick.addTick(tickType, value);
        return true;
    }

    /**
     * Copies the provided stock tick and stores it in these stock tick results associated with the
     * market data identifier provided.
     *
     * @param tickId Integer market data identifier associated with this stock
     * @param tick StockTick to ocpy into these results
     */
    public void copyStockTick(int tickId, StockTick tick) {
        this.results.put(tickId, new StockTick(tick));
        this.tickers.put(tick.getTicker(), tickId);
    }

    /**
     * Gets the stock tick associated to the market data identifier provided.
     *
     * @param tickId Integer market data identifier associated with this stock to retrieve
     * @return StockTick associated to the market data identifier provided; if no stock is associated with the
     *         market data identifier provided in these stock results, null is returned instead
     */
    public StockTick getStockTick(int tickId) {
        if (!this.hasStockTick(tickId)) {
            return null;
        }

        return this.results.get(tickId);
    }

    /**
     * Gets the stock tick with the stock ticker provided.
     *
     * @param ticker The String stock ticker of the stock to retrieve
     * @return StockTick with the String ticker provided; if no stock is associated with the ticker in these stock
     *         results, null is returned instead
     */
    public StockTick getStockTick(String ticker) {
        if (!this.hasTicker(ticker)) {
            return null;
        }

        return this.getStockTick(this.tickers.get(ticker));
    }

    /**
     * Retrieves all the individual stock tick data from these stock tick results.
     *
     * @return An unmodifiable collection of StockTicks contained in these results
     */
    public Collection<StockTick> getStockTicks() {
        return Collections.unmodifiableCollection(this.results.values());
    }

    /**
     * Checks whether these StockTickResults has stock tick data associated with the market data identifier provided.
     *
     * @param tickType Integer corresponding to the market data identifier of the stock to verify
     * @return True if these StockTickResults contain stock tick data associated to the market data identifier
     *         specified, and false otherwise
     */
    public boolean hasStockTick(int tickType) {
        return this.results.containsKey(tickType);
    }

    /**
     * Checks whether these StockTickResults has stock tick data for the ticker specified.
     *
     * @param ticker The String stock ticker of the stock to verify
     * @return True if these StockTickResults contain stock tick data for the ticker specified, and false otherwise
     */
    public boolean hasTicker(String ticker) {
        return this.tickers.containsKey(ticker);
    }
}
