package main.java.penny.marketdata;

import com.ib.client.ScannerSubscription;
import main.java.penny.Broker;
import main.java.penny.concurrent.LockManager;

import static main.java.penny.constants.MarketDataConstants.SECURITY_TYPE;
import static main.java.penny.constants.ScannerConstants.*;

/**
 * PumpScanner represents a utility class for creating and requesting broker-specific scanners.  Specifically,
 * this class contains functions which create the necessary specification and request the top 50 stock tickers
 * associated with the most active dollar volume stocks for the day.
 */
public class PumpScanner {

    /**
     * Requests a penny stock scanner with the specified scanner identifier.  Specifically, requests the most active
     * dollar volume stocks for the delay that are below a particular price and market capitalization as detailed
     * in the MarketDataConstants.
     *
     * @param scannerId - Integer identifier associated with the scanner request
     */
    public static void requestScanner(int scannerId) {
        ScannerSubscription pumpStockSubscription =
                pinkSheetDollarVolumeSubscription(PUMP_SCANNER_BELOW_PRICE, PUMP_SCANNER_MARKET_CAP_BELOW);
        LockManager.getInstance().getLock(scannerId).lock();
        Broker.getInstance().getClient().reqScannerSubscription(scannerId, pumpStockSubscription, null);
    }

    /**
     * Provides the default TWS Advanced Market StockScanner for most active dollar volume (penny) stocks.
     * The subscription scans for stocks in the OTC Market which show the highest trading dollar volume (and thus
     * a potentially unusually high amount of trading activity).
     *
     * @param belowPrice - Dollar amount in the form of dollars.cents ($$.cc) to exclude stocks that trade above this
     *                     value from being returned.
     * @param marketCapBelow - Dollar amount to exclude all stocks with a capitalization that is less than this amount
     * @return A ScannerSubscription corresponding to the TWS Advanced Market StockScanner retrieving stocks in the
     *         OTC Market with the most active dollar volume.
     */
    public static ScannerSubscription pinkSheetDollarVolumeSubscription(double belowPrice, double marketCapBelow) {
        ScannerSubscription scannerSub = new ScannerSubscription();
        scannerSub.scanCode(PUMP_SCANNER_SCAN_CODE);
        scannerSub.instrument(SECURITY_TYPE);
        scannerSub.locationCode(PINK_SHEET_LOCATION_CODE);
        scannerSub.marketCapBelow(marketCapBelow);
        scannerSub.belowPrice(belowPrice);

        return scannerSub;
    }
}
