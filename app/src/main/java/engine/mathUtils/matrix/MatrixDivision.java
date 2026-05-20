package engine.mathUtils.matrix;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

public class MatrixDivision {
    /**
     * Performs A / B == (B' \ A')'
     * <p>
     * Uses QRDecomposition
     * </p>
     * @param A
     * @param B
     * @return
     */
    public static double[][] rightDiv(double[][] A, double[][] B) {
        RealMatrix matrixA = new Array2DRowRealMatrix(A, false);
        RealMatrix matrixB = new Array2DRowRealMatrix(B, false);

        // MATLAB A / B == (B' \ A')'
        RealMatrix matrixB_T = matrixB.transpose();
        RealMatrix matrixA_T = matrixA.transpose();

        QRDecomposition qrDecomposition = new QRDecomposition(matrixB_T);
        DecompositionSolver solver = qrDecomposition.getSolver();

        RealMatrix matrixY = solver.solve(matrixA_T);

        RealMatrix matrixX = matrixY.transpose();

        return matrixX.getData();
    }

    /**
     * Performs A \ B
     * <p>
     * Uses QRDecomposition
     * </p>
     * @param A
     * @param B
     * @return
     */
    public static double[][] leftDiv(double[][] A, double[][] B) {
        RealMatrix matrix1 = new Array2DRowRealMatrix(A);
        RealMatrix matrix2 = new Array2DRowRealMatrix(B);

        QRDecomposition qrDecomposition = new QRDecomposition(matrix1);
        DecompositionSolver solver = qrDecomposition.getSolver();

        RealMatrix resultMatrixX = solver.solve(matrix2);

        return resultMatrixX.getData();
    }
}
