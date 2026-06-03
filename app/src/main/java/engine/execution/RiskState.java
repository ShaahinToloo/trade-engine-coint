package engine.execution;

public class RiskState {

    public class State {
        public boolean killSwitch;
        // TODO: Add other risk metrics to log
    }

    public State state = new State();

    private volatile boolean killSwitch = false;

    public RiskState() {
        state.killSwitch = killSwitch;
    }

    public void enableKillSwitch() {
        killSwitch = true;
        updateState();
    }

    public boolean killSwitchActive() {
        return killSwitch;
    }

    private void updateState() {
        state.killSwitch = killSwitch;
    }
}