package engine.mathUtils.timeseries;

public class Differencing {
    /**
     * produce matrix differences by 1
     * 
     * @param x
     * @return
     */
    public static double[][] tdiff(double[][] x) {
        /*
         * dmat = zeros(nobs,nvar);
         * dmat(2:nobs,:) = x(2:nobs,:)-x(1:nobs-1,:);
         */

        double[][] dmat = new double[x.length][x[0].length];

        for (int j = 0; j < dmat[0].length; j++) {
            for (int i = 1; i < dmat.length; i++) {
                dmat[i][j] = x[i][j] - x[i - 1][j];
            }
        }

        return dmat;
    }
}
