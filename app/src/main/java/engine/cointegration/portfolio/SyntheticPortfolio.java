package engine.cointegration.portfolio;

import engine.cointegration.johansen.JohansenResult;

public class SyntheticPortfolio {
    /**
     * <p>
     * single price (no spread) yPort
     * </>
     * Can be Cached, needs boolean isCache, double[] prev_yPort to be added to
     * arguments.
     * 1. Shift the given prev_yPort (WE DO NOT MAKE A NEW ARRAY) by 1, so the last
     * element is empty; Also you can use RingBuffer for O(1) notation, but using
     * that for all calculations requires heavy changes in the project
     * 2. calculate only for the last element and add it to prev_yPort;
     * <Return> prev_yPort
     */
    public static double[] yPort(JohansenResult jr, double[][] x, double[] beta) {
        // Compute yPort
        double[] yPort = new double[x.length];
        double[] var1 = new double[x[0].length];
        for (int i = 0; i < yPort.length; i++) {

            for (int j = 0; j < x[0].length; j++) {
                var1[j] = beta[j] * x[i][j];
            }

            double var2 = 0.0;
            for (int j = 0; j < var1.length; j++) {
                var2 += var1[j];
            }

            yPort[i] = var2;
        }
        return yPort;
    }

    /**
     * 
     * @param jr
     * @param xBid
     * @param spreads
     * @return [0] == Bid && [1] == Ask
     */
    public static double[][] yPort(JohansenResult jr, double[][] xBid, double[] spreads, double[] beta) {
        // Compute yPort
        double[][] xAsk = SyntheticPortfolio.computeAskPrice(xBid, spreads);

        double[] yPortAsk = SyntheticPortfolio.yPortAsk(beta, xAsk, xBid);
        double[] yPortBid = SyntheticPortfolio.yPortBid(beta, xAsk, xBid);

        // int rows = yPortAsk.length;
        // int cols = 2;
        // double[][] yPort = new double[rows][cols];

        // for (int i = 0; i < rows; i++) {
        //     yPort[i][0] = yPortBid[i];
        //     yPort[i][1] = yPortAsk[i];
        // }

        return new double[][] { yPortBid, yPortAsk };
    }

    private static double[][] computeAskPrice(double[][] xBid, double[] spreads) {
        int rows = xBid.length;
        int cols = xBid[0].length;

        double[][] xAsk = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            double[] bidRow = xBid[i];
            double[] askRow = xAsk[i];

            for (int j = 0; j < cols; j++) {
                askRow[j] = bidRow[j] + spreads[j];
            }
        }

        return xAsk;
    }

    private static double[] yPortAsk(double[] beta, double[][] xAsk, double[][] xBid) {
        int rows = xBid.length;
        int cols = xBid[0].length;

        double[] yPort = new double[rows];
        double[] var1 = new double[cols];
        for (int i = 0; i < rows; i++) {

            for (int j = 0; j < cols; j++) {
                double cBeta = beta[j];
                var1[j] = cBeta * ((cBeta >= 0) ? xAsk[i][j] : xBid[i][j]);
            }

            double var2 = 0.0;
            for (int j = 0; j < cols; j++) {
                var2 += var1[j];
            }

            yPort[i] = var2;
        }
        return yPort;
    }

    private static double[] yPortBid(double[] beta, double[][] xAsk, double[][] xBid) {
        int rows = xBid.length;
        int cols = xBid[0].length;

        double[] yPort = new double[rows];
        double[] var1 = new double[cols];
        for (int i = 0; i < rows; i++) {

            for (int j = 0; j < cols; j++) {
                double cBeta = beta[j];
                var1[j] = cBeta * ((cBeta >= 0) ? xBid[i][j] : xAsk[i][j]);
            }

            double var2 = 0.0;
            for (int j = 0; j < cols; j++) {
                var2 += var1[j];
            }

            yPort[i] = var2;
        }
        return yPort;
    }
}
