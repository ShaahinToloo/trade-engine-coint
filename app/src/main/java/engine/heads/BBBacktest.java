package engine.heads;

import java.nio.file.Path;
import java.util.List;

import engine.constants.BacktestConstants;
import engine.constants.PublicConstants;
import engine.core.BBCore;
import engine.core.Core;
import engine.logger.InfoLogger;
import engine.reporter.ProgressReporter;

public class BBBacktest extends Backtest {

    protected final InfoLogger log = new InfoLogger(Path.of(PublicConstants.INFO_LOG_PATH, "BackTest/").toString(),
            PublicConstants.INFO_LOG_NAME);

    public BBBacktest(double[][][] mohlcv, List<String> dateTimeIndex) {
        super(mohlcv, dateTimeIndex);
    }

    @Override
	protected Core initializeCore(double[][] priceMatrix,
			List<String> dateTimeSlice) {

		String initMsg = "Initializing BBCore with data: priceMatrix=" + priceMatrix.length + "x" + priceMatrix[0].length
				+ ", dateTimeSlice size=" + dateTimeSlice.size()
				+ ", from=" + dateTimeSlice.get(0)
				+ ", to=" + dateTimeSlice.get(dateTimeSlice.size() - 1)
				+ ", ENTERY_Z_SCORE=" + BacktestConstants.ENTRY_Z_SCORE
				+ ", EXIT_Z_SCORE=" + BacktestConstants.EXIT_Z_SCORE;
//		log.info(initMsg);
		System.out.println(initMsg);

		long startCore = System.nanoTime();

		var core = new BBCore(
				priceMatrix,
				dateTimeSlice,
				logger.getRunPath(),
				BacktestConstants.ENTRY_Z_SCORE,
				BacktestConstants.EXIT_Z_SCORE);

		ProgressReporter.printElapsedNanoTime(
				System.nanoTime() - startCore,
				"Initialization");

		return core;
	}

}
