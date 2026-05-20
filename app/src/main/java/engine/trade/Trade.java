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

    /**
     * Dummy Constructor for fake trades
     */
    public Trade() {
        this.id = -1;
        this.entryIdx = -1;
        this.entryPrice = Double.NaN;
        this.type = -1;
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

        this.id = NEXT_ID++;

        this.entryIdx = entryIdx;
        this.entryPrice = entryPrice;
        this.type = type;

        initialPnL();
    }

    private void initialPnL() {

    }

    /**
     * Updates live unrealized PnL using current market price.
     *
     * @param currentBidPrice current market price
     */
    public void update(double currentBidPrice, double currentAskPrice, double[] beta) {
        if (closed) {
            return;
        }
        double lastPnl = this.pnl;

        switch (this.type) {
            case 1 -> this.pnl = BacktestConstants.longPnL(currentBidPrice, this.entryPrice);
            case 0 -> this.pnl = BacktestConstants.shortPnL(currentAskPrice, this.entryPrice);
        }

        computeCommissions(beta);

        this.diff_pnl = this.pnl - lastPnl;
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
            sumCommission -= (absBeta[i] * BacktestConstants.COMMISION_PER_UNIT[i]);
        }

        this.pnl += sumCommission; // Sums with a negative variable
    }
}