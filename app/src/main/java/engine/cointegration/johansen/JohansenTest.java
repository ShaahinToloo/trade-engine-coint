package engine.cointegration.johansen;

import engine.mathUtils.decomposition.eigenUtils.Eigen;
import engine.mathUtils.matrix.MatrixArithmetic;
import engine.mathUtils.matrix.MatrixDivision;
import engine.mathUtils.matrix.MatrixMultiplication;
import engine.mathUtils.matrix.MatrixOps;
import engine.mathUtils.matrix.MatrixTransform;
import engine.mathUtils.stats.JohansenCriticalValues;
import engine.mathUtils.timeseries.Detrending;
import engine.mathUtils.timeseries.Differencing;
import engine.mathUtils.vector.VectorSorting;

public class JohansenTest {
    public static JohansenResult johansen(double[][] x) {
        @SuppressWarnings("unused")
        int lag = 1;
        @SuppressWarnings("unused")
        int n = x.length;

        int m = x[0].length;

        /*
         * f = 0
         * p = 0
         * k = 1
         * MATLAB code:
         *     x = detrend(x,p);
         *     dx = tdiff(x,1);
         *     dx = trimr(dx,1,0);
         *     z = mlag(dx,k);
         *     z = detrend(trimr(z,k,0),f);
         *     dx = detrend(trimr(dx,k,0),f);
         *     r0t = dx - z*(z\dx);
         *     dx = detrend(trimr(lag(x,k),k+1,0),f);
         *     rkt = dx - z*(z\dx);
         *     skk = rkt'*rkt/rows(rkt);
         *     sk0 = rkt'*r0t/rows(rkt);
         *     s00 = r0t'*r0t/rows(r0t);
         *     sig = sk0*inv(s00)*(sk0');
         *     tmp = inv(skk);
         *     [du au] = eig(tmp*sig);
         *     orig = tmp*sig;
         *     dt = du*inv(chol(du'*skk*du));
         *     temp = inv(chol(du'*skk*du));
         *     dt = transpose(dt);
         *     [au auind] = sort(diag(au));
         *     a = flipud(au);
         *     aind = flipud(auind);
         *     d = dt(aind,:);
         *     d = transpose(d);
         *     test = transpose(d) * skk * d;
         *     lr1 = zeros(m,1);
         *     lr2 = zeros(m,1);
         *     cvm = zeros(m,3);
         *     cvt = zeros(m,3);
         *     iota = ones(m,1);
         *     [t junk] = size(rkt);
         *     for i=1:m;
         *     tmp = trimr(log(iota-a),i-1,0);
         *     lr1(i,1) = -t*sum(tmp);
         *     lr2(i,1) = -t*log(1-a(i,1));
         *     cvm(i,:) = c_sja(m-i+1,p);
         *     cvt(i,:) = c_sjt(m-i+1,p);
         *     aind(i) = i;
         *     end;
         *     
         *     % set up results structure
         *     result.eig = a;
         *     result.evec = d;
         *     result.lr1 = lr1;
         *     result.lr2 = lr2;
         *     result.cvt = cvt;
         *     result.cvm = cvm;
         *     result.ind = aind;
         *     result.meth = 'johansen';
         */

        x = Detrending.detrend(x);
        double[][] dx = Differencing.tdiff(x);
        dx = MatrixOps.trimr(dx, 1, 0);
        double[][] z = MatrixOps.mlag(dx);
        z = Detrending.detrend(MatrixOps.trimr(z, 1, 0));
        dx = Detrending.detrend(MatrixOps.trimr(dx, 1, 0));

        double[][] r0t = MatrixArithmetic.matSub(dx, MatrixMultiplication.matrix(z, MatrixDivision.leftDiv(z, dx)));
        dx = Detrending.detrend(MatrixOps.trimr(MatrixOps.lag(x, 1), 2, 0));

        double[][] rkt = MatrixArithmetic.matSub(dx, MatrixMultiplication.matrix(z, MatrixDivision.leftDiv(z, dx)));

        double[][] skk = MatrixMultiplication.matrix(MatrixTransform.transpose(rkt), rkt);
        skk = MatrixMultiplication.dotProduct(skk, 1.0 / rkt.length);

        double[][] sk0 = MatrixMultiplication.matrix(MatrixTransform.transpose(rkt), r0t);
        sk0 = MatrixMultiplication.dotProduct(sk0, 1.0 / rkt.length);

        double[][] s00 = MatrixMultiplication.matrix(MatrixTransform.transpose(r0t), r0t);
        s00 = MatrixMultiplication.dotProduct(s00, 1.0 / r0t.length);

        double[][] sig = MatrixMultiplication.matrix(MatrixMultiplication.matrix(sk0, MatrixTransform.inv(s00)), MatrixTransform.transpose(sk0));
        double[][] tmp = MatrixTransform.inv(skk);

        Object[] eigOut = Eigen.eig(MatrixMultiplication.matrix(tmp, sig));
        double[][] du = (double[][]) eigOut[0];
        double[][] au = (double[][]) eigOut[1];

        // Unused
        // double[][] orig = matmul(tmp, sig);

        double[][] dt = Eigen.normalizeEigenvectors(du, skk);
        dt = MatrixTransform.transpose(dt);

        double[] diagVals = MatrixTransform.diag(au);

        Object[] sortedOut = VectorSorting.sortWithIndices(diagVals);

        double[] auSorted = (double[]) sortedOut[0];
        int[] auind = (int[]) sortedOut[1];

        double[] a = (double[]) MatrixTransform.flipud1D(auSorted);
        int[] aind = (int[]) MatrixTransform.flipud1D(auind);

        double[][] d = new double[aind.length][];
        for (int i = 0; i < aind.length; i++) {
            d[i] = dt[aind[i]];
        }

        d = MatrixTransform.transpose(d);

        /*
         * lr2 = zeros(m,1);
         * [t junk] = size(rkt);
         * for i=1:m;
         * lr2(i,1) = -t*log(1-a(i,1));
         */
        double[] lr2 = new double[m];
        int t = rkt.length;
        for (int i = 0; i < m; i++) {
            lr2[i] = -t * Math.log(1 - a[i]);
        }

        double[][] cvm = new double[m][];
        for (int i = 0; i < m; i++) {
            cvm[i] = JohansenCriticalValues.c_sja(m - (i + 1));
        }

        JohansenResult result = new JohansenResult();
        result.eig = a;
        result.evec = d;
        result.lr1 = null;
        result.lr2 = lr2;
        result.cvt = null;
        result.cvm = cvm;
        result.ind = aind;
        return result;
    }
}
