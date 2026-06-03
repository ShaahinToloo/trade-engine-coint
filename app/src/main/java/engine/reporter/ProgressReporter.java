package engine.reporter;

public final class ProgressReporter {
	public static void printProgress(int current, int total, long start) {
		long now = System.nanoTime();
		int barLength = 50;
		int filledLength = (int) (barLength * current / (double) total);
		String bar = "$".repeat(filledLength) + " ".repeat(barLength - filledLength);

		double percent = (current * 100.0) / total;
		System.err.printf("\rProgress: [%s] %.2f%% [Took (Hour:Minute:Second): %02d:%02d:%02d]", bar, percent,
				(now - start) / 3_600_000_000_000L, // hours
				((now - start) / 60_000_000_000L) % 60, // minutes
				((now - start) / 1_000_000_000L) % 60);

		if (current == total)
			System.err.println();
	}

	public static void printProgressMeanSpeed(int current, int total, long[] range, long start, String name) {
		long now = System.nanoTime();
		int barLength = 50;
		int filledLength = (int) (barLength * current / (double) total);
		String bar = "$".repeat(filledLength) + " ".repeat(barLength - filledLength);

		double percent = (current * 100.0) / total;

		long rMean = 0L;
		long rSum = 0L;

		for (int j = 0; j < range.length; j++) {
			rSum += range[j];
		}

		rMean = rSum / range.length;

		long nanoseconds = rMean;
		long microseconds = nanoseconds / 1000;
		long milliseconds = microseconds / 1000;
		long seconds = milliseconds / 1000;

		System.err.printf(
				"\rProgress: [%s] %.2f%% [Took (Hour:Minute:Second): %02d:%02d:%02d | "+name+" Mean Speed: %d s:%d ms:%d μs:%d ns ]",
				bar, percent,
				(now - start) / 3_600_000_000_000L, // hours
				((now - start) / 60_000_000_000L) % 60, // minutes
				((now - start) / 1_000_000_000L) % 60,
				seconds % 60, milliseconds % 1000, microseconds % 1000, nanoseconds % 1000);

		if (current == total)
			System.err.println();
	}

	public static void printElapsedNanoTime(long elapsedNanos, String name) {
		// Convert nanoseconds to appropriate time units
		long nanoseconds = elapsedNanos % 1000;
		long microseconds = (elapsedNanos / 1000) % 1000;
		long milliseconds = (elapsedNanos / 1_000_000) % 1000;
		long seconds = elapsedNanos / 1_000_000_000;

		System.err.printf(name+" Took: %d s:%d ms:%d μs:%d ns\n",
				seconds, milliseconds, microseconds, nanoseconds);
	}
}
