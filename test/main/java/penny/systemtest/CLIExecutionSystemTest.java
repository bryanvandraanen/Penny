package main.java.penny.systemtest;

import main.java.penny.Broker;
import main.java.penny.Main;
import main.java.penny.constants.CLIConstants;
import main.java.penny.marketdata.StockTickResults;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import java.util.Set;

import static main.java.penny.constants.ScannerConstants.MAX_TICKER_LENGTH_FILTER;

public class CLIExecutionSystemTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    private static String[] args = {CLIConstants.SPOOF_COMMAND, CLIConstants.SPOOF_DELIVER_IN_PARALLEL_COMMAND};

    @Test
    public void testCLIExecutionSystem() {
        exit.expectSystemExitWithStatus(0);
        Main.main(args);

        Set<String> tickers = Broker.getInstance().getMarketData().getTickers();

        for (String ticker : tickers) {
            if (ticker.length() <= MAX_TICKER_LENGTH_FILTER) {
                Assert.assertTrue(tickers.contains(ticker));
            }
        }

        StockTickResults results = Broker.getInstance().getMarketData().getStockTickResults();
        int countIncomplete = 0;
        for (String ticker : tickers) {
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
