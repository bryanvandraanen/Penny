package main.java.penny.marketdata;

import main.java.penny.concurrent.LockManagerUtil;
import main.java.penny.mock.MockMarketData;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import main.java.penny.Broker;
import main.java.penny.mock.MockBroker;

import java.util.*;

public class ScannerTest {

    protected List<String> simpleTickers;

    protected List<String> scannerTickers;

    protected List<String> filteredOTCTickers;

    protected List<String> completeOTCTickers;

    @Before
    public void setup() {
        MockBroker.init();
        simpleTickers = new ArrayList<String>(26);

        for (char let = 'a'; let <= 'z'; let++) {
            simpleTickers.add(Character.toString(let));
        }

        scannerTickers = new ArrayList<String>(50);

        for (int i = 1; i <= 50; i++) {
            scannerTickers.add(Integer.toString(i));
        }

        filteredOTCTickers = new ArrayList<String>(4000);

        for (int i = 1; i <= 4000; i++) {
            filteredOTCTickers.add(Integer.toString(i));
        }

        completeOTCTickers = new ArrayList<String>(4000);

        for (int i = 1; i <= 10000; i++) {
            completeOTCTickers.add(Integer.toString(i));
        }
    }

    @After
    public void cleanup() {
        ((MockMarketData) Broker.getInstance().getMarketData()).cleanup();
        ((MockBroker) Broker.getInstance()).cleanup();
        LockManagerUtil.cleanup();
    }

    @Test
    public void testScanParallelSimpleScale() {
        testParallel(simpleTickers);
    }

    @Test
    public void testScanSequentialSimpleScale() {
        testSequential(simpleTickers);
    }

    @Test
    public void testScanParallelScannerScale() {
        testParallel(scannerTickers);
    }

    @Test
    public void testScanSequentialScannerScale() {
        testSequential(scannerTickers);
    }

    @Test
    public void testScanParallelFilteredOTCScale() {
        testParallel(filteredOTCTickers);
    }

    @Test
    public void testScanSequentialFilteredOTCScale() {
        testSequential(filteredOTCTickers);
    }

    @Test
    public void testScanParallelCompleteOTCScale() {
        testParallel(completeOTCTickers);
    }

    @Test
    public void testScanSequentialCompleteOTCScale() {
        testSequential(completeOTCTickers);
    }

    private void testParallel(List<String> tickers) {
        StockScanner.scan(tickers);
        Broker.getInstance().getMarketData().waitForActiveDataToDeliver();
        assertMarketDataComplete(tickers);
    }

    private void testSequential(List<String> tickers) {
        StockScanner.scanSequential(tickers);
        Broker.getInstance().getMarketData().waitForActiveDataToDeliver();
        assertMarketDataComplete(tickers);
    }

    private static void assertMarketDataComplete(Collection<String> tickers) {
        MockMarketData mockMarketData = (MockMarketData) Broker.getInstance().getMarketData();
        StockTickResults results = Broker.getInstance().getMarketData().getStockTickResults();
        int countIncomplete = 0;
        for (String ticker : tickers) {
            Assert.assertTrue(results.hasTicker(ticker));
            if (!mockMarketData.isTimingOutMarketDataRequests()) {
                Assert.assertTrue(results.getStockTick(ticker).isComplete());
            } else {
                if (!results.getStockTick(ticker).isComplete()) {
                    countIncomplete++;
                }
            }
        }

        double percentIncomplete = (double) countIncomplete / tickers.size();

        // Assert the number of incomplete tickers is less than 10% (from timeouts)
        Assert.assertTrue("Percent Incomplete was: " + percentIncomplete, percentIncomplete <= 0.1);
    }
}