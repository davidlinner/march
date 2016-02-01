package org.march.sync.context;

public class ContextException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -6284587797883152069L;

    public ContextException() {
    }

    public ContextException(String message) {
        super(message);
    }

    public ContextException(Throwable cause) {
        super(cause);
    }

    public ContextException(String message, Throwable cause) {
        super(message, cause);
    }

    public ContextException(String message, Throwable cause,
                            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
