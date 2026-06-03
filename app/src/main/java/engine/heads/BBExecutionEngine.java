package engine.heads;

import java.util.List;

import engine.constants.BacktestConstants;
import engine.core.BBCore;
import engine.reporter.ProgressReporter;

public class BBExecutionEngine extends ExecutionEngine {
    private BBCore core;

    private double[] yPortBid = headState.indicators.yPortBid;
    private double[] yPortAsk = headState.indicators.yPortAsk;


    public BBExecutionEngine(double[][][] mohlcv, List<String> datetimeIndex) {
        super(mohlcv, datetimeIndex);
    }

    @Override
    protected void processExecutionCycle() {
        
    }

    @Override
    protected void callDesiredCore() {
        initializeCore(super.fetchAndMakeInitialPriceMatrix(), super.buildInitialDatetimeSlice());
    }

    @Override
    protected void initializeCore(double[][] priceMatrix,
            List<String> dateTimeSlice) {

        long startCore = System.nanoTime();

        this.core = new BBCore(
                priceMatrix,
                dateTimeSlice,
                super.logger.getRunPath(),
                BacktestConstants.ENTERY_Z_SCORE,
                BacktestConstants.EXIT_Z_SCORE);

        ProgressReporter.printElapsedNanoTime(
                System.nanoTime() - startCore,
                "Initialization");
    }
}
