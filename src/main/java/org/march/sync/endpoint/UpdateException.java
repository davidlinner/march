package org.march.sync.endpoint;

/**
 * Created by dli on 01.02.2016.
 */
public class UpdateException extends Exception{
    public UpdateException() {
    }

    public UpdateException(Throwable cause) {
        super(cause);
    }

    public UpdateException(String message) {
        super(message);
    }

    public UpdateException(String message, Throwable cause) {
        super(message, cause);
    }

    public UpdateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
