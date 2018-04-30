package io.viewserver.server.steps;

public class TestLogicException extends RuntimeException{
    public TestLogicException() {
        super();
    }

    public TestLogicException(String message) {
        super(message);
    }

    public TestLogicException(String message, Throwable cause) {
        super(message, cause);
    }

    public TestLogicException(Throwable cause) {
        super(cause);
    }

    protected TestLogicException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
