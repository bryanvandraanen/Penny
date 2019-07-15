package main.java.penny.marketdata;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class StockScannerFilterTest {

    private List<String> tickers;

    @Before
    public void setup() {
        tickers = new ArrayList<String>();

        // Length 1 tickers
        for (int i = 0; i < 10; i++) {
            tickers.add(Integer.toString(i));
        }

        // Length 2 tickers
        for (int i = 10; i < 20; i++) {
            tickers.add(Integer.toString(i));
        }

        // Length 3 tickers
        for (int i = 100; i < 110; i++) {
            tickers.add(Integer.toString(i));
        }

        // Length 4 tickers
        for (int i = 1000; i < 1010; i++) {
            tickers.add(Integer.toString(i));
        }

        // Length 5 tickers
        for (int i = 10000; i < 10010; i++) {
            tickers.add(Integer.toString(i));
        }
    }


    @Test
    public void testFilterEmpty() {
        List<String> empty = new ArrayList<String>();

        StockScannerFilter filter = new StockScannerFilter.StockScannerFilterBuilder().withMaximumTickerLength(5).build();

        List<String> result = filter.filter(empty);

        Assert.assertTrue(empty.isEmpty());
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testFilterRemovesEverything() {
        StockScannerFilter filter = new StockScannerFilter.StockScannerFilterBuilder().withMaximumTickerLength(0).build();

        List<String> result = filter.filter(tickers);

        Assert.assertFalse(tickers.isEmpty());
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testFilterRemovesNothing() {
        StockScannerFilter filter = new StockScannerFilter.StockScannerFilterBuilder().withMaximumTickerLength(5).build();

        List<String> result = filter.filter(tickers);

        Assert.assertEquals(tickers.size(), result.size());

        for (String ticker : tickers) {
            Assert.assertTrue(result.contains(ticker));
        }
    }

    @Test
    public void testFilterRemovesExpected() {
        StockScannerFilter filter = new StockScannerFilter.StockScannerFilterBuilder().withMaximumTickerLength(4).build();

        List<String> result = filter.filter(tickers);

        Assert.assertTrue(result.size() < tickers.size());

        for (String ticker : tickers) {
            if (ticker.length() > 4) {
                Assert.assertFalse(result.contains(ticker));
            } else {
                Assert.assertTrue(result.contains(ticker));
            }
        }
    }

    @Test
    public void testFilterIsIdempotent() {
        StockScannerFilter filter = new StockScannerFilter.StockScannerFilterBuilder().withMaximumTickerLength(4).build();

        List<String> result = filter.filter(tickers);

        Assert.assertTrue(result.size() < tickers.size());

        for (String ticker : tickers) {
            if (ticker.length() > 4) {
                Assert.assertFalse(result.contains(ticker));
            } else {
                Assert.assertTrue(result.contains(ticker));
            }
        }

        List<String> doubleFilter = filter.filter(result);

        Assert.assertEquals(result.size(), doubleFilter.size());

        for (String ticker : result) {
            Assert.assertTrue(doubleFilter.contains(ticker));
        }
    }
}