package main.java.penny.marketdata;

import com.ib.client.Contract;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

public class StockTickResultsTest {

    private StockTickResults results;

    private List<Contract> contracts;

    @Before
    public void setup() {
        results = new StockTickResults();

        contracts = new ArrayList<Contract>();

        for (int i = 0; i < 10; i++) {
            contracts.add(contract());
        }
    }

    @Test
    public void testAddStockTick() {
        for (int i = 0; i < contracts.size(); i++) {
            Contract contract = contracts.get(i);
            results.addStockTick(i, contract);
            Assert.assertTrue(results.hasStockTick(i));
        }

        for (int i = 0; i < contracts.size(); i++) {
            Assert.assertTrue(results.hasStockTick(i));
        }
    }

    @Test
    public void testAddTickResult() {
        for (int i = 0; i < contracts.size(); i++) {
            Contract contract = contracts.get(i);
            results.addStockTick(i, contract);
            Assert.assertTrue(results.hasStockTick(i));
        }

        for (int i = 0; i < contracts.size(); i++) {
            for (int j = 0; j < 10; j++) {
                boolean addResult = results.addTickResult(i, j, j);
                Assert.assertTrue(addResult);
            }
        }

        for (int i = 0; i < contracts.size(); i++) {
            StockTick tick = results.getStockTick(i);
            for (int j = 0; j < 10; j++) {
                Assert.assertTrue(tick.hasTick(j));
                Assert.assertEquals(j, tick.getTick(j));
            }
        }

        boolean failedAdd = results.addTickResult(100, 0, 0);
        Assert.assertFalse(failedAdd);
    }


    @Test
    public void testHasStockTick() {
        for (int i = 0; i < contracts.size(); i++) {
            Contract contract = contracts.get(i);
            results.addStockTick(i, contract);
        }

        for (int i = 0; i < contracts.size(); i++) {
            Assert.assertTrue(results.hasStockTick(i));
        }
    }

    @Test
    public void testHasTicker() {
        for (int i = 0; i < contracts.size(); i++) {
            Contract contract = contracts.get(i);
            results.addStockTick(i, contract);
            Assert.assertTrue(results.hasStockTick(i));
        }

        for (Contract contract : contracts) {
            Assert.assertTrue(results.hasTicker(contract.symbol()));
        }

        Assert.assertFalse(results.hasTicker("DoesNotHaveTicker"));
    }

    @Test
    public void testGetStockTickWithTicker() {
        for (int i = 0; i < contracts.size(); i++) {
            Contract contract = contracts.get(i);
            results.addStockTick(i, contract);
            Assert.assertTrue(results.hasStockTick(i));

            for (int j = 0; j < 10; j++) {
                results.addTickResult(i, j, j);
            }
        }

        for (Contract contract : contracts) {
            Assert.assertTrue(results.hasTicker(contract.symbol()));
            StockTick tick = results.getStockTick(contract.symbol());

            for (int j = 0; j < 10; j++) {
                Assert.assertTrue(tick.hasTick(j));
                Assert.assertEquals(j, tick.getTick(j));
            }
        }
    }

    @Test
    public void testGetStockTickUnknownTicker() {
        Assert.assertNull(results.getStockTick("NotFound"));
    }

    @Test
    public void testGetStockTickWithId() {
        for (int i = 0; i < contracts.size(); i++) {
            Contract contract = contracts.get(i);
            results.addStockTick(i, contract);
            Assert.assertTrue(results.hasStockTick(i));

            for (int j = 0; j < 10; j++) {
                results.addTickResult(i, j, j);
            }
        }

        for (int i = 0; i < contracts.size(); i++) {
            Assert.assertTrue(results.hasStockTick(i));
            StockTick tick = results.getStockTick(i);

            for (int j = 0; j < 10; j++) {
                Assert.assertTrue(tick.hasTick(j));
                Assert.assertEquals(j, tick.getTick(j));
            }
        }
    }

    @Test
    public void testGetStockTickUnknownId() {
        Assert.assertNull(results.getStockTick(100));
    }

    @Test
    public void testGetStockTicks() {
        Set<String> symbols = new HashSet<String>(contracts.size());
        for (int i = 0; i < contracts.size(); i++) {
            Contract contract = contracts.get(i);
            symbols.add(contract.symbol());
            results.addStockTick(i, contract);
            Assert.assertTrue(results.hasStockTick(i));
        }

        Collection<StockTick> ticks = results.getStockTicks();

        Assert.assertEquals(contracts.size(), ticks.size());
        for (StockTick tick : ticks) {
            Assert.assertTrue(symbols.contains(tick.getTicker()));
        }
    }

    @Test
    public void testCopyStockTick() {
        StockTick tick = new StockTick(contract().symbol());

        for (int i = 0; i < 10; i++) {
            tick.addTick(i, i);
        }
        results.copyStockTick(0, tick);

        StockTick copy = results.getStockTick(0);
        Assert.assertEquals(copy, results.getStockTick(tick.getTicker()));

        Assert.assertNotSame(tick, copy);

        for (int i = 0; i < 10; i++) {
            Assert.assertEquals(tick.getTick(i), copy.getTick(i));
        }
    }

    private Contract contract() {
        Contract contract = MarketData.contract(UUID.randomUUID().toString());

        return contract;
    }
}