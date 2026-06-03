package engine.data.feature;

import engine.constants.BacktestConstants;

public class CachingFeatureDerivator {
	public CachingFeatureDerivator() {
	}

	private void shiftAndAppend(double[] arr, double value) {
		System.arraycopy(arr, 1, arr, 0, arr.length - 1);
		arr[arr.length - 1] = value;
	}

	private void lengthChecker(double[] arr1, double[] output) {
		if (arr1.length != output.length) {
			System.out.println("\nArr1 length: " + arr1.length);
			System.out.println("\nOutput length: " + output.length);
			throw new IllegalArgumentException("input arrays length mismatch");
		}
	}

	private void lengthChecker(double[] arr1, double[] arr2, double[] output) {
		if (arr1.length != arr2.length) {
			System.out.println("\nArr1 length: " + arr1.length);
			System.out.println("\nArr2 length: " + arr2.length);
			throw new IllegalArgumentException("input arrays length mismatch");
		}
		if (arr2.length != output.length) {
			System.out.println("\nArr2 length: " + arr2.length);
			System.out.println("\nOutput length: " + output.length);
			throw new IllegalArgumentException("input arrays length mismatch");
		}
	}

	public double[] diff(double[] arr1, double[] output, boolean cached) {
		lengthChecker(arr1, output);

		int start = 1;
		int end = arr1.length;

		if (cached) {
			shiftAndAppend(output, Double.NaN);
			start = end - 1;
		} else {
			output[0] = Double.NaN;
		}

		for (int i = start; i < end; i++) {
			output[i] = arr1[i] - arr1[i - 1];
		}
		return output;
	}

	public double[] pctChange(double[] arr1, double[] output, boolean cached) {
		lengthChecker(arr1, output);

		int start = 1;
		int end = arr1.length;

		if (cached) {
			shiftAndAppend(output, Double.NaN);
			start = end - 1;
		} else {
			output[0] = Double.NaN;
		}

		for (int i = start; i < end; i++) {
			output[i] = (arr1[i] - arr1[i - 1]) / (arr1[i - 1] + BacktestConstants.EPSILON);
		}
		return output;
	}

	public double[] radSlope(double[] arr1, double[] output, boolean cached) {
		lengthChecker(arr1, output);

		int start = 1;
		int end = arr1.length;

		if (cached) {
			shiftAndAppend(output, Double.NaN);
			start = end - 1;
		} else {
			output[0] = Double.NaN;
		}

		for (int i = start; i < end; i++) {
			output[i] = Math.atan(arr1[i] - arr1[i - 1]);
		}
		return output;
	}

	public double[] degSlope(double[] arr1, double[] output, boolean cached) {
		lengthChecker(arr1, output);

		int start = 1;
		int end = arr1.length;

		if (cached) {
			// shiftAndAppend(output, Double.NaN);
			start = end - 1;
		} else {
			output[0] = Double.NaN;
		}

		radSlope(arr1, output, cached);
		
		for (int i = start; i < end; i++) {
			output[i] = Math.toDegrees(output[i]);
		}
		return output;
	}

	public double[] isOB(double[] arr1, double[] output, double threshold, boolean cached) {
		lengthChecker(arr1, output);

		int start = 0;
		int end = arr1.length;

		if (cached) {
			shiftAndAppend(output, Double.NaN);
			start = end - 1;
		}

		for (int i = start; i < end; i++) {
			output[i] = (arr1[i] >= threshold) ? 1.0 : 0.0;
		}
		return output;
	}

	public double[] isOS(double[] arr1, double[] output, double threshold, boolean cached) {
		lengthChecker(arr1, output);

		int start = 0;
		int end = arr1.length;

		if (cached) {
			shiftAndAppend(output, Double.NaN);
			start = end - 1;
		}

		for (int i = start; i < end; i++) {
			output[i] = (arr1[i] <= threshold) ? 1.0 : 0.0;
		}
		return output;
	}

	public double[] division(double[] arr1, double[] arr2, double[] output, boolean cached) {
		lengthChecker(arr1, arr2, output);

		int start = 0;
		int end = arr1.length;

		if (cached) {
			shiftAndAppend(output, Double.NaN);
			start = end - 1;
		}

		for (int i = start; i < end; i++) {
			output[i] = arr1[i] / (arr2[i] + BacktestConstants.EPSILON);
		}
		return output;
	}

