package engine.core;

import java.nio.file.Path;
import java.util.List;

import engine.constants.BacktestConstants;
import engine.strategyLogic.BBLogic;
import engine.strategyLogic.Logic;
import engine.trade.Trade;
import engine.trade.TradeManager;

public class BBCore extends Core {

    private static class StrategyContext {
        static double[][] yPort = null;
        static final List<Trade> openTrades = TradeManager.openTrades;
        static int lookback = BacktestConstants.MR_LOOKBACK;
    }

    public static class CoreGetter extends Core.CoreGetter {
        public static double lastAvg;
        public static double lastStd;
    }

    public final BBLogic strategyLogicChild;

    private int countUntilTradeAgreement = 0;

    public BBCore(
            double[][] priceMatrix,
            List<String> datetimeIndex,
            Path runPath,
            double entryZscore, double exitZscore) {
        super(priceMatrix, datetimeIndex, runPath);
        this.strategyLogicChild = new BBLogic(entryZscore, exitZscore);
    }

    @Override
    protected void prepareStrategyContext() {
        StrategyContext.yPort = super.yPort;
        StrategyContext.lookback = super.lookback;
    }

    @Override
    protected void processStrategy() {
        double[][] y = StrategyContext.yPort;
        List<Trade> openTrades = StrategyContext.openTrades;
        Logic.LogicContext.lookback = StrategyContext.lookback;

        strategyLogicChild.reValue(y);
        strategyLogicChild.executeLogic();

        TradeContext.tradeIndicesToEliminate = new int[openTrades.size()];
        strategyLogicChild.processOpenTradesForExit(openTrades, TradeContext.tradeIndicesToEliminate);
    
        processCoreGetter();
    }

    private void processCoreGetter() {
        BBCore.CoreGetter.lastAvg = strategyLogicChild.lastAvg;
        BBCore.CoreGetter.lastStd = strategyLogicChild.lastStd;
    }

    @Override
    protected boolean isWithinTradingWindow() {
        double[][] y = StrategyContext.yPort;

        boolean isFlat = true;
        for (int i = y[0].length-1; i > y[0].length-50; i--) {
            double diff = y[0][i] - y[0][i-1];
            if (Math.abs(diff) >= 1e-15) {
                isFlat = false;
            }
        }

        if (isFlat) {
            countUntilTradeAgreement = 15;
            return false;
        }

        if (0 == countUntilTradeAgreement--) {
            countUntilTradeAgreement = 0;
            return true;
        }
        return false;
    }

    @Override
    protected void generateTrade(int outerLoopIdx) {
        if (Logic.LogicContext.validTrade) {
            TradeContext.trade = new Trade(outerLoopIdx, Logic.LogicContext.entry, Logic.LogicContext.tradeType);
        }
    }
}
