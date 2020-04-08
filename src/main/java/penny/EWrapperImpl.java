package main.java.penny;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ib.client.*;

import main.java.penny.concurrent.LockManager;
import main.java.penny.constants.MarketDataConstants;

/**
 * Implementation for EWrapper Broker communication channel.  Primary interface of market data delivery from broker to
 * client.  Major interaction consists of stock tick value delivery (i.e. last price, volume, etc.).  Supports
 * standard market scanners for stock tickers delivered by Broker for a particular query.
 */
public class EWrapperImpl implements EWrapper {

    /** Signal used to read messages from Broker */
    private EReaderSignal readerSignal;

    /** Client socket for channel of communication from Broker */
    private EClientSocket clientSocket;

    /** The current identifier for live market place orders */
    protected int currentOrderId = -1;

    /**
     * Constructs a new EWrapperImpl which establishes a new channel of communication to read
     * messages from the Broker.
     */
    public EWrapperImpl() {
        this.readerSignal = new EJavaSignal();
        this.clientSocket = new EClientSocket(this, this.readerSignal);
    }

    /**
     * Returns the client socket of communication providing the channel of communication with the Broker.
     */
    public EClientSocket getClient() {
        return this.clientSocket;
    }

    /**
     * Returns the signal receiving and interpreting messages from the Broker.
     */
    public EReaderSignal getSignal() {
        return this.readerSignal;
    }

    /**
     * Returns the current order identifier for market actions.
     */
    public int getCurrentOrderId() {
        return this.currentOrderId;
    }

    /**
     * Updates the particular tick type of the StockTick associated with the provided tickId to the price specified.
     *
     * @param tickId Identifier associated with the StockTick to update
     * @param tickType Tick type to update with the provided price
     * @param price Price to associate with the particular tick type
     *
     * All other parameters have no effect on the market data price.
     */
    @Override
    public void tickPrice(int tickId, int tickType, double price, TickAttrib attribute) {
        if (Broker.getInstance().getMarketData().getStockTickResults().hasStockTick(tickId)) {
            Broker.getInstance().getMarketData().getStockTickResults().addTickResult(tickId, tickType, price);

            // If we've collected all the necessary tick fields from this StockTick, unlock
            if (Broker.getInstance().getMarketData().getStockTickResults().getStockTick(tickId).isComplete()) {
                Broker.getInstance().getMarketData().cancelMarketData(tickId);
            }
        }
    }

    /**
     * Updates the particular tick type of the StockTick associated with the provided tickId to the size specified.
     *
     * @param tickId Identifier associated with the StockTick to update
     * @param tickType Tick type to update with the provided price
     * @param size Size value to associate with the particular tick type
     */
    @Override
    public void tickSize(int tickId, int tickType, int size) {
        if (Broker.getInstance().getMarketData().getStockTickResults().hasStockTick(tickId)) {
            Broker.getInstance().getMarketData().getStockTickResults().addTickResult(tickId, tickType, size);

            // If we've collected all the necessary tick fields from this StockTick, unlock
            if (Broker.getInstance().getMarketData().getStockTickResults().getStockTick(tickId).isComplete()) {
                Broker.getInstance().getMarketData().cancelMarketData(tickId);
            }
        }
    }

    /**
     * Signals that the StockTick associated with the provided tickId will no longer have market data delivered.
     * Frees up resources associated with the StockTick previously requesting market data.
     *
     * @param tickId Identifier associated with the StockTick no longer receiving market data
     *
     * All other parameters have no effect on the market data cancellation signal.
     */
    @Override
    public void marketDataType(int tickId, int marketDataType) {
        // Delivers when market data is cancelled for a particular tickId
        LockManager.getInstance().getLock(tickId).unlock();
    }

    /**
     * Delivery handler for stock tickers associated with a requested scanner.  More specifically, updates market data
     * tickers to include the ticker retrieved by this requested scanner.
     *
     * @param reqId Identifier of the requested scanner
     * @param contractDetails Contract details of the scanned stock ticker
     *
     * All other parameters have no effect on the scanner delivery.
     */
    @Override
    public void scannerData(int reqId, int rank,
                            ContractDetails contractDetails, String distance, String benchmark,
                            String projection, String legsStr) {
        Broker.getInstance().getMarketData().addTicker(contractDetails.contract().symbol());
    }

    /**
     * Indicates that the previously requested scanner has finished delivery.
     * Frees up resources associated with the Scanner previously requesting stock symbols.
     *
     * @param reqId Identifier of the requested scanner
     */
    @Override
    public void scannerDataEnd(int reqId) {
        LockManager.getInstance().getLock(reqId).unlock();
    }

