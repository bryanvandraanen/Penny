package main.java.penny.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static main.java.penny.constants.ResourceConstants.*;

/**
 * OTCTickersReader represents a utility class that extracts all unique stock tickers that are currently
 * valid in the OTC market.  OTCTickersReader assumes that active stock tickers can be found in the OTC_TICKER_FILE.
 * The reader assumes that this ticker file follows the ticker format found on eoddata.com
 */
public class OTCTickersReader {

    /**
     * Returns all the OTC tickers found in the OTC market (OTC_TICKER_FILE).
     */
    public static List<String> getOTCTickers() {
        List<String> tickers = new ArrayList<String>();

        Scanner tickersFile = null;
        try {
            tickersFile = new Scanner(new File(DATA_FILE_PATH + OTC_TICKER_FILE));
        } catch (IOException e) {
            System.out.println("Failed to open OTC ticker file: " + DATA_FILE_PATH + OTC_TICKER_FILE);
        }

        if (tickersFile == null) {
            return tickers;
        }

        // Read all tickers from the file, extracting tickers following the eoddata.com format
        while (tickersFile.hasNextLine()) {
            String ticker = tickersFile.nextLine().split("\\.")[0];

            tickers.add(ticker);
        }

        return tickers;
    }
}
