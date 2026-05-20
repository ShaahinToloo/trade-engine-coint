package engine.execution;

import engine.constants.ExecutionConstants;
import engine.logger.InfoLogger;

public class OrderGateway {
    Python py = null;

    public void refresh() {
        // TODO
    }

    public void reconnect(InfoLogger log) {
        if (py != null) {
            py.closeInterp();
        }

        py = new Python(ExecutionConstants.SYMBOLS);
        py.setGMTOffest(ExecutionConstants.GMT_OFFSET);
        py.setOverallBalance((float) ExecutionConstants.INIT_BALANCE);

        for (int i = 1; i < 4; i++) {

            boolean loginStatue = py.login();
            if (loginStatue) {
                break;
            }

            try {
                Thread.sleep(1_000 * i);
            } catch (InterruptedException e) {
                log.warn("Thread Interrupted while logging into account");
            }
        }
    }

    public void cancelAll() {
        py.terminateAllPositions();
        py.closeInterp();
    }
}
