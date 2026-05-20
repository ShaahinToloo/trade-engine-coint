package engine.data.feature;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import engine.constants.BacktestConstants;
import engine.featureUtils.KeyValDerived;

public class CachingMainFeatures {
	private List<String> dateTimeIndexArr;
	private String lastDateTimeIndex;

	private int windowSize = BacktestConstants.SEQ_LENGTH;
	private boolean cached = false;

	public final int n;

	Map<String, double[]> features = new HashMap<>();

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

		BacktestConstants.PRICE_MATRIX = priceMatrix.clone();
		BacktestConstants.DATETIME_INDEX = this.dateTimeIndexArr;

		initializeAllFeatures();
		calculateAllFeatures();
	}

	private void initializeAllFeatures() {
		// type | period(s) | source

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
			for (int i = 0; i < BacktestConstants.PRICE_MATRIX.length; i++) {
				BacktestConstants.PRICE_MATRIX[i][j] = var1[i];
			}
		}

		this.lastDateTimeIndex = dateTimeIndex;
		this.dateTimeIndexArr.remove(0);
		this.dateTimeIndexArr.add(this.lastDateTimeIndex);

		this.updateAllFeatures();
	}

	private void calculateAllFeatures() {
		this.updateAllFeatures();
		this.cached = true;
	}

	public void updateAllFeatures() {
		calculateTimeFeatures();
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
}
