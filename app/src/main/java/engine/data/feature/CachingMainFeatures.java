package engine.data.feature;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import engine.constants.PublicConstants;
import engine.keyUtils.KeyValDerived;
import engine.keyUtils.KeyValMain;

public class CachingMainFeatures {
	private final List<String> dateTimeIndexArr;
	private String lastDateTimeIndex;

	private double[] yPort1D;

	private final int windowSize = PublicConstants.SEQ_LENGTH;
	private boolean cached = false;

	private int currIdx = windowSize - 1;

	public final int n;

	Map<String, double[]> features = new HashMap<>();

	Map<String, double[]> hullStates = new HashMap<>();
	Map<String, double[]> rsiStates = new HashMap<>();
	Map<String, double[]> superTrendStates = new HashMap<>();
	Map<String, double[]> adxStates = new HashMap<>();

	public CachingMainFeatures(double[][] priceMatrix, List<String> dateTimeIndex) {
		n = priceMatrix[0].length;
		for (int i = 0; i < n; i++) {
			double[] close = new double[priceMatrix.length];
			for (int index = 0; index < close.length; index++) {
				close[index] = priceMatrix[index][i];
			}
			this.features.put("close" + i, close);
		}
		this.dateTimeIndexArr = new ArrayList<>(dateTimeIndex);
		this.currIdx = windowSize - 1;

		PublicConstants.PRICE_MATRIX = priceMatrix.clone();
		PublicConstants.DATETIME_INDEX = this.dateTimeIndexArr;

		// initializeAllFeatures(); // <-- [M1] Uncomment this for using of main
		// features
	}

	private void initializeAllFeatures() {
		// type | period(s) | source

		// SMA Features
		features.put(KeyValMain.SMA_2_YPORT, new double[this.windowSize]);
		features.put(KeyValMain.SMA_3_SMA_2_YPORT, new double[this.windowSize]);

		// Time Features
		features.put(KeyValDerived.HOUR_SIN, new double[this.windowSize]);
		features.put(KeyValDerived.HOUR_COS, new double[this.windowSize]);
		features.put(KeyValDerived.DOW_SIN, new double[this.windowSize]);
		features.put(KeyValDerived.DOW_COS, new double[this.windowSize]);
		features.put(KeyValDerived.MINUTE_OF_DAY, new double[this.windowSize]);
		features.put(KeyValDerived.SESSION_TOKYO, new double[this.windowSize]);
		features.put(KeyValDerived.SESSION_LONDON, new double[this.windowSize]);
		features.put(KeyValDerived.SESSION_NY, new double[this.windowSize]);
		features.put(KeyValDerived.SESSION_LON_NY_OVERLAP, new double[this.windowSize]);
		features.put(KeyValDerived.SESSION_TOK_LON_OVERLAP, new double[this.windowSize]);
		features.put(KeyValDerived.SESSION_ID, new double[this.windowSize]);
		features.put(KeyValDerived.MINS_SINCE_TOKYO_OPEN, new double[this.windowSize]);
		features.put(KeyValDerived.MINS_SINCE_LONDON_OPEN, new double[this.windowSize]);
		features.put(KeyValDerived.MINS_SINCE_NY_OPEN, new double[this.windowSize]);
		features.put(KeyValDerived.MINS_TO_TOKYO_CLOSE, new double[this.windowSize]);
		features.put(KeyValDerived.MINS_TO_LONDON_CLOSE, new double[this.windowSize]);
		features.put(KeyValDerived.MINS_TO_NY_CLOSE, new double[this.windowSize]);

		// Initialize with NaN

		Arrays.fill(features.get(KeyValMain.SMA_2_YPORT), Double.NaN);
		Arrays.fill(features.get(KeyValMain.SMA_3_SMA_2_YPORT), Double.NaN);

		Arrays.fill(features.get(KeyValDerived.HOUR_SIN), Double.NaN);
		Arrays.fill(features.get(KeyValDerived.HOUR_COS), Double.NaN);
		Arrays.fill(features.get(KeyValDerived.DOW_SIN), Double.NaN);
		Arrays.fill(features.get(KeyValDerived.DOW_COS), Double.NaN);
		Arrays.fill(features.get(KeyValDerived.MINUTE_OF_DAY), Double.NaN);
		Arrays.fill(features.get(KeyValDerived.SESSION_TOKYO), Double.NaN);
		Arrays.fill(features.get(KeyValDerived.SESSION_LONDON), Double.NaN);
		Arrays.fill(features.get(KeyValDerived.SESSION_NY), Double.NaN);
		Arrays.fill(features.get(KeyValDerived.SESSION_LON_NY_OVERLAP), Double.NaN);
		Arrays.fill(features.get(KeyValDerived.SESSION_TOK_LON_OVERLAP), Double.NaN);
		Arrays.fill(features.get(KeyValDerived.SESSION_ID), Double.NaN);
		Arrays.fill(features.get(KeyValDerived.MINS_SINCE_TOKYO_OPEN), Double.NaN);
		Arrays.fill(features.get(KeyValDerived.MINS_SINCE_LONDON_OPEN), Double.NaN);
		Arrays.fill(features.get(KeyValDerived.MINS_SINCE_NY_OPEN), Double.NaN);
		Arrays.fill(features.get(KeyValDerived.MINS_TO_TOKYO_CLOSE), Double.NaN);
		Arrays.fill(features.get(KeyValDerived.MINS_TO_LONDON_CLOSE), Double.NaN);
		Arrays.fill(features.get(KeyValDerived.MINS_TO_NY_CLOSE), Double.NaN);
	}

