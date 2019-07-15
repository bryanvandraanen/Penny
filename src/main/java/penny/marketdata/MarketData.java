package main.java.penny.marketdata;

import com.ib.client.Contract;
import main.java.penny.Broker;
import main.java.penny.concurrent.Lock;
import main.java.penny.concurrent.LockManager;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static main.java.penny.constants.MarketDataConstants.*;

/**
 * MarketData represents the primary method of requesting and accessing market data from the broker.
 * MarketData retains information about each scanned and requested stock and associates the active trading
 * information provided by the broker with each ticker.  MarketData provides a simple, unified means of requesting
 * market data given a particular stock ticker for the OTC Market and ensures consistent delivery of information
 * from the broker.
 */
public class MarketData {
    /** Atomic market data identifier for unique requests to broker */
    private AtomicInteger marketDataId;

    /** Set of all unique tickers scanned or requested for associated market data */
    protected Set<String> tickers;

    /** Map from integer market data identifiers to request time UTC for all active market data requests to broker */
    protected Map<Integer, Long> activeMarketData;

    /** Compiled collection of market data information received for each particular requested stock */
    private StockTickResults stockTickResults;

    /** Thread spinning indefinitely to timeout market data that failed to deliver */
    private Thread timeoutMarketDataThread;

    /** Flag indicating whether this market data is currently timing market data requests out */
    private boolean isTimingMarketDataOut;

    /**
     * Constructs a new MarketData with no active market data requests and empty stock results.
     */
    public MarketData() {
        this.tickers = ConcurrentHashMap.newKeySet();
        this.activeMarketData = new ConcurrentHashMap<Integer, Long>();
        this.stockTickResults = new StockTickResults();

        this.marketDataId = new AtomicInteger(0);

        // Create a timeout cycling thread to cancel market data that takes too long to deliver
        this.isTimingMarketDataOut = true;
        this.timeoutMarketDataThread = new Thread(() -> timeoutCancelMarketData(MARKET_DATA_TIMEOUT_MILLIS));
        this.timeoutMarketDataThread.start();
    }

    /**
     * Halts the existing timeout process of this market data to cancel market data requests for stalled
     * out requests.  This operation is irreversible and future market data request behaviors is not
     * guaranteed to terminate with no timeout process in place.
     */
    public void shutdownTimeoutProcess() {
        this.isTimingMarketDataOut = false;
        this.timeoutMarketDataThread.interrupt();
    }

    /**
     * Requests market data for the specified symbol.  Specifically, requests current market data information
     * associated with the particular symbol found on the OTC Market.  Adds the ticker provided to the unified
     * collection of scanned tickers and updates the market data of the stock to be active.  If too many market data
     * requests are currently active (as enforced by the broker), blocks execution until a market data request
     * can be safely made following broker limitations.
     *
     * @param ticker The stock ticker to request market data for in the OTC Market
     */
    public void requestMarketData(String ticker) {
        Contract contract = contract(ticker);
        int tickId = this.getNextMarketDataId();

        // Add this ticker to the active market data requests
        this.tickers.add(ticker);
        this.activeMarketData.put(tickId, System.currentTimeMillis());

        this.addStockTick(tickId, contract);

        // Wait until we can acquire a lock so as not to violate broker market data request limitations
        Lock lock = LockManager.getInstance().getLock(tickId);
        lock.lock();
        Broker.getInstance().getClient().reqMktData(tickId, contract, TICK_STRING,
                false /* Snapshot */, null /* MktDataOptions */);
    }

    /**
     * Cancels the market data associated with the provided tick identifier.  When market data requests are made,
     * they are associated with a unique tick identifier to ensure consistency and match the delivery of values from
     * the broker.  Cancelling market data communicates to the broker that the stock associated with this identifier
     * no longer wants continued updated market data information.
     *
     * @param tickId The tick identifier associated with the stock to cancel market data
     */
    public void cancelMarketData(int tickId) {
        this.removeActiveMarketData(tickId);
        // Try to notify anyone waiting for all outstanding market data requests to deliver if this
        // market data request is the final request being waited on
        this.maybeNotifyAllDataDelivered();
        Broker.getInstance().getClient().cancelMktData(tickId);
    }

