package engine.cointegration.portfolio;

import engine.constants.PublicConstants;

/**
 * Volatility-targeted position sizer for the synthetic portfolio.
 *
 * <p>
 * Measures sigma of the unit-beta yPort bid series over a trailing window,
 * defines the price range as {@code R = 2 * sigma}, and returns a beta
 * multiplier {@code m} such that an adverse half-range move ({@code R / 2})
 * loses {@code RISK_PER_HALF_RANGE} of {@code INIT_BALANCE}:
 * </p>
 *
 * <pre>
 * m = 2 * RISK_PER_HALF_RANGE * INIT_BALANCE / R
 * </pre>
 *
 * <p>
 * Low volatility gives a larger multiplier and vice versa. The multiplier is
 * always positive, so beta signs (long/short legs) are preserved and yPort
 * scales exactly linearly.
 * </p>
 */
public class PositionSizer {

    /**
     * Result of a sizing computation.
     */
    public static class Sizing {
        public final double multiplier;
        public final double sigma;
        public final double range;

        public Sizing(double multiplier, double sigma, double range) {
            this.multiplier = multiplier;
            this.sigma = sigma;
            this.range = range;
        }
    }

    /**
     * Computes the beta multiplier from the unit-beta yPort bid series.
     *
     * @param yPortBid  bid leg of yPort computed with the unscaled (unit) beta
     * @param lookback  trailing window in candles (clamped to series length)
     * @return sizing with multiplier, sigma and range
     */
    public static Sizing compute(double[] yPortBid, int lookback) {
        double sigma = trailingStd(yPortBid, lookback);
        double range = 2.0 * sigma;

        if (!(range > PublicConstants.EPSILON)) {
            return new Sizing(1.0, sigma, range);
        }

        double m = 2.0 * PublicConstants.RISK_PER_HALF_RANGE
                * PublicConstants.INIT_BALANCE / range;

        m = Math.min(Math.max(m, PublicConstants.MIN_BETA_MULTIPLIER),
                PublicConstants.MAX_BETA_MULTIPLIER);

        return new Sizing(m, sigma, range);
    }

    /**
     * Population standard deviation over the trailing window.
     */
    public static double trailingStd(double[] series, int lookback) {
        if (series == null || series.length == 0) {
            return 0.0;
        }
        int n = series.length;
        int window = Math.min(Math.max(lookback, 2), n);
        int start = n - window;

        double sum = 0.0;
        for (int i = start; i < n; i++) {
            sum += series[i];
        }
        double mu = sum / window;

        double sq = 0.0;
        for (int i = start; i < n; i++) {
            double d = series[i] - mu;
            sq += d * d;
        }

        return Math.sqrt(sq / window);
    }
}
