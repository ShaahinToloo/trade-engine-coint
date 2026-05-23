package engine.constants;

public final class BacktestConstants extends PublicConstants {
	public static int NUM_SERIES; // Will be set in Main.java
	public static int DATA_LENGTH; // Will be set in Main.java

	private BacktestConstants() {
		throw new AssertionError("Cannot instantiate Constants class");
	}
}
