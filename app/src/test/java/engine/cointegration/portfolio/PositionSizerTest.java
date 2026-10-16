package engine.cointegration.portfolio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import engine.constants.PublicConstants;
import engine.mathUtils.vector.VectorOps;

public class PositionSizerTest {
    private static final double EPS = 1e-9;

    @Test
    void testLowVolatilityGivesLargerMultiplier() {
        double[] calm = { 100.0, 100.1, 99.9, 100.05, 99.95, 100.0 };
        double[] wild = { 100.0, 110.0, 90.0, 108.0, 92.0, 100.0 };

        double mCalm = PositionSizer.compute(calm, 6).multiplier;
        double mWild = PositionSizer.compute(wild, 6).multiplier;

        assertTrue(mCalm > mWild, "calm market must size larger than wild market");
    }

    @Test
    void testHalfRangeMoveLosesConfiguredRisk() {
        double risk = PublicConstants.RISK_PER_HALF_RANGE;
        double balance = PublicConstants.INIT_BALANCE;

        double[] series = { 100.0, 110.0, 90.0, 105.0, 95.0, 100.0 };
        PositionSizer.Sizing sizing = PositionSizer.compute(series, 6);

        double loss = sizing.multiplier * (sizing.range / 2.0);
        assertEquals(risk * balance, loss, 1e-6);
    }

    @Test
    void testFlatSeriesFallsBackToOne() {
        double[] flat = { 5.0, 5.0, 5.0, 5.0 };

        PositionSizer.Sizing sizing = PositionSizer.compute(flat, 4);

        assertEquals(1.0, sizing.multiplier, EPS);
    }

    @Test
    void testMultiplierIsClamped() {
        double[] tiny = { 100.0, 100.000001, 99.999999, 100.0 };

        double m = PositionSizer.compute(tiny, 4).multiplier;

        assertTrue(m <= PublicConstants.MAX_BETA_MULTIPLIER, "must respect max clamp");
        assertTrue(m >= PublicConstants.MIN_BETA_MULTIPLIER, "must respect min clamp");
    }

    @Test
    void testScaleClonesWhenAsked() {
        double[] beta = { 1.0, -2.0 };

        double[] out = VectorOps.scale(beta, 3.0, true);

        assertEquals(1.0, beta[0], EPS);
        assertEquals(3.0, out[0], EPS);
        assertEquals(-6.0, out[1], EPS);
    }
}
