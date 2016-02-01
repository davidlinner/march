package org.march.sync.endpoint;

/**
 * Created by dli on 01.02.2016.
 */
public class BucketDeliveryException extends Exception{
    public BucketDeliveryException() {
    }

    public BucketDeliveryException(Throwable cause) {
        super(cause);
    }

    public BucketDeliveryException(String message) {
        super(message);
    }

    public BucketDeliveryException(String message, Throwable cause) {
        super(message, cause);
    }

    public BucketDeliveryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
