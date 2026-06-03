package engine.mathUtils.vector;

public class VectorOps {
    public static double[] ffillNaN(
            double[] x,
            boolean clone) {

        double[] arr = (clone) ? x.clone() : x;

        double last = Double.NaN;

        for (int i = 0; i < arr.length; i++) {

            if (Double.isNaN(arr[i])) {

                if (!Double.isNaN(last)) {
                    arr[i] = last;
                }

            } else {

                last = arr[i];
            }
        }

        return arr;
    }

    /**
     * Replaces all occurrences of a target value in an array.
     *
     * <p>
     * Useful for replacing NaN placeholders or sentinel values.
     * </p>
     *
     * @param arr      input array (modified in-place)
     * @param target value to replace
     * @param fill   replacement value
     */
    public static double[] fillXwithY(
            double[] x,
            double target,
            double fill,
            boolean clone) {

        double[] arr = (clone) ? x.clone() : x;

        for (int i = 0; i < arr.length; i++) {

            boolean match;

            if (Double.isNaN(target)) {
                match = Double.isNaN(arr[i]);
            } else {
                match = arr[i] == target;
            }

            if (match) {
                arr[i] = fill;
            }
        }

        return arr;
    }

	private static void shiftAndAppend(double[] arr, double[] values, int k) {
        if (k == 0)
            return;

        int kAbs = Math.abs(k);

        if (k > 0) {
            System.arraycopy(arr, k, arr, 0, arr.length - k);
            for (int i = arr.length-k; i < arr.length; i++) {
                arr[i] = values[i - (arr.length-k)];
            }
        } else {
            System.arraycopy(arr, 0, arr, kAbs, arr.length+k); // we just do +k, because k is minus
            System.arraycopy(values, 0, arr, 0, kAbs);
        }
	}
}
