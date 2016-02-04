package org.march.sync.channel;

/**
 * Created by dli on 01.02.2016.
 */
public class ChannelException extends Exception{
    public ChannelException() {
    }

    public ChannelException(Throwable cause) {
        super(cause);
    }

    public ChannelException(String message) {
        super(message);
    }

    public ChannelException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChannelException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
