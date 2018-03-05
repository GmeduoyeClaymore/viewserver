package com.shotgun.viewserver.user;

public class UserNotFoundException extends ApplicationDataException{
    public UserNotFoundException() {
    }

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserNotFoundException(Throwable cause) {
        super(cause);
    }

    public UserNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    static UserNotFoundException fromUserId(String userId){
        return new UserNotFoundException(String.format("Unable to find user with id \"%s\"",userId));
    }
}
