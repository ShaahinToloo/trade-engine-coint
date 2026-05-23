package engine.execution;

import java.util.List;
import java.util.Map;

import jep.JepException;
import jep.SharedInterpreter;

public class Python {
    private SharedInterpreter interp;
    private List<String> symbols;

    public Python(List<String> symbols) throws JepException {
        SharedInterpreter interp = new SharedInterpreter();
        interp.eval("import sys");
        interp.eval("sys.path.insert(0, 'app/src/main/java/engine/execution')");

        String pythonPath = "../tools/python/src/execution/pythonMethods.py";
        interp.runScript(pythonPath);

        this.interp = interp;
        this.symbols = symbols;
    }

    public void closeInterp() {
        if (interp != null) {
            interp.close();
        }
    }

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

        System.out.printf("Position size calculation: Risk=%0.2f, Amount=$0.2f, Volume=%0.2f lots", risk, riskAmount,
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
}
