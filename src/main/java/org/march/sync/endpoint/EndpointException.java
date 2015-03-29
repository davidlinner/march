package org.march.sync.endpoint;

public class EndpointException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -6284587797883152069L;

    public EndpointException() {
    }

    public EndpointException(String message) {
        super(message);
    }

    public EndpointException(Throwable cause) {
        super(cause);
    }

    public EndpointException(String message, Throwable cause) {
        super(message, cause);
    }

    public EndpointException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
