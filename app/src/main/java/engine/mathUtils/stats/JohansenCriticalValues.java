package engine.mathUtils.stats;

public class JohansenCriticalValues {
    public static double[] c_sja(int n) {
        double[][] jcp1 = new double[][] {
                new double[] { 2.7055, 3.8415, 6.6349 },
                new double[] { 12.2971, 14.2639, 18.5200 },
                new double[] { 18.8928, 21.1314, 25.8650 },
                new double[] { 25.1236, 27.5858, 32.7172 },
                new double[] { 31.2379, 33.8777, 39.3693 },
                new double[] { 37.2786, 40.0763, 45.8662 },
                new double[] { 43.2947, 46.2299, 52.3069 },
                new double[] { 49.2855, 52.3622, 58.6634 },
                new double[] { 55.2412, 58.4332, 64.9960 },
                new double[] { 61.2041, 64.5040, 71.2525 },
                new double[] { 67.1307, 70.5392, 77.4877 },
                new double[] { 73.0563, 76.5734, 83.7105 }
        };

        return jcp1[n];
    }
}
