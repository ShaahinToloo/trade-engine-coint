package engine.mathUtils.decomposition.choleskyUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import engine.mathUtils.matrix.MatrixMultiplication;
import engine.mathUtils.matrix.MatrixTransform;

public class CholeskyTest {
    private static final double EPS = 1e-8;

    @Test
    void testCholBasic2x2() {
        double[][] A = {
                { 4, 2 },
                { 2, 3 }
        };

        double[][] R = Cholesky.choleskyDecomposition(A);

        // upper triangular checks
        assertEquals(2.0, R[0][0], EPS);
        assertEquals(1.0, R[0][1], EPS);
        assertEquals(0.0, R[1][0], EPS);
        assertEquals(Math.sqrt(2), R[1][1], EPS);
    }

    @Test
    void testCholReconstruction() {
        double[][] A = {
                { 25, 15, -5 },
                { 15, 18, 0 },
                { -5, 0, 11 }
        };

        double[][] R = Cholesky.choleskyDecomposition(A);

        double[][] reconstructed = MatrixMultiplication.matrix(
                MatrixTransform.transpose(R),
                R);

        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[0].length; j++) {
                assertEquals(A[i][j], reconstructed[i][j], EPS);
            }
        }
    }

    @Test
    void testCholIdentity() {
        double[][] I = {
                { 1, 0 },
                { 0, 1 }
        };

        double[][] R = Cholesky.choleskyDecomposition(I);

        assertEquals(1.0, R[0][0], EPS);
        assertEquals(0.0, R[0][1], EPS);
        assertEquals(0.0, R[1][0], EPS);
        assertEquals(1.0, R[1][1], EPS);
    }

    @Test
    void testCholFailsForNonPositiveDefinite() {
        double[][] A = {
                { 1, 2 },
                { 2, 1 }
        };

        assertThrows(Exception.class, () -> Cholesky.choleskyDecomposition(A));
    }
}
