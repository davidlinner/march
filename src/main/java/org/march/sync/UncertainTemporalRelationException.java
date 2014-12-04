package org.march.sync;

public class UncertainTemporalRelationException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public UncertainTemporalRelationException() {
    }

    public UncertainTemporalRelationException(String message) {
        super(message);
    }

    public UncertainTemporalRelationException(Throwable cause) {
        super(cause);
    }

    public UncertainTemporalRelationException(String message, Throwable cause) {
        super(message, cause);
    }

    public UncertainTemporalRelationException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
