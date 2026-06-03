package engine.metrics;

import java.util.List;

import engine.trade.Trade;

public class TradeMetrics {
    /**
     * Computes maximum consecutive winning and losing streaks.
     *
     * A trade is:
     * - winning if pnl >= 0
     * - losing if pnl < 0
     *
     * @param closedTrades list of trades, where index 4 is pnl
     * @return int array: [maxLosingStreak, maxWinningStreak]
     */
    public static int[] calculateLSAndWS(List<Trade> closedTrades) {
        int maxWin = 0, maxLoss = 0;
        int winStreak = 0, lossStreak = 0;

        for (Trade trade : closedTrades) {
            double pnl = trade.pnl;

            if (pnl >= 0) {
                winStreak++;
                lossStreak = 0;
                if (winStreak > maxWin)
                    maxWin = winStreak;
            } else {
                lossStreak++;
                winStreak = 0;
                if (lossStreak > maxLoss)
                    maxLoss = lossStreak;
            }
        }

        return new int[] { maxLoss, maxWin };
    }

    /**
     * Computes the average number of trades per trading day.
     *
     * <p>
     * This assumes a continuous 24/x market (1440 minutes per day).
     * For traditional market sessions (stocks), this metric is not adjusted
     * for trading hours or holidays.
     * </p>
     *
     * @param tradesSum        total number of executed trades
     * @param dataLength       number of data points (bars/candles)
     * @param timeframeMinutes duration of each bar in minutes
     *                         (e.g. 1m = 1.0, 15m = 15.0, 2h = 120.0, 3s = 3/60)
     *
     * @return average trades per trading day
     *
     *         <p>
     *         Note: Tick-based data is not supported by this function.
     *         </p>
     */
    public static double avgTradesPerTradingDay(int tradesSum, int dataLength, double timeframeMinutes) {
        double days = (dataLength * timeframeMinutes) / 1440.0;
        return tradesSum / days;
    }
}
