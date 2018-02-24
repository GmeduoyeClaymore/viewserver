package io.viewserver.operators;

public class OperatorNotFoundException extends RuntimeException{

    public OperatorNotFoundException(String message) {
        super(message);
    }

    public OperatorNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public OperatorNotFoundException(Throwable cause) {
        super(cause);
    }

    public OperatorNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public static OperatorNotFoundException forName(String operatorName){
        throw new OperatorNotFoundException(String.format("Unable to find operator named \"%s\"",operatorName));
    }
}
