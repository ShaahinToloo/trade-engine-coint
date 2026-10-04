package engine.strategyLogic;

import java.util.List;

import engine.constants.BacktestConstants;
import engine.data.feature.CachingMainFeatures;
import engine.trade.Trade;

public class BBLogic extends Logic {
    public double zScoreAsk = Double.NaN, zScoreBid = Double.NaN;
    public double prevZScoreAsk = Double.NaN, prevZScoreBid = Double.NaN;
    public double lastAvg = Double.NaN, lastStd = Double.NaN;
    public double adx = Double.NaN;

    private final double entryZscore, exitZscore;
    private double stopZscore = 30.2;

    public BBLogic(double entryZscore, double exitZscore) {
        super();
        this.entryZscore = entryZscore;
        this.exitZscore = exitZscore;
    }

    @Override
    public void executeLogic() {
        int lookback = LogicContext.lookback;

        int i = bid.length - 1;

        this.lastAvg = this.lastAvg(lookback);
        double currStd = this.lastStd(lookback, lastAvg);

        if (Math.abs(currStd) >= BacktestConstants.EPSILON) {
            this.lastStd = currStd;
        }

//        this.zScoreAsk = (ask[i] - this.lastAvg) / this.lastStd;
//        this.zScoreBid = (bid[i] - this.lastAvg) / this.lastStd;
//
//        boolean longsEntry = zScoreAsk <= -entryZscore;
//        boolean shortsEntry = zScoreBid >= entryZscore;

        this.prevZScoreAsk = this.zScoreAsk;
        this.prevZScoreBid = this.zScoreBid;

        this.zScoreAsk = (ask[i] - this.lastAvg) / this.lastStd;
        this.zScoreBid = (bid[i] - this.lastAvg) / this.lastStd;

        double[] adxSeries = CachingMainFeatures.computeAdxSeries(bid, bid, bid, 14);
        this.adx = adxSeries[adxSeries.length - 1];

        // شرط مومنتوم: Z-Score باید تغییر جهت داده و به سمت صفر حرکت کرده باشد
        boolean isAskTurningUp = !Double.isNaN(this.prevZScoreAsk) && (this.zScoreAsk > this.prevZScoreAsk);
        boolean isBidTurningDown = !Double.isNaN(this.prevZScoreBid) && (this.zScoreBid < this.prevZScoreBid);

        double maxAdxThreshold = 20.0;
        boolean isAdxAllowed = false;
        if (!Double.isNaN(this.adx)) {
            isAdxAllowed = this.adx < maxAdxThreshold;
        }

        boolean longsEntry = (zScoreAsk <= -entryZscore) && isAskTurningUp && isAdxAllowed;
        boolean shortsEntry = (zScoreBid >= entryZscore) && isBidTurningDown && isAdxAllowed;

        if (longsEntry) {
            tradeType = 1;
            entry = ask[i];
        } else if (shortsEntry) {
            tradeType = 0;
            entry = bid[i];
        }

        prepLogicContext();
    }

    private void prepLogicContext() {
        LogicContext.entry = entry;
        LogicContext.tradeType = tradeType;
        if (!Double.isNaN(entry)) {
            LogicContext.validTrade = true;
        }
    }

    public int[] processOpenTradesForExit(List<Trade> openTrades, int[] out) {
        int tradesSize = out.length;

        for (int i = 0; i < tradesSize; i++) {
            Trade trade = openTrades.get(i);

            if (trade.type == 1) { // پوزیشن Long
                // خروج با تارگت سود OR خروج اضطراری با حد ضرر Z-Score (وقتی اسپرد واگرا‌تر می‌شود)
                if (this.zScoreBid >= -this.exitZscore || this.zScoreBid <= -this.stopZscore) {
                    out[i] = 1;
                }
            } else { // پوزیشن Short
                // خروج با تارگت سود OR خروج اضطراری با حد ضرر Z-Score (وقتی اسپرد واگرا‌تر می‌شود)
                if (this.zScoreAsk <= this.exitZscore || this.zScoreAsk >= this.stopZscore) {
                    out[i] = 1;
                }
            }
        }

        if (BacktestConstants.USE_STOP_LOSS) {
            for (int i = 0; i < tradesSize; i++) {
                Trade trade = openTrades.get(i);

                if (trade.pnl <= BacktestConstants.STOP_LOSS) {
                    out[i] = 1;
                }
            }
        }

        return out;
    }

    /* Helpers */
    private double lastAvg(int lookback) {

        int start = Math.max(this.bid.length - lookback, 0);

        double sum = 0.0;

        for (int i = start; i < this.bid.length; i++) {
            sum += this.bid[i];
        }

        int n = this.bid.length - start;

        return sum / n;
    }

    private double lastStd(int lookback, double mu) {

        int start = Math.max(this.bid.length - lookback, 0);

        double sum = 0.0;

        for (int i = start; i < this.bid.length; i++) {
            double d = this.bid[i] - mu;
            sum += d * d;
        }

        int n = this.bid.length - start;

        return Math.sqrt(sum / n);
    }
}
