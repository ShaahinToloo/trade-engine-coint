package engine.strategyLogic;

public abstract class Logic {

    public static class LogicContext {
        public static boolean validTrade;
        public static double entry;
        public static double exitSl;
        public static double exitTp;
        public static int tradeType;
        public static int lookback;
    }

    protected double[] bid, ask;
    protected double entry = Double.NaN;
    protected double exitSl = Double.NaN;
    protected double exitTp = Double.NaN;
    protected int tradeType = -1;

    public Logic() {
    }

    public void reValue(double[][] yPort) {
        this.bid = yPort[0];
        this.ask = yPort[1];
        this.entry = Double.NaN;
        this.exitSl = Double.NaN;
        this.exitTp = Double.NaN;
        this.tradeType = -1;
        LogicContext.validTrade = false;
    }

    public abstract void executeLogic();

}