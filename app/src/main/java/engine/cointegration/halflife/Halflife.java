package engine.cointegration.halflife;

public class Halflife {
    public static double halflife(double[] priceSeries) {
        // مرحله 1: محاسبه بازده (تفاضل اول) و سری وقفه‌دار (قیمت با یک وقفه)
        int n = priceSeries.length;
        double[] returns = new double[n - 1]; // Δy_t
        double[] lagged = new double[n - 1]; // y_{t-1}

        for (int i = 1; i < n; i++) {
            returns[i - 1] = priceSeries[i] - priceSeries[i - 1];
            lagged[i - 1] = priceSeries[i - 1];
        }

        // مرحله 2: رگرسیون OLS: returns = α + β * lagged + خطا
        double beta = linearRegressionBeta(lagged, returns);
        double halfLife = -Math.log(2) / beta; // نیمه‌عمر
        return halfLife;
    }

    /**
     * محاسبه ضریب شیب (β) در رگرسیون خطی ساده y = α + β x + ε
     * از روش حداقل مربعات معمولی (OLS)
     */
    public static double linearRegressionBeta(double[] x, double[] y) {
        int m = x.length;
        if (m != y.length) {
            throw new IllegalArgumentException("آرایه‌های x و y باید هم‌طول باشند");
        }

        double meanX = 0, meanY = 0;
        for (int i = 0; i < m; i++) {
            meanX += x[i];
            meanY += y[i];
        }
        meanX /= m;
        meanY /= m;

        double cov = 0, varX = 0;
        for (int i = 0; i < m; i++) {
            double dx = x[i] - meanX;
            double dy = y[i] - meanY;
            cov += dx * dy;
            varX += dx * dx;
        }

        if (Math.abs(varX) < 1e-12) {
            // اگر واریانس صفر باشد، شیب را صفر در نظر می‌گیریم (نیمه‌عمر بینهایت)
            return 0.0;
        }
        return cov / varX;
    }
}
