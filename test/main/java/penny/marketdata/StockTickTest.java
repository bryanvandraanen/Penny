package main.java.penny.marketdata;

import com.ib.client.Contract;
import main.java.penny.constants.TickTypes;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StockTickTest {

    private StockTick tick;

    private Contract contract;

    @Before
    public void setup() {
        contract = MarketData.contract("Test");
        tick = new StockTick(contract.symbol());
    }

    @Test
    public void testAddTick() {
        for (int i = 0; i < 10; i++) {
            tick.addTick(i, i);

            Assert.assertTrue(tick.hasTick(i));
            Assert.assertEquals(i, tick.getTick(i));
        }

        for (int i = 0; i < 10; i++) {
            Assert.assertTrue(tick.hasTick(i));
            Assert.assertEquals(i, tick.getTick(i));
        }
    }

    @Test
    public void testGetTick() {
        for (int i = 0; i < 10; i++) {
            tick.addTick(i, 10 - i);
        }

        for (int i = 0; i < 10; i++) {
            Assert.assertEquals(10 - i, tick.getTick(i));
        }
    }

    @Test
    public void testHasTick() {
        for (int i = 0; i < 10; i++) {
            tick.addTick(i, i);

            Assert.assertTrue(tick.hasTick(i));
        }

        for (int i = 0; i < 10; i++) {
            Assert.assertTrue(tick.hasTick(i));
        }

        for (int i = 10; i < 20; i++) {
            Assert.assertFalse(tick.hasTick(i));
        }
    }

    @Test
    public void testGetTicker() {
        Assert.assertEquals(contract.clone().symbol(), tick.getTicker());
    }

    @Test
    public void testIsComplete() {
        Assert.assertFalse(tick.isComplete());

        for (int requiredTick : TickTypes.requiredTicks) {
            tick.addTick(requiredTick, 0);
        }

        Assert.assertTrue(tick.isComplete());
    }

    @Test
    public void testEquals() {
        StockTick tick2 = new StockTick(contract.clone().symbol());
        Assert.assertEquals(tick, tick2);

        tick2 = new StockTick(MarketData.contract("NotEqual").symbol());
        Assert.assertNotEquals(tick, tick2);
    }

    @Test
    public void testHashCode() {
        StockTick tick2 = new StockTick(contract.clone().symbol());
        Assert.assertEquals(tick.hashCode(), tick2.hashCode());
    }

    @Test
    public void testToString() {
        StockTick tick2 = new StockTick(contract.clone().symbol());
        Assert.assertEquals(tick.toString(), tick2.toString());

        tick2 = new StockTick(MarketData.contract("NotEqual").symbol());
        Assert.assertNotEquals(tick.toString(), tick2.toString());
    }
}