	private void shiftAndAppend(double[] arr, double value) {
		System.arraycopy(arr, 1, arr, 0, arr.length - 1);
		arr[arr.length - 1] = value;
	}

	public void newPrice(double[] newPrice, String dateTimeIndex) {
		for (int i = 0; i < n; i++) {
			this.shiftAndAppend(this.features.get("close" + i), newPrice[i]);
		}
		for (int j = 0; j < n; j++) {
			double[] var1 = this.features.get("close" + j);
			for (int i = 0; i < PublicConstants.PRICE_MATRIX.length; i++) {
				PublicConstants.PRICE_MATRIX[i][j] = var1[i];
			}
		}

		this.lastDateTimeIndex = dateTimeIndex;
		this.dateTimeIndexArr.remove(0);
		this.dateTimeIndexArr.add(this.lastDateTimeIndex);
	}

	public void calculateAllFeatures(double[][] yPort) {
		this.init_yPort(yPort);

		this.updateAllFeatures();
		this.cached = true;
	}

	public void init_yPort(double[][] yPort) {
		this.yPort1D = new double[yPort.length];
		for (int i = 0; i < yPort.length; i++) {
			this.yPort1D[i] = yPort[i][0];
		}
	}

	public void updateAllFeatures() {
		// vvv [M2] Uncomment these for using of main features
		// calculateSmaFeatures();
		// calculateTimeFeatures();
	}

	private void calculateSmaFeatures() {
		simpleMovingAverage(this.yPort1D, currIdx, lastDateTimeIndex);
	}

	private void simpleMovingAverage(double[] input, int period, String source) {
		int start = period - 1;
		int end = input.length;
		double[] sma = this.features.get("simpleMovingAverage_" + source + "_" + period);
		this.shiftAndAppend(sma, Double.NaN);

		if (this.cached) {
			start = this.currIdx;
		}

		for (int i = start; i < end; i++) {
			double sum = 0.0;
			for (int j = i - period + 1; j <= i; j++) {
				sum += input[j];
			}

			sma[i] = sum / period;
		}
	}

	private void exponentialMovingAverage(double[] input, int period, String source) {
		double alpha = 2.0 / (period + 1);
		int start = this.currIdx;
		int end = input.length;
		double[] ema = this.features.get("exponentialMovingAverage_" + source + "_" + period);
		this.shiftAndAppend(ema, Double.NaN);

		if (!this.cached) {
			start = period;
			simpleMovingAverage(input, period, "ema_" + source);
			double sma = this.features.get("simpleMovingAverage_" + "ema_" + source + "_" + period)[period - 1];
			ema[period - 1] = sma;
		}

		for (int i = start; i < end; i++) {
			ema[i] = alpha * input[i] + (1 - alpha) * ema[i - 1];
		}
	}

	private void weightedMovingAverage(double[] input, int period, String source) {
		int sumWeights = period * (period + 1) / 2;
		int start = period - 1;
		int end = input.length;
		double[] wma = this.features.get("weightedMovingAverage_" + source + "_" + period);
		this.shiftAndAppend(wma, Double.NaN);

		if (this.cached) {
			start = this.currIdx;
		}

		for (int i = start; i < end; i++) {
			double sum = 0.0;
			for (int j = 1; j <= period; j++) {
				sum += j * input[i - period + j];
			}
			wma[i] = sum / sumWeights;
		}
	}

	private void hullMovingAverage(double[] input, int period, String source) {
		int halfPeriod = period / 2;
		int sqrtPeriod = (int) Math.sqrt(period);
		int start = period - 1;
		int end = input.length;
		// double[] hma = this.features.get("hullMovingAverage_" + source + "_" +
		// period);
		// this.shiftAndAppend(hma, Double.NaN);
		double[] diffHull = this.hullStates.get("diffHull_" + source + "_" + period);
		this.shiftAndAppend(diffHull, Double.NaN);

		if (this.cached) {
			start = input.length - 1;
		}

		weightedMovingAverage(input, halfPeriod, "hma_hp_" + source);
		double[] wmaHalf = this.features.get("weightedMovingAverage_" + "hma_hp_" +
				source + "_" + halfPeriod);
		weightedMovingAverage(input, period, "hma_p_" + source);
		double[] wmaFull = this.features.get("weightedMovingAverage_" + "hma_p_" +
				source + "_" + period);

		for (int i = start; i < end; i++) {
			diffHull[i] = 2 * wmaHalf[i] - wmaFull[i];
		}

		weightedMovingAverage(diffHull, sqrtPeriod, "hma_sp_" + "diffHull_" +
				source);
		this.features.put("hullMovingAverage_" + source + "_" + period,
				getWeightedMovingAverage(sqrtPeriod, "hma_sp_" + "diffHull_" +
						source).clone());
	}

	private void fullHullMovingAverage(double[] input, int period, String source) {
		int halfPeriod = period / 2;
		int sqrtPeriod = (int) Math.sqrt(period);
		int start = period - 1;
		int end = input.length;
		double[] diffHull = new double[input.length];
		Arrays.fill(diffHull, Double.NaN);

		double[] wmaHalf = fullWMA(input, halfPeriod);
		double[] wmaFull = fullWMA(input, period);

		for (int i = start; i < end; i++) {
			diffHull[i] = 2 * wmaHalf[i] - wmaFull[i];
		}

		this.features.put("hullMovingAverage_" + source + "_" + period, fullWMA(diffHull, sqrtPeriod));
	}

