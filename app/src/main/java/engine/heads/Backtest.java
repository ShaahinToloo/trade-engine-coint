package engine.heads;

import java.util.Arrays;
import java.util.List;

import engine.constants.BacktestConstants;
import engine.core.BBCore;
import engine.core.Core;
import engine.logger.DataLogger;
import engine.metrics.PerformanceMetrics;
import engine.metrics.RiskMetrics;
import engine.metrics.TradeMetrics;
import engine.reporter.BacktestReporter;
import engine.reporter.ProgressReporter;
import engine.state.BalanceTracker;
import engine.state.EquityTracker;
import engine.trade.Trade;
import engine.trade.TradeManager;

public class Backtest {
	public final HeadState headState = new HeadState();

	private final DataLogger logger = new DataLogger(BacktestConstants.MAIN_FOLDER + "/");

	public Backtest(double[][][] mohlcv, List<String> dateTimeIndex) {
		headState.market.datetimeIndex = dateTimeIndex;
		headState.market.length = BacktestConstants.DATA_LENGTH;

		initializeTimerArray();
		initializePortfolioBuffers();
		initializeEquityBuffers();
		initializePriceMatrix(mohlcv);
	}

	private void initializeTimerArray() {
		headState.perf.coreSpeedRange = new long[headState.perf.reportPeriod];
	}

	/**
	 * Initializes portfolio-related tracking arrays.
	 */
	private void initializePortfolioBuffers() {
		var dataLength = headState.market.length;
	
		headState.indicators.yPortBid = new double[dataLength];
		headState.indicators.yPortAsk = new double[dataLength];
		headState.indicators.movingAvg = new double[dataLength];
		headState.indicators.std = new double[dataLength];
	}

	/**
	 * Initializes equity and PnL tracking arrays.
	 */
	private void initializeEquityBuffers() {
		var dataLength = headState.market.length;

		headState.portfolio.equity = new double[dataLength];
		headState.portfolio.realisedEquity = new double[dataLength];

		Arrays.fill(
				headState.portfolio.equity,
				BacktestConstants.INIT_BALANCE);

		Arrays.fill(
				headState.portfolio.realisedEquity,
				BacktestConstants.INIT_BALANCE);
	}

