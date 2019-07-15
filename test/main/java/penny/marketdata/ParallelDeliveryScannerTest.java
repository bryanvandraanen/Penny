package main.java.penny.marketdata;

import main.java.penny.mock.MockBroker;
import org.junit.Before;
import org.junit.Test;

public class ParallelDeliveryScannerTest extends ScannerTest {

    @Override
    @Before
    public void setup() {
        super.setup();
        MockBroker.init(true /* Deliver in Parallel */);
    }

    @Override
    @Test
    public void testScanParallelSimpleScale() {
        super.testScanParallelSimpleScale();
    }

    @Override
    @Test
    public void testScanSequentialSimpleScale() {
        super.testScanSequentialSimpleScale();
    }

    @Override
    @Test
    public void testScanParallelScannerScale() {
        super.testScanParallelScannerScale();
    }

    @Override
    @Test
    public void testScanSequentialScannerScale() {
        super.testScanSequentialScannerScale();
    }

    @Override
    @Test
    public void testScanParallelFilteredOTCScale() {
        super.testScanParallelFilteredOTCScale();
    }

    @Override
    @Test
    public void testScanSequentialFilteredOTCScale() {
        super.testScanSequentialFilteredOTCScale();
    }

    @Override
    @Test
    public void testScanParallelCompleteOTCScale() {
        super.testScanParallelCompleteOTCScale();
    }

    @Override
    @Test
    public void testScanSequentialCompleteOTCScale() {
        super.testScanSequentialCompleteOTCScale();
    }
}
