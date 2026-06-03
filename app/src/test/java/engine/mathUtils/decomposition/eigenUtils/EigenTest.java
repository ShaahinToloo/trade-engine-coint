package engine.mathUtils.decomposition.eigenUtils;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class EigenTest {
    private static final double EPS = 1e-8;

    @Test
    void testEigSimpleMatrix() {
        double[][] A = {
                { 4, 2 },
                { 1, 3 }
        };

        Object[] result = Eigen.eig(A);

        double[][] Vdata = (double[][]) result[0];
        double[][] Ddata = (double[][]) result[1];

        RealMatrix A_mat = new Array2DRowRealMatrix(A);
        RealMatrix V = new Array2DRowRealMatrix(Vdata);
        RealMatrix D = new Array2DRowRealMatrix(Ddata);

        RealMatrix AV = A_mat.multiply(V);
        RealMatrix VD = V.multiply(D);

        for (int i = 0; i < AV.getRowDimension(); i++) {
            for (int j = 0; j < AV.getColumnDimension(); j++) {
                assertEquals(
                        AV.getEntry(i, j),
                        VD.getEntry(i, j),
                        EPS,
                        "A*V != V*D at [" + i + "," + j + "]");
            }
        }
    }

    @Test
    void testEigDiagonalMatrix() {
        double[][] A = {
                { 7, 0 },
                { 0, 2 }
        };

        Object[] result = Eigen.eig(A);

        double[][] Ddata = (double[][]) result[1];

        double ev1 = Ddata[0][0];
        double ev2 = Ddata[1][1];

        boolean ok = (Math.abs(ev1 - 7) < EPS && Math.abs(ev2 - 2) < EPS) ||
                (Math.abs(ev1 - 2) < EPS && Math.abs(ev2 - 7) < EPS);

        assertTrue(ok, "Eigenvalues do not match expected values");
    }

    @Test
    void testEigIdentityMatrix() {
        double[][] A = {
                { 1, 0 },
                { 0, 1 }
        };

        Object[] result = Eigen.eig(A);

        double[][] Ddata = (double[][]) result[1];

        assertEquals(1.0, Ddata[0][0], EPS);
        assertEquals(1.0, Ddata[1][1], EPS);
    }

    @Test
    void testEig3x3Matrix() {
        double[][] A = {
                { 6, 2, 1 },
                { 2, 3, 1 },
                { 1, 1, 1 }
        };

        Object[] result = Eigen.eig(A);

        double[][] Vdata = (double[][]) result[0];
        double[][] Ddata = (double[][]) result[1];

        RealMatrix A_mat = new Array2DRowRealMatrix(A);
        RealMatrix V = new Array2DRowRealMatrix(Vdata);
        RealMatrix D = new Array2DRowRealMatrix(Ddata);

        RealMatrix AV = A_mat.multiply(V);
        RealMatrix VD = V.multiply(D);

        for (int i = 0; i < AV.getRowDimension(); i++) {
            for (int j = 0; j < AV.getColumnDimension(); j++) {
                assertEquals(
                        AV.getEntry(i, j),
                        VD.getEntry(i, j),
                        EPS,
                        "3x3 decomposition failed at [" + i + "," + j + "]");
            }
        }
    }
}
