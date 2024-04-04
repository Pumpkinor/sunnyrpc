package org.sunny.sunnyrpccore.exception;

public class ZkException extends RuntimeException{
    public ZkException() {
    }
    
    public ZkException(final String message) {
        super(message);
    }
    
    public ZkException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
    public ZkException(final Throwable cause) {
        super(cause);
    }
    
    public ZkException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