	private double[] fullWMA(double[] src, int period) {
		double[] wma = new double[src.length];
		Arrays.fill(wma, Double.NaN);
		int weightSum = period * (period + 1) / 2;

		for (int i = period - 1; i < src.length; i++) {
			double sum = 0.0;
			for (int j = 1; j <= period; j++) {
				sum += j * src[i - period + j];
			}
			wma[i] = sum / weightSum;
		}
		return wma;
	}

	// private double[] exactHullOnHull(int period) {
	// // this is exactly what MainFeatures does: fresh HMA(period) → fresh
	// HMA(period)
	// // on that result
	// int half = period / 2;
	// int sqrtP = (int) Math.sqrt(period);
	//
	// double[] wmaHalf = fullWMA(closeArr, half);
	// double[] wmaFull = fullWMA(closeArr, period);
	//
	// double[] diff = new double[closeArr.length];
	// for (int i = 0; i < closeArr.length; i++) {
	// if (!Double.isNaN(wmaHalf[i]) && !Double.isNaN(wmaFull[i])) {
	// diff[i] = 2 * wmaHalf[i] - wmaFull[i];
	// } else {
	// diff[i] = Double.NaN;
	// }
	// }
	//
	// return fullWMA(diff, sqrtP); // this is the true HMA-of-HMA(period,period)
	// }

	private void relativeMovingAverage(double[] input, int period, String source) {
		double alpha = 1.0 / period;
		int start = input.length - 1;
		int end = input.length;
		double[] rma = this.features.get("relativeMovingAverage_" + source + "_" + period);
		this.shiftAndAppend(rma, Double.NaN);

		if (!this.cached) {
			start = period;
			simpleMovingAverage(input, period, "rma_" + source);
			double sma = this.features.get("simpleMovingAverage_" + "rma_" + source + "_" + period)[period - 1];
			rma[period - 1] = sma;
		}

		for (int i = start; i < end; i++) {
			rma[i] = alpha * input[i] + (1 - alpha) * rma[i - 1];
		}
	}

	private void relativeStrengthIndex(double[] input, int period, String source) {
		int start = 1;
		int end = input.length;
		double[] diffRSI = this.rsiStates.get("diffRSI_" + source + "_" + period);
		double[] upRSI = this.rsiStates.get("upRSI_" + source + "_" + period);
		double[] downRSI = this.rsiStates.get("downRSI_" + source + "_" + period);

		this.shiftAndAppend(diffRSI, Double.NaN);
		this.shiftAndAppend(upRSI, Double.NaN);
		this.shiftAndAppend(downRSI, Double.NaN);

		if (this.cached) {
			start = this.currIdx;
		}

		for (int i = start; i < end; i++) {
			diffRSI[i - 1] = input[i] - input[i - 1];
			upRSI[i - 1] = Math.max(diffRSI[i - 1], 0);
			downRSI[i - 1] = Math.max(-diffRSI[i - 1], 0);
		}

		// Step 2: RMA of gains and losses
		relativeMovingAverage(upRSI, period, "uprsi_" + source);
		double[] avgGain = this.getRelativeMovingAverage(period, "uprsi_" + source);
		relativeMovingAverage(downRSI, period, "downrsi_" + source);
		double[] avgLoss = this.getRelativeMovingAverage(period, "downrsi_" + source);

		double[] rsi = this.features.get("relativeStrengthIndex_" + source + "_" + period);
		this.shiftAndAppend(rsi, Double.NaN);

		start = period;
		if (this.cached) {
			start = this.currIdx;
		}

		for (int i = start; i < end; i++) {
			double gain = avgGain[i - 1];
			double loss = avgLoss[i - 1];

			if (Double.isNaN(gain) || Double.isNaN(loss)) {
				rsi[i] = Double.NaN;
			} else if (loss == 0) {
				rsi[i] = 100.0; // Avoid division by zero
			} else {
				double rs = gain / loss;
				rsi[i] = 100.0 - (100.0 / (1.0 + rs));
			}
		}
	}

	private void trueRange(double[] high, double[] low, double[] close, int period) {
		this.trueRange(high, low, close, period, "hlc");
	}

	private void trueRange(double[] high, double[] low, double[] close, int period, String source) {
		int start = this.currIdx;
		int end = close.length;
		double[] tr = this.features.get("trueRange_" + source + "_" + period);
		this.shiftAndAppend(tr, Double.NaN);

		if (!this.cached) {
			start = 1;
			tr[0] = high[0] - low[0];
		}

		for (int i = start; i < end; i++) {
			double highLow = high[i] - low[i];
			double highClose = Math.abs(high[i] - close[i - 1]);
			double lowClose = Math.abs(low[i] - close[i - 1]);
			tr[i] = Math.max(highLow, Math.max(highClose, lowClose));
		}
	}

	private void averageTrueRange(double[] high, double[] low, double[] close, int period) {
		this.averageTrueRange(high, low, close, period, "hlc");
	}

	private void averageTrueRange(double[] high, double[] low, double[] close, int period, String source) {
		this.trueRange(high, low, close, period, "atr_" + source);
		double[] tr = this.getTrueRange(period, "atr_" + source);
		this.relativeMovingAverage(tr, period, "atr_" + source);
		double[] atr = this.getRelativeMovingAverage(period, "atr_" + source);
		this.features.put("averageTrueRange_" + source + "_" + period, atr);
	}

