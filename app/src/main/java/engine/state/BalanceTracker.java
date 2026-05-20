package engine.state;

import java.util.List;

import engine.trade.Trade;

public class BalanceTracker {
    /**
     * Consumes realized trades and updates global balance state.
     *
     * Each trade format assumed:
     * index 4 = realized PnL
     *
     * @param recClosedTrades list of closed trades (consumed)
     * @return updated global balance
     */
    public static double trackCurrBalance(List<Trade> recClosedTrades, double currBalance) {
        double balance = currBalance;

        for (Trade trade : recClosedTrades) {
            balance += trade.pnl;
        }

        return balance;
    }
}
