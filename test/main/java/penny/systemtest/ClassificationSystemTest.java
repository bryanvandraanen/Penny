package main.java.penny.systemtest;

import main.java.penny.Broker;
import main.java.penny.concurrent.LockManagerUtil;
import main.java.penny.constants.Classification;
import main.java.penny.constants.TickTypes;
import main.java.penny.marketdata.*;
import main.java.penny.marketdata.StockScanner;
import main.java.penny.mock.MockBroker;
import main.java.penny.mock.MockMarketData;
import main.java.penny.models.Classifier;
import main.java.penny.models.PumpClassifier;
import main.java.penny.models.classification.ClassificationFilter;
import main.java.penny.models.classification.ClassificationResult;
import main.java.penny.util.OTCTickersReader;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static main.java.penny.constants.ClassificationConstants.MIN_PRICE_FILTER;
import static main.java.penny.constants.ClassificationConstants.MIN_VOLUME_USD_FILTER;
import static main.java.penny.constants.ScannerConstants.MAX_TICKER_LENGTH_FILTER;

public class ClassificationSystemTest {

    private Classifier classifier;

    private List<String> tickers;

    private Map<String, Classification> spoofedTickers;

    private int testTickID;

    @Before
    public void setup() throws Exception {
        MockBroker.init(true /* Deliver in Parallel */);
        tickers = OTCTickersReader.getOTCTickers();
        classifier = new PumpClassifier();
        spoofedTickers = new HashMap<String, Classification>();
        testTickID = -1;

        addSpoofContracts();
    }

    @After
    public void cleanup() {
        ((MockMarketData) Broker.getInstance().getMarketData()).cleanup();
        ((MockBroker) Broker.getInstance()).cleanup();
        LockManagerUtil.cleanup();
    }

    @Test
    public void testSystemClassification() {
        StockScannerFilter scannerFilter = new StockScannerFilter.StockScannerFilterBuilder()
                .withMaximumTickerLength(MAX_TICKER_LENGTH_FILTER).build();
        StockScanner.scan(tickers, scannerFilter);

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

        double percentIncomplete = (double) countIncomplete / scannedTickers.size();

        // Assert the number of incomplete tickers is less than 10% (from timeouts)
        Assert.assertTrue("Percent Incomplete was: " + percentIncomplete, percentIncomplete <= 0.1);

        ClassificationFilter classificationFilter = new ClassificationFilter.ClassificationFilterBuilder()
                .withMinimumVolumeUSD(MIN_VOLUME_USD_FILTER)
                .withMinimumPrice(MIN_PRICE_FILTER)
                .build();

        Collection<StockTick> stockTicks = results.getStockTicks();
        stockTicks = classificationFilter.filter(stockTicks);

        int countInvalid = 0;
        for (StockTick tick : stockTicks) {
            ClassificationResult classification = classifier.classify(tick);

            if (spoofedTickers.containsKey(tick.getTicker())) {
                Classification expected = spoofedTickers.get(tick.getTicker());

                boolean isPositive = expected == Classification.Positive;
                boolean isNegative = expected == Classification.Negative;

                Assert.assertFalse(classification.isInvalid());

                Assert.assertEquals(isPositive, classification.isPositive());
                Assert.assertEquals(isNegative, classification.isNegative());
            }

            if (classification.isInvalid()) {
                countInvalid++;
            }
        }

        double percentInvalid = (double) countInvalid / stockTicks.size();

        // Assert the number of invalid tickers is less than 10%
        Assert.assertTrue("Percent Classifications Invalid was: " + percentInvalid, percentInvalid <= 0.1);
    }

