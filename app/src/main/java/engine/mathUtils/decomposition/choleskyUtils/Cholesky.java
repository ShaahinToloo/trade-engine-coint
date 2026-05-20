package engine.mathUtils.decomposition.choleskyUtils;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.CholeskyDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import engine.mathUtils.matrix.MatrixTransform;

public class Cholesky {
    public static double[][] choleskyDecomposition(double[][] A) {
        A = MatrixTransform.symmetrize(A);

        RealMatrix matrix = new Array2DRowRealMatrix(A);
        CholeskyDecomposition chol = new CholeskyDecomposition(matrix);

        // MATLAB chol(A) returns upper triangular R such that A = R' * R
        RealMatrix R = chol.getL().transpose();

        return R.getData();
    }
}
