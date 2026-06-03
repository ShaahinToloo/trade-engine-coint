package engine.mathUtils.normalization;

import java.util.Arrays;

import engine.constants.BacktestConstants;

public class VectorNormalization {

    public static double[] maxAbsNorm(double[] arr) {
        if (arr.length > 1) {
            double betaMax = Arrays.stream(arr).map(Math::abs).max().getAsDouble();
            if (!(Math.abs(betaMax) < BacktestConstants.EPSILON)) {
                for (int i = 0; i < arr.length; i++) {
                    arr[i] /= betaMax;
                }
            }
            return arr;
        }
        return arr;
    }
}
