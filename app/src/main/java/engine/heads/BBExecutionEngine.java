package engine.heads;

import engine.constants.BacktestConstants;
import engine.core.BBCore;
import engine.core.Core;
import engine.reporter.ProgressReporter;
import engine.trade.Trade;
import engine.trade.TradeManager;

public class BBExecutionEngine extends ExecutionEngine {
    private BBCore core;

    public BBExecutionEngine() {
        super();
    }

    @Override
    protected void processExecutionCycle() {
        processCoreCandle();

        Trade trade = Core.TradeContext.trade;
        int[] tradeIndicesToEliminate = Core.TradeContext.tradeIndicesToEliminate;

        
    }

    @Override
    protected void initializeCore() {

        long startCore = System.nanoTime();

        this.core = new BBCore(
                headState.market.priceMatrix,
                headState.market.datetimeIndex,
                super.logger.getRunPath(),
                BacktestConstants.ENTERY_Z_SCORE,
                BacktestConstants.EXIT_Z_SCORE);

        ProgressReporter.printElapsedNanoTime(
                System.nanoTime() - startCore,
                "Initialization");
    }

    @Override
    protected void processCoreCandle() {

        long startCore = System.nanoTime();

        this.core.processCandle(
                headState.buffers.newPrice,
                headState.buffers.datetimeIndex,
                TradeManager.openTrades,
                super.index);

        headState.perf.currCoreSpeed = System.nanoTime() - startCore;
    }
}
