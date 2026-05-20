package engine.mathUtils.matrix;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class MatrixDivisionTest {
    private static final double EPS = 1e-8;

    @Test
    void testMatLeftDivSquareSystem() {
        double[][] A = {
                { 2, 1 },
                { 1, 3 }
        };

        double[][] B = {
                { 5 },
                { 6 }
        };

        double[][] X = MatrixDivision.leftDiv(A, B);

        RealMatrix A_mat = new Array2DRowRealMatrix(A);
        RealMatrix X_mat = new Array2DRowRealMatrix(X);
        RealMatrix B_expected = new Array2DRowRealMatrix(B);

        RealMatrix result = A_mat.multiply(X_mat);

        for (int i = 0; i < result.getRowDimension(); i++) {
            for (int j = 0; j < result.getColumnDimension(); j++) {
                assertEquals(
                        B_expected.getEntry(i, j),
                        result.getEntry(i, j),
                        EPS,
                        "matLeftDiv failed at [" + i + "," + j + "]");
            }
        }
    }

    @Test
    void testMatRightDivSquareSystem() {
        double[][] A = {
                { 5, 6 }
        };

        double[][] B = {
                { 2, 1 },
                { 1, 3 }
        };

        double[][] X = MatrixDivision.rightDiv(A, B);

        RealMatrix X_mat = new Array2DRowRealMatrix(X);
        RealMatrix B_mat = new Array2DRowRealMatrix(B);
        RealMatrix A_expected = new Array2DRowRealMatrix(A);

        RealMatrix result = X_mat.multiply(B_mat);

        for (int i = 0; i < result.getRowDimension(); i++) {
            for (int j = 0; j < result.getColumnDimension(); j++) {
                assertEquals(
                        A_expected.getEntry(i, j),
                        result.getEntry(i, j),
                        EPS,
                        "matRightDiv failed at [" + i + "," + j + "]");
            }
        }
    }

    @Test
    void testMatLeftDivIdentity() {
        double[][] A = {
                { 1, 0 },
                { 0, 1 }
        };

        double[][] B = {
                { 7 },
                { 9 }
        };

        double[][] X = MatrixDivision.leftDiv(A, B);

        assertEquals(7.0, X[0][0], EPS);
        assertEquals(9.0, X[1][0], EPS);
    }

    @Test
    void testMatRightDivIdentity() {
        double[][] A = {
                { 7, 9 }
        };

        double[][] B = {
                { 1, 0 },
                { 0, 1 }
        };

        double[][] X = MatrixDivision.rightDiv(A, B);

        assertEquals(7.0, X[0][0], EPS);
        assertEquals(9.0, X[0][1], EPS);
    }

    @Test
    void testMatLeftDivRectangularLeastSquares() {
        double[][] A = {
                { 1, 1 },
                { 1, 2 },
                { 1, 3 }
        };

        double[][] B = {
                { 1 },
                { 2 },
                { 2 }
        };

        double[][] X = MatrixDivision.leftDiv(A, B);

        // expected regression approx: intercept ~0.6667, slope ~0.5
        assertEquals(0.6667, X[0][0], 1e-3);
        assertEquals(0.5, X[1][0], 1e-3);
    }

}