    /**
     * Error handling protocol when some Broker identified request cannot be completed.  More specifically, frees up
     * any resources associated with the associated id if the error code is a code that would block execution.
     *
     * @param id Identifier of the request that experienced an error
     * @param errorCode Error code associated with the request that failed to be completed
     * @param errorMsg Error message explaining potential cause of the error
     */
    @Override
    public void error(int id, int errorCode, String errorMsg) {
        // If requested market data reports an error, almost always from an OTC ticker that no longer exists; unlock id
        // and cancel market data to clear up market data notion that current id is active
        if (MarketDataConstants.ERROR_CODE_UNLOCKS.contains(errorCode)) {
            Broker.getInstance().getMarketData().cancelMarketData(id);
        }
    }

    /**
     * Acknowledges the current connection to the Broker and initializes the Broker API
     */
    @Override
    public void connectAck() {
        if (this.clientSocket.isAsyncEConnect()) {
            this.clientSocket.startAPI();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////
    //////////////////// UNUSED ABSTRACT INHERITED BROKER METHODS ////////////////////
    //////////////////////////////////////////////////////////////////////////////////
    @Override
    public void tickOptionComputation(int tickerId, int field,
                                      double impliedVol, double delta, double optPrice,
                                      double pvDividend, double gamma, double vega, double theta,
                                      double undPrice) {
    }

    @Override
    public void tickGeneric(int tickerId, int tickType, double value) {
    }

    @Override
    public void tickString(int tickerId, int tickType, String value) {
    }

    @Override
    public void tickEFP(int tickerId, int tickType, double basisPoints,
                        String formattedBasisPoints, double impliedFuture, int holdDays,
                        String futureLastTradeDate, double dividendImpact,
                        double dividendsToLastTradeDate) {
    }

    @Override
    public void orderStatus(int orderId, String status, double filled,
                            double remaining, double avgFillPrice, int permId, int parentId,
                            double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
    }

    @Override
    public void openOrder(int orderId, Contract contract, Order order,
                          OrderState orderState) {
    }

    @Override
    public void openOrderEnd() {
    }

    @Override
    public void updateAccountValue(String key, String value, String currency,
                                   String accountName) {
    }

    @Override
    public void updatePortfolio(Contract contract, double position,
                                double marketPrice, double marketValue, double averageCost,
                                double unrealizedPNL, double realizedPNL, String accountName) {
    }

    @Override
    public void updateAccountTime(String timeStamp) {
    }

    @Override
    public void accountDownloadEnd(String accountName) {
    }

    @Override
    public void nextValidId(int orderId) {
    }

    @Override
    public void contractDetails(int reqId, ContractDetails contractDetails) {
    }

    @Override
    public void bondContractDetails(int reqId, ContractDetails contractDetails) {
    }

    @Override
    public void contractDetailsEnd(int reqId) {
    }

    @Override
    public void execDetails(int reqId, Contract contract, Execution execution) {
    }

    @Override
    public void execDetailsEnd(int reqId) {
    }

    @Override
    public void updateMktDepth(int tickerId, int position, int operation,
                               int side, double price, int size) {
    }
    @Override
    public void updateMktDepthL2(int tickerId, int position, String marketMaker,
                                 int operation, int side, double price, int size, boolean isSmartDepth) {
    }

    @Override
    public void updateNewsBulletin(int msgId, int msgType, String message,
                                   String origExchange) {
    }

    @Override
    public void managedAccounts(String accountsList) {
    }

    @Override
    public void receiveFA(int faDataType, String xml) {
    }

    @Override
    public void historicalData(int reqId, Bar bar) {
    }

    @Override
    public void scannerParameters(String xml) {
    }

    @Override
    public void realtimeBar(int reqId, long time, double open, double high,
                            double low, double close, long volume, double wap, int count) {
    }

    @Override
    public void currentTime(long time) {
    }

    @Override
    public void fundamentalData(int reqId, String data) {
    }

    @Override
    public void deltaNeutralValidation(int reqId, DeltaNeutralContract underComp) {
    }

    @Override
    public void tickSnapshotEnd(int reqId) {
    }

    @Override
    public void commissionReport(CommissionReport commissionReport) {
    }

    @Override
    public void position(String account, Contract contract, double pos,
                         double avgCost) {
    }

    @Override
    public void positionEnd() {
    }

    @Override
    public void accountSummary(int reqId, String account, String tag,
                               String value, String currency) {
    }

    @Override
    public void accountSummaryEnd(int reqId) {
    }

    @Override
    public void verifyMessageAPI(String apiData) {
    }

    @Override
    public void verifyCompleted(boolean isSuccessful, String errorText) {
    }

    @Override
    public void verifyAndAuthMessageAPI(String apiData, String xyzChallange) {
    }

    @Override
    public void verifyAndAuthCompleted(boolean isSuccessful, String errorText) {
    }

    @Override
    public void displayGroupList(int reqId, String groups) {
    }

    @Override
    public void displayGroupUpdated(int reqId, String contractInfo) {
    }

    @Override
    public void error(Exception e) {
    }

    @Override
    public void error(String str) {
    }

    @Override
    public void connectionClosed() {
    }

    @Override
    public void positionMulti(int reqId, String account, String modelCode,
                              Contract contract, double pos, double avgCost) {
    }

    @Override
    public void positionMultiEnd(int reqId) {
    }

    @Override
    public void accountUpdateMulti(int reqId, String account, String modelCode,
                                   String key, String value, String currency) {
    }

    @Override
    public void accountUpdateMultiEnd(int reqId) {
    }

    @Override
    public void securityDefinitionOptionalParameter(int reqId, String exchange,
                                                    int underlyingConId, String tradingClass, String multiplier,
                                                    Set<String> expirations, Set<Double> strikes) {
    }

    @Override
    public void securityDefinitionOptionalParameterEnd(int reqId) {

    }
    @Override
    public void softDollarTiers(int reqId, SoftDollarTier[] tiers) {
    }

    @Override
    public void familyCodes(FamilyCode[] familyCodes) {
    }

    @Override
    public void symbolSamples(int reqId, ContractDescription[] contractDescriptions) {
    }

    @Override
    public void historicalDataEnd(int reqId, String startDateStr, String endDateStr) {
    }

    @Override
    public void mktDepthExchanges(DepthMktDataDescription[] depthMktDataDescriptions) {
    }

    @Override
    public void tickNews(int tickerId, long timeStamp, String providerCode, String articleId, String headline, String extraData) {
    }

    @Override
    public void smartComponents(int reqId, Map<Integer, Map.Entry<String, Character>> theMap) {
    }

    @Override
    public void tickReqParams(int tickerId, double minTick, String bboExchange, int snapshotPermissions) {
    }

    @Override
    public void newsProviders(NewsProvider[] newsProviders) {
    }

    @Override
    public void newsArticle(int requestId, int articleType, String articleText) {
    }

    @Override
    public void historicalNews(int requestId, String time, String providerCode, String articleId, String headline) {
    }

    @Override
    public void historicalNewsEnd(int requestId, boolean hasMore) {
    }

    @Override
    public void headTimestamp(int reqId, String headTimestamp) {
    }

    @Override
    public void histogramData(int reqId, List<HistogramEntry> items) {
    }

    @Override
    public void historicalDataUpdate(int reqId, Bar bar) {
    }

    @Override
    public void rerouteMktDataReq(int reqId, int conId, String exchange) {
    }

    @Override
    public void rerouteMktDepthReq(int reqId, int conId, String exchange) {
    }

    @Override
    public void marketRule(int marketRuleId, PriceIncrement[] priceIncrements) {
    }

    @Override
    public void pnl(int reqId, double dailyPnL, double unrealizedPnL, double realizedPnL) {
    }

    @Override
    public void pnlSingle(int reqId, int pos, double dailyPnL, double unrealizedPnL, double realizedPnL, double value) {
    }

    @Override
    public void historicalTicks(int reqId, List<HistoricalTick> ticks, boolean done) {
    }

    @Override
    public void historicalTicksBidAsk(int reqId, List<HistoricalTickBidAsk> ticks, boolean done) {
    }

    @Override
    public void historicalTicksLast(int reqId, List<HistoricalTickLast> ticks, boolean done) {
    }

    @Override
    public void tickByTickAllLast(int reqId, int tickType, long time, double price, int size,
                                  TickAttribLast tickAttribLast, String exchange, String specialConditions) {
    }

    @Override
    public void tickByTickBidAsk(int reqId, long time, double bidPrice, double askPrice, int bidSize, int askSize,
                                 TickAttribBidAsk tickAttribBidAsk) {
    }

    @Override
    public void tickByTickMidPoint(int reqId, long time, double midPoint) {
    }

    @Override
    public void orderBound(long orderId, int apiClientId, int apiOrderId) {
    }

    @Override
    public void completedOrder(Contract contract, Order order, OrderState orderState) {
    }

    @Override
    public void completedOrdersEnd() {
    }
}
