package engine.mathUtils.matrix;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

public class MatrixArithmetic {
    public static double[][] matAdd(double[][] A, double[][] B) {
        RealMatrix mA = new Array2DRowRealMatrix(A, false);
        RealMatrix mB = new Array2DRowRealMatrix(B, false);

        RealMatrix result = mA.add(mB);

        return result.getData();
    }

    public static double[][] matSub(double[][] A, double[][] B) {
        RealMatrix mA = new Array2DRowRealMatrix(A, false);
        RealMatrix mB = new Array2DRowRealMatrix(B, false);

        RealMatrix result = mA.subtract(mB);

        return result.getData();
    }
}
