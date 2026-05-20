package engine.constants;

public final class BacktestConstants extends PublicConstants {
	// Order matters && for reversed currencies recompute spread manually and put it
	// here
	public static final double[] SPREADS = new double[] { 0.00003, 0.00001, 0.00003, 0, 0, 0 };

	public static int NUM_SERIES; // Will be set in Main.java
	public static int DATA_LENGTH; // Will be set in Main.java

	private BacktestConstants() {
		throw new AssertionError("Cannot instantiate Constants class");
	}
}
