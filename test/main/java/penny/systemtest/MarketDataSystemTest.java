package main.java.penny.systemtest;

import main.java.penny.Broker;
import main.java.penny.concurrent.LockManagerUtil;
import main.java.penny.marketdata.StockScanner;
import main.java.penny.marketdata.StockScannerFilter;
import main.java.penny.marketdata.StockTickResults;
import main.java.penny.mock.MockBroker;
import main.java.penny.mock.MockMarketData;
import main.java.penny.util.OTCTickersReader;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static main.java.penny.constants.ScannerConstants.MAX_TICKER_LENGTH_FILTER;

public class MarketDataSystemTest {

    private List<String> tickers;

    @Before
    public void setup() {
        MockBroker.init(true /* Deliver in Parallel */);
        tickers = OTCTickersReader.getOTCTickers();
    }

    @After
    public void cleanup() {
        ((MockMarketData) Broker.getInstance().getMarketData()).cleanup();
        ((MockBroker) Broker.getInstance()).cleanup();
        LockManagerUtil.cleanup();
    }

    @Test
    public void testSystemMarketData() {
        StockScannerFilter filter = new StockScannerFilter.StockScannerFilterBuilder().withMaximumTickerLength(MAX_TICKER_LENGTH_FILTER).build();
        StockScanner.scan(tickers, filter);

        Broker.getInstance().getMarketData().waitForActiveDataToDeliver();

        Set<String> scannedTickers = Broker.getInstance().getMarketData().getTickers();

        for (String ticker : tickers) {
            if (ticker.length() <= MAX_TICKER_LENGTH_FILTER) {
                Assert.assertTrue(scannedTickers.contains(ticker));
            }
        }

        StockTickResults results = Broker.getInstance().getMarketData().getStockTickResults();
        int countIncomplete = 0;
        for (String ticker : scannedTickers) {
            Assert.assertTrue(results.hasTicker(ticker));
            if (!results.getStockTick(ticker).isComplete()) {
                countIncomplete++;
            }
        }

        double percentIncomplete = (double) countIncomplete / tickers.size();

        // Assert the number of incomplete tickers is less than 10% (from timeouts)
        Assert.assertTrue("Percent Incomplete was: " + percentIncomplete, percentIncomplete <= 0.1);
    }
}
