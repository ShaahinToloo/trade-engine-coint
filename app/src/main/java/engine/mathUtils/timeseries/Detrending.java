package engine.mathUtils.timeseries;

public class Detrending {
    /**
     * detrend a matrix x of time-series using regression
     * of x against a polynomial time trend of order p (which p = 0)
     * 
     * @param x
     * @return
     */
    public static double[][] detrend(double[][] x) {
        int n = x.length;
        int m = x[0].length;

        double[][] resid = new double[n][m];
        double[] mean = new double[m];

        // column means = β
        for (int j = 0; j < m; j++) {
            double sum = 0.0;
            for (int i = 0; i < n; i++) {
                sum += x[i][j];
            }
            mean[j] = sum / n; // β_j
        }

        // subtract β
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                resid[i][j] = x[i][j] - mean[j];
            }
        }

        return resid;
    }
}
