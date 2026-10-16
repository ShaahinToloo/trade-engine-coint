package engine.trade;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import engine.constants.PublicConstants;

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
     * Day PnL as FRACTION of INIT_BALANCE (open + closed trades entered on the
     * candidate trade's day, e.g. 0.01 = +1%). Refreshed on every
     * {@link #addTrade} call.
     */
    public static double lastDayPnl = 0.0;

    /**
     * Month PnL as FRACTION of INIT_BALANCE (open + closed trades entered in
     * the candidate trade's month, e.g. 0.01 = +1%). Refreshed on every
     * {@link #addTrade} call.
     */
    public static double lastMonthPnl = 0.0;

    /**
     * Number of profitable (pnl >= 0) open + closed trades entered on the
     * candidate trade's day. Refreshed on every {@link #addTrade} call.
     */
    public static int lastDayProfitableCount = 0;

    /**
     * Number of losing (pnl < 0) open + closed trades entered on the
     * candidate trade's day. Refreshed on every {@link #addTrade} call.
     */
    public static int lastDayLosingCount = 0;

    /**
     * Adds a new trade to open trades, unless a daily/monthly guard blocks it.
     *
     * <p>
     * A rejected trade is dropped completely: it is added nowhere else, and
     * since it never enters {@code openTrades}, elimination indices built from
     * {@code openTrades} stay aligned.
     * </p>
     *
     * @param trade trade to add
     * @return true if the trade was added, false if blocked
     */
    public static boolean addTrade(Trade trade) {
        if (trade == null) {
            return false;
        }
        if (!canOpenTrade(trade)) {
            return false;
        }
        openTrades.add(trade);
        // Count the newly accepted trade in the current day bucket.
        if (trade.pnl >= 0) {
            lastDayProfitableCount++;
        } else {
            lastDayLosingCount++;
        }
        return true;
    }

    /**
     * Checks daily/monthly profit, drawdown and trade-count guards.
     *
     * @param trade candidate trade (its timestamp defines the day/month window)
     * @return true if the trade may be opened
     */
    public static boolean canOpenTrade(Trade trade) {
        if (trade == null) {
            return false;
        }
        if (trade.timestamp == null) {
            return true;
        }
        refreshTrackers(trade.timestamp);

        if (lastDayProfitableCount >= PublicConstants.MAX_PROFITABLE_TRADES_PER_DAY) {
            return false;
        }
        if (lastDayLosingCount >= PublicConstants.MAX_LOSING_TRADES_PER_DAY) {
            return false;
        }
        if (lastDayPnl >= PublicConstants.DAILY_MAX_PROFIT) {
            return false;
        }
        if (lastDayPnl <= PublicConstants.DAILY_MAX_DD) {
            return false;
        }
        if (lastMonthPnl >= PublicConstants.MONTHLY_MAX_PROFIT) {
            return false;
        }
        if (lastMonthPnl <= PublicConstants.MONTHLY_MAX_DD) {
            return false;
        }
        return true;
    }

    /**
     * Recomputes day/month PnL (as FRACTION of INIT_BALANCE) and profitable /
     * losing day trade counts for the given reference timestamp by scanning
     * open + closed trades entered in the same window. Floating PnL is
     * included since open trades carry live pnl.
     */
    private static void refreshTrackers(Timestamp ref) {
        double dayPnl = 0.0;
        double monthPnl = 0.0;
        int dayProfitable = 0;
        int dayLosing = 0;

        for (Trade t : openTrades) {
            if (t.timestamp == null) {
                continue;
            }
            if (t.timestamp.isSameDay(ref)) {
                dayPnl += t.pnl;
                if (t.pnl >= 0) {
                    dayProfitable++;
                } else {
                    dayLosing++;
                }
            }
            if (t.timestamp.isSameMonth(ref)) {
                monthPnl += t.pnl;
            }
        }
        for (Trade t : closedTrades) {
            if (t.timestamp == null) {
                continue;
            }
            if (t.timestamp.isSameDay(ref)) {
                dayPnl += t.pnl;
                if (t.pnl >= 0) {
                    dayProfitable++;
                } else {
                    dayLosing++;
                }
            }
            if (t.timestamp.isSameMonth(ref)) {
                monthPnl += t.pnl;
            }
        }

        lastDayPnl = toFraction(dayPnl);
        lastMonthPnl = toFraction(monthPnl);
        lastDayProfitableCount = dayProfitable;
        lastDayLosingCount = dayLosing;
    }

    /**
     * Converts a dollar PnL into a fraction of INIT_BALANCE (0.01 = 1%).
     */
    private static double toFraction(double pnlDollars) {
        if (PublicConstants.INIT_BALANCE == 0.0) {
            return 0.0;
        }
        return pnlDollars / PublicConstants.INIT_BALANCE;
    }

    /**
     * Resets tracker fields (used when starting a fresh backtest run).
     */
    public static void resetTrackers() {
        lastDayPnl = 0.0;
        lastMonthPnl = 0.0;
        lastDayProfitableCount = 0;
        lastDayLosingCount = 0;
    }

    /**
     * Updates live PnL for all open trades.
     *
     * @param currentBidPrice   current market price (scaled by currentMultiplier)
     * @param currentMultiplier beta multiplier of the live series
     */
    public static void updateOpenTrades(double currentBidPrice, double currentAskPrice, double[] beta,
            double currentMultiplier) {
        for (Trade trade : openTrades) {
            trade.update(currentBidPrice, currentAskPrice, beta, currentMultiplier);
        }
    }

    /**
     * Legacy update assuming the live series carries each trade's own scale.
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