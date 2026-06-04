package engine.execution;

import java.util.List;
import java.util.Map;

import engine.caster.ObjectCaster;
import engine.constants.ExecutionConstants;
import jep.JepException;
import jep.SharedInterpreter;

public class JEPLayer {
    private List<String> symbols = ExecutionConstants.SYMBOLS;
    private SharedInterpreter interp;

    public JEPLayer() throws JepException {
        this.interp = new SharedInterpreter();
        // interp.eval("import sys");
        // interp.eval("sys.path.insert(0, 'app/src/main/java/engine/execution')");

        this.interp.runScript("../tools/python/src/execution/pythonMethods.py");
        this.interp.runScript("../tools/python/src/fetch/cleanLiveDataInitPrice.py");
        this.interp.runScript("../tools/python/src/fetch/cleanLiveDataNewPrice.py");
        this.interp.runScript("../tools/python/src/fetch/FetchMT5Data.py");
    }

    public void closeInterp() {
        if (interp != null) {
            interp.close();
        }
    }

    /*
     * Python Order Execution Methods
     */

    public void setGMTOffest(float offset) {
        interp.invoke("set_gmt_offset", (Object) offset);
    }

    public void setOverallBalance(float OverallBalance) {
        interp.invoke("set_overall_balance", (Object) OverallBalance);
    }

    public boolean login() {
        System.out.println("Logging in...");
        boolean loginStatue = (boolean) interp.invoke("login");
        return loginStatue;
    }

    public Object newNewsClass() throws JepException {
        interp.eval("from pythonMethods import news");
        interp.eval("news_obj = news()");
        return interp.getValue("news_obj");
    }

    public void fetchNews(Object newsObj) {
        interp.invoke("getattr", newsObj, "fetch_metalsmine_news");
    }

    public boolean newsSleepUntilAllowance(Object newsObj) {
        return (boolean) interp.invoke("getattr", newsObj, "news_sleep_until_allowance");
    }

    public double getBalance() {
        return (double) interp.invoke("account_balance");
    }

    public void terminateAllPositions() {
        interp.invoke("terminate_all_positions");
    }

    public double volumeCalculator(double entry, double sl, double risk, double balance) {
        @SuppressWarnings("unchecked")
        Map<String, Object> info = (Map<String, Object>) interp.invoke("get_symbol_info", this.symbols);
        int contractSize = (int) info.get("trade_contract_size");

        double riskAmount = balance * (risk / 100);
        double price_diff = Math.abs(entry - sl);
        double dollarRiskPerLot = price_diff * contractSize;
        double volume = riskAmount / dollarRiskPerLot;
        volume = Math.round(volume * 100.0) / 100.0;

        System.out.printf("Position size calculation: Risk=%.2f, Amount=$%.2f, Volume=%.2f lots", risk, riskAmount,
                volume);
        return volume;
    }

    public void executeOrder(double entry, double sl, double tp, double volume) {
        boolean conflictingOrderFound = (boolean) interp.invoke("check_for_conflicting_order", this.symbols, entry, sl,
                tp);

        if (!conflictingOrderFound) {
            // python method
            int signal = 0;
            if (entry > sl) {
                signal = 1;
            }

            interp.invoke("execute_order", signal, volume, this.symbols, entry, sl, tp);
        }
    }

    /*
     * Fetch Data
     */

    /*
     * Fetch Initial Data
     */

    List<String> indexInitialData;

    public double[][][] getInitialData() {
        Object[] dataframes = new Object[ExecutionConstants.SYMBOLS.size()];
        this.interp.set("bars_var", ExecutionConstants.SEQ_LENGTH);

        for (int i = 0; i < dataframes.length; i++) {
            this.interp.set("symbol_var", ExecutionConstants.SYMBOLS.get(i));
            dataframes[i] = this.interp.invoke("fetch(bars_var, symbol_var)");
        }

        this.interp.invoke("cleanDataLiveInit",
                new Object[] { dataframes, ExecutionConstants.SYMBOLS_TO_REVERSE, ExecutionConstants.SYMBOLS });

        List<List<List<Double>>> data = (List<List<List<Double>>>) interp.invoke("get_ret_list");
        double[][][] retArr3D = ObjectCaster.toDouble3D(data);

        List<Object> rawIndices = (List<Object>) interp.invoke("get_indices");
        this.indexInitialData = (List<String>) rawIndices.get(0);

        return retArr3D;
    }

    public List<String> getInitialIndex() {
        return this.indexInitialData;
    }

    /*
     * Fetch New Data
     */

    List<String> indexNewData;

    public double[][][] getNewData() {
        Object[] dataframes = new Object[ExecutionConstants.SYMBOLS.size()];
        this.interp.set("bars_var", 1);

        for (int i = 0; i < dataframes.length; i++) {
            this.interp.set("symbol_var", ExecutionConstants.SYMBOLS.get(i));
            dataframes[i] = this.interp.invoke("fetch(bars_var, symbol_var)");
        }

        this.interp.invoke("cleanDataLiveNewPrice",
                new Object[] { dataframes, ExecutionConstants.SYMBOLS_TO_REVERSE, ExecutionConstants.SYMBOLS });

        List<List<List<Double>>> data = (List<List<List<Double>>>) interp.invoke("get_ret_list");
        double[][][] retArr3D = ObjectCaster.toDouble3D(data);

        List<Object> rawIndices = (List<Object>) interp.invoke("get_indices");
        this.indexNewData = (List<String>) rawIndices.get(0);

        return retArr3D;
    }

    public List<String> getNewIndex() {
        return this.indexNewData;
    }
}
