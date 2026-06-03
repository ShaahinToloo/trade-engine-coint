package engine.mathUtils.decomposition.eigenUtils;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import engine.mathUtils.decomposition.choleskyUtils.Cholesky;
import engine.mathUtils.matrix.MatrixMultiplication;
import engine.mathUtils.matrix.MatrixTransform;

public class Eigen {
    public static Object[] eig(double[][] X) {
        RealMatrix matrix = new Array2DRowRealMatrix(X, false);
        EigenDecomposition ed = new EigenDecomposition(matrix);

        double[] eigenvalues = ed.getRealEigenvalues();
        RealMatrix V = ed.getV();

        int n = eigenvalues.length;

        double[][] D = new double[n][n];
        for (int i = 0; i < n; i++) {
            D[i][i] = eigenvalues[i];
        }

        return new Object[] {
                V.getData(),
                D
        };
    }

    public static double[][] normalizeEigenvectors(double[][] du, double[][] skk) {
        double[][] M = MatrixMultiplication.matrix(MatrixMultiplication.matrix(MatrixTransform.transpose(du), skk), du);

        double[][] R = Cholesky.choleskyDecomposition(M); // upper triangular
        double[][] Rinv = MatrixTransform.inv(R);

        return MatrixMultiplication.matrix(du, Rinv);
    }
}
