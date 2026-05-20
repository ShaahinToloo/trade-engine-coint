package engine.execution;

import java.nio.file.Files;
import java.nio.file.Path;

import engine.logger.InfoLogger;

public class StateSnapshotStore {
    private final InfoLogger log;

    public StateSnapshotStore(InfoLogger log) {
        this.log = log;
    }

    public void save(RiskState state) {
        try {
            String json = serialize(state);

            // Write json to new file
            // TODO

        } catch (Exception e) {
            log.err("Snapshot failed; " + e);
        }
    }

    public RiskState load() {
        String lastFile = getLastFile();
        try {
            return deserialize(Files.readString(Path.of(lastFile)));
        } catch (Exception e) {
            return new RiskState(); // safe fallback
        }
    }

    private String serialize(RiskState state) {
        // TODO
    }

    private RiskState deserialize(String state) {
        // TODO
    }

    private String getLastFile() {
        // TODO
    }
}
