package engine.caster;

import java.util.List;

public class ObjectCaster {
    public static double[][][] toDouble3D(Object raw) {
        if (!(raw instanceof List)) {
            throw new IllegalArgumentException("Expected List.");
        }

        List<?> l1 = (List<?>) raw;
        if (l1.isEmpty())
            throw new IllegalArgumentException("l1 isEmpty.");

        List<?> firstLayer = (List<?>) l1.get(0);
        if (firstLayer.isEmpty())
            throw new IllegalArgumentException("firstLayer isEmpty.");

        List<?> firstRow = (List<?>) firstLayer.get(0);
        int d1 = l1.size();
        int d2 = firstLayer.size();
        int d3 = firstRow.size();

        double[][][] out = new double[d1][d2][d3];

        for (int i = 0; i < d1; i++) {
            List<?> layer = (List<?>) l1.get(i);
            if (layer.size() != d2) {
                throw new IllegalArgumentException("Inconsistent depth 2 at index " + i);
            }

            for (int j = 0; j < d2; j++) {
                List<?> row = (List<?>) layer.get(j);
                if (row.size() != d3) {
                    throw new IllegalArgumentException(
                            String.format("Jagged array at [%d][%d]: expected %d, got %d", i, j, d3, row.size()));
                }

                for (int k = 0; k < d3; k++) {
                    Object val = row.get(k);
                    if (val == null) {
                        throw new IllegalArgumentException("Null value at [" + i + "][" + j + "][" + k + "]");
                    }
                    out[i][j][k] = ((Number) val).doubleValue();
                }
            }
        }
        return out;
    }

    public static double[][] toDouble2D(Object raw) {
        if (!(raw instanceof List)) {
            throw new IllegalArgumentException("Expected List.");
        }

        List<?> l1 = (List<?>) raw;
        if (l1.isEmpty())
            throw new IllegalArgumentException("l1 isEmpty.");

        int d1 = l1.size();
        int d2 = ((List<?>) l1.get(0)).size();

        double[][] out = new double[d1][d2];

        for (int i = 0; i < d1; i++) {
            List<?> row = (List<?>) l1.get(i);
            if (row.size() != d2) {
                throw new IllegalArgumentException(
                        String.format("Jagged array detected at row %d: expected %d columns, got %d",
                                i, d2, row.size()));
            }
            for (int j = 0; j < d2; j++) {
                Object val = row.get(j);
                if (val == null) {
                    throw new IllegalArgumentException("Null value at [" + i + "][" + j + "]");
                }
                out[i][j] = ((Number) val).doubleValue();
            }
        }
        return out;
    }
}
