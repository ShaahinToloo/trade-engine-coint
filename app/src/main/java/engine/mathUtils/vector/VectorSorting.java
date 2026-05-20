package engine.mathUtils.vector;

import java.util.Arrays;
import java.util.Comparator;

public class VectorSorting {
    public static Object[] sortWithIndices(double[] x) {
        Integer[] idx = new Integer[x.length];

        for (int i = 0; i < x.length; i++) {
            idx[i] = i;
        }

        Arrays.sort(idx, Comparator.comparingDouble(i -> x[i]));

        double[] sorted = new double[x.length];
        int[] indices = new int[x.length];

        for (int i = 0; i < x.length; i++) {
            sorted[i] = x[idx[i]];
            indices[i] = idx[i];
        }

        return new Object[] { sorted, indices };
    }
}
