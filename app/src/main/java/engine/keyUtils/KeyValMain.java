package engine.keyUtils;

public class KeyValMain {
    // Base OHLCV
    public static final String OPEN = "Open";
    public static final String HIGH = "High";
    public static final String LOW = "Low";
    public static final String CLOSE = "Close";
    public static final String VOLUME = "Volume";

    public static final String SMA_2_YPORT = KeyGen.build().type("sma").period(2).source("yPort").build();
    public static final String SMA_3_SMA_2_YPORT = KeyGen.build().type("sma").period(3).source("simpleMovingAverage_yPort_2").build();

    static {
    }
}
