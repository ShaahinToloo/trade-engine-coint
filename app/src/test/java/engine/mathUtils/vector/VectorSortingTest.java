package engine.mathUtils.vector;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class VectorSortingTest {
    private static final double EPS = 1e-8;

    @Test
    void testSortBasic() {
        double[] x = { 3.0, 1.0, 2.0 };

        Object[] out = VectorSorting.sortWithIndices(x);

        double[] sorted = (double[]) out[0];
        int[] indices = (int[]) out[1];

        assertArrayEquals(new double[] { 1.0, 2.0, 3.0 }, sorted, EPS);
        assertArrayEquals(new int[] { 1, 2, 0 }, indices);
    }

    @Test
    void testSortAlreadySorted() {
        double[] x = { 1.0, 2.0, 3.0 };

        Object[] out = VectorSorting.sortWithIndices(x);

        double[] sorted = (double[]) out[0];
        int[] indices = (int[]) out[1];

        assertArrayEquals(new double[] { 1.0, 2.0, 3.0 }, sorted, EPS);
        assertArrayEquals(new int[] { 0, 1, 2 }, indices);
    }

    @Test
    void testSortReverseOrder() {
        double[] x = { 5.0, 4.0, 3.0, 2.0 };

        Object[] out = VectorSorting.sortWithIndices(x);

        double[] sorted = (double[]) out[0];
        int[] indices = (int[]) out[1];

        assertArrayEquals(new double[] { 2.0, 3.0, 4.0, 5.0 }, sorted, EPS);
        assertArrayEquals(new int[] { 3, 2, 1, 0 }, indices);
    }

    @Test
    void testSortWithDuplicates() {
        double[] x = { 2.0, 1.0, 2.0 };

        Object[] out = VectorSorting.sortWithIndices(x);

        double[] sorted = (double[]) out[0];
        int[] indices = (int[]) out[1];

        assertArrayEquals(new double[] { 1.0, 2.0, 2.0 }, sorted, EPS);

        // first should be index of 1.0
        assertEquals(1, indices[0]);

        // remaining two should be indices of 2.0 values
        assertTrue(
                (indices[1] == 0 && indices[2] == 2) ||
                        (indices[1] == 2 && indices[2] == 0));
    }

    @Test
    void testSortSingleElement() {
        double[] x = { 42.0 };

        Object[] out = VectorSorting.sortWithIndices(x);

        double[] sorted = (double[]) out[0];
        int[] indices = (int[]) out[1];

        assertArrayEquals(new double[] { 42.0 }, sorted, EPS);
        assertArrayEquals(new int[] { 0 }, indices);
    }
}