	private void standardDeviation(double[] input, int period, String source) {
		int start = period - 1;
		int end = input.length;
		this.simpleMovingAverage(input, period, "stddev_" + source);
		double[] sma = this.features.get("simpleMovingAverage_" + "stddev_" + source + "_" + period);
		double[] stddev = this.features.get("standardDeviation_" + source + "_" + period);
		this.shiftAndAppend(stddev, Double.NaN);

		if (this.cached) {
			start = end - 1;
		}

		for (int i = start; i < end; i++) {
			double mean = sma[i];
			double sumSq = 0;
			for (int j = i - period + 1; j <= i; j++) {
				double diff = input[j] - mean;
				sumSq += diff * diff;
			}
			stddev[i] = Math.sqrt(sumSq / period);
		}
	}

	private void bollingerBands(double[] input, int period, double stdDevMultiplier, String source) {
		int start = period - 1;
		int end = input.length;

		if (this.cached) {
			start = this.currIdx;
		}

		standardDeviation(input, period, "bb_" + source);
		double[] stddev = getStandardDeviation(period, "bb_" + source);
		double[] sma = getSimpleMovingAverage(period, "stddev_" + "bb_" + source);

		double[] bbl = this.features.get("bollingerBands_" + "bbl_" + source + "_" + period + "_" + stdDevMultiplier);
		double[] bbm = this.features.get("bollingerBands_" + "bbm_" + source + "_" + period + "_" + stdDevMultiplier);
		double[] bbu = this.features.get("bollingerBands_" + "bbu_" + source + "_" + period + "_" + stdDevMultiplier);
		double[] bbb = this.features.get("bollingerBands_" + "bbb_" + source + "_" + period + "_" + stdDevMultiplier);
		double[] bbp = this.features.get("bollingerBands_" + "bbp_" + source + "_" + period + "_" + stdDevMultiplier);
		this.shiftAndAppend(bbl, Double.NaN);
		this.shiftAndAppend(bbm, Double.NaN);
		this.shiftAndAppend(bbu, Double.NaN);
		this.shiftAndAppend(bbb, Double.NaN);
		this.shiftAndAppend(bbp, Double.NaN);

		for (int i = start; i < end; i++) {
			bbm[i] = sma[i];

			bbl[i] = sma[i] - stdDevMultiplier * stddev[i];
			bbu[i] = sma[i] + stdDevMultiplier * stddev[i];

			if (bbm[i] != 0) {
				bbb[i] = (bbu[i] - bbl[i]) / bbm[i];
			} else {
				bbb[i] = 0;
			}

			if ((bbu[i] - bbl[i]) != 0) {
				bbp[i] = (input[i] - bbl[i]) / (bbu[i] - bbl[i]);
			} else {
				bbp[i] = 0.5;
			}
		}
	}

	private void superTrend(double[] high, double[] low, double[] close, int atrLength, double factor) {
		this.superTrend(high, low, close, atrLength, factor, "hlc");
	}

	private void superTrend(double[] high, double[] low, double[] close, int atrLength, double factor, String source) {
		int start_first_loop = 0;
		int end = close.length;

		if (this.cached) {
			start_first_loop = this.currIdx;
		}

		averageTrueRange(high, low, close, atrLength, "superTrend_" + source);
		double[] atr = this.features.get("averageTrueRange_" + "superTrend_" + source + "_" + atrLength);

		double[] hl2ST = this.superTrendStates.get("hl2ST_" + atrLength + "_" + factor);
		double[] basicUpper = this.superTrendStates.get("basicUpper_" + atrLength + "_" + factor);
		double[] basicLower = this.superTrendStates.get("basicLower_" + atrLength + "_" + factor);
		double[] upperBandST = this.superTrendStates.get("upperBandST_" + atrLength + "_" + factor);
		double[] lowerBandST = this.superTrendStates.get("lowerBandST_" + atrLength + "_" + factor);

		double[] supertrend = this.features.get("superTrend_" + source + "_" + atrLength + "_" + factor);
		double[] direction = this.features.get("superTrend_" + "direction_" + source + "_" + atrLength + "_" + factor);

		this.shiftAndAppend(hl2ST, Double.NaN);
		this.shiftAndAppend(basicUpper, Double.NaN);
		this.shiftAndAppend(basicLower, Double.NaN);
		this.shiftAndAppend(upperBandST, Double.NaN);
		this.shiftAndAppend(lowerBandST, Double.NaN);

		this.shiftAndAppend(supertrend, Double.NaN);
		this.shiftAndAppend(direction, Double.NaN);

		for (int i = start_first_loop; i < end; i++) {
			hl2ST[i] = (high[i] + low[i]) / 2.0;
			basicUpper[i] = hl2ST[i] + factor * atr[i];
			basicLower[i] = hl2ST[i] - factor * atr[i];
		}

		for (int i = start_first_loop; i < end; i++) {
			if (Double.isNaN(basicUpper[i]) || Double.isNaN(basicLower[i]) || Double.isNaN(close[i]))
				continue;

			double currUpper = basicUpper[i];
			double currLower = basicLower[i];

			double prevLower = (i == 0 || Double.isNaN(lowerBandST[i - 1])) ? 0 : lowerBandST[i - 1];
			double prevUpper = (i == 0 || Double.isNaN(upperBandST[i - 1])) ? 0 : upperBandST[i - 1];

			double prevLowPrice = (i == 0) ? 0 : close[i - 1];
			double prevHighPrice = (i == 0) ? 0 : close[i - 1];

			lowerBandST[i] = (currLower > prevLower || prevLowPrice < prevLower) ? currLower : prevLower;
			upperBandST[i] = (currUpper < prevUpper || prevHighPrice > prevUpper) ? currUpper : prevUpper;

			double prevST = (i == 0 || Double.isNaN(supertrend[i - 1])) ? 0 : supertrend[i - 1];
			double prevU = prevUpper;

			double pineDir;
			if (i == 0 || Double.isNaN(atr[i - 1])) {
				pineDir = 1.0;
			} else if (prevST == prevU) {
				pineDir = (close[i] > upperBandST[i]) ? -1.0 : 1.0;
			} else {
				pineDir = (close[i] < lowerBandST[i]) ? 1.0 : -1.0;
			}

			supertrend[i] = (pineDir == -1.0) ? lowerBandST[i] : upperBandST[i];
			direction[i] = -pineDir; // Flip to match: 1 up, -1 down
		}
	}

