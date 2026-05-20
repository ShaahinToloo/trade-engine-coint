package engine.trade;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages all trade lifecycle operations.
 * 
 * NOTE: Currently the static approach is find. But if you need to do some
 * serious changes, like multiple backtesting in one run for any reason, like
 * hyperparameter tunings. Then 'YOU MUST CHANGE' the TradeManager from static
 * to instance (non-static) and make TimeManager object in Backtest, and pass it
 * to Core in its constructor. Hence you may have to handle it another way,
 * depends on your implementation. This is just a warning to not to mix the old
 * backtest trades with new backtest. Being said, you may need to create a new
 * TradeManager object for each backtest, or you may just want to keep this
 * static approach and just clear everything using some helper wrappers.
 *
 * <p>
 * Responsibilities:
 * </p>
 *
 * <ul>
 * <li>Track open trades</li>
 * <li>Track closed trades</li>
 * <li>Track recently closed trades</li>
 * <li>Update live PnL</li>
 * <li>Flush closed trades automatically</li>
 * </ul>
 */
public class TradeManager {

    /**
     * Currently active/open trades.
     */
    public static final List<Trade> openTrades = new ArrayList<>();

    /**
     * All historical closed trades.
     */
    public static final List<Trade> closedTrades = new ArrayList<>();

    /**
     * Recently closed trades.
     *
     * <p>
     * Usually cleared every iteration after metrics/equity updates.
     * </p>
     */
    public static final List<Trade> recClosedTrades = new ArrayList<>();

    /**
     * Adds a new trade to open trades.
     *
     * @param trade trade to add
     */
    public static void addTrade(Trade trade) {
        if (trade != null) {
            openTrades.add(trade);
        }
    }

    /**
     * Updates live PnL for all open trades.
     *
     * @param currentBidPrice current market price
     */
    public static void updateOpenTrades(double currentBidPrice, double currentAskPrice, double[] beta) {
        for (Trade trade : openTrades) {
            trade.update(currentBidPrice, currentAskPrice, beta);
        }
    }

    /**
     * Moves closed trades from openTrades
     * into closedTrades and recClosedTrades.
     *
     * <p>
     * Automatically removes them from openTrades.
     * </p>
     */
    public static void flushClosedTrades() {
        Iterator<Trade> iterator = openTrades.iterator();

        while (iterator.hasNext()) {
            Trade trade = iterator.next();

            if (trade.closed) {
                closedTrades.add(trade);
                recClosedTrades.add(trade);

                iterator.remove();
            }
        }
    }

    /**
     * Clears recently closed trades buffer.
     */
    public static void clearRecentClosedTrades() {
        recClosedTrades.clear();
    }

    /**
     * Returns current number of open trades.
     *
     * @return open trades count
     */
    public static int openTradesSize() {
        return openTrades.size();
    }

    /**
     * Returns current number of closed trades.
     *
     * @return closed trades count
     */
    public static int closedTradesSize() {
        return closedTrades.size();
    }
}