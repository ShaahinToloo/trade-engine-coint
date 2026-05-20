package engine.exceptions;

public final class BrokenCodeException extends RuntimeException {
    public BrokenCodeException(String msg) {
        super(msg);
    }

    public BrokenCodeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}