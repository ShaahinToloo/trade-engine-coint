package engine.mathUtils.matrix;

public class MatrixOps {
    /**
     * Shifts a matrix vertically by {@code k} rows.
     *
     * <p>
     * Positive lag values shift the matrix downward
     * (past observations), while negative lag values
     * shift the matrix upward (future observations / lead).
     * </p>
     *
     * <p>
     * Values shifted outside matrix bounds are replaced with {@code 0.0}.
     * </p>
     *
     * <p>
     * Examples:
     * </p>
     *
     * <ul>
     * <li>{@code k = 1} → first row becomes zeros</li>
     * <li>{@code k = -1} → last row becomes zeros</li>
     * </ul>
     *
     * @param A input matrix
     * @param k lag amount
     *
     * @return lagged matrix
     */
    public static double[][] lag(double[][] A, int k) {
        double[][] out = new double[A.length][A[0].length];

        for (int j = 0; j < out[0].length; j++) {
            for (int i = 0; i < out.length; i++) {
                int srcIdx = i - k;

                if (srcIdx < 0 || srcIdx >= A.length) {
                    out[i][j] = 0;
                    continue;
                }

                out[i][j] = A[srcIdx][j];
            }
        }

        return out;
    }

    /**
     * Generates a single-lag version of a matrix.
     *
     * <p>
     * Each column is shifted downward by one row.
     * The first row is filled with {@code 0.0}.
     * </p>
     *
     * <p>
     * Primarily used in VAR/VECM and time-series routines.
     * </p>
     *
     * @param x input matrix
     *
     * @return matrix lagged by one observation
     */
    public static double[][] mlag(double[][] x) {
        /*
         * Matlab Code:
         * [rw, m] = size(x);
         * 
         * xlag = zeros(rw,m);
         * icnt = 0;
         * for i=1:m;
         * xlag(1+1:rw,icnt+1) = x(1:rw-1,i);
         * icnt = icnt+n;
         * end;
         */

        int n = x.length, m = x[0].length;

        double[][] xlag = new double[n][m]; // init = 0

        for (int j = 0; j < m; j++) {
            for (int i = 1; i < n; i++) {
                xlag[i][j] = x[i - 1][j];
            }
        }

        return xlag;
    }

    /**
     * Removes rows from the beginning and end of a matrix.
     *
     * <p>
     * Equivalent to MATLAB:
     * </p>
     *
     * <pre>
     * x(n1 : end - n2, :)
     * </pre>
     *
     * <p>
     * Useful for aligning lagged and differenced matrices
     * in econometric and time-series calculations.
     * </p>
     *
     * @param x  input matrix
     * @param n1 number of rows to remove from the start
     * @param n2 number of rows to remove from the end
     *
     * @return trimmed matrix
     *
     * @throws IllegalArgumentException
     *                                  if trim sizes are invalid or remove all rows
     */
    public static double[][] trimr(double[][] x, int n1, int n2) {
        /*
         * [n junk] = size(x);
         * h1 = n1+1;
         * h2 = n-n2;
         * z = x(h1:h2,:);
         */
        int n = x.length;
        int h1 = n1;
        int h2 = n - n2;

        if ((n1 + n2) >= n)
            throw new IllegalArgumentException(String.format("n1 %d n2 %d n %d", n1, n2, n));

        if (h1 < 0 || h2 > n || h1 > h2)
            throw new IllegalArgumentException("Invalid trim indices");

        double[][] z = new double[h2 - h1][x[0].length];

        for (int i = h1; i < h2; i++) {
            for (int j = 0; j < x[i].length; j++) {
                z[i - h1][j] = x[i][j];
            }
        }

        return z;
    }
}
