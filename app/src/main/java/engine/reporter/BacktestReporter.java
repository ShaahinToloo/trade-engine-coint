package engine.reporter;

import java.util.List;

import engine.constants.BacktestConstants;
import engine.heads.HeadState;
import engine.heads.HeadState.MarketDataState;
import engine.heads.HeadState.PerformanceState;
import engine.heads.HeadState.PortfolioState;
import engine.heads.HeadState.RiskMetricsState;
import engine.metrics.TradeMetrics;
import engine.trade.Trade;

public class BacktestReporter {

    public static String build(
            List<Trade> closedTrades,
            double biggestLoss,
            double biggestProfit,
            HeadState state) {

        PortfolioState portfolio = state.portfolio;
        RiskMetricsState risk = state.risk;
        PerformanceState perf = state.perf;
        MarketDataState market = state.market;

        int tradesSum = closedTrades.size();

        int targetsSum = (int) closedTrades.stream()
                .filter(x -> x.pnl >= 0)
                .count();

        int stopsSum = tradesSum - targetsSum;

        int numBuys = (int) closedTrades.stream()
                .filter(x -> x.type == 1)
                .count();

        int numSells = tradesSum - numBuys;

        double avgGain = closedTrades.stream()
                .mapToDouble(x -> x.pnl)
                .average()
                .orElse(0.0);

        double tradesADay = TradeMetrics.avgTradesPerTradingDay(
                tradesSum,
                market.length,
                BacktestConstants.TIMEFRAME_MINUTES);

        double winRate = tradesSum == 0
                ? 0.0
                : (targetsSum / (double) tradesSum) * 100.0;

        StringBuilder sb = new StringBuilder(1024);

        sb.append("\n======== BackTest Results ========\n");

        sb.append(String.format("\tTrades/day: %.2f\n", tradesADay));
        sb.append(String.format("\tTrades: %d\n", tradesSum));

        sb.append(String.format(
                "\tTargets: %d | Stops: %d\n",
                targetsSum,
                stopsSum));

        sb.append(String.format(
                "\tBuys: %d | Sells: %d\n",
                numBuys,
                numSells));

        sb.append(String.format(
                "\tWin Rate: %.2f%%\n",
                winRate));

        sb.append(String.format(
                "\tLS: %d | WS: %d\n",
                portfolio.losingStreak,
                portfolio.winningStreak));

        sb.append(String.format(
                "\tAvg PnL ($): %.9f\n",
                avgGain));

        sb.append(String.format(
                "\tBalance ($): %.9f\n",
                portfolio.currBalance));

        sb.append(String.format(
                "\tNet ($): %.9f\n",
                portfolio.currBalance - BacktestConstants.INIT_BALANCE));

        sb.append(String.format(
                "\tDD ($): %.9f | Realised DD ($): %.9f\n",
                risk.equityDrawdown,
                risk.realisedDrawdown));

        sb.append(String.format(
                "\tBiggest Loss ($): %.9f | Biggest Profit ($): %.9f\n",
                biggestLoss,
                biggestProfit));

        sb.append(String.format(
                "\tEquity Calmar: %.2f\n",
                risk.equityCalmar));

        sb.append(String.format(
                "\tSharpe: %.2f\n",
                risk.sharpe));

        sb.append(String.format(
                "\tModified Sharpe: %.2f\n",
                risk.mSharpe));

        sb.append(String.format(
                "\tSortino: %.2f\n",
                risk.sortino));

        sb.append(String.format(
                "\tOmega: %.2f\n",
                risk.omega));

        sb.append(String.format(
                "\tKappa3: %.2f\n",
                risk.kappa3));

        sb.append(String.format(
                "\tExpected Return: %.2f\n",
                risk.er));

        sb.append(String.format(
                "\tExpected Value: %.2f\n",
                risk.ev));

        appendTime(sb, perf.start, perf.stop);

        return sb.toString();
    }

    private static void appendTime(StringBuilder sb, long start, long stop) {
        long diff = stop - start;

        long hours = diff / 3_600_000_000_000L;
        long minutes = (diff / 60_000_000_000L) % 60;
        long seconds = (diff / 1_000_000_000L) % 60;

        sb.append(String.format("\tTime: %02d:%02d:%02d\n", hours, minutes, seconds));
    }
}
