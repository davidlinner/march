package org.march.sync.backlog;

public class BacklogException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -6284587797883152069L;

    public BacklogException() {
    }

    public BacklogException(String message) {
        super(message);
    }

    public BacklogException(Throwable cause) {
        super(cause);
    }

    public BacklogException(String message, Throwable cause) {
        super(message, cause);
    }

    public BacklogException(String message, Throwable cause,
                            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
