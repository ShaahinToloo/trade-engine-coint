package engine.mathUtils.matrix;

import java.lang.reflect.Array;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.QRDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

public class MatrixTransform {
    public static Object flipud1D(Object array) {
        int n = Array.getLength(array);

        Object out = Array.newInstance(array.getClass().getComponentType(), n);

        for (int i = 0; i < n; i++) {
            Array.set(out, i, Array.get(array, n - 1 - i));
        }

        return out;
    }

    public static Object flipud2D(Object matrix) {
        int rows = Array.getLength(matrix);

        Object out = Array.newInstance(
                matrix.getClass().getComponentType(),
                rows);

        for (int i = 0; i < rows; i++) {
            Object row = Array.get(matrix, rows - 1 - i);

            Object rowCopy = Array.newInstance(
                    row.getClass().getComponentType(),
                    Array.getLength(row));

            for (int j = 0; j < Array.getLength(row); j++) {
                Array.set(rowCopy, j, Array.get(row, j));
            }

            Array.set(out, i, rowCopy);
        }

        return out;
    }

    public static double[] diag(double[][] A) {
        int n = Math.min(A.length, A[0].length);
        double[] d = new double[n];

        for (int i = 0; i < n; i++) {
            d[i] = A[i][i];
        }

        return d;
    }

    public static double[][] symmetrize(double[][] A) {
        int n = A.length;
        double[][] S = new double[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                S[i][j] = 0.5 * (A[i][j] + A[j][i]);
            }
        }
        return S;
    }

    public static double[][] transpose(double[][] A) {
        RealMatrix matrixA = new Array2DRowRealMatrix(A);
        RealMatrix transposed = matrixA.transpose();
        return transposed.getData();
    }

    public static double[][] inv(double[][] X) {
        RealMatrix matrix = new Array2DRowRealMatrix(X, false); // 'false' means don't copy array
        QRDecomposition qrDecomposition = new QRDecomposition(matrix);
        RealMatrix inverseMatrix = qrDecomposition.getSolver().getInverse();
        return inverseMatrix.getData();
    }
}
