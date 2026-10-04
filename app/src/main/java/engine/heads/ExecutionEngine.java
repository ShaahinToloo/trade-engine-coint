package engine.heads;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import engine.constants.BacktestConstants;
import engine.constants.ExecutionConstants;
import engine.constants.PublicConstants;
import engine.execution.JEPLayer;
import engine.execution.OrderGateway;
import engine.execution.RiskState;
import engine.execution.StateSnapshotStore;
import engine.logger.DataLogger;
import engine.logger.InfoLogger;
import engine.timeUtils.SleeperUtils;

public abstract class ExecutionEngine implements Runnable {
    protected final HeadState headState = new HeadState();

    protected final InfoLogger log = new InfoLogger(Path.of(PublicConstants.INFO_LOG_PATH, "Execution/").toString(),
            PublicConstants.INFO_LOG_NAME);
    protected final DataLogger logger = new DataLogger(Path.of(PublicConstants.DATA_LOG_PATH, "Execution/").toString());

    private final RiskState riskState = new RiskState();
    private final OrderGateway gateway = new OrderGateway();
    private final StateSnapshotStore snapshotStore = new StateSnapshotStore(log);

    protected JEPLayer jepLayer = new JEPLayer();

    protected int index = PublicConstants.SEQ_LENGTH;

    public ExecutionEngine() {
        headState.market.length = ExecutionConstants.SEQ_LENGTH;

        initializePortfolioBuffers();
        initializeEquityBuffers();
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

    private double[][] initializePriceMatrix(double[][][] initialData) {
        int rows = headState.market.length;
        int cols = ExecutionConstants.NUM_SERIES;

        double[][] priceMatrix = new double[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                priceMatrix[i][j] = initialData[j][3][i];
            }
        }
        return priceMatrix;
    }

    @Override
    public void run() {
        int backoffMs = 500;

        initializeCore();
        feedInitializationArray();

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

                // Fetch New Price
                processNewData();

                // Strategy + execution
                processExecutionCycle();

                // Persist snapshot after success
                snapshotStore.save(riskState);

                backoffMs = 500;

                index++;

            } catch (ArithmeticException | IllegalArgumentException | IllegalMonitorStateException
                    | IllegalStateException e) { // Recoverable (RecoverableException)

                log.warn("Recoverable failure " + e);
                gateway.reconnect(log);

                PublicConstants.sleep(backoffMs);
                backoffMs = Math.min(backoffMs * 2, 10_000);

            } catch (ArrayStoreException | ClassCastException | EnumConstantNotPresentException | IllegalCallerException
                    | IndexOutOfBoundsException | NegativeArraySizeException | NullPointerException
                    | TypeNotPresentException | UnsupportedOperationException e) { // Non-Recoverable
                                                                                   // (BrokenCodeException)

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

    private void feedInitializationArray() {
        double[][][] initialData = jepLayer.getInitialData();
        List<String> initialIndex = jepLayer.getInitialIndex();

        headState.market.priceMatrix = initializePriceMatrix(initialData);
        headState.market.datetimeIndex = initialIndex;
    }

    private void processNewData() {
        proccessNewPrice();
        processNewIndex();
    }

    private void proccessNewPrice() {
        double[][][] newData = jepLayer.getNewData();

        // Shift price matrix left and append new data
        for (int i = 0; i < headState.buffers.newPrice.length; i++) {
            headState.buffers.newPrice[i] = newData[0][3][0];
        }
    }

    private void processNewIndex() {
        List<String> newIndex = jepLayer.getNewIndex();

        headState.buffers.datetimeIndex = newIndex.get(0);
    }

    protected int[] getIndicesToEliminate(int[] tradeIndicesToEliminate) {
        int numToClose = 0;

        for (int j = 0; j < tradeIndicesToEliminate.length; j++) {
            if (tradeIndicesToEliminate[j] == 1) {
                numToClose++;
            }
        }

        int[] newTradeIndicesToEliminate = new int[numToClose];

        for (int j = tradeIndicesToEliminate.length - 1; j > -1; j--) {
            if (tradeIndicesToEliminate[j] == 1) {
                newTradeIndicesToEliminate[--numToClose] = j;
            }
        }

        return newTradeIndicesToEliminate;
    }

    protected abstract void processExecutionCycle();

    /**
     * Initializes Core and prints initialization timing.
     */
    protected abstract void initializeCore();

    /**
     * Processes a new candle through Core and tracks execution latency.
     *
     * @param core      core engine instance
     * @param timestamp current timestamp
     */
    protected abstract void processCoreCandle();
}