	private void williamFractals(double[] high, double[] low, int period) {
		this.williamFractals(high, low, period, "hl");
	}

	private void williamFractals(double[] high, double[] low, int period, String source) {
		int start = period;
		int end = high.length - period;

		double[] fracHigh = this.features.get("williamFractals_" + "High_" + source + "_" + period);
		double[] fracLow = this.features.get("williamFractals_" + "Low_" + source + "_" + period);
		// Arrays.fill(fracHigh, 0.0);
		// Arrays.fill(fracLow, 0.0);
		this.shiftAndAppend(fracHigh, 0.0);
		this.shiftAndAppend(fracLow, 0.0);

		if (this.cached) {
			start = end - 1;
		}

		for (int i = start; i < end; i++) {
			int window_start = i - period;
			int window_end = i + period;

			double maxHigh = -Double.POSITIVE_INFINITY;
			double minLow = Double.POSITIVE_INFINITY;

			for (int j = window_start; j <= window_end; j++) {
				if (j != i) {
					if (high[j] > maxHigh)
						maxHigh = high[j];
					if (low[j] < minLow)
						minLow = low[j];
				}
			}

			fracHigh[i] = (high[i] >= maxHigh) ? 1 : 0;
			fracLow[i] = (low[i] <= minLow) ? 1 : 0;
		}
	}

	public void ichimoku(double[] high, double[] low, double[] close, int tenkenSen, int kijunSen, int senkouSpanB,
			int chikouSpan) {
		this.ichimoku(high, low, close, tenkenSen, kijunSen, senkouSpanB, chikouSpan, "hlc");
	}

	public void ichimoku(double[] high, double[] low, double[] close, int tenkenSen, int kijunSen, int senkouSpanB,
			int chikouSpan, String source) {
		int start = 0;
		int end = high.length;

		if (this.cached) {
			start = this.currIdx;
		}

		double[] tenken = this.features.get("ichimoku_" + "tenken_" + source + "_" + tenkenSen);
		double[] kijun = this.features.get("ichimoku_" + "kijun_" + source + "_" + kijunSen);
		double[] senkouA = this.features.get("ichimoku_" + "senkouA_" + source + "_" + tenkenSen + "_" + kijunSen);
		double[] senkouB = this.features.get("ichimoku_" + "senkouB_" + source + "_" + senkouSpanB);
		double[] chikou = this.features.get("ichimoku_" + "chikou_" + source + "_" + chikouSpan);

		this.shiftAndAppend(tenken, Double.NaN);
		this.shiftAndAppend(kijun, Double.NaN);
		this.shiftAndAppend(senkouA, Double.NaN);
		this.shiftAndAppend(senkouB, Double.NaN);
		this.shiftAndAppend(chikou, Double.NaN);

		for (int i = start; i < end; i++) {
			// Tenkan-sen
			if (i >= tenkenSen - 1) {
				double maxHigh = max(high, i - tenkenSen + 1, i);
				double minLow = min(low, i - tenkenSen + 1, i);
				tenken[i] = (maxHigh + minLow) / 2.0;
			}

			// Kijun-sen
			if (i >= kijunSen - 1) {
				double maxHigh = max(high, i - kijunSen + 1, i);
				double minLow = min(low, i - kijunSen + 1, i);
				kijun[i] = (maxHigh + minLow) / 2.0;
			}

			// Senkou Span A (leading)
			if (i >= kijunSen - 1) {
				double t = tenken[i];
				double k = kijun[i];
				if (!Double.isNaN(t) && !Double.isNaN(k)) {
					senkouA[i + kijunSen] = (t + k) / 2.0;
				}
			}

			// Senkou Span B (leading)
			if (i >= senkouSpanB - 1) {
				double maxHigh = max(high, i - senkouSpanB + 1, i);
				double minLow = min(low, i - senkouSpanB + 1, i);
				senkouB[i + kijunSen] = (maxHigh + minLow) / 2.0;
			}

			// Chikou Span (lagging)
			if (i >= chikouSpan) {
				chikou[i - chikouSpan] = close[i];
			}
		}
	}

	// Ichimoku Helper function
	private double max(double[] arr, int from, int to) {
		double max = Double.NEGATIVE_INFINITY;
		for (int i = from; i <= to; i++) {
			if (arr[i] > max)
				max = arr[i];
		}
		return max;
	}

	// Ichimoku Helper function
	private double min(double[] arr, int from, int to) {
		double min = Double.POSITIVE_INFINITY;
		for (int i = from; i <= to; i++) {
			if (arr[i] < min)
				min = arr[i];
		}
		return min;
	}

