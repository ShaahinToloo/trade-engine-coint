package engine.state;

import java.util.List;

import engine.trade.Trade;

public class EquityTracker {

    /**
     * Updates equity curve with unrealized PnL from open trades.
     *
     * Each open trade format assumed:
     * index 1 = entry price
     *
     * Equity update rule:
     * equity[t] = equity[t-1] + sum(unrealizedPnL)
     *
     * @param openTrades  active trades
     * @param equity      equity curve array (may be mutated or cloned)
     * @param currPrice   current market price
     * @param currIdx     current index in equity array
     * @param cloneEquity if true, returns a cloned equity array (non-destructive
     *                    mode)
     * @return updated equity array
     */
    public static double[] trackEquity(
            List<Trade> openTrades,
            double[] equity,
            int currIdx,
            boolean cloneEquity) {
        double[] eq = cloneEquity ? equity.clone() : equity;

        // Calculate sum diff_pnl
        double sumDiffPnL = 0.0;
        for (Trade trade : openTrades) {
            sumDiffPnL += trade.diff_pnl;
        }

        // Update eq
        eq[currIdx] = eq[currIdx-1] + sumDiffPnL;

        return eq;
    }

    /**
     * Updates realized equity curve using closed trades.
     *
     * Each closed trade format assumed:
     * index 4 = realized PnL
     *
     * Equity update rule:
     * realisedEquity[t] = realisedEquity[t-1] + sum(realizedPnL)
     *
     * @param recentlyClosedTrades list of closed trades
     * @param realisedEquity       equity curve array (may be mutated or cloned)
     * @param currIdx              current index in curve
     * @param cloneRealisedEquity  if true, returns a cloned realisedEquity array
     *                             (non-destructive mode)
     * @return updated realized equity curve
     */
    public static double[] trackRealisedEquity(
            List<Trade> recentlyClosedTrades,
            double[] realisedEquity,
            int currIdx,
            boolean cloneRealisedEquity) {
        double[] eq = cloneRealisedEquity ? realisedEquity.clone() : realisedEquity;

        if (currIdx <= 0 || currIdx >= eq.length)
            return eq;

        double sumPnls = 0.0;

        for (Trade trade : recentlyClosedTrades) {
            sumPnls += trade.pnl;
        }

        eq[currIdx] = eq[currIdx - 1] + sumPnls;

        return eq;
    }
}
