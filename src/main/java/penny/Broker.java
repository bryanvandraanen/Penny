package main.java.penny;

import com.ib.client.EClientSocket;
import com.ib.client.EReader;
import com.ib.client.EReaderSignal;
import main.java.penny.constants.MarketDataConstants;
import main.java.penny.marketdata.MarketData;

import java.util.concurrent.TimeUnit;

/**
 * Broker represents a singleton access to the active stock broker.  Broker provides access to market data, and
 * communication channels for requests and information about particular stocks between the client and the broker.
 */
public class Broker {
    /** Singleton Broker */
    protected static Broker broker;

    /** Methods of communication and delivery about stocks between the client and broker */
    protected EWrapperImpl wrapper;

    /** Active market data retrieved by broker for requested stocks */
    protected MarketData marketData;

    /**
     * Constructs a new broker with empty market data information and a new communication channel
     * between client and broker.
     */
    protected Broker() {
        this.wrapper = new EWrapperImpl();
        this.marketData = new MarketData();
    }

    /**
     * Returns the singleton instance of the Broker.
     */
    public static Broker getInstance() {
        if (broker == null) {
            init();
        }
        return broker;
    }

    /**
     * Returns the client socket of communication providing the channel of communication with the Broker.
     */
    public EClientSocket getClient() {
        return this.getWrapper().getClient();
    }

    /**
     * Returns the signal receiving and interpreting messages from the Broker.
     */
    public EReaderSignal getSignal() {
        return this.getWrapper().getSignal();
    }

    /**
     * Returns the delivery interface for stock and scanner information from broker to client.
     */
    public EWrapperImpl getWrapper() {
        return this.wrapper;
    }

    /**
     * Returns the active market data associated with the broker.
     */
    public MarketData getMarketData() {
        return this.marketData;
    }

    /**
     * Initializes a new singleton Broker and establishes a new connection with the broker.
     * The connection to the broker is configured under default IP and port.
     */
    public static void init() {
        broker = new Broker();

        // Default broker connection IP and port.
        broker.getClient().eConnect(MarketDataConstants.BROKER_CONNECTION_IP,
                MarketDataConstants.BROKER_CONNECTION_PORT, 0);

        final EReader reader = new EReader(broker.getClient(), broker.getSignal());
        reader.start();

        // Create a new communication thread between us and the broker
        new Thread(() -> {
                while (broker.getClient().isConnected()) {
                    broker.getSignal().waitForSignal();
                    try {
                        reader.processMsgs();
                    } catch (Exception e) {
                        System.out.println("Exception: " + e.getMessage());
                    }
                }
            }).start();

        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        } catch (InterruptedException e) {
            System.err.println("Broker connection interrupted... Exiting");
            System.exit(0);
        }

        // Request real-time market data
        broker.getClient().reqMarketDataType(MarketDataConstants.MARKET_DATA_TYPE);
    }
}
