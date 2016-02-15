package org.march.data.model;

import org.march.data.model.ObjectException;

public class DuplicateObjectException extends ObjectException {

    /**
     * 
     */
    private static final long serialVersionUID = 3677316040367878234L;

    public DuplicateObjectException() {
        // TODO Auto-generated constructor stub
    }

    public DuplicateObjectException(String message) {
        super(message);
        // TODO Auto-generated constructor stub
    }

    public DuplicateObjectException(Throwable cause) {
        super(cause);
        // TODO Auto-generated constructor stub
    }

    public DuplicateObjectException(String message, Throwable cause) {
        super(message, cause);
        // TODO Auto-generated constructor stub
    }

    public DuplicateObjectException(String message, Throwable cause,
            boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        // TODO Auto-generated constructor stub
    }

}
