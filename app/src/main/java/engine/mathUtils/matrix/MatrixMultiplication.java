package engine.mathUtils.matrix;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

public class MatrixMultiplication {
    /**
     * 
     * @param A the 2D matrix
     * @param n if you want division, just pass 1.0/scalarVal
     * @return element-wise divided matrix
     */
    public static double[][] dotProduct(double[][] A, double n) {
        return new Array2DRowRealMatrix(A, false)
                .scalarMultiply(n)
                .getData();
    }

    public static double[][] matrix(double[][] A, double[][] B) {
        RealMatrix matrixA = new Array2DRowRealMatrix(A);
        RealMatrix matrixB = new Array2DRowRealMatrix(B);
        RealMatrix muled = matrixA.multiply(matrixB);
        return muled.getData();
    }
}
