package org.march.sync.master;

/**
 * Created by dli on 22.02.2016.
 */
public class DuplicateRegistrationException extends MasterException {
    public DuplicateRegistrationException() {
    }

    public DuplicateRegistrationException(String message) {
        super(message);
    }

    public DuplicateRegistrationException(Throwable cause) {
        super(cause);
    }

    public DuplicateRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicateRegistrationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
