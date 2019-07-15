package main.java.penny.marketdata;

import main.java.penny.Broker;
import main.java.penny.constants.ScannerConstants;
import main.java.penny.util.ProgressBar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * StockScanner represents a utility class that requests market data for a series of provided stock tickers.
 * StockScanner provides the efficiency of scanning in parallel while working within the broker constraints
 * of the maximum allowed number of concurrent market data requests.
 */
public class StockScanner {

    /** Sequential cutoff indicating when to scan tickers sequentially */
    private static final int SEQUENTIAL_CUTOFF = 5;

    /**
     * Scans and requests market data for all the stock tickers provided.
     *
     * @param tickers The stock tickers to scan and request market data for
     */
    public static void scan(List<String> tickers) {
        scan(tickers, null);
    }

    /**
     * Scans and request market data for all the stock tickers provided excluding any stocks filtered
     * by the StockScannerFilter predicate.  Scans the subset of stock tickers that satisfy the stock filter.
     *
     * @param tickers The stock tickers to scan and request market data for
     * @param filter The scanner filter to extract a subset of provided tickers using
     */
    public static void scan(Collection<String> tickers, StockScannerFilter filter) {
        // If a filter exists, extract the subset of tickers that satisfy the ticker
        List<String> scanTickers;
        if (filter != null) {
            scanTickers = filter.filter(tickers);
        } else {
            scanTickers = new ArrayList<String>(tickers);
        }

        ProgressBar progressBar = makeScannerProgressBar(scanTickers);

        scanParallel(scanTickers, 0, scanTickers.size(), progressBar);
    }

    /**
     * Scans and requests market data for all the stock tickers provided sequentially as opposed to parallel.
     * Offers a noticeably slower performance in market data collection for specified stocks compared to the parallel
     * standard "scan".
     *
     * @param tickers The stock tickers to scan and request market data for
     */
    public static void scanSequential(Collection<String> tickers) {
        scanSequential(tickers, null);
    }

    /**
     * Scans and requests market data for all the stock tickers provided sequentially and excluding any stocks filtered
     * by the StockScannerFilter predicate.  Scans the subset of stock tickers that satisfy the stock filter.
     * Offers a noticeably slower performance in market data collection for specified stocks compared to the parallel
     * standard "scan".
     *
     * @param tickers The stock tickers to scan and request market data for
     * @param filter The scanner filter to extract a subset of provided tickers using
     */
    public static void scanSequential(Collection<String> tickers, StockScannerFilter filter) {
        // If a filter exists, extract the subset of tickers that satisfy the ticker
        List<String> scanTickers;
        if (filter != null) {
            scanTickers = filter.filter(tickers);
        } else {
            scanTickers = new ArrayList<String>(tickers);
        }

        ProgressBar progressBar = makeScannerProgressBar(scanTickers);

        scanSequential(scanTickers, 0, scanTickers.size(), progressBar);
    }

    /**
     * Scans the provided range of tickers in parallel leveraging divide-and-conquer.  Specifically, splits the
     * range indicated and delegates scanning to a newly spawned thread for half of this range - continually
     * splitting the range in half until it is below the sequential cutoff where the tickers are then scanned
     * sequentially in the current thread.  Assumes the range is valid with respect to the complete ticker list.
     *
     * @param tickers The complete list of tickers to divide and request market data for
     * @param low The low index of the range of tickers to scan in this thread (inclusive)
     * @param high The high index of the range of tickers to scan in this thread (exclusive)
     * @param progressBar The progress bar display to show the current status of successfully scanned tickers
     */
    private static void scanParallel(List<String> tickers, int low, int high, ProgressBar progressBar) {
        if (high - low < SEQUENTIAL_CUTOFF) {
            scanSequential(tickers, low, high, progressBar);
        } else {
            int mid = low + (high - low) / 2;

            Thread thread = new Thread(() -> scanParallel(tickers, mid, high, progressBar));
            thread.start();
            scanParallel(tickers, low, mid, progressBar);

            // Wait for thread to join...
            try {
                thread.join();
            } catch (InterruptedException e) {
                // Proceed since thread was interrupted
            }
        }
    }

    /**
     * Scans the provided range of tickers sequentially.  Assumes the range is valid with respect to the
     * complete ticker list.
     *
     * @param tickers The complete list of tickers to scan a range from
     * @param low The low (first) index of the range of tickers to scan sequentially (inclusive)
     * @param high The high (last) index of the range of tickers to scan sequentially (exclusive)
     * @param progressBar The progress bar display to show the current status of successfully scanned tickers
     */
    private static void scanSequential(List<String> tickers, int low, int high, ProgressBar progressBar) {
        for (int i = low; i < high; i++) {
            String ticker = tickers.get(i);
            Broker.getInstance().getMarketData().requestMarketData(ticker);

            if (progressBar != null) {
                progressBar.increment();
                progressBar.display();
            }
        }
    }

    /**
     * Makes the default scanner progress bar display including the list of tickers provided.
     *
     * @param tickers The list of tickers to display alongside the progress bar
     * @return A new progress bar configured with the scanner-style specification.
     */
    private static ProgressBar makeScannerProgressBar(List<String> tickers) {
        ProgressBar progressBar = new ProgressBar.ProgressBarBuilder()
                .withTitle("Scan")
                .withStartSymbol("|")
                .withProgressToken("#")
                .withGapToken(" ")
                .withEndSymbol("|")
                .withNumberOfBars(Math.min(tickers.size(), ScannerConstants.PROGRESS_NUMBER_OF_BARS))
                .withTotal(tickers.size())
                .withUniqueTokens(tickers)
            .build();

        return progressBar;
    }
}
