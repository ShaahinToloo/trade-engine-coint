package engine.tuner;

import java.util.ArrayList;
import java.util.List;

import engine.backtestMain.BBMain;
import engine.constants.BacktestConstants;
import engine.heads.BBBacktest;
import engine.heads.HeadState;

public class HyperparameterTuner {

    private static class Result {
        double entryZ;
        double exitZ;
        int seq;
        int period;
        double score;

        Result(double e1, double e2, int s, int p, double sc) {
            this.entryZ = e1;
            this.exitZ = e2;
            this.seq = s;
            this.period = p;
            this.score = sc;
        }
    }

    private final double[] entryZ = { 3.5, 3, 2.5, 2 };
    private final double[] exitZ = { -0.5, 0, 0.5 };
    private final int[] seqLen = { 300, 400 };
    private final int[] testPer = { 59, 89 };

    private final List<Result> results = new ArrayList<>();

    public void runSweep() {

        int total = calculateTotalRuns();
        int round = 0;
        System.out.println("Total Runs: " + total);

        double bestScore = Double.NEGATIVE_INFINITY;
        Result best = null;

        for (double e1 : entryZ) {
            for (double e2 : exitZ) {

                if (Math.abs(e2) >= e1)
                    continue;

                for (int seq : seqLen) {
                    for (int period : testPer) {
                        round += 1;
                        System.out.print(String.format("\rFinished %d/%d", round, total));

                        overrideConstants(e1, e2, seq, period);

                        BBBacktest bt = new BBBacktest(loadA(), loadB());
                        bt.start();

                        double score = score(bt.headState);

                        Result r = new Result(e1, e2, seq, period, score);
                        results.add(r);

                        if (score > bestScore) {
                            bestScore = score;
                            best = r;
                        }

                        System.out.printf(
                                "entry=%.2f exit=%.2f seq=%d period=%d score=%.4f%n",
                                e1, e2, seq, period, score);
                    }
                }
            }
        }

        System.out.println("\nBEST RESULT:");
        System.out.printf(
                "entry=%.2f exit=%.2f seq=%d period=%d score=%.4f%n",
                best.entryZ, best.exitZ, best.seq, best.period, best.score);
    }

    private int calculateTotalRuns() {
        int total = 0;
        for (double e1 : entryZ) {
            for (double e2 : exitZ) {

                if (Math.abs(e2) >= e1)
                    continue;

                for (int i = 0; i < seqLen.length; i++) {
                    for (int j = 0; j < testPer.length; i++) {
                        total += 1;
                    }
                }
            }
        }
        return total;
    }

    private void overrideConstants(double entry, double exit, int seq, int period) {
        BacktestConstants.ENTERY_Z_SCORE = entry;
        BacktestConstants.EXIT_Z_SCORE = exit;
        BacktestConstants.SEQ_LENGTH = seq;
        BacktestConstants.TESTS_PERIOD = period;
    }

    private double score(HeadState hs) {
        return hs.risk.sharpe
                + 0.5 * hs.risk.equityCalmar
                + 0.2 * hs.risk.ev
                - 0.3 * Math.abs(hs.risk.equityDrawdown);
    }

    private double[][][] loadA() {
        return new BBMain().A;
    }

    private List<String> loadB() {
        return new BBMain().B;
    }

    public static void main(String[] args) {
        HyperparameterTuner hyperT = new HyperparameterTuner();
        hyperT.runSweep();
    }
}