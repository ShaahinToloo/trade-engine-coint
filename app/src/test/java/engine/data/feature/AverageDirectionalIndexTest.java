package engine.data.feature;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import engine.constants.PublicConstants;

public class AverageDirectionalIndexTest {

    private final int originalSeqLength = PublicConstants.SEQ_LENGTH;

    @AfterEach
    void restoreSeqLength() {
        PublicConstants.SEQ_LENGTH = originalSeqLength;
    }

    private CachingMainFeatures buildCmf(int length) {
        PublicConstants.SEQ_LENGTH = length;
        double[][] priceMatrix = new double[length][1];
        for (int i = 0; i < length; i++) {
            priceMatrix[i][0] = 100 + i;
        }
        return new CachingMainFeatures(priceMatrix,
                new ArrayList<>(Collections.nCopies(length, "2024-01-01 00:00:00")));
    }

    @Test
    void strongUptrendProducesAdxNear100() {
        int length = 100;
        CachingMainFeatures cmf = buildCmf(length);

        double[] close = new double[length];
        double[] high = new double[length];
        double[] low = new double[length];
        for (int i = 0; i < length; i++) {
            close[i] = 100 + i;
            high[i] = close[i] + 0.5;
            low[i] = close[i] - 0.5;
        }

        cmf.averageDirectionalIndex(high, low, close, 14, "hlc");
        double[] adx = cmf.getAverageDirectionalIndex(14, "hlc");

        assertEquals(100.0, adx[length - 1], 1e-9);
    }

    @Test
    void flatMarketProducesAdxNear0() {
        int length = 100;
        CachingMainFeatures cmf = buildCmf(length);

        double[] close = new double[length];
        double[] high = new double[length];
        double[] low = new double[length];
        for (int i = 0; i < length; i++) {
            close[i] = 100;
            high[i] = 100.5;
            low[i] = 99.5;
        }

        cmf.averageDirectionalIndex(high, low, close, 14, "hlc");
        double[] adx = cmf.getAverageDirectionalIndex(14, "hlc");

        assertEquals(0.0, adx[length - 1], 1e-9);
    }

    @Test
    void warmupPeriodIsNaN() {
        int length = 100;
        CachingMainFeatures cmf = buildCmf(length);

        double[] close = new double[length];
        double[] high = new double[length];
        double[] low = new double[length];
        for (int i = 0; i < length; i++) {
            close[i] = 100 + Math.sin(i);
            high[i] = close[i] + 0.5;
            low[i] = close[i] - 0.5;
        }

        int period = 14;
        cmf.averageDirectionalIndex(high, low, close, period, "hlc");
        double[] adx = cmf.getAverageDirectionalIndex(period, "hlc");

        assertTrue(Double.isNaN(adx[0]));
        assertTrue(Double.isNaN(adx[2 * period - 3]));
        assertTrue(!Double.isNaN(adx[2 * period - 2]));
        for (int i = 2 * period - 2; i < length; i++) {
            assertTrue(adx[i] >= 0.0 && adx[i] <= 100.0);
        }
    }
}
