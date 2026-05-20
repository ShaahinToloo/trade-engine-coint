package engine.core;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import engine.cointegration.halflife.LookbackCalculator;
import engine.cointegration.johansen.JohansenResult;
import engine.cointegration.johansen.JohansenTest;
import engine.cointegration.portfolio.SyntheticPortfolio;
import engine.constants.BacktestConstants;
import engine.data.feature.CachedFeatureInitializer;
import engine.logger.DataLogger;
import engine.mathUtils.normalization.VectorNormalization;
import engine.trade.Trade;
import engine.trade.TradeManager;

public abstract class Core {

	public static class TradeContext {
		public static Trade trade;
		public static int[] tradeIndicesToEliminate;
	}

	/**
	 * Dead code, But needed for structure.
	 */
	public abstract static class CoreGetter {
	}

	public double[][] yPort;

	protected final CachedFeatureInitializer cfi;
	protected JohansenResult jr;
	protected boolean canTrade = true;
	protected int lookback;

	private final DataLogger logger;
	private final double[] close0;
	private boolean changeJR = false;
	private int methodCall = 0;

	/**
	 * Some Notes:
	 * - When periodCond activates, any trading is disabled.
	 * - Only changes the Beta when all trades are closed.
	 * - When periodCond activates any trading is disabled, and for each new price
	 * (each processCandle call) pnl is checked to be >0
	 * and when all trades are not in loss, they are all gets closed, and in the
	 * next call, beta and lookback will be recalculated.
	 * - Why beta is recalculated in the next call? Because if we change it in the
	 * current call that trades are still there, they either will be
	 * closed with the new beta's yPortPrice :Skull: or the logged
	 * lastPortfolioPrice does not show the trade's related Beta, it expresses the
	 * new Beta.
	 * In other words, at that time t, the portfolio has 2 prices, old Beta price
	 * and new Beta price, and the trades are got closed with old Beta price,
	 * while the logged price in csv files, will be the new Beta's price, and the
	 * old Beta's price wouldnt be in the logs, which is not good at all.
	 * 
	 * @param priceMatrix
	 * @param datetimeIndex
	 * @param entryZscore
	 * @param exitZscore
	 */
	public Core(double[][] priceMatrix, List<String> datetimeIndex, Path runPath) {
		this.cfi = new CachedFeatureInitializer(priceMatrix, datetimeIndex);
		this.logger = new DataLogger(runPath, "AllFeaturesTrades.csv");
		this.close0 = BacktestConstants.globalFeatureMap.get("close0");
	}

	/**
	 * 
	 * @param newPrice
	 * @param datetimeIndex
	 * @param tradesType
	 * @return Object[] { null || double[] , null || int[] }
	 */
	public void processCandle(double[] newPrice, String datetimeIndex, List<Trade> openTrades, int outerLoopIdx) {
		boolean periodCond = this.methodCall % BacktestConstants.TESTS_PERIOD == 0;

		this.cfi.newPrice(newPrice, datetimeIndex);
		this.validateNewPrice(newPrice);

		if (changeJR || (outerLoopIdx == BacktestConstants.SEQ_LENGTH)) {
			jr = JohansenTest.johansen(BacktestConstants.PRICE_MATRIX);
		}

		double[] beta = getBetaFromJr();
		yPort = SyntheticPortfolio.yPort(jr, BacktestConstants.PRICE_MATRIX, BacktestConstants.SPREADS, beta);
		TradeManager.updateOpenTrades(yPort[0][yPort[0].length - 1], yPort[1][yPort[1].length - 1], beta);

		/**
		 * Here, you can just get the lookbackCalculator out of the box, and adaptively
		 * update the lookback on each new price.
		 * You can test it and see how will the strategy performs.
		 */
		if (changeJR) {
			lookback = LookbackCalculator.computeLookback(yPort[0]);
			changeJR = false;
			canTrade = true;
		}

		prepareStrategyContext();
		processStrategy();

		if (periodCond) {
			canTrade = false;
			if (canChangeBeta(openTrades)) {
				changeJR = true;
				Arrays.fill(TradeContext.tradeIndicesToEliminate, 1);
				this.methodCall = 0;
			} else {
				this.methodCall = -1;
			}
		}

		generateTradeOuter(outerLoopIdx);
		processDataForSaving(datetimeIndex, TradeContext.trade);
	}

	protected abstract void prepareStrategyContext();

	protected abstract void processStrategy();

	/**
	 * Constants.DATETIME_INDEX from Constants gets initialized at CachingMainFeatures just like Constants.PRICE_MATRIX.
	 * They both gets updated automatically at this.cfi.newPrice(newPrice, datetimeIndex); in processCandle in class Core.
	 * @return
	 */
	protected abstract boolean isWithinTradingWindow();

	protected abstract void generateTrade(int outerLoopIdx);

	private void generateTradeOuter(int outerLoopIdx) {
		TradeContext.trade = null;
		boolean timeAgreement = isWithinTradingWindow();
		if (canTrade && timeAgreement) {
			generateTrade(outerLoopIdx);
		}
	}

	private boolean canChangeBeta(List<Trade> trades) {
		for (Trade trade : trades) {
			if (trade.pnl < 0) {
				return false;
			}
		}

		return true;
	}

	private double[] getBetaFromJr() {
		double[] beta = new double[jr.evec.length];
		for (int i = 0; i < beta.length; i++) {
			double share = jr.evec[i][0];
			beta[i] = share;
		}
		// normalize Beta = Beta / max(|Beta|) && Apply Units
		beta = VectorNormalization.maxAbsNorm(beta);
		for (int i = 0; i < beta.length; i++) {
			beta[i] *= BacktestConstants.FIXED_TRADE_UNITS;
		}
		return beta;
	}

	/* Helpers */
	public void processDataForSaving(String currDatetimeIndex, Trade trade) {
		this.methodCall += 1;
		this.logger.logFeatures(currDatetimeIndex, trade);
	}

	public void flushLastRows() {
		this.logger.flush();
	}

	private void validateNewPrice(double[] newPrice) {
		if (close0[close0.length - 1] != newPrice[0]) {
			System.out.println(close0[close0.length - 1]);
			System.out.println(newPrice[0]);
			throw new IllegalArgumentException("Close0 and given last close0 does not match!"
					+ " newCandle() didnt work, check the CFI Class' newCandle() function");
		}
	}
}
