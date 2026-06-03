package engine.mathUtils.matrix;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class MatrixMultiplicationTest {
    private static final double EPS = 1e-8;

    @Test
    void testMatmulBasic() {
        double[][] A = {
                { 1, 2 },
                { 3, 4 }
        };

        double[][] B = {
                { 5, 6 },
                { 7, 8 }
        };

        double[][] C = MatrixMultiplication.matrix(A, B);

        RealMatrix A_mat = new Array2DRowRealMatrix(A);
        RealMatrix B_mat = new Array2DRowRealMatrix(B);
        RealMatrix expected = A_mat.multiply(B_mat);

        for (int i = 0; i < C.length; i++) {
            for (int j = 0; j < C[0].length; j++) {
                assertEquals(expected.getEntry(i, j), C[i][j], EPS);
            }
        }
    }

    @Test
    void testMatmulIdentity() {
        double[][] A = {
                { 2, 3 },
                { 4, 5 }
        };

        double[][] I = {
                { 1, 0 },
                { 0, 1 }
        };

        double[][] C = MatrixMultiplication.matrix(A, I);

        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[0].length; j++) {
                assertEquals(A[i][j], C[i][j], EPS);
            }
        }
    }

    @Test
    void testMatmulNonSquare() {
        double[][] A = {
                { 1, 2, 3 },
                { 4, 5, 6 }
        };

        double[][] B = {
                { 1, 2 },
                { 3, 4 },
                { 5, 6 }
        };

        double[][] C = MatrixMultiplication.matrix(A, B);

        RealMatrix expected = new Array2DRowRealMatrix(A)
                .multiply(new Array2DRowRealMatrix(B));

        for (int i = 0; i < C.length; i++) {
            for (int j = 0; j < C[0].length; j++) {
                assertEquals(expected.getEntry(i, j), C[i][j], EPS);
            }
        }
    }

}
