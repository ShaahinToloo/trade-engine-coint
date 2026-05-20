package engine.metrics;

import java.util.List;

import engine.trade.Trade;

public class RiskMetrics {
    /**
     * Calculates maximum drawdown from an equity curve.
     *
     * Drawdown is defined as the maximum drop from a historical peak:
     * dd = equity - peak
     * dd is either negative or zero
     *
     * Returns a negative value in the qouted currency (e.g. -0.15Q which Q is the
     * qouted currency of all assets in the portfolio).
     *
     * @param equity array of equity values over time
     * @return maximum drawdown (negative value)
     */
    public static double calculateDrawdown(double[] equity) {
        if (equity == null || equity.length == 0)
            return 0.0;

        double peak = equity[0];
        double maxDD = 0.0;

        for (double eq : equity) {
            if (eq > peak) {
                peak = eq;
                continue;
            }

            double dd = eq - peak; // negative or zero
            if (dd < maxDD) {
                maxDD = dd;
            }
        }

        return maxDD;
    }

    public static double calculateCalmarRatio(List<Trade> trades, double[] equity) {
        if (trades == null || trades.isEmpty() || equity == null || equity.length == 0) {
            System.out.println("Equity Length: " + ((equity != null)?equity.length:0));
            System.out.println("Trades Length: " + ((trades != null)?trades.size():"Null"));
            throw new IllegalArgumentException("Invalid inputs");
        }

        double totalReturn = 0.0;
        for (Trade t : trades) {
            totalReturn += t.pnl;
        }

        double maxDrawdown = calculateDrawdown(equity);

        if (maxDrawdown == 0.0) {
            return Double.NaN; // no risk detected (or broken data)
        }

        return totalReturn / Math.abs(maxDrawdown);
    }

    public static double biggestLoss(List<Trade> closedTrades) {
        double biggestLoss = 0.0;
        for (Trade trade : closedTrades) {
            if (trade.pnl < biggestLoss) {
                biggestLoss = trade.pnl;
            }
        }
        return biggestLoss;
    }

    public static double biggestProfit(List<Trade> closedTrades) {
        double biggestProfit = 0.0;
        for (Trade trade : closedTrades) {
            if (trade.pnl > biggestProfit) {
                biggestProfit = trade.pnl;
            }
        }
        return biggestProfit;
    }
}
