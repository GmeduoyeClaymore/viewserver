package com.shotgun.viewserver.user;

public class ApplicationDataException extends RuntimeException{
    public ApplicationDataException() {
    }

    public ApplicationDataException(String message) {
        super(message);
    }

    public ApplicationDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public ApplicationDataException(Throwable cause) {
        super(cause);
    }

    public ApplicationDataException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