    private void addSpoofContracts() {
        StockTickResults results = Broker.getInstance().getMarketData().getStockTickResults();

        // Positive Classification Stock Ticks
        results.copyStockTick(testTickID--, FPTA());
        results.copyStockTick(testTickID--, KLMN());
        results.copyStockTick(testTickID--, BRKO());

        spoofedTickers.put(FPTA().getTicker(), Classification.Positive);
        spoofedTickers.put(KLMN().getTicker(), Classification.Positive);
        spoofedTickers.put(BRKO().getTicker(), Classification.Positive);

        tickers.remove(FPTA().getTicker());
        tickers.remove(KLMN().getTicker());
        tickers.remove(BRKO().getTicker());

        // Negative Classification Stock Ticks
        results.copyStockTick(testTickID--, PRED());
        results.copyStockTick(testTickID--, WHZT());

        spoofedTickers.put(PRED().getTicker(), Classification.Negative);
        spoofedTickers.put(WHZT().getTicker(), Classification.Negative);

        tickers.remove(PRED().getTicker());
        tickers.remove(WHZT().getTicker());
    }

    private StockTick FPTA() {
        StockTick tick = new StockTick(MarketData.contract("FPTA").symbol());

        tick.addTick(TickTypes.OPEN, 2.5);
        tick.addTick(TickTypes.LAST, 2.9);
        tick.addTick(TickTypes.HIGH, 3.02);
        tick.addTick(TickTypes.LOW, 2.5);
        tick.addTick(TickTypes.VOLUME, 3279);
        tick.addTick(TickTypes.AVERAGE_VOLUME, 31);
        tick.addTick(TickTypes.HIGH_13_WEEKS, 10.4);
        tick.addTick(TickTypes.LOW_13_WEEKS, 1);

        return tick;
    }

    private StockTick KLMN() {
        StockTick tick = new StockTick(MarketData.contract("KLMN").symbol());

        tick.addTick(TickTypes.OPEN, 1.26);
        tick.addTick(TickTypes.LAST, 1.65);
        tick.addTick(TickTypes.HIGH, 1.69);
        tick.addTick(TickTypes.LOW, 1.15);
        tick.addTick(TickTypes.VOLUME, 3290);
        tick.addTick(TickTypes.AVERAGE_VOLUME, 32);
        tick.addTick(TickTypes.HIGH_13_WEEKS, 11);
        tick.addTick(TickTypes.LOW_13_WEEKS, 1);

        return tick;
    }

    private StockTick BRKO() {
        StockTick tick = new StockTick(MarketData.contract("BRKO").symbol());

        tick.addTick(TickTypes.OPEN, 2.05);
        tick.addTick(TickTypes.LAST, 2.71);
        tick.addTick(TickTypes.HIGH, 2.8);
        tick.addTick(TickTypes.LOW, 2.05);
        tick.addTick(TickTypes.VOLUME, 6314);
        tick.addTick(TickTypes.AVERAGE_VOLUME, 142);
        tick.addTick(TickTypes.HIGH_13_WEEKS, 3.45);
        tick.addTick(TickTypes.LOW_13_WEEKS, 0.5);

        return tick;
    }

    private StockTick PRED() {
        StockTick tick = new StockTick(MarketData.contract("PRED").symbol());

        tick.addTick(TickTypes.OPEN, 1.05);
        tick.addTick(TickTypes.LAST, 1.05);
        tick.addTick(TickTypes.HIGH, 1.1);
        tick.addTick(TickTypes.LOW, 0.95);
        tick.addTick(TickTypes.VOLUME, 3310);
        tick.addTick(TickTypes.AVERAGE_VOLUME, 1575);
        tick.addTick(TickTypes.HIGH_13_WEEKS, 1.4);
        tick.addTick(TickTypes.LOW_13_WEEKS, 0.87);

        return tick;
    }

    private StockTick WHZT() {
        StockTick tick = new StockTick(MarketData.contract("WHZT").symbol());

        tick.addTick(TickTypes.OPEN, 2.82);
        tick.addTick(TickTypes.LAST, 2.7);
        tick.addTick(TickTypes.HIGH, 2.82);
        tick.addTick(TickTypes.LOW, 2.6);
        tick.addTick(TickTypes.VOLUME, 562);
        tick.addTick(TickTypes.AVERAGE_VOLUME, 1208);
        tick.addTick(TickTypes.HIGH_13_WEEKS, 3.5149901);
        tick.addTick(TickTypes.LOW_13_WEEKS, 1.1749901);

        return tick;
    }
}
