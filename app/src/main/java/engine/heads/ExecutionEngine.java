package engine.heads;

import java.util.Arrays;
import java.util.List;

import engine.constants.BacktestConstants;
import engine.constants.PublicConstants;
import engine.execution.OrderGateway;
import engine.execution.RiskState;
import engine.execution.StateSnapshotStore;
import engine.logger.DataLogger;
import engine.logger.InfoLogger;
import engine.timeUtils.SleeperUtils;

public abstract class ExecutionEngine implements Runnable {
    protected final HeadState headState = new HeadState();

    protected final InfoLogger log = new InfoLogger(PublicConstants.INFO_LOG_PATH, PublicConstants.INFO_LOG_NAME);
    protected final DataLogger logger = new DataLogger(PublicConstants.DATA_LOG_PATH);

    private final RiskState riskState = new RiskState();
    private final OrderGateway gateway = new OrderGateway();
    private final StateSnapshotStore snapshotStore = new StateSnapshotStore(log);

    public ExecutionEngine(double[][][] mohlcv, List<String> dateTimeIndex) {
        headState.market.datetimeIndex = dateTimeIndex;
        headState.market.length = BacktestConstants.DATA_LENGTH;

        initializeTimerArray();
        initializePortfolioBuffers();
        initializeEquityBuffers();
        initializePriceMatrix(mohlcv);
    }

    private void initializeTimerArray() {
        headState.perf.coreSpeedRange = new long[headState.perf.reportPeriod];
    }

    /**
     * Initializes portfolio-related tracking arrays.
     */
    private void initializePortfolioBuffers() {
        var dataLength = headState.market.length;
        headState.indicators.yPortBid = new double[dataLength];
        headState.indicators.yPortAsk = new double[dataLength];
        headState.indicators.movingAvg = new double[dataLength];
        headState.indicators.std = new double[dataLength];
    }

    /**
     * Initializes equity and PnL tracking arrays.
     */
    private void initializeEquityBuffers() {
        var dataLength = headState.market.length;

        headState.portfolio.equity = new double[dataLength];
        headState.portfolio.realisedEquity = new double[dataLength];

        Arrays.fill(
                headState.portfolio.equity,
                BacktestConstants.INIT_BALANCE);

        Arrays.fill(
                headState.portfolio.realisedEquity,
                BacktestConstants.INIT_BALANCE);
    }

    private void initializePriceMatrix(double[][][] mohlcv) {
        int rows = headState.market.length;
        int cols = BacktestConstants.NUM_SERIES;

        headState.market.priceMatrix = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                headState.market.priceMatrix[i][j] = mohlcv[j][3][i];
            }
        }
    }

    @Override
    public void run() {

        int backoffMs = 500;
        callDesiredCore();

        while (true) {
            try {

                // Check kill switch
                if (riskState.killSwitchActive()) {
                    gateway.cancelAll();
                    break;
                }

                // Wait until a short period before new price
                long boundary = SleeperUtils.sleepUntilNextInterval(PublicConstants.TIMEFRAME_MINUTES, -5);

                // Load latest safe state
                gateway.refresh();

                // Wait until new price
                SleeperUtils.sleepUntilGivenBoundary(boundary, 1);

                // Strategy + execution
                processExecutionCycle();

                // Persist snapshot after success
                snapshotStore.save(riskState);

                backoffMs = 500;

                // } catch (InterruptedException e) {
                // Thread.currentThread().interrupt();
                // gateway.cancelAll();
                // break;

            } catch (ArithmeticException | IllegalArgumentException | IllegalMonitorStateException
                    | IllegalStateException e) { // Recoverable (RecoverableException)

                log.warn("Recoverable failure " + e);
                gateway.reconnect(log);

                PublicConstants.sleep(backoffMs);
                backoffMs = Math.min(backoffMs * 2, 10_000);

            } catch (ArrayStoreException | ClassCastException | EnumConstantNotPresentException | IllegalCallerException
                    | IndexOutOfBoundsException | NegativeArraySizeException | NullPointerException
                    | TypeNotPresentException | UnsupportedOperationException e) { // Non-Recoverable (BrokenCodeException)

            } catch (Throwable t) {

                log.err("Critical failure " + t.toString());

                // HARD safety actions
                gateway.cancelAll();
                riskState.enableKillSwitch();

                snapshotStore.save(riskState);

                PublicConstants.sleep(2000);
            }
        }

        log.info("Execution loop stopped");

    }

    public void stop() {
        gateway.cancelAll();
        riskState.enableKillSwitch();

        snapshotStore.save(riskState);
    }

    protected abstract void callDesiredCore();

    protected abstract void processExecutionCycle();

    /**
     * Initializes Core and prints initialization timing.
     */
    protected abstract void initializeCore(double[][] priceMatrix,
            List<String> dateTimeSlice);

    protected double[][] buildInitialPriceMatrix() {
        double[][] sliced = new double[BacktestConstants.SEQ_LENGTH][headState.market.priceMatrix[0].length];

        for (int i = 0; i < sliced.length; i++) {
            System.arraycopy(
                    headState.market.priceMatrix[i],
                    0,
                    sliced[i],
                    0,
                    headState.market.priceMatrix[0].length);
        }

        return sliced;
    }

    protected List<String> buildInitialDatetimeSlice() {
        return headState.market.datetimeIndex.subList(0, BacktestConstants.SEQ_LENGTH);
    }
}
