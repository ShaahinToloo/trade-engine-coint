package engine.main;

import java.util.List;

import engine.constants.BacktestConstants;
import engine.data.fetch.BacktestDataLoader;

public abstract class Main {
    public List<String> B;
    public double[][][] A;

    public Main(boolean log) {
        if (log) {
            System.out.println("Python --Fetching Data");
        }

        BacktestDataLoader call = new BacktestDataLoader();
        A = call.getMOHLCV();
        B = call.getIndex();

        setLens();
        if (log) {
            log();
        }
    }

    private void setLens() {
        BacktestConstants.NUM_SERIES = A.length;
        BacktestConstants.DATA_LENGTH = A[0][0].length;
    }

    private void log() {
        System.out.println("Java --Log");
        System.out.println("Data Length: " + BacktestConstants.DATA_LENGTH);
        System.out.printf("OHLCV Shape: [%d][%d][%d]%n",
                BacktestConstants.NUM_SERIES,
                A[0].length,
                BacktestConstants.DATA_LENGTH);
    }
}