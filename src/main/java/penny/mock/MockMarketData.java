package main.java.penny.mock;

import com.ib.client.Contract;
import main.java.penny.Broker;
import main.java.penny.concurrent.Lock;
import main.java.penny.concurrent.LockManager;
import main.java.penny.constants.MarketDataConstants;
import main.java.penny.marketdata.MarketData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static main.java.penny.constants.TickTypes.*;

/**
 * MockMarketData represents a contrived market data request and delivery interface.
 * MockMarketData operates identically to live MarketData except it circumvents communication
 * with the actual broker and delivers random, simulated market data instead for requests.
 * The market data can be configured so it delivers synchronously or asynchronously in parallel.
 *
 * Leveraged for program demonstration and testing purposes.
 */
public class MockMarketData extends MarketData {

    /** Maximum simulated volume to deliver for stocks */
    private static int MAX_MOCK_VOLUME = 200;

    /** Maximum simulated price to deliver for stocks */
    private static double MAX_MOCK_PRICE = 2.00;

    /** Unified random number generator for market data simulation */
    private static Random rng = new Random();

    /** True if market data should deliver asynchronously and in parallel, and false otherwise */
    private final boolean isDeliveringInParallel;

    /** True if market data delivery should timeout after a default period of time, and false otherwise */
    private final boolean timeoutMarketDataDelivery;

    /** Set of all parallel threads delivering market data */
    private final Set<Thread> deliverers;

    /**
     * Constructs a new MockMarketData following the protocol specified.  Delivers simulated data
     * asynchronously if market data is requested to deliver in parallel and likewise times out
     * market data delivery if specified.
     *
     * @param deliverInParallel True if this MockMarketData should deliver results in parallel and asynchronously,
     *                          and false if it should deliver data sequentially and synchronously
     * @param timeoutMarketDataDelivery True if this MockMarketData should timeout delivery requests after the
     *                                  default market data timeout time, and false otherwise
     */
    public MockMarketData(boolean deliverInParallel, boolean timeoutMarketDataDelivery) {
        super();
        this.isDeliveringInParallel = deliverInParallel;
        this.timeoutMarketDataDelivery = timeoutMarketDataDelivery;

        if (!this.timeoutMarketDataDelivery) {
            this.shutdownTimeoutProcess();
        }
        this.deliverers = ConcurrentHashMap.newKeySet();
    }

    @Override
    public void requestMarketData(String ticker) {
        Contract contract = contract(ticker);
        int tickId = this.getNextMarketDataId();

        this.tickers.add(ticker);
        this.activeMarketData.put(tickId, System.currentTimeMillis());

        this.addStockTick(tickId, contract);

        Lock lock = LockManager.getInstance().getLock(tickId);
        lock.lock();

        // Mock market data requests here (can create a thread to fake market data)
        this.deliverMarketData(tickId);
    }

    @Override
    public void cancelMarketData(int tickId) {
        this.removeActiveMarketData(tickId);
        this.maybeNotifyAllDataDelivered();
        Broker.getInstance().getWrapper().marketDataType(tickId, MarketDataConstants.MARKET_DATA_TYPE);
    }

    /**
     * Returns true if this MockMarketData is timing out market data deliveries, and false otherwise.
     */
    public boolean isTimingOutMarketDataRequests() {
        return this.timeoutMarketDataDelivery;
    }

    /**
     * Cleans up this MockMarketData interrupting any currently active threads delivering market data.
     */
    public void cleanup() {
        this.shutdownTimeoutProcess();
        for (Thread thread : this.deliverers) {
            if (thread.isAlive()) {
                thread.interrupt();
            }
        }
    }

    /**
     * Delivers all required market data ticks to the market data identifier specified.  If this MockMarketData
     * is configured to deliver tick data in parallel, spawns threads for all required tick data to deliver.
     * Otherwise, delivers market data immediately and sequentially to the identifier provided.
     *
     * @param tickId The market data identifier associated with the stock to deliver data to
     */
    private void deliverMarketData(int tickId) {
        // Create simulated data for all required stock tick types
        int volume = rng.nextInt(MAX_MOCK_VOLUME);
        int averageVolume = rng.nextInt(MAX_MOCK_VOLUME);

        double open = rng.nextDouble() * MAX_MOCK_PRICE;
        double last = rng.nextDouble() * MAX_MOCK_PRICE;

        double low = Math.min(Math.min(open, last), rng.nextDouble() * MAX_MOCK_PRICE);
        double high = Math.max(Math.max(open, last), rng.nextDouble() * MAX_MOCK_PRICE);

        double low13Weeks = Math.min(low, rng.nextDouble() * MAX_MOCK_PRICE);
        double high13Weeks = Math.max(high, rng.nextDouble() * MAX_MOCK_PRICE);

        if (this.isDeliveringInParallel) {
            // Start delivering all market data in parallel (random delay occurs by OS scheduler)
            Thread thread = new Thread(() -> deliver(tickId, open, OPEN));
            addAndStart(thread);
            thread = new Thread(() -> deliver(tickId, last, LAST));
            addAndStart(thread);
            thread = new Thread(() -> deliver(tickId, low, LOW));
            addAndStart(thread);
            thread = new Thread(() -> deliver(tickId, high, HIGH));
            addAndStart(thread);
            thread = new Thread(() -> deliver(tickId, low13Weeks, LOW_13_WEEKS));
            addAndStart(thread);
            thread = new Thread(() -> deliver(tickId, high13Weeks, HIGH_13_WEEKS));
            addAndStart(thread);
            thread = new Thread(() -> deliver(tickId, volume, VOLUME));
            addAndStart(thread);
            thread = new Thread(() -> deliver(tickId, averageVolume, AVERAGE_VOLUME));
            addAndStart(thread);
        } else {
            // Deliver sequentially
            deliver(tickId, open, OPEN);
            deliver(tickId, last, LAST);
            deliver(tickId, low, LOW);
            deliver(tickId, high, HIGH);
            deliver(tickId, low13Weeks, LOW_13_WEEKS);
            deliver(tickId, high13Weeks, HIGH_13_WEEKS);
            deliver(tickId, volume, VOLUME);
            deliver(tickId, averageVolume, AVERAGE_VOLUME);
        }
    }

    /**
     * Delivers the integer size value to the stock associated with the market data identifier provided for
     * the particular tick type.
     */
    private void deliver(int tickId, int value, int tickType) {
        Broker.getInstance().getWrapper().tickSize(tickId, tickType, value);
    }

    /**
     * Delivers the double price value to the stock associated with the market data identifier provided
     * for the particular tick type.
     */
    private void deliver(int tickId, double value, int tickType) {
        Broker.getInstance().getWrapper().tickPrice(tickId, tickType, value, null);
    }

    /**
     * Adds the thread provided to the set of all market data delivery threads and starts the process.
     *
     * @param thread The thread to start and add to the set of all delivery threads from this MockMarketData
     */
    private void addAndStart(Thread thread) {
        this.deliverers.add(thread);
        thread.start();
    }
}
