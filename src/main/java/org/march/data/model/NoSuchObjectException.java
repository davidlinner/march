package org.march.data.model;

import org.march.data.model.ObjectException;

public class NoSuchObjectException extends ObjectException {

    /**
     * 
     */
    private static final long serialVersionUID = -7905951637249833690L;

    public NoSuchObjectException() {
        // TODO Auto-generated constructor stub
    }

    public NoSuchObjectException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public NoSuchObjectException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    public NoSuchObjectException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    public NoSuchObjectException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        // TODO Auto-generated constructor stub
    }

}
