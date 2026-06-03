package engine.exceptions;

public final class RecoverableException extends RuntimeException {
    public RecoverableException(String msg) {
        super(msg);
    }

    public RecoverableException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
