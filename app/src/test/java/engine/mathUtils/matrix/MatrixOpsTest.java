package engine.mathUtils.matrix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class MatrixOpsTest {
    private static final double EPS = 1e-8;

    @Test
    void testLagK1BasicVector() {
        double[][] x = {
                { 1 },
                { 2 },
                { 3 },
                { 4 }
        };

        double[][] lag = MatrixOps.lag(x, 1);

        assertEquals(0.0, lag[0][0], EPS);
        assertEquals(1.0, lag[1][0], EPS);
        assertEquals(2.0, lag[2][0], EPS);
        assertEquals(3.0, lag[3][0], EPS);
    }

    @Test
    void testLagK2BasicVector() {
        double[][] x = {
                { 10 },
                { 20 },
                { 30 },
                { 40 }
        };

        double[][] lag = MatrixOps.lag(x, 2);

        assertEquals(0.0, lag[0][0], EPS);
        assertEquals(0.0, lag[1][0], EPS);
        assertEquals(10.0, lag[2][0], EPS);
        assertEquals(20.0, lag[3][0], EPS);
    }

    @Test
    void testLagMultiColumn() {
        double[][] x = {
                { 1, 10 },
                { 2, 20 },
                { 3, 30 },
                { 4, 40 }
        };

        double[][] lag = MatrixOps.lag(x, 1);

        // first row zeros
        assertEquals(0.0, lag[0][0], EPS);
        assertEquals(0.0, lag[0][1], EPS);

        // shifted values
        assertEquals(1.0, lag[1][0], EPS);
        assertEquals(10.0, lag[1][1], EPS);

        assertEquals(2.0, lag[2][0], EPS);
        assertEquals(20.0, lag[2][1], EPS);
    }

    @Test
    void testLagZeroKIdentity() {
        double[][] x = {
                { 1, 2 },
                { 3, 4 }
        };

        double[][] lag = MatrixOps.lag(x, 0);

        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[0].length; j++) {
                assertEquals(x[i][j], lag[i][j], EPS);
            }
        }
    }

    @Test
    void testLagFullShiftBecomesZero() {
        double[][] x = {
                { 5 },
                { 6 },
                { 7 }
        };

        double[][] lag = MatrixOps.lag(x, 3);

        for (int i = 0; i < x.length; i++) {
            assertEquals(0.0, lag[i][0], EPS);
        }
    }

    @Test
    void testLagShapePreserved() {
        double[][] x = {
                { 1, 2, 3 },
                { 4, 5, 6 }
        };

        double[][] lag = MatrixOps.lag(x, 1);

        assertEquals(x.length, lag.length);
        assertEquals(x[0].length, lag[0].length);
    }

    @Test
    void testMlagBasicVector() {
        double[][] x = {
                { 1 },
                { 2 },
                { 3 },
                { 4 }
        };

        double[][] lag = MatrixOps.mlag(x);

        // first row = 0
        assertEquals(0.0, lag[0][0], EPS);

        // shifted values
        assertEquals(1.0, lag[1][0], EPS);
        assertEquals(2.0, lag[2][0], EPS);
        assertEquals(3.0, lag[3][0], EPS);
    }

    @Test
    void testMlagMultiColumn() {
        double[][] x = {
                { 1, 10 },
                { 2, 20 },
                { 3, 30 }
        };

        double[][] lag = MatrixOps.mlag(x);

        // row 0 = zeros
        assertEquals(0.0, lag[0][0], EPS);
        assertEquals(0.0, lag[0][1], EPS);

        // lagged values
        assertEquals(1.0, lag[1][0], EPS);
        assertEquals(10.0, lag[1][1], EPS);

        assertEquals(2.0, lag[2][0], EPS);
        assertEquals(20.0, lag[2][1], EPS);
    }

    @Test
    void testMlagConstantSeries() {
        double[][] x = {
                { 5 },
                { 5 },
                { 5 }
        };

        double[][] lag = MatrixOps.mlag(x);

        assertEquals(0.0, lag[0][0], EPS);
        assertEquals(5.0, lag[1][0], EPS);
        assertEquals(5.0, lag[2][0], EPS);
    }

    @Test
    void testMlagShapePreserved() {
        double[][] x = {
                { 1, 2, 3 },
                { 4, 5, 6 }
        };

        double[][] lag = MatrixOps.mlag(x);

        assertEquals(x.length, lag.length);
        assertEquals(x[0].length, lag[0].length);
    }

    @Test
    void testTrimrBasic() {
        double[][] x = {
                { 1 },
                { 2 },
                { 3 },
                { 4 },
                { 5 }
        };

        double[][] z = MatrixOps.trimr(x, 1, 1);

        // expected: rows 2..4 → [2,3,4]
        assertEquals(3, z.length);
        assertEquals(1, z[0].length);

        assertEquals(2.0, z[0][0], EPS);
        assertEquals(3.0, z[1][0], EPS);
        assertEquals(4.0, z[2][0], EPS);
    }

    @Test
    void testTrimrMultiColumn() {
        double[][] x = {
                { 1, 10 },
                { 2, 20 },
                { 3, 30 },
                { 4, 40 }
        };

        double[][] z = MatrixOps.trimr(x, 1, 1);

        assertEquals(2, z.length);
        assertEquals(2, z[0].length);

        // row 1 → [2,20]
        assertEquals(2.0, z[0][0], EPS);
        assertEquals(20.0, z[0][1], EPS);

        // row 2 → [3,30]
        assertEquals(3.0, z[1][0], EPS);
        assertEquals(30.0, z[1][1], EPS);
    }

    @Test
    void testTrimrNoOpCase() {
        double[][] x = {
                { 1, 2 },
                { 3, 4 },
                { 5, 6 }
        };

        double[][] z = MatrixOps.trimr(x, 0, 0);

        assertEquals(3, z.length);

        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[0].length; j++) {
                assertEquals(x[i][j], z[i][j], EPS);
            }
        }
    }

    @Test
    void testTrimrEdgeSingleRow() {
        double[][] x = {
                { 1 },
                { 2 },
                { 3 }
        };

        double[][] z = MatrixOps.trimr(x, 2, 0);

        // keeps only middle row
        assertEquals(1, z.length);
        assertEquals(3.0, z[0][0], EPS);
    }

    @Test
    void testTrimrThrowsOnTooMuchTrim() {
        double[][] x = {
                { 1 },
                { 2 },
                { 3 }
        };

        assertThrows(IllegalArgumentException.class, () -> {
            MatrixOps.trimr(x, 2, 2);
        });
    }
}
