package engine.mathUtils.matrix;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class MatrixTransformTest {
    private static final double EPS = 1e-8;

    @Test
    void testDiagSquareMatrix() {
        double[][] A = {
                { 1, 2, 3 },
                { 4, 5, 6 },
                { 7, 8, 9 }
        };

        double[] d = MatrixTransform.diag(A);

        assertEquals(3, d.length);
        assertEquals(1.0, d[0], EPS);
        assertEquals(5.0, d[1], EPS);
        assertEquals(9.0, d[2], EPS);
    }

    @Test
    void testDiagRectangularMoreRows() {
        double[][] A = {
                { 1, 2 },
                { 3, 4 },
                { 5, 6 }
        };

        double[] d = MatrixTransform.diag(A);

        assertEquals(2, d.length);
        assertEquals(1.0, d[0], EPS);
        assertEquals(4.0, d[1], EPS);
    }

    @Test
    void testDiagRectangularMoreCols() {
        double[][] A = {
                { 7, 8, 9 },
                { 1, 2, 3 }
        };

        double[] d = MatrixTransform.diag(A);

        assertEquals(2, d.length);
        assertEquals(7.0, d[0], EPS);
        assertEquals(2.0, d[1], EPS);
    }

    @Test
    void testDiagSingleElement() {
        double[][] A = {
                { 42 }
        };

        double[] d = MatrixTransform.diag(A);

        assertEquals(1, d.length);
        assertEquals(42.0, d[0], EPS);
    }

    @Test
    void testDiagZeros() {
        double[][] A = {
                { 0, 1 },
                { 2, 0 }
        };

        double[] d = MatrixTransform.diag(A);

        assertEquals(2, d.length);
        assertEquals(0.0, d[0], EPS);
        assertEquals(0.0, d[1], EPS);
    }

    @Test
    void testTransposeBasic() {
        double[][] A = {
                { 1, 2, 3 },
                { 4, 5, 6 }
        };

        double[][] T = MatrixTransform.transpose(A);

        assertEquals(3, T.length);
        assertEquals(2, T[0].length);

        assertEquals(1, T[0][0], EPS);
        assertEquals(4, T[0][1], EPS);
        assertEquals(2, T[1][0], EPS);
        assertEquals(5, T[1][1], EPS);
        assertEquals(3, T[2][0], EPS);
        assertEquals(6, T[2][1], EPS);
    }

    @Test
    void testTransposeDoubleTransposeIdentity() {
        double[][] A = {
                { 3, 7 },
                { 2, 5 }
        };

        double[][] TT = MatrixTransform.transpose(MatrixTransform.transpose(A));

        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[0].length; j++) {
                assertEquals(A[i][j], TT[i][j], EPS);
            }
        }
    }

    @Test
    void testInv2x2Matrix() {
        double[][] A = {
                { 4, 7 },
                { 2, 6 }
        };

        double[][] invData = MatrixTransform.inv(A);

        RealMatrix A_mat = new Array2DRowRealMatrix(A);
        RealMatrix A_inv = new Array2DRowRealMatrix(invData);

        RealMatrix identity = A_mat.multiply(A_inv);

        for (int i = 0; i < identity.getRowDimension(); i++) {
            for (int j = 0; j < identity.getColumnDimension(); j++) {
                if (i == j) {
                    assertEquals(1.0, identity.getEntry(i, j), EPS,
                            "Diagonal element not close to 1");
                } else {
                    assertEquals(0.0, identity.getEntry(i, j), EPS,
                            "Off-diagonal element not close to 0");
                }
            }
        }
    }

    @Test
    void testInvIdentityMatrix() {
        double[][] A = {
                { 1, 0, 0 },
                { 0, 1, 0 },
                { 0, 0, 1 }
        };

        double[][] invData = MatrixTransform.inv(A);

        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[0].length; j++) {
                assertEquals(A[i][j], invData[i][j], EPS,
                        "Inverse of identity should be identity");
            }
        }
    }

    @Test
    void testInv3x3Matrix() {
        double[][] A = {
                { 3, 0, 2 },
                { 2, 0, -2 },
                { 0, 1, 1 }
        };

        double[][] invData = MatrixTransform.inv(A);

        RealMatrix A_mat = new Array2DRowRealMatrix(A);
        RealMatrix A_inv = new Array2DRowRealMatrix(invData);

        RealMatrix identity = A_mat.multiply(A_inv);

        for (int i = 0; i < identity.getRowDimension(); i++) {
            for (int j = 0; j < identity.getColumnDimension(); j++) {
                if (i == j) {
                    assertEquals(1.0, identity.getEntry(i, j), EPS,
                            "3x3 diagonal mismatch");
                } else {
                    assertEquals(0.0, identity.getEntry(i, j), EPS,
                            "3x3 off-diagonal mismatch");
                }
            }
        }
    }

}
