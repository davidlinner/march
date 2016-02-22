package org.march.server.endpoint;

/**
 * Created by dli on 22.02.2016.
 */
public enum ErrorCode {

    UNKNOWN(0),

    RESOURCE_NOT_FOUND(404),
    METHOD_NOT_ALLOWED(405),
    UNSUPPORTED_MEDIA_TYPE(415),

    INTERNAL_SERVER_ERROR(500),
    DUPLICATE_REGISTRATION(550);

    private int errorNumber;

    ErrorCode(int errorNumber){
        this.errorNumber = errorNumber;
    }

    public int getErrorNumber() {
        return errorNumber;
    }

    public static ErrorCode fromNumber(int code){
        for(ErrorCode errorCode: ErrorCode.values()){
            if(errorCode.getErrorNumber() == code) return errorCode;
        }
        return UNKNOWN;
    }
}
