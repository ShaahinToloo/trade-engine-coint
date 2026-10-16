package engine.trade;

import engine.constants.BacktestConstants;

/**
 * Represents a single live or closed trade.
 *
 * <p>
 * Handles:
 * </p>
 *
 * <ul>
 * <li>Entry information</li>
 * <li>Exit information</li>
 * <li>Live PnL updates</li>
 * <li>Trade state (open/closed)</li>
 * </ul>
 */
public class Trade {
    private static int NEXT_ID = 0;
    public final int id;

    public final int entryIdx;
    public final double entryPrice;

    /**
     * Trade direction/type.
     *
     * <pre>
     * 0 = short
     * 1 = long
     * </pre>
     */
    public final int type;

    public int exitIdx = -1;
    public double exitPrice = Double.NaN;

    public boolean closed = false;
    public double pnl = 0.0;
    public double diff_pnl = 0.0;

    public final Timestamp timestamp;

    /**
     * Beta multiplier applied at entry (volatility-targeted sizing).
     * Live yPort prices are divided by the current multiplier and re-scaled
     * by this frozen value, so later multiplier changes never distort PnL.
     */
    public final double betaMultiplier;

    /**
     * Entry price expressed in unit-beta terms ({@code entryPrice / betaMultiplier}).
     */
    public final double entryUnitPrice;

    /**
     * Frozen scaled beta at entry, used for commissions. Null for legacy
     * trades (falls back to the live beta).
     */
    public final double[] entryBeta;

    /**
     * Dummy Constructor for fake trades
     */
    public Trade() {
        this.id = -1;
        this.entryIdx = -1;
        this.entryPrice = Double.NaN;
        this.type = -1;
        this.timestamp = null;
        this.betaMultiplier = 1.0;
        this.entryUnitPrice = Double.NaN;
        this.entryBeta = null;
    }

    /**
     * Creates a new trade.
     *
     * @param entryIdx   candle index of entry
     * @param entryPrice entry price
     * @param type       trade direction/type
     */
    public Trade(
            int entryIdx,
            double entryPrice,
            int type) {
        this(entryIdx, entryPrice, type, (Timestamp) null);
    }

    /**
     * Creates a new trade with a datetime string.
     *
     * @param entryIdx    candle index of entry
     * @param entryPrice  entry price
     * @param type        trade direction/type
     * @param datetimeStr entry datetime, {@code "yyyy-MM-dd HH:mm:ss"}
     */
    public Trade(
            int entryIdx,
            double entryPrice,
            int type,
            String datetimeStr) {
        this(entryIdx, entryPrice, type, Timestamp.from(datetimeStr));
    }

    /**
     * Creates a new trade with a Timestamp.
     *
     * @param entryIdx   candle index of entry
     * @param entryPrice entry price
     * @param type       trade direction/type
     * @param timestamp  entry timestamp
     */
    public Trade(
            int entryIdx,
            double entryPrice,
            int type,
            Timestamp timestamp) {

        this(entryIdx, entryPrice, type, timestamp, 1.0, null);
    }

    /**
     * Creates a sized trade with a datetime string.
     *
     * @param entryIdx      candle index of entry
     * @param entryPrice    entry price (already scaled by betaMultiplier)
     * @param type          trade direction/type
     * @param datetimeStr   entry datetime, {@code "yyyy-MM-dd HH:mm:ss"}
     * @param betaMultiplier multiplier applied to beta at entry (must be positive)
     * @param betaAtEntry   scaled beta at entry, used for commissions (cloned)
     */
    public Trade(
            int entryIdx,
            double entryPrice,
            int type,
            String datetimeStr,
            double betaMultiplier,
            double[] betaAtEntry) {
        this(entryIdx, entryPrice, type, Timestamp.from(datetimeStr), betaMultiplier, betaAtEntry);
    }

    /**
     * Creates a new trade with volatility-targeted sizing.
     *
     * @param entryIdx      candle index of entry
     * @param entryPrice    entry price (already scaled by betaMultiplier)
     * @param type          trade direction/type
     * @param timestamp     entry timestamp
     * @param betaMultiplier multiplier applied to beta at entry (must be positive)
     * @param betaAtEntry   scaled beta at entry, used for commissions (cloned)
     */
    public Trade(
            int entryIdx,
            double entryPrice,
            int type,
            Timestamp timestamp,
            double betaMultiplier,
            double[] betaAtEntry) {

        this.id = NEXT_ID++;

        this.entryIdx = entryIdx;
        this.entryPrice = entryPrice;
        this.type = type;
        this.timestamp = timestamp;
        this.betaMultiplier = betaMultiplier;
        this.entryUnitPrice = entryPrice / betaMultiplier;
        this.entryBeta = (betaAtEntry != null) ? betaAtEntry.clone() : null;

        initialPnL();
    }

    private void initialPnL() {

    }

    /**
     * Updates live unrealized PnL using current market price.
     *
     * @param currentBidPrice    current market price (scaled by currentMultiplier)
     * @param currentMultiplier beta multiplier of the live series
     */
    public void update(double currentBidPrice, double currentAskPrice, double[] beta, double currentMultiplier) {
        if (closed) {
            return;
        }
        double lastPnl = this.pnl;

        // Re-scale live prices into this trade's frozen entry scale.
        double scale = currentMultiplier / this.betaMultiplier;
        double bid = currentBidPrice / scale;
        double ask = currentAskPrice / scale;

        switch (this.type) {
            case 1 -> this.pnl = BacktestConstants.longPnL(bid, this.entryPrice);
            case 0 -> this.pnl = BacktestConstants.shortPnL(ask, this.entryPrice);
        }

        computeCommissions((this.entryBeta != null) ? this.entryBeta : beta);

        this.diff_pnl = this.pnl - lastPnl;
    }

    /**
     * Legacy update assuming the live series carries this trade's own scale.
     * Equivalent to {@code update(bid, ask, beta, this.betaMultiplier)}.
     */
    public void update(double currentBidPrice, double currentAskPrice, double[] beta) {
        update(currentBidPrice, currentAskPrice, beta, this.betaMultiplier);
    }

    /**
     * Closes the trade.
     *
     * <p>
     * Final PnL is calculated automatically.
     * </p>
     *
     * @param exitPrice exit price
     * @param exitIdx   exit candle index
     */
    public void close(int exitIdx) {
        if (closed) {
            return;
        }

        this.exitIdx = exitIdx;

        this.closed = true;
    }

    /**
     * This part can be easily used in the close() function instead of update
     * function.
     * That way this commission calculation part is much faster, because we dont
     * calculate commissions on each data point.
     * But i prefer it this way, im gooG :)
     * 
     * @param beta
     */
    private void computeCommissions(double[] beta) {
        double[] absBeta = beta.clone();

        for (int i = 0; i < absBeta.length; i++) {
            absBeta[i] = Math.abs(absBeta[i]);
        }

        double sumCommission = 0.0; // its Always negative
        for (int i = 0; i < absBeta.length; i++) {
            sumCommission -= (absBeta[i] * BacktestConstants.COMMISSION_PER_UNIT[i]);
        }

        this.pnl += sumCommission; // Sums with a negative variable
    }
}