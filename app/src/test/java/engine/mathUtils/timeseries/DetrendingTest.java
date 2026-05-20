package engine.mathUtils.timeseries;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class DetrendingTest {
    private static final double EPS = 1e-8;

    @Test
    void testDetrendSingleColumn() {
        double[][] X = {
                { 1 },
                { 2 },
                { 3 }
        };

        double[][] R = Detrending.detrend(X);

        // mean = 2 → residual = [-1, 0, 1]
        assertEquals(-1.0, R[0][0], EPS);
        assertEquals(0.0, R[1][0], EPS);
        assertEquals(1.0, R[2][0], EPS);
    }

    @Test
    void testDetrendMultiColumn() {
        double[][] X = {
                { 1, 10 },
                { 2, 20 },
                { 3, 30 }
        };

        double[][] R = Detrending.detrend(X);

        // col1 mean = 2 → [-1,0,1]
        assertEquals(-1.0, R[0][0], EPS);
        assertEquals(0.0, R[1][0], EPS);
        assertEquals(1.0, R[2][0], EPS);

        // col2 mean = 20 → [-10,0,10]
        assertEquals(-10.0, R[0][1], EPS);
        assertEquals(0.0, R[1][1], EPS);
        assertEquals(10.0, R[2][1], EPS);
    }

    @Test
    void testDetrendMeanIsZero() {
        double[][] X = {
                { 4, 7 },
                { 6, 9 },
                { 8, 11 }
        };

        double[][] R = Detrending.detrend(X);

        int cols = X[0].length;

        for (int j = 0; j < cols; j++) {
            double sum = 0;
            for (int i = 0; i < X.length; i++) {
                sum += R[i][j];
            }
            assertEquals(0.0, sum, EPS, "Column mean not zero");
        }
    }

    @Test
    void testDetrendMatchesExplicitMeanSubtraction() {
        double[][] X = {
                { 3, 5 },
                { 6, 7 },
                { 9, 11 }
        };

        double[][] R = Detrending.detrend(X);

        RealMatrix M = new Array2DRowRealMatrix(X);
        double[][] expected = new double[X.length][X[0].length];

        for (int j = 0; j < X[0].length; j++) {
            double mean = (3 + 6 + 9) / 3.0; // column 1
            if (j == 1)
                mean = (5 + 7 + 11) / 3.0;

            for (int i = 0; i < X.length; i++) {
                expected[i][j] = X[i][j] - mean;
            }
        }

        for (int i = 0; i < X.length; i++) {
            for (int j = 0; j < X[0].length; j++) {
                assertEquals(expected[i][j], R[i][j], EPS);
            }
        }
    }
}
