package engine.heads;

import java.util.List;

import engine.constants.BacktestConstants;

public final class HeadState {

    public final MarketDataState market = new MarketDataState();

    public final PortfolioState portfolio = new PortfolioState();

    public final PortfolioIndicators indicators = new PortfolioIndicators();

    public final RiskMetricsState risk = new RiskMetricsState();

    public final PerformanceState perf = new PerformanceState();

    public final RuntimeBuffers buffers = new RuntimeBuffers();

    public final class MarketDataState {
        public double[][] priceMatrix;
        public List<String> datetimeIndex;
        public int length;
    }

    public final class PortfolioState {
        public double lastPortfolioBidPrice = Double.NaN;
        public double lastPortfolioAskPrice = Double.NaN;

        public double[] equity;
        public double[] realisedEquity;

        public double currBalance = BacktestConstants.INIT_BALANCE;

        public int winningStreak;
        public int losingStreak;
    }

    public final class PortfolioIndicators {
        public double[] yPortBid;
        public double[] yPortAsk;
        public double[] movingAvg;
        public double[] std;
    }

    public final class RiskMetricsState {
        public double equityDrawdown;
        public double realisedDrawdown;
        public double equityCalmar;

        public double sharpe;
        public double mSharpe;
        public double sortino;
        public double omega;
        public double kappa3;

        public double er;
        public double ev;
    }

    public final class PerformanceState {
        public long start;
        public long stop;

        public long[] coreSpeedRange;
        public int rangeIdx;

        public final int reportPeriod = BacktestConstants.DATA_LENGTH / 10;
    }

    public final class RuntimeBuffers {
        public final double[] newPrice = new double[BacktestConstants.NUM_SERIES];
    }
}