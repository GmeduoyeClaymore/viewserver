package io.viewserver.client;

/**
 * Created by nick on 10/02/2015.
 */


public class CommandResult{
    boolean status;
    String message;

    public CommandResult(boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
