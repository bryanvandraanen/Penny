package main.java.penny.marketdata;

import main.java.penny.constants.TickTypes;

/**
 * PumpStockTick represents a StockTick with additional convenient computation of common pump and dump
 * indicating values.  Often what characterizes a pump and dump is unusually active trading (i.e. high
 * dollar volume) when compared to its historical, average trading activity.
 */
public class PumpStockTick extends StockTick {

    /**
     * Constructs a new PumpStockTick identical to the StockTick provided.
     */
    public PumpStockTick(StockTick tick) {
        super(tick);
    }

    /**
     * Retrieves the true value of the volume of this stock.
     *
     * @return The true volume (scaled by 100 to correct broker simplification) of this StockTick
     */
    public int getVolume() {
        // Volume received by market data needs to be adjusted by 100 multiplier
        // per Broker TickType specification)
        return this.getTick(TickTypes.VOLUME).intValue() * 100;
    }

    /**
     * Retrieves the true value of the average volume of this stock.
     *
     * @return The true average volume (scaled by 100 to correct broker simplification) of this StockTick
     */
    public int getAverageVolume() {
        // Average Volume received by market data needs to be adjusted by 100 multiplier
        // (per Broker TickType specification)
        return this.getTick(TickTypes.AVERAGE_VOLUME).intValue() * 100;
    }

    /**
     * Retrieves the opening price of this stock.
     *
     * @return The price which this StockTick opened with for the day
     */
    public double getOpen() {
        return this.getTick(TickTypes.OPEN).doubleValue();
    }

    /**
     * Retrieves the last trading price of this stock
     *
     * @return The last price which this StockTick traded at for the day
     */
    public double getLastPrice() {
        return this.getTick(TickTypes.LAST).doubleValue();
    }

    /**
     * Retrieves the high trading price of this stock.
     *
     * @return The highest price which this StockTick traded at for the day
     */
    public double getHigh() {
        return this.getTick(TickTypes.HIGH).doubleValue();
    }

    /**
     * Retrieves the low trading price of this stock.
     *
     * @return The lowest price which this StockTick traded at for the day
     */
    public double getLow() {
        return this.getTick(TickTypes.LOW).doubleValue();
    }

    /**
     * Retrieves the 13 week high price of this stock.
     *
     * @return The highest price which this StockTick traded at over the last 13 weeks
     */
    public double getHigh13Weeks() {
        return this.getTick(TickTypes.HIGH_13_WEEKS).doubleValue();
    }

    /**
     * Retrieves the 13 week low price for this stock.
     *
     * @return The lowest price which this StockTick traded at over the last 13 weeks
     */
    public double getLow13Weeks() {
        return this.getTick(TickTypes.LOW_13_WEEKS).doubleValue();
    }

    /**
     * Retrieves the dollar volume of this stock.
     *
     * @return The total dollar volume of this StockTick for the day.
     */
    public double getVolumeUSD() {
        return Math.round(this.getVolume() * this.getLastPrice());
    }

    /**
     * Retrieves the average dollar volume of this stock.
     *
     * @return The rough average dollar volume of this StockTick over the last 90 days (13 weeks)
     */
    public double getAverageVolumeUSD() {
        return Math.round(((getHigh13Weeks() + getLow13Weeks()) / 2) * this.getAverageVolume());
    }

    /**
     * Retrieves the percent change of this stock.
     *
     * @return The percent change of the price of this stock for the day since the opening price
     */
    public double getPercentChange() {
        return (this.getLastPrice() - this.getOpen()) / this.getOpen();
    }

    /**
     * Retrieves the day's range of this stock.
     *
     * @return The daily price range of this stock between the high and low prices of the day
     */
    public double getDayRange() {
        return (this.getHigh() - this.getLow()) / this.getOpen();
    }
}