    /**
     * Adds the stock ticker provided to the unique collection of tickers observed by this MarketData.
     *
     * @param ticker Stock ticker to add to the set of tickers known by this MarketData.
     */
    public void addTicker(String ticker) {
        this.tickers.add(ticker);
    }

    /**
     * Retrieve all the tickers known by this MarketData that have been scanned or received market data information
     * at some point in time.
     *
     * @return An unmodifiable set of stock tickers that have been observed by this MarketData.
     */
    public Set<String> getTickers() {
        return Collections.unmodifiableSet(this.tickers);
    }

    /**
     * Blocks the current execution until all active market data requests have delivered.  That is, causes the
     * current execution to wait until all stocks with active market data have completed or timed out.  This function
     * is thread-safe.
     */
    public synchronized void waitForActiveDataToDeliver() {
        // While there is any active market data requests, wait
        while (!this.activeMarketData.isEmpty()) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                // Simply recheck if all active market data delivered
            }
        }
    }

    /**
     * Provides the stock tick results containing all the stock information retrieved by this MarketData from the broker.
     *
     * @return StockTickResults maintained by this MarketData containing all stock pricing and sizing information
     *         retrieved from the broker.
     */
    public StockTickResults getStockTickResults() {
        return this.stockTickResults;
    }

    /**
     * Converts the provided stock ticker to a valid OTC Market contract.  The contract helps identify and route
     * market data requests to the broker specifying the security type, exchange, and currency of US stocks in the
     * OTC Market.
     *
     * @param ticker The stock ticker to convert to a valid OTC Market contract.
     * @return A contract associated with the stock ticker for US stocks in the OTC Market.
     */
    public static Contract contract(String ticker) {
        Contract contract = new Contract();

        contract.symbol(ticker);
        contract.secType(SECURITY_TYPE);
        contract.exchange(EXCHANGE);
        contract.currency(CURRENCY);

        return contract;
    }

    /**
     * Adds a new stock tick associated to the market data request tick id and the contract specified to the market
     * data results.
     *
     * @param tickId - Integer corresponding to the market data request associated with a particular stock
     * @param contract -  Stock contract detailing routing information of the trading market
     */
    protected void addStockTick(int tickId, Contract contract) {
        this.getStockTickResults().addStockTick(tickId, contract);
    }

    /**
     * Returns the next unique market data request identifier.  This method returns a monotonically increasing ID
     * and only returns each unique identifier a single time per invocation, thus no two market data ids
     * will ever be returned more than once.
     */
    protected int getNextMarketDataId() {
        return this.marketDataId.addAndGet(1);
    }

    /**
     * Removes the indication that market data is still active for the tick identifier specified.
     *
     * @param tickId Stock tick request identifier to no longer consider having active market data
     */
    protected void removeActiveMarketData(int tickId) {
        this.activeMarketData.remove(tickId);
    }

    /**
     * Potentially notify all waiting threads that all market data has delivered and is no longer active.
     * Specifically, if there is no active market data currently outstanding in this MarketData,
     * all threads waiting for data to complete are notified and unblocked.  This method is thread-safe.
     */
    protected synchronized void maybeNotifyAllDataDelivered() {
        if (this.activeMarketData.isEmpty()) {
            this.notifyAll();
        }
    }

    /**
     * Timeout market data cancellation function that cancels market data for any market data identifier that has
     * exceeded the timeout period specified.  If market data is still active after the timeout period specified,
     * cancels the market data request, otherwise does not influence the market data.  Spins indefinitely checking
     * for market data requests that have stalled out.
     *
     * @param timeout The timeout period to wait prior to cancelling the market data for the identifier provided
     */
    private void timeoutCancelMarketData(int timeout) {
        // Spin indefinitely, cancelling active market data that has had an outstanding request for too long
        while (this.isTimingMarketDataOut) {
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                // If interrupted, simply continue with timeout process protocol
            }

                // After the timeout period, if market data is still active, cancel the market data for this identifier
            Set<Integer> activeMarketDataSnapshot = new HashSet<Integer>(this.activeMarketData.keySet());
            for (Integer tickId : activeMarketDataSnapshot) {
                long requestStartTimeMillis = this.activeMarketData.getOrDefault(tickId, System.currentTimeMillis());

                if (System.currentTimeMillis() - requestStartTimeMillis > timeout) {
                    this.cancelMarketData(tickId);
                }
            }
        }
    }
}
