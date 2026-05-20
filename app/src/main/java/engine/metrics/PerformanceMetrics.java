package engine.metrics;

import java.util.List;

import engine.trade.Trade;

public class PerformanceMetrics {
    /**
     * Calculates Sharpe ratio from trade returns.
     *
     * Assumes:
     * - returns are already in consistent units (e.g. % or PnL)
     * - no risk-free rate adjustment (simplified version)
     *
     * Sharpe = mean / std
     *
     * @param closedTrades list of trades, where index 4 is return/PnL
     * @return Sharpe ratio (0 if undefined)
     */
    public static double calculateSharpe(List<Trade> closedTrades) {
        int n = closedTrades.size();
        if (n < 2)
            return 0.0;

        // Mean
        double sum = 0.0;

        for (Trade trade : closedTrades) {
            sum += trade.pnl;
        }

        double mean = sum / n;

        // StdDev
        double variance = 0.0;
        for (Trade trade : closedTrades) {
            double r = trade.pnl;
            double diff = r - mean;
            variance += diff * diff;
        }

        variance /= (n - 1);

        if (variance == 0.0)
            return 0.0;

        double std = Math.sqrt(variance);

        // Sharpe
        return mean / std;
    }

    public static double calculateSortino(List<Trade> closedTrades) {
        int n = closedTrades.size();

        if (n < 2)
            return 0.0;

        double mean = 0.0;

        for (Trade trade : closedTrades) {
            mean += trade.pnl;
        }

        mean /= n;

        double downside = 0.0;

        for (Trade trade : closedTrades) {
            double pnl = trade.pnl;

            if (pnl < 0) {
                downside += pnl * pnl;
            }
        }

        downside /= n;

        if (downside == 0.0)
            return 0.0;

        return mean / Math.sqrt(downside);
    }

    public static double calculateOmega(List<Trade> closedTrades) {
        double gains = 0.0;
        double losses = 0.0;

        for (Trade trade : closedTrades) {
            double pnl = trade.pnl;

            if (pnl > 0) {
                gains += pnl;
            } else {
                losses += -pnl;
            }
        }

        if (losses == 0.0)
            return 0.0;

        return gains / losses;
    }

    public static double calculateKappa3(List<Trade> closedTrades) {
        int n = closedTrades.size();

        if (n < 2)
            return 0.0;

        double mean = 0.0;

        for (Trade trade : closedTrades) {
            mean += trade.pnl;
        }

        mean /= n;

        double lpm3 = 0.0;

        for (Trade trade : closedTrades) {
            double pnl = trade.pnl;

            if (pnl < 0) {
                lpm3 += Math.pow(-pnl, 3);
            }
        }

        lpm3 /= n;

        if (lpm3 == 0.0)
            return 0.0;

        return mean / Math.cbrt(lpm3);
    }

    public static double calculateExpectedReturn(List<Trade> closedTrades) {
        int n = closedTrades.size();

        if (n == 0)
            return 0.0;

        double sum = 0.0;

        for (Trade trade : closedTrades) {
            sum += trade.pnl;
        }

        return sum / n;
    }

    public static double calculateExpectedValueBinned(List<Trade> closedTrades, int bins) {
        int n = closedTrades.size();
        if (n == 0)
            return 0.0;

        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        for (Trade t : closedTrades) {
            min = Math.min(min, t.pnl);
            max = Math.max(max, t.pnl);
        }

        if (min == max)
            return min; // degenerate case

        double width = (max - min) / bins;

        double[] count = new double[bins];
        double[] sum = new double[bins];

        for (Trade t : closedTrades) {
            int idx = (int) ((t.pnl - min) / width);
            if (idx == bins)
                idx = bins - 1; // edge fix

            count[idx] += 1;
            sum[idx] += t.pnl;
        }

        double ev = 0.0;

        for (int i = 0; i < bins; i++) {
            if (count[i] == 0)
                continue;

            double p = count[i] / n;
            double meanBin = sum[i] / count[i];

            ev += p * meanBin;
        }

        return ev;
    }

    public static double calculateModifiedSharpeRatio(List<Trade> trades) {
        return calculateModifiedSharpeRatio(trades, 0.0, 0.95);
    }

