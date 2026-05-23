package engine.constants;

import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import engine.timeUtils.TimeUtils;

public sealed class PublicConstants permits ExecutionConstants, BacktestConstants {
    public static final String MAIN_FOLDER = "/home/arch/data/BackTest/BBMR/";

    public static final String DATA_LOG_PATH = MAIN_FOLDER;

    public static final String INFO_LOG_PATH = MAIN_FOLDER;
    public static final String INFO_LOG_NAME = "inf.txt";

    // in qouted currency | Must be positive! | If your
    // provider has a 2-sided commission, you must multiply
    // this nunmber by 2 in here. If you dont know what is a
    // 2 or 1 sided commission is. DO ASK an AI NOW!
    public static final double[] COMMISION_PER_UNIT = new double[10];
    private static final boolean TWO_SIDED_COMM = false;
    
	// Order matters && for reversed currencies recompute spread manually and put it
	// here
    public static final double[] SPREADS = new double[] { 0.00003, 0.00001, 0.00003, 0, 0, 0, 0, 0, 0, 0 };

    public static double ENTERY_Z_SCORE = 2.0;
    public static double EXIT_Z_SCORE = -2.0; // '-' sign means "in the opposite side"

    public static final int MR_LOOKBACK = 120;
    public static int SEQ_LENGTH = 320;
    public static int TESTS_PERIOD = 45 - 1; // '-1' is necessary because start and end are inclusive

    public static Map<String, double[]> globalFeatureMap = new LinkedHashMap<>();
    public static Map<String, double[]> globalDerivedFeatureMap = new LinkedHashMap<>();
    public static double[][] PRICE_MATRIX;
    public static List<String> DATETIME_INDEX;

    public static final float GMT_OFFSET = 3.0f;
    public static final ZoneOffset ZONE_OFFSET = ZoneOffset.ofTotalSeconds(TimeUtils.gmtConverter(PublicConstants.GMT_OFFSET));

    public static final double INIT_BALANCE = 5000;
    public static final int FIXED_TRADE_UNITS = 100_0; // Must be positive!
    public static final double MAX_CAP_UNITS = Double.POSITIVE_INFINITY;

    public static final boolean USE_STOP_LOSS = false;
    public static final double STOP_LOSS = -500; // Must be negative!
    public static int MAX_SIMUTANIOUS_TRADES = 1;

    public static final double EPSILON = 1e-9;
	public static final int EV_BINS = 20;
	public static final double TIMEFRAME_MINUTES = 1;

    static {
        double val = 0.00007;
        Arrays.fill(COMMISION_PER_UNIT, ((TWO_SIDED_COMM) ? val * 2 : val));
    }

    public static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            System.out.println("Sleep interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    public static float longPnL(float currentPrice, float entryPrice) {
        return currentPrice - entryPrice;
    }

    public static double longPnL(double currentPrice, double entryPrice) {
        return currentPrice - entryPrice;
    }

    public static int longPnL(int currentPrice, int entryPrice) {
        return currentPrice - entryPrice;
    }

    public static long longPnL(long currentPrice, long entryPrice) {
        return currentPrice - entryPrice;
    }

    public static float shortPnL(float currentPrice, float entryPrice) {
        return entryPrice - currentPrice;
    }

    public static double shortPnL(double currentPrice, double entryPrice) {
        return entryPrice - currentPrice;
    }

    public static int shortPnL(int currentPrice, int entryPrice) {
        return entryPrice - currentPrice;
    }

    public static long shortPnL(long currentPrice, long entryPrice) {
        return entryPrice - currentPrice;
    }
}
