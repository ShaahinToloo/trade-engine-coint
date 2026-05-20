package engine.execution;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import engine.constants.PublicConstants;
import engine.heads.BBExecutionEngine;
import engine.heads.ExecutionEngine;
import engine.logger.InfoLogger;

class Watchdog implements Runnable {

    private final ExecutionEngine engine;
    private final AtomicLong lastHeartbeat = new AtomicLong(0);

    private static final long TIMEOUT_MS = 5000;

    InfoLogger log = new InfoLogger(PublicConstants.INFO_LOG_PATH, PublicConstants.INFO_LOG_NAME);

    public Watchdog(double[][][] mohlcv, List<String> datetimeIndex) {
        this.engine = new BBExecutionEngine(mohlcv, datetimeIndex);
    }

    @Override
    public void run() {
        int tryI = 1;

        while (true) {
            try {

                // TODO: Heartbeat must be updated somehow
                long age = System.currentTimeMillis() - lastHeartbeat.get();

                if (age > TIMEOUT_MS) {
                    log.err("Execution thread frozen -> triggering restart");

                    engine.stop();
                    triggerLocalRestart();
                }

                PublicConstants.sleep(1000);

            } catch (Exception e) {
                log.err("Watchdog failure, " + tryI + "th try" + "; " + e);
            }
        }
    }

    private void triggerLocalRestart() {
        // TODO: Do some MT5 refreshing and system syncing. stuff...

        engine.run();
    }
}