    public static double calculateModifiedSharpeRatio(List<Trade> trades, double riskFreePnlPerTrade, double confidenceLevel) {
        if (trades == null || trades.size() < 4) {
            throw new IllegalArgumentException("Need at least 4 trades.");
        }
        if (confidenceLevel <= 0.0 || confidenceLevel >= 1.0) {
            throw new IllegalArgumentException("confidenceLevel must be between 0 and 1.");
        }

        int n = trades.size();

        double sum = 0.0;
        for (Trade t : trades) {
            sum += t.pnl;
        }
        double mean = sum / n;

        double m2 = 0.0;
        double m3 = 0.0;
        double m4 = 0.0;

        for (Trade t : trades) {
            double d = t.pnl - mean;
            double d2 = d * d;
            m2 += d2;
            m3 += d2 * d;
            m4 += d2 * d2;
        }

        double variance = m2 / (n - 1.0);
        double std = Math.sqrt(variance);

        if (std == 0.0) {
            return Double.NaN;
        }

        // Moment-based skewness and excess kurtosis
        double skewness = (m3 / n) / Math.pow(std, 3);
        double excessKurtosis = (m4 / n) / Math.pow(std, 4) - 3.0;

        // Left-tail quantile for VaR
        double p = 1.0 - confidenceLevel;
        double z = utilityInvNormalCDF(p);

        // Cornish-Fisher expansion
        double z2 = z * z;
        double z3 = z2 * z;

        double modifiedZ =
                z
                + (1.0 / 6.0) * (z2 - 1.0) * skewness
                + (1.0 / 24.0) * (z3 - 3.0 * z) * excessKurtosis
                - (1.0 / 36.0) * (2.0 * z3 - 5.0 * z) * skewness * skewness;

        // Modified VaR in pnl units
        double modifiedVaR = -(mean + std * modifiedZ);

        if (modifiedVaR <= 0.0) {
            return Double.NaN;
        }

        double excessReturn = mean - riskFreePnlPerTrade;
        return excessReturn / modifiedVaR;
    }

    /**
     * Inverse standard normal CDF approximation.
     * Peter J. Acklam's approximation.
     */
    private static double utilityInvNormalCDF(double p) {
        if (p <= 0.0 || p >= 1.0) {
            throw new IllegalArgumentException("p must be in (0, 1)");
        }

        // Coefficients in rational approximations
        double[] a = {
                -3.969683028665376e+01,
                 2.209460984245205e+02,
                -2.759285104469687e+02,
                 1.383577518672690e+02,
                -3.066479806614716e+01,
                 2.506628277459239e+00
        };

        double[] b = {
                -5.447609879822406e+01,
                 1.615858368580409e+02,
                -1.556989798598866e+02,
                 6.680131188771972e+01,
                -1.328068155288572e+01
        };

        double[] c = {
                -7.784894002430293e-03,
                -3.223964580411365e-01,
                -2.400758277161838e+00,
                -2.549732539343734e+00,
                 4.374664141464968e+00,
                 2.938163982698783e+00
        };

        double[] d = {
                 7.784695709041462e-03,
                 3.224671290700398e-01,
                 2.445134137142996e+00,
                 3.754408661907416e+00
        };

        // Define break-points
        double plow = 0.02425;
        double phigh = 1.0 - plow;

        double q, r;

        if (p < plow) {
            q = Math.sqrt(-2.0 * Math.log(p));
            return (((((c[0] * q + c[1]) * q + c[2]) * q + c[3]) * q + c[4]) * q + c[5]) /
                   ((((d[0] * q + d[1]) * q + d[2]) * q + d[3]) * q + 1.0);
        }

        if (p > phigh) {
            q = Math.sqrt(-2.0 * Math.log(1.0 - p));
            return -(((((c[0] * q + c[1]) * q + c[2]) * q + c[3]) * q + c[4]) * q + c[5]) /
                    ((((d[0] * q + d[1]) * q + d[2]) * q + d[3]) * q + 1.0);
        }

        q = p - 0.5;
        r = q * q;

        return (((((a[0] * r + a[1]) * r + a[2]) * r + a[3]) * r + a[4]) * r + a[5]) * q /
               (((((b[0] * r + b[1]) * r + b[2]) * r + b[3]) * r + b[4]) * r + 1.0);
    }
}
