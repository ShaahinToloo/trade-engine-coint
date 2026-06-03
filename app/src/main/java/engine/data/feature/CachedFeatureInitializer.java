package engine.data.feature;

import java.util.Arrays;
import java.util.List;

import engine.cointegration.portfolio.SyntheticPortfolio;
import engine.constants.PublicConstants;
import engine.featureUtils.KeyValDerived;
import engine.featureUtils.KeyValMain;

public class CachedFeatureInitializer {
	double[][] yPort;
	boolean cached = false, canInit = true, doCalc = true;
	CachingMainFeatures cmf;
	CachingFeatureDerivator cfd;

	public CachedFeatureInitializer(double[][] priceMatrix, List<String> dateTimeIndex) {
		this.cmf = new CachingMainFeatures(priceMatrix, dateTimeIndex);
		this.cfd = new CachingFeatureDerivator();

		// initDerivedArrays(); // <-- [D1] Uncomment this for using of derived features
		this.canInit = false;
		globalInit();
		this.cached = true;
	}

	public void newPrice(double[] newPrice, String dateTimeIndex) {
		// We pass a dummy yPort at the beginning.
		if (doCalc) {
			var arr = new double[newPrice.length];
			Arrays.fill(arr, 1.0);

			this.cmf.calculateAllFeatures(this.calc_yPort(arr));
			doCalc = false;
		}

		this.cmf.newPrice(newPrice, dateTimeIndex);
	}

	public double[][] calc_yPort(double[] beta) {
		this.yPort = SyntheticPortfolio.yPort(PublicConstants.PRICE_MATRIX, PublicConstants.SPREADS, beta);
		return this.yPort;
	}

	public void updateMainFeatures() {
		this.cmf.init_yPort(this.yPort);
		this.cmf.updateAllFeatures();
	}

	private void initDerivedArrays() {
		if (!this.canInit) {
			return;
		}

		int length = PublicConstants.SEQ_LENGTH;

		// Initialize all derived feature arrays directly in the globalDerivedFeatureMap
		PublicConstants.globalDerivedFeatureMap.put(KeyValDerived.RAD_SLOPE_YPORT, new double[length]);

	}

	private void globalInit() {
		// Main priceSeries
		initPriceSeries();

		// Main Features
		// initMainFeatures(); //  <-- [M3] Uncomment this for using of main features

		// Derived Features
		// initDerivedFeatures(); // <-- [D2] Uncomment this for using of derived
		// features
	}

	private void initPriceSeries() {
		for (int i = 0; i < this.cmf.n; i++) {
			String key = "close" + i;
			PublicConstants.globalFeatureMap.put(key, this.cmf.getCustomArray(key));
		}
	}

	private void initMainFeatures() {
		initSMAFeatures();
		initTimeFeatures();
	}

	private void initSMAFeatures() {
		// SMA 2 close
		PublicConstants.globalFeatureMap.put(KeyValMain.SMA_2_YPORT, this.cmf.getSimpleMovingAverage(2, "yPort"));

		// SMA 3 of SMA 2 close
		PublicConstants.globalFeatureMap.put(KeyValMain.SMA_3_SMA_2_YPORT,
				this.cmf.getSimpleMovingAverage(3, "simpleMovingAverage_yPort_2"));
	}

	private void initTimeFeatures() {
		PublicConstants.globalDerivedFeatureMap.put(KeyValDerived.HOUR_SIN,
				this.cmf.getCustomArray(KeyValDerived.HOUR_SIN));
		PublicConstants.globalDerivedFeatureMap.put(KeyValDerived.HOUR_COS,
				this.cmf.getCustomArray(KeyValDerived.HOUR_COS));
		PublicConstants.globalDerivedFeatureMap.put(KeyValDerived.DOW_SIN,
				this.cmf.getCustomArray(KeyValDerived.DOW_SIN));
		PublicConstants.globalDerivedFeatureMap.put(KeyValDerived.DOW_COS,
				this.cmf.getCustomArray(KeyValDerived.DOW_COS));
		PublicConstants.globalDerivedFeatureMap.put(KeyValDerived.MINUTE_OF_DAY,
				this.cmf.getCustomArray(KeyValDerived.MINUTE_OF_DAY));
		PublicConstants.globalDerivedFeatureMap.put(KeyValDerived.SESSION_TOKYO,
				this.cmf.getCustomArray(KeyValDerived.SESSION_TOKYO));
		PublicConstants.globalDerivedFeatureMap.put(KeyValDerived.SESSION_LONDON,
				this.cmf.getCustomArray(KeyValDerived.SESSION_LONDON));
		PublicConstants.globalDerivedFeatureMap.put(KeyValDerived.SESSION_NY,
				this.cmf.getCustomArray(KeyValDerived.SESSION_NY));
		PublicConstants.globalDerivedFeatureMap.put(KeyValDerived.SESSION_LON_NY_OVERLAP,
				this.cmf.getCustomArray(KeyValDerived.SESSION_LON_NY_OVERLAP));
		PublicConstants.globalDerivedFeatureMap.put(KeyValDerived.SESSION_TOK_LON_OVERLAP,
				this.cmf.getCustomArray(KeyValDerived.SESSION_TOK_LON_OVERLAP));
		PublicConstants.globalDerivedFeatureMap.put(KeyValDerived.SESSION_ID,
				this.cmf.getCustomArray(KeyValDerived.SESSION_ID));
		PublicConstants.globalDerivedFeatureMap.put(KeyValDerived.MINS_SINCE_TOKYO_OPEN,
				this.cmf.getCustomArray(KeyValDerived.MINS_SINCE_TOKYO_OPEN));
		PublicConstants.globalDerivedFeatureMap.put(KeyValDerived.MINS_SINCE_LONDON_OPEN,
				this.cmf.getCustomArray(KeyValDerived.MINS_SINCE_LONDON_OPEN));
		PublicConstants.globalDerivedFeatureMap.put(KeyValDerived.MINS_SINCE_NY_OPEN,
				this.cmf.getCustomArray(KeyValDerived.MINS_SINCE_NY_OPEN));
		PublicConstants.globalDerivedFeatureMap.put(KeyValDerived.MINS_TO_TOKYO_CLOSE,
				this.cmf.getCustomArray(KeyValDerived.MINS_TO_TOKYO_CLOSE));
		PublicConstants.globalDerivedFeatureMap.put(KeyValDerived.MINS_TO_LONDON_CLOSE,
				this.cmf.getCustomArray(KeyValDerived.MINS_TO_LONDON_CLOSE));
		PublicConstants.globalDerivedFeatureMap.put(KeyValDerived.MINS_TO_NY_CLOSE,
				this.cmf.getCustomArray(KeyValDerived.MINS_TO_NY_CLOSE));
	}

	private void initDerivedFeatures() {
		initDerivedSMAFeatures();
	}

	private void initDerivedSMAFeatures() {
		final double[] SMA_2_YPORT = PublicConstants.globalFeatureMap.get(KeyValMain.SMA_2_YPORT);

		// SMA Section
		this.cfd.radSlope(
				SMA_2_YPORT,
				PublicConstants.globalDerivedFeatureMap.get(KeyValDerived.RAD_SLOPE_YPORT),
				this.cached);

	}
}
