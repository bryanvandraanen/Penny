package main.java.penny.mock;

import com.ib.client.EClientSocket;
import com.ib.client.EReaderSignal;

import main.java.penny.Broker;

/**
 * MockBroker represents a singleton contrived broker that maintains manufactured market data without establishing
 * an actual connection with a live broker.  MockBroker operates identically to a live Broker singleton except it
 * circumvents communication over a network to receive real market data and instead opts for simulated market data.
 *
 * Leveraged for program demonstration and testing purposes.
 */
public class MockBroker extends Broker {

    /**
     * Constructs a new MockBroker with simulated market data that delivers sequentially and synchronously.
     */
    protected MockBroker() {
        this(false /* Don't deliver in parallel */);
    }

    /**
     * Constructs a new MockBroker with simulated market data that delivers data following the protocol
     * specified.  If market data is requested to deliver in parallel, acts accordingly and configures
     * timeouts to kill stalled out delivery responses.
     *
     * @param deliverInParallel True if this MockBroker should maintain market data that delivers in parallel
     *                          and asynchronously (with timeouts), and false if this MockBroker should deliver
     *                          data sequentially and synchronously
     */
    protected MockBroker(boolean deliverInParallel) {
        if (deliverInParallel) {
            this.marketData = new MockMarketData(true /* Deliver in parallel */, true /* Timeout requests */);
        } else {
            this.marketData = new MockMarketData(false /* Don't deliver in parallel */, false /* No timeout */);
        }
    }

    /**
     * @throws UnsupportedOperationException since MockBroker does not establish a live connection to the broker.
     */
    @Override
    public EClientSocket getClient() {
        throw new UnsupportedOperationException("Mock Broker does not allow Client access");
    }

    /**
     * @throws UnsupportedOperationException since MockBroker does not establish a live connection to the broker.
     */
    @Override
    public EReaderSignal getSignal() {
        throw new UnsupportedOperationException("Mock Broker does not allow Signal access");
    }

    /**
     * Initializes a new singleton MockBroker with sequentially and synchronously delivered market data.
     */
    public static void init() {
        broker = new MockBroker();
    }

    /**
     * Initializes a new singleton MockBroker with market data delivered following the protocol specified.
     * If market data is requested to deliver in parallel, acts accordingly and configures timeouts to kill
     * stalled out delivery responses.
     *
     * @param deliverInParallel True if this MockBroker should maintain market data that delivers in parallel
     *                          and asynchronously (with timeouts), and false if this MockBroker should deliver
     *                          data sequentially and synchronously
     */
    public static void init(boolean deliverInParallel) {
        broker = new MockBroker(deliverInParallel);
    }

    /**
     * Cleans up the singleton MockBroker instance created previously - leveraged during testing to prevent
     * inconsistencies and data leaks across individual tests.
     */
    public static void cleanup() {
        broker = null;
    }
}
