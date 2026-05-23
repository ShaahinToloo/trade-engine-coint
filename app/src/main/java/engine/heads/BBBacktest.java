package engine.heads;

import java.util.List;

import engine.constants.BacktestConstants;
import engine.core.BBCore;
import engine.core.Core;
import engine.reporter.ProgressReporter;

public class BBBacktest extends Backtest {

    public BBBacktest(double[][][] mohlcv, List<String> dateTimeIndex) {
        super(mohlcv, dateTimeIndex);
    }

    @Override
	protected Core initializeCore(double[][] priceMatrix,
			List<String> dateTimeSlice) {

		long startCore = System.nanoTime();

		var core = new BBCore(
				priceMatrix,
				dateTimeSlice,
				logger.getRunPath(),
				BacktestConstants.ENTERY_Z_SCORE,
				BacktestConstants.EXIT_Z_SCORE);

		ProgressReporter.printElapsedNanoTime(
				System.nanoTime() - startCore,
				"Initialization");

		return core;
	}

}
