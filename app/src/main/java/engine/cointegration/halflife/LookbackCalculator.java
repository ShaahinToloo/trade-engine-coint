package engine.cointegration.halflife;

public class LookbackCalculator {

    /**
     * محاسبه طول بازگشت از روی سری قیمت
     *
     * @param priceSeries آرایه قیمت‌ها (حداقل دو عنصر لازم است)
     * @return طول بازگشت (عدد صحیح، حداقل 20)
     * @throws IllegalArgumentException اگر آرایه ورودی نال یا کوتاه‌تر از 2 باشد
     */
    public static int computeLookback(double[] priceSeries) {
        double halflife = Halflife.halflife(priceSeries);

        int lookback = realiseLookback(halflife);
        return lookback;
    }

    private static int realiseLookback(double halflife) {
        int lookback;

        if (halflife >= 20.0)
            lookback = (int) Math.round(halflife);
        else
            lookback = 20;

        return lookback;
    }
}