	public double[] divisionAbs(double[] arr1, double[] arr2, double[] output, boolean cached) {
		lengthChecker(arr1, arr2, output);

		int start = 0;
		int end = arr1.length;

		if (cached) {
			shiftAndAppend(output, Double.NaN);
			start = end - 1;
		}

		for (int i = start; i < end; i++) {
			output[i] = Math.abs(arr1[i] / (arr2[i] + BacktestConstants.EPSILON));
		}
		return output;
	}

	public double[] range(double[] arr1, double[] arr2, double[] output, boolean cached) {
		lengthChecker(arr1, arr2, output);

		int start = 0;
		int end = arr1.length;

		if (cached) {
			shiftAndAppend(output, Double.NaN);
			start = end - 1;
		}

		for (int i = start; i < end; i++) {
			output[i] = arr1[i] - arr2[i];
		}
		return output;
	}

	public double[] rangeAbs(double[] arr1, double[] arr2, double[] output, boolean cached) {
		lengthChecker(arr1, arr2, output);

		int start = 0;
		int end = arr1.length;

		if (cached) {
			shiftAndAppend(output, Double.NaN);
			start = end - 1;
		}

		for (int i = start; i < end; i++) {
			output[i] = Math.abs(arr1[i] - arr2[i]);
		}
		return output;
	}

	public double[] pctRange(double[] arr1, double[] arr2, double[] output, boolean cached) {
		lengthChecker(arr1, arr2, output);

		int start = 0;
		int end = arr1.length;

		if (cached) {
			shiftAndAppend(output, Double.NaN);
			start = end - 1;
		}

		for (int i = start; i < end; i++) {
			output[i] = (arr1[i] - arr2[i]) / (arr2[i] + BacktestConstants.EPSILON);
		}
		return output;
	}

	public double[] pctRangeAbs(double[] arr1, double[] arr2, double[] output, boolean cached) {
		lengthChecker(arr1, arr2, output);

		int start = 0;
		int end = arr1.length;

		if (cached) {
			shiftAndAppend(output, Double.NaN);
			start = end - 1;
		}

		for (int i = start; i < end; i++) {
			output[i] = Math.abs((arr1[i] - arr2[i]) / (arr2[i] + BacktestConstants.EPSILON));
		}
		return output;
	}

	public double[] strictPctRange(double[] arr1, double[] arr2, double[] output, boolean cached) {
		lengthChecker(arr1, arr2, output);

		int start = 0;
		int end = arr1.length;

		if (cached) {
			shiftAndAppend(output, Double.NaN);
			start = end - 1;
		}

		for (int i = start; i < end; i++) {
			double min = Math.min(arr1[i], arr2[i]);
			double max = Math.max(arr1[i], arr2[i]);
			output[i] = Math.abs((min - max) / (max + BacktestConstants.EPSILON));
		}
		return output;
	}

	public double[] cross(double[] arr1, double[] arr2, double[] output, boolean cached) {
		lengthChecker(arr1, arr2, output);

		int start = 1;
		int end = arr1.length;

		if (cached) {
			shiftAndAppend(output, Double.NaN);
			start = end - 1;
		} else {
			output[0] = 0.0;
		}

		for (int i = start; i < end; i++) {
			double prevDiff = (arr1[i - 1] - arr2[i - 1]);
			double currDiff = (arr1[i] - arr2[i]);
			if (prevDiff < 0 && currDiff > 0) {
				output[i] = 1.0;
			} else if (prevDiff > 0 && currDiff < 0) {
				output[i] = -1.0;
			} else {
				output[i] = 0.0;
			}
		}
		return output;
	}

	public double[] location(double[] arr1, double[] arr2, double[] output, boolean cached) {
		lengthChecker(arr1, arr2, output);

		int start = 0;
		int end = arr1.length;

		if (cached) {
			shiftAndAppend(output, Double.NaN);
			start = end - 1;
		}

		for (int i = start; i < end; i++) {
			output[i] = Math.signum(arr1[i] - arr2[i]);
		}
		return output;
	}
}
