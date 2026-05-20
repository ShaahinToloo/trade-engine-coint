package engine.data.feature;

import java.util.List;

import engine.constants.BacktestConstants;
import engine.featureUtils.KeyValDerived;

public class CachedFeatureInitializer {
	boolean cached = false, canInit = true;
	CachingMainFeatures cmf;

	public CachedFeatureInitializer(double[][] priceMatrix, List<String> dateTimeIndex) {
		this.cmf = new CachingMainFeatures(priceMatrix, dateTimeIndex);

		initDerivedArrays();
		this.canInit = false;
		globalInit();
		this.cached = true;
	}

	public void newPrice(double[] newPrice, String dateTimeIndex) {
		this.cmf.newPrice(newPrice, dateTimeIndex);
		globalInit();
	}

	private void initDerivedArrays() {
		if (!this.canInit) {
			return;
		}

		int length = BacktestConstants.SEQ_LENGTH;

		// Initialize all derived feature arrays directly in the globalDerivedFeatureMap

	}

	private void globalInit() {
		// Main Features
		initMainFeatures();

		// Derived Features
		initDerivedFeatures();
	}

	private void initMainFeatures() {
		initPriceSeries();
		initTimeFeatures();
	}

	private void initPriceSeries() {
		for (int i = 0; i < this.cmf.n; i++) {
			String key = "close" + i;
			BacktestConstants.globalFeatureMap.put(key, this.cmf.getCustomArray(key));
		}
	}

	private void initTimeFeatures() {
		BacktestConstants.globalDerivedFeatureMap.put(KeyValDerived.HOUR_SIN,
				this.cmf.getCustomArray(KeyValDerived.HOUR_SIN));
		BacktestConstants.globalDerivedFeatureMap.put(KeyValDerived.HOUR_COS,
				this.cmf.getCustomArray(KeyValDerived.HOUR_COS));
		BacktestConstants.globalDerivedFeatureMap.put(KeyValDerived.DOW_SIN,
				this.cmf.getCustomArray(KeyValDerived.DOW_SIN));
		BacktestConstants.globalDerivedFeatureMap.put(KeyValDerived.DOW_COS,
				this.cmf.getCustomArray(KeyValDerived.DOW_COS));
		BacktestConstants.globalDerivedFeatureMap.put(KeyValDerived.MINUTE_OF_DAY,
				this.cmf.getCustomArray(KeyValDerived.MINUTE_OF_DAY));
		BacktestConstants.globalDerivedFeatureMap.put(KeyValDerived.SESSION_TOKYO,
				this.cmf.getCustomArray(KeyValDerived.SESSION_TOKYO));
		BacktestConstants.globalDerivedFeatureMap.put(KeyValDerived.SESSION_LONDON,
				this.cmf.getCustomArray(KeyValDerived.SESSION_LONDON));
		BacktestConstants.globalDerivedFeatureMap.put(KeyValDerived.SESSION_NY,
				this.cmf.getCustomArray(KeyValDerived.SESSION_NY));
		BacktestConstants.globalDerivedFeatureMap.put(KeyValDerived.SESSION_LON_NY_OVERLAP,
				this.cmf.getCustomArray(KeyValDerived.SESSION_LON_NY_OVERLAP));
		BacktestConstants.globalDerivedFeatureMap.put(KeyValDerived.SESSION_TOK_LON_OVERLAP,
				this.cmf.getCustomArray(KeyValDerived.SESSION_TOK_LON_OVERLAP));
		BacktestConstants.globalDerivedFeatureMap.put(KeyValDerived.SESSION_ID,
				this.cmf.getCustomArray(KeyValDerived.SESSION_ID));
		BacktestConstants.globalDerivedFeatureMap.put(KeyValDerived.MINS_SINCE_TOKYO_OPEN,
				this.cmf.getCustomArray(KeyValDerived.MINS_SINCE_TOKYO_OPEN));
		BacktestConstants.globalDerivedFeatureMap.put(KeyValDerived.MINS_SINCE_LONDON_OPEN,
				this.cmf.getCustomArray(KeyValDerived.MINS_SINCE_LONDON_OPEN));
		BacktestConstants.globalDerivedFeatureMap.put(KeyValDerived.MINS_SINCE_NY_OPEN,
				this.cmf.getCustomArray(KeyValDerived.MINS_SINCE_NY_OPEN));
		BacktestConstants.globalDerivedFeatureMap.put(KeyValDerived.MINS_TO_TOKYO_CLOSE,
				this.cmf.getCustomArray(KeyValDerived.MINS_TO_TOKYO_CLOSE));
		BacktestConstants.globalDerivedFeatureMap.put(KeyValDerived.MINS_TO_LONDON_CLOSE,
				this.cmf.getCustomArray(KeyValDerived.MINS_TO_LONDON_CLOSE));
		BacktestConstants.globalDerivedFeatureMap.put(KeyValDerived.MINS_TO_NY_CLOSE,
				this.cmf.getCustomArray(KeyValDerived.MINS_TO_NY_CLOSE));
	}

	private void initDerivedFeatures() {
	}
}
