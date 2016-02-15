package org.march.sync.channel;

/**
 * Created by dli on 01.02.2016.
 */
public class CommitException extends Exception{
    public CommitException() {
    }

    public CommitException(Throwable cause) {
        super(cause);
    }

    public CommitException(String message) {
        super(message);
    }

    public CommitException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommitException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
