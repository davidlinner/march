package org.march.sync.channel;

public class ChannelException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -6284587797883152069L;

    public ChannelException() {
    }

    public ChannelException(String message) {
        super(message);
    }

    public ChannelException(Throwable cause) {
        super(cause);
    }

    public ChannelException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChannelException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