	private void calculateTimeFeatures() {
		int start = 0;
		int end = windowSize;

		if (cached) {
			start = end - 1;
		}

		double[] hourSin = features.get(KeyValDerived.HOUR_SIN);
		double[] hourCos = features.get(KeyValDerived.HOUR_COS);
		double[] dowSin = features.get(KeyValDerived.DOW_SIN);
		double[] dowCos = features.get(KeyValDerived.DOW_COS);
		double[] minuteOfDay = features.get(KeyValDerived.MINUTE_OF_DAY);
		double[] sessionTokyo = features.get(KeyValDerived.SESSION_TOKYO);
		double[] sessionLondon = features.get(KeyValDerived.SESSION_LONDON);
		double[] sessionNY = features.get(KeyValDerived.SESSION_NY);
		double[] sessionLonNyOverlap = features.get(KeyValDerived.SESSION_LON_NY_OVERLAP);
		double[] sessionTokLonOverlap = features.get(KeyValDerived.SESSION_TOK_LON_OVERLAP);
		double[] sessionId = features.get(KeyValDerived.SESSION_ID);
		double[] minsSinceTokyoOpen = features.get(KeyValDerived.MINS_SINCE_TOKYO_OPEN);
		double[] minsSinceLondonOpen = features.get(KeyValDerived.MINS_SINCE_LONDON_OPEN);
		double[] minsSinceNyOpen = features.get(KeyValDerived.MINS_SINCE_NY_OPEN);
		double[] minsToTokyoClose = features.get(KeyValDerived.MINS_TO_TOKYO_CLOSE);
		double[] minsToLondonClose = features.get(KeyValDerived.MINS_TO_LONDON_CLOSE);
		double[] minsToNyClose = features.get(KeyValDerived.MINS_TO_NY_CLOSE);
		shiftAndAppend(hourSin, Double.NaN);
		shiftAndAppend(hourCos, Double.NaN);
		shiftAndAppend(dowSin, Double.NaN);
		shiftAndAppend(dowCos, Double.NaN);
		shiftAndAppend(minuteOfDay, Double.NaN);
		shiftAndAppend(sessionTokyo, Double.NaN);
		shiftAndAppend(sessionLondon, Double.NaN);
		shiftAndAppend(sessionNY, Double.NaN);
		shiftAndAppend(sessionLonNyOverlap, Double.NaN);
		shiftAndAppend(sessionTokLonOverlap, Double.NaN);
		shiftAndAppend(sessionId, Double.NaN);
		shiftAndAppend(minsSinceTokyoOpen, Double.NaN);
		shiftAndAppend(minsSinceLondonOpen, Double.NaN);
		shiftAndAppend(minsSinceNyOpen, Double.NaN);
		shiftAndAppend(minsToTokyoClose, Double.NaN);
		shiftAndAppend(minsToLondonClose, Double.NaN);
		shiftAndAppend(minsToNyClose, Double.NaN);

		for (int i = start; i < end; i++) {
			String dateTimeStr = dateTimeIndexArr.get(i);

			// Parse UTC datetime string "YYYY-MM-DD HH:mm:SS"
			LocalDateTime dateTime = LocalDateTime.parse(dateTimeStr,
					DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

			int hour = dateTime.getHour();
			int minute = dateTime.getMinute();
			int minuteOfDayValue = hour * 60 + minute;
			DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
			int dow = dayOfWeek.getValue() - 1; // Monday=0, Sunday=6

			// Hour cycle (24h)
			hourSin[i] = Math.sin(2 * Math.PI * hour / 24.0);
			hourCos[i] = Math.cos(2 * Math.PI * hour / 24.0);

			// Day-of-week cycle
			dowSin[i] = Math.sin(2 * Math.PI * dow / 7.0);
			dowCos[i] = Math.cos(2 * Math.PI * dow / 7.0);

			// Minute of day raw
			minuteOfDay[i] = minuteOfDayValue;

			// Session masks (UTC fixed)
			boolean tokyoMask = (hour >= 0) && (hour < 9); // 00:00 - 09:00 UTC
			boolean londonMask = (hour >= 8) && (hour < 17); // 08:00 - 17:00 UTC
			boolean nyMask = (hour >= 13) && (hour < 22); // 13:00 - 22:00 UTC

			// One-hot session flags
			sessionTokyo[i] = tokyoMask ? 1.0 : 0.0;
			sessionLondon[i] = londonMask ? 1.0 : 0.0;
			sessionNY[i] = nyMask ? 1.0 : 0.0;

			// Overlaps
			sessionLonNyOverlap[i] = (londonMask && nyMask) ? 1.0 : 0.0;
			sessionTokLonOverlap[i] = (tokyoMask && londonMask) ? 1.0 : 0.0;

			// Session ID: 0=none, 1=tokyo, 2=london, 3=ny, 4=lon_ny_overlap,
			// 5=tok_lon_overlap
			double sessionIdValue = 0.0;
			if (sessionLonNyOverlap[i] == 1.0) {
				sessionIdValue = 4.0;
			} else if (sessionTokLonOverlap[i] == 1.0) {
				sessionIdValue = 5.0;
			} else if (sessionNY[i] == 1.0) {
				sessionIdValue = 3.0;
			} else if (sessionLondon[i] == 1.0) {
				sessionIdValue = 2.0;
			} else if (sessionTokyo[i] == 1.0) {
				sessionIdValue = 1.0;
			}
			sessionId[i] = sessionIdValue;

			// Minutes since session open (normalized)
			minsSinceTokyoOpen[i] = minutesSinceOpen(tokyoMask, minuteOfDayValue, 0 * 60, 9 * 60);
			minsSinceLondonOpen[i] = minutesSinceOpen(londonMask, minuteOfDayValue, 8 * 60, 9 * 60);
			minsSinceNyOpen[i] = minutesSinceOpen(nyMask, minuteOfDayValue, 13 * 60, 9 * 60);

			// Minutes to session close (normalized)
			minsToTokyoClose[i] = minutesToClose(tokyoMask, minuteOfDayValue, 9 * 60, 9 * 60);
			minsToLondonClose[i] = minutesToClose(londonMask, minuteOfDayValue, 17 * 60, 9 * 60);
			minsToNyClose[i] = minutesToClose(nyMask, minuteOfDayValue, 22 * 60, 9 * 60);
		}
	}

	// Helper methods for session time calculations
	private double minutesSinceOpen(boolean mask, int minuteOfDay, int openMinute, int sessionLength) {
		if (!mask) {
			return -1.0;
		}
		return (minuteOfDay - openMinute) / (double) sessionLength;
	}

	private double minutesToClose(boolean mask, int minuteOfDay, int closeMinute, int sessionLength) {
		if (!mask) {
			return -1.0;
		}
		return (closeMinute - minuteOfDay) / (double) sessionLength;
	}

	public double[] getCustomArray(String key) {
		return this.features.get(key);
	}

	public double[] getAverageDirectionalIndex(int period, String source) {
		return this.features.get("averageDirectionalIndex_" + source + "_" + period);
	}

	private void averageDirectionalIndex(double[] high, double[] low, double[] close, int period) {
		this.averageDirectionalIndex(high, low, close, period, "hlc");
	}

	public void averageDirectionalIndex(double[] high, double[] low, double[] close, int period, String source) {
		int end = close.length;
		int start = this.cached ? this.currIdx : 1;

		double[] tr = adxState("tr_", source, period, close.length);
		double[] pdm = adxState("pdm_", source, period, close.length);
		double[] mdm = adxState("mdm_", source, period, close.length);
		double[] str = adxState("str_", source, period, close.length);
		double[] spdm = adxState("spdm_", source, period, close.length);
		double[] smdm = adxState("smdm_", source, period, close.length);
		double[] dxArr = adxState("dx_", source, period, close.length);

		String adxKey = "averageDirectionalIndex_" + source + "_" + period;
		double[] adx = this.features.get(adxKey);
		if (adx == null) {
			adx = new double[close.length];
			Arrays.fill(adx, Double.NaN);
			this.features.put(adxKey, adx);
		}

		if (this.cached) {
			this.shiftAndAppend(tr, Double.NaN);
			this.shiftAndAppend(pdm, Double.NaN);
			this.shiftAndAppend(mdm, Double.NaN);
			this.shiftAndAppend(str, Double.NaN);
			this.shiftAndAppend(spdm, Double.NaN);
			this.shiftAndAppend(smdm, Double.NaN);
			this.shiftAndAppend(dxArr, Double.NaN);
			this.shiftAndAppend(adx, Double.NaN);
		}

		if (!this.cached) {
			tr[0] = high[0] - low[0];
			pdm[0] = 0.0;
			mdm[0] = 0.0;
		}

		for (int i = start; i < end; i++) {
			double highLow = high[i] - low[i];
			double highClose = Math.abs(high[i] - close[i - 1]);
			double lowClose = Math.abs(low[i] - close[i - 1]);
			tr[i] = Math.max(highLow, Math.max(highClose, lowClose));

			double upMove = high[i] - high[i - 1];
			double downMove = low[i - 1] - low[i];
			pdm[i] = (upMove > downMove && upMove > 0) ? upMove : 0.0;
			mdm[i] = (downMove > upMove && downMove > 0) ? downMove : 0.0;
		}

		if (!this.cached) {
			wilderSmooth(tr, str, period);
			wilderSmooth(pdm, spdm, period);
			wilderSmooth(mdm, smdm, period);
		} else {
			str[end - 1] = (str[end - 2] * (period - 1) + tr[end - 1]) / period;
			spdm[end - 1] = (spdm[end - 2] * (period - 1) + pdm[end - 1]) / period;
			smdm[end - 1] = (smdm[end - 2] * (period - 1) + mdm[end - 1]) / period;
		}

		int dxStart = this.cached ? end - 1 : period - 1;
		for (int i = dxStart; i < end; i++) {
			if (Double.isNaN(str[i]) || str[i] == 0.0) {
				dxArr[i] = Double.NaN;
				continue;
			}
			double plusDI = 100.0 * spdm[i] / str[i];
			double minusDI = 100.0 * smdm[i] / str[i];
			double sum = plusDI + minusDI;
			dxArr[i] = (sum == 0.0) ? 0.0 : 100.0 * Math.abs(plusDI - minusDI) / sum;
		}

		if (this.cached) {
			double prev = adx[end - 2];
			if (Double.isNaN(prev) || Double.isNaN(dxArr[end - 1])) {
				adx[end - 1] = Double.NaN;
			} else {
				adx[end - 1] = (prev * (period - 1) + dxArr[end - 1]) / period;
			}
		} else {
			int dxCount = end - (period - 1);
			if (dxCount >= period) {
				int firstAdxIdx = (period - 1) + period - 1;
				double sum = 0.0;
				boolean valid = true;
				for (int i = period - 1; i <= firstAdxIdx; i++) {
					if (Double.isNaN(dxArr[i])) {
						valid = false;
						break;
					}
					sum += dxArr[i];
				}
				if (valid) {
					adx[firstAdxIdx] = sum / period;
					for (int i = firstAdxIdx + 1; i < end; i++) {
						adx[i] = (adx[i - 1] * (period - 1) + dxArr[i]) / period;
					}
				}
			}
		}
	}

	private double[] adxState(String prefix, String source, int period, int length) {
		String key = prefix + source + "_" + period;
		double[] arr = this.adxStates.get(key);
		if (arr == null) {
			arr = new double[length];
			Arrays.fill(arr, Double.NaN);
			this.adxStates.put(key, arr);
		}
		return arr;
	}

	private void wilderSmooth(double[] input, double[] output, int period) {
		double sum = 0.0;
		for (int i = 0; i < period; i++) {
			sum += input[i];
		}
		output[period - 1] = sum / period;
		for (int i = period; i < input.length; i++) {
			output[i] = (output[i - 1] * (period - 1) + input[i]) / period;
		}
	}

	public static double[] computeAdxSeries(double[] high, double[] low, double[] close, int period) {
		int n = close.length;
		double[] adx = new double[n];
		Arrays.fill(adx, Double.NaN);
		if (n < 2 * period) {
			return adx;
		}

		double[] tr = new double[n];
		double[] pdm = new double[n];
		double[] mdm = new double[n];
		tr[0] = high[0] - low[0];
		for (int i = 1; i < n; i++) {
			double highLow = high[i] - low[i];
			double highClose = Math.abs(high[i] - close[i - 1]);
			double lowClose = Math.abs(low[i] - close[i - 1]);
			tr[i] = Math.max(highLow, Math.max(highClose, lowClose));

			double upMove = high[i] - high[i - 1];
			double downMove = low[i - 1] - low[i];
			pdm[i] = (upMove > downMove && upMove > 0) ? upMove : 0.0;
			mdm[i] = (downMove > upMove && downMove > 0) ? downMove : 0.0;
		}

		double[] str = new double[n];
		double[] spdm = new double[n];
		double[] smdm = new double[n];
		Arrays.fill(str, Double.NaN);
		Arrays.fill(spdm, Double.NaN);
		Arrays.fill(smdm, Double.NaN);

		double sumTr = 0.0, sumP = 0.0, sumM = 0.0;
		for (int i = 0; i < period; i++) {
			sumTr += tr[i];
			sumP += pdm[i];
			sumM += mdm[i];
		}
		str[period - 1] = sumTr / period;
		spdm[period - 1] = sumP / period;
		smdm[period - 1] = sumM / period;
		for (int i = period; i < n; i++) {
			str[i] = (str[i - 1] * (period - 1) + tr[i]) / period;
			spdm[i] = (spdm[i - 1] * (period - 1) + pdm[i]) / period;
			smdm[i] = (smdm[i - 1] * (period - 1) + mdm[i]) / period;
		}

		double[] dx = new double[n];
		Arrays.fill(dx, Double.NaN);
		for (int i = period - 1; i < n; i++) {
			if (str[i] == 0.0) {
				dx[i] = 0.0;
				continue;
			}
			double plusDI = 100.0 * spdm[i] / str[i];
			double minusDI = 100.0 * smdm[i] / str[i];
			double sum = plusDI + minusDI;
			dx[i] = (sum == 0.0) ? 0.0 : 100.0 * Math.abs(plusDI - minusDI) / sum;
		}

		int firstAdxIdx = 2 * period - 2;
		double sumDx = 0.0;
		for (int i = period - 1; i <= firstAdxIdx; i++) {
			sumDx += dx[i];
		}
		adx[firstAdxIdx] = sumDx / period;
		for (int i = firstAdxIdx + 1; i < n; i++) {
			adx[i] = (adx[i - 1] * (period - 1) + dx[i]) / period;
		}
		return adx;
	}

	public double[] getSimpleMovingAverage(int period, String source) {
		return this.features.get("simpleMovingAverage_" + source + "_" + period);
	}

	public double[] getExponentialMovingAverage(int period, String source) {
		return this.features.get("exponentialMovingAverage_" + source + "_" + period);
	}

	public double[] getWeightedMovingAverage(int period, String source) {
		return this.features.get("weightedMovingAverage_" + source + "_" + period);
	}

	public double[] getHullMovingAverage(int period, String source) {
		return this.features.get("hullMovingAverage_" + source + "_" + period);
	}

	public double[] getRelativeMovingAverage(int period, String source) {
		return this.features.get("relativeMovingAverage_" + source + "_" + period);
	}

	public double[] getRelativeStrengthIndex(int period, String source) {
		return this.features.get("relativeStrengthIndex_" + source + "_" + period);
	}

	public double[] getTrueRange(int period, String source) {
		return this.features.get("trueRange_" + source + "_" + period);
	}

	public double[] getAverageTrueRange(int period, String source) {
		return this.features.get("averageTrueRange_" + source + "_" + period);
	}

	public double[] getStandardDeviation(int period, String source) {
		return this.features.get("standardDeviation_" + source + "_" + period);
	}

	/**
	 * You must enter the one of
	 * 'bbl_'
	 * 'bbm_'
	 * 'bbu_'
	 * 'bbb_'
	 * 'bbp_'
	 * + source
	 * or it raises err
	 */
	public double[] getBollingerBands(String band, String period, String stdDevMultiplier, String source) {
		String key = String.format("bollingerBands_%s_%s_%s_%s", band, source, period, stdDevMultiplier);
		return this.features.get(key);
	}

	public double[] getSuperTrend(int period, double factor, String source) {
		return this.features.get("superTrend_" + source + "_" + period + "_" + factor);
	}

	public double[] getWilliamFractals(int period, String source) {
		return this.features.get("williamFractals_" + source + "_" + period);
	}

	public double[] getIchimoku(int period, String source) {
		return this.features.get("ichimoku_" + source + "_" + period);
	}

	public double[] getIchimokuSenkouSpanA(int tenkenSen, int kijunSen, String source) {
		return this.features.get("ichimoku_" + "senkouA_" + source + "_" + tenkenSen + "_" + kijunSen);
	}
}
