package engine.mathUtils.timeseries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class DifferencingTest {
    private static final double EPS = 1e-8;

    @Test
    void testTdiffSimpleVector() {
        double[][] x = {
                { 1 },
                { 2 },
                { 4 },
                { 7 }
        };

        double[][] d = Differencing.tdiff(x);

        // expected:
        // [0, 1, 2, 3]
        assertEquals(0.0, d[0][0], EPS);
        assertEquals(1.0, d[1][0], EPS);
        assertEquals(2.0, d[2][0], EPS);
        assertEquals(3.0, d[3][0], EPS);
    }

    @Test
    void testTdiffMultiColumn() {
        double[][] x = {
                { 1, 10 },
                { 3, 20 },
                { 6, 35 }
        };

        double[][] d = Differencing.tdiff(x);

        // col 1: [0, 2, 3]
        assertEquals(0.0, d[0][0], EPS);
        assertEquals(2.0, d[1][0], EPS);
        assertEquals(3.0, d[2][0], EPS);

        // col 2: [0, 10, 15]
        assertEquals(0.0, d[0][1], EPS);
        assertEquals(10.0, d[1][1], EPS);
        assertEquals(15.0, d[2][1], EPS);
    }

    @Test
    void testTdiffZeroFirstRow() {
        double[][] x = {
                { 5, 5 },
                { 5, 5 },
                { 5, 5 }
        };

        double[][] d = Differencing.tdiff(x);

        // all differences should be zero
        for (int j = 0; j < x[0].length; j++) {
            for (int i = 1; i < x.length; i++) {
                assertEquals(0.0, d[i][j], EPS);
            }
        }
    }

    @Test
    void testTdiffMatchesManualComputation() {
        double[][] x = {
                { 2, 3 },
                { 5, 7 },
                { 9, 11 }
        };

        double[][] d = Differencing.tdiff(x);

        double[][] expected = {
                { 0, 0 },
                { 3, 4 },
                { 4, 4 }
        };

        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < x[0].length; j++) {
                assertEquals(expected[i][j], d[i][j], EPS);
            }
        }
    }
}