	/**
	 * Converts MOHLCV close prices into a 2D matrix where:
	 *
	 * <pre>
	 * rows    = timestamps
	 * columns = assets
	 * </pre>
	 *
	 * Structure:
	 *
	 * <pre>
	 * matrix[timestamp][asset]
	 * </pre>
	 *
	 * Uses:
	 * 
	 * <pre>
	 * mohlcv[asset][ohlcv][timestamp]
	 * </pre>
	 */
	private void initializePriceMatrix(double[][][] mohlcv) {
		int rows = headState.market.length;
		int cols = BacktestConstants.NUM_SERIES;

		headState.market.priceMatrix = new double[rows][cols];

		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				headState.market.priceMatrix[i][j] = mohlcv[j][3][i];
			}
		}
	}

	public void start() {
		headState.perf.start = System.nanoTime();

		tradeFinder();

		headState.perf.stop = System.nanoTime();

		computeRiskMetrics();
		computeTradeMetrics();
		computePerformanceMetrics();

	}

	private void computeRiskMetrics() {
		headState.risk.equityDrawdown = RiskMetrics.calculateDrawdown(headState.portfolio.equity);
		headState.risk.equityCalmar = RiskMetrics.calculateCalmarRatio(TradeManager.closedTrades, headState.portfolio.equity);
		headState.risk.realisedDrawdown = RiskMetrics.calculateDrawdown(headState.portfolio.realisedEquity);
	}

	private void computeTradeMetrics() {
		int[] stats = TradeMetrics.calculateLSAndWS(TradeManager.closedTrades);

		headState.portfolio.losingStreak = stats[0];
		headState.portfolio.winningStreak = stats[1];
	}

	private void computePerformanceMetrics() {
		var closedTrades = TradeManager.closedTrades;

		headState.risk.sharpe = PerformanceMetrics.calculateSharpe(closedTrades);
		headState.risk.sortino = PerformanceMetrics.calculateSortino(closedTrades);
		headState.risk.omega = PerformanceMetrics.calculateOmega(closedTrades);
		headState.risk.kappa3 = PerformanceMetrics.calculateKappa3(closedTrades);
		headState.risk.er = PerformanceMetrics.calculateExpectedReturn(closedTrades);
		headState.risk.ev = PerformanceMetrics.calculateExpectedValueBinned(closedTrades, BacktestConstants.EV_BINS);
		headState.risk.mSharpe = PerformanceMetrics.calculateModifiedSharpeRatio(closedTrades);
	}

	private void tradeFinder() {
		double[][] priceMatrixSliced = buildInitialPriceMatrix();
		List<String> dateTimeIndexSliced = buildInitialDateTimeSlice();
		Core core = initializeCore(priceMatrixSliced, dateTimeIndexSliced);

		for (int i = BacktestConstants.SEQ_LENGTH; i < headState.market.length; i++) {
			handleProgressReporting(i);

			// Get the currentNewPrice from the globalScoped array
			System.arraycopy(headState.market.priceMatrix[i], 0, headState.buffers.newPrice, 0, BacktestConstants.NUM_SERIES);

			// Call CORE
			processCoreCandle(
					core,
					headState.market.datetimeIndex.get(i),
					i);

			Trade trade = Core.TradeContext.trade;
			int[] tradeIndicesToEliminate = Core.TradeContext.tradeIndicesToEliminate;

			/*
			 * methods that work with TradeManager.openTrades List must be called before the
			 * tradeEliminator.
			 * Why? Because the TradeManager.openTrades does reach the currPrice, but they
			 * can get closed before we apply the track functions and updating functions.
			 */
			var cyP = core.yPort;
			headState.portfolio.lastPortfolioBidPrice = cyP[0][cyP[0].length - 1];
			headState.portfolio.lastPortfolioAskPrice = cyP[1][cyP[1].length - 1];

			updateEquityState(i);

			tradeEliminator(tradeIndicesToEliminate, i);

			newTrade(trade);

			updateRealisedState(i);
			TradeManager.clearRecentClosedTrades();

			updatePortfolioTracking(
					core,
					i);
		}
		core.flushLastRows();
	}

	/**
	 * Updates realized equity/balance state
	 * using recently closed trades.
	 */
	private void updateRealisedState(int i) {

		headState.portfolio.realisedEquity = EquityTracker.trackRealisedEquity(
				TradeManager.recClosedTrades,
				headState.portfolio.realisedEquity,
				i,
				false);

		headState.portfolio.currBalance = BalanceTracker.trackCurrBalance(
				TradeManager.recClosedTrades,
				headState.portfolio.currBalance);
	}

	/**
	 * Stores latest portfolio-related series values.
	 */
	private void updatePortfolioTracking(
			Core core,
			int i) {

		if (i != BacktestConstants.SEQ_LENGTH) {

			headState.indicators.yPortBid[i] = headState.portfolio.lastPortfolioBidPrice;
			headState.indicators.yPortAsk[i] = headState.portfolio.lastPortfolioAskPrice;

			headState.indicators.movingAvg[i] = BBCore.CoreGetter.lastAvg;
			headState.indicators.std[i] = BBCore.CoreGetter.lastStd;

		} else {

			System.arraycopy(
					core.yPort[0],
					0,
					headState.indicators.yPortBid,
					0,
					core.yPort[0].length);

			System.arraycopy(
					core.yPort[1],
					0,
					headState.indicators.yPortAsk,
					0,
					core.yPort[1].length);
		}
	}

	private double[][] buildInitialPriceMatrix() {
		double[][] sliced = new double[BacktestConstants.SEQ_LENGTH][headState.market.priceMatrix[0].length];

		for (int i = 0; i < sliced.length; i++) {
			System.arraycopy(
					headState.market.priceMatrix[i],
					0,
					sliced[i],
					0,
					headState.market.priceMatrix[0].length);
		}

		return sliced;
	}

	private List<String> buildInitialDateTimeSlice() {
		return headState.market.datetimeIndex.subList(0, BacktestConstants.SEQ_LENGTH);
	}

	/**
	 * Initializes Core and prints initialization timing.
	 */
	private Core initializeCore(double[][] priceMatrix,
			List<String> dateTimeSlice) {

		long startCore = System.nanoTime();

		var core = new BBCore(
				priceMatrix,
				dateTimeSlice,
				logger.getRunPath(),
				BacktestConstants.ENTERY_Z_SCORE,
				BacktestConstants.EXIT_Z_SCORE);

		ProgressReporter.printElapsedNanoTime(
				System.nanoTime() - startCore,
				"Initialization");

		return core;
	}

	private void handleProgressReporting(int i) {
		if (i % headState.perf.reportPeriod == 0) {
			ProgressReporter.printProgressMeanSpeed(i, headState.market.length, headState.perf.coreSpeedRange, headState.perf.start, "Core");
			headState.perf.rangeIdx = 0;
		}
	}

	/**
	 * Processes a new candle through Core and tracks execution latency.
	 *
	 * @param core      core engine instance
	 * @param timestamp current timestamp
	 */
	private void processCoreCandle(
			Core core,
			String timestamp,
			int i) {

		long startCore = System.nanoTime();

		core.processCandle(
				headState.buffers.newPrice,
				timestamp,
				TradeManager.openTrades,
				i);

		headState.perf.coreSpeedRange[headState.perf.rangeIdx++] = System.nanoTime() - startCore;
	}

	private void updateEquityState(int i) {
		headState.portfolio.equity = EquityTracker.trackEquity(TradeManager.openTrades, headState.portfolio.equity, i, false);
	}

	private void tradeEliminator(int[] tradeIndicesToEliminate, int i) {
		for (int j = tradeIndicesToEliminate.length - 1; j >= 0; j--) {
			if (tradeIndicesToEliminate[j] == 1) {
				TradeManager.openTrades.get(j).close(i);
			}
		}
		TradeManager.flushClosedTrades();
	}

	private void newTrade(Trade trade) {
		boolean isThereSpace = TradeManager.openTradesSize() < BacktestConstants.MAX_SIMUTANIOUS_TRADES;
		if (isThereSpace) {
			TradeManager.addTrade(trade);
		}
	}

	// Log
	public void getResults(boolean doSave, String folderPath, String folderName) {
		String report = BacktestReporter.build(
				TradeManager.closedTrades,
				RiskMetrics.biggestLoss(TradeManager.closedTrades),
				RiskMetrics.biggestProfit(TradeManager.closedTrades),
				headState);

		System.out.print(report);

		if (doSave) {
			System.out.println("Saving Results...");
			logger.logBacktestResult(report, "backtestResults.txt");
			logger.logTrades("allTrades.csv", TradeManager.closedTrades, headState.market.length,
					headState.indicators.yPortBid,
					new double[][] { headState.indicators.yPortAsk, headState.indicators.movingAvg, headState.indicators.std },
					List.of("priceAsk", "mavg", "std"),
					headState.market.datetimeIndex);

			logger.logChart(headState.portfolio.equity, "Equity.csv");
			logger.logChart(headState.portfolio.realisedEquity, "RealisedEquity.csv");
		}
	}
}
