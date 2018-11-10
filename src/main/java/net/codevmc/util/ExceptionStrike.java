package net.codevmc.util;

public class ExceptionStrike extends RuntimeException {

    private final Exception exception;

    public ExceptionStrike(Exception exception) {
        this.exception = exception;
    }

    public Exception getException() {
        return exception;
    }
}
