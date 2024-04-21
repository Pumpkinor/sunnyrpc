package org.sunny.sunnyrpccore.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RpcException extends RuntimeException{
    private String ErrorCode;
    public RpcException() {
    }
    
    public RpcException(final String message) {
        super(message);
    }
    
    public RpcException(final String message, final Throwable cause) {
        super(message, cause);
    }
    
    public RpcException(final Throwable cause) {
        super(cause);
    }
    
    public RpcException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
    public RpcException(Throwable cause, String errCode) {
        super(cause);
        this.ErrorCode = errCode;
    }
    
    public RpcException(String message, String errCode) {
        super(message);
        this.ErrorCode = errCode;
    }
    
    // X => 技术类异常：
    // Y => 业务类异常：
    // Z => unknown, 搞不清楚，再归类到X或Y
    public static final String SocketTimeoutEx = "X001" + "-" + "http_invoke_timeout";
    public static final String NoSuchMethodEx  = "X002" + "-" + "method_not_exists";
    public static final String ExceedLimitEx  = "X003" + "-" + "tps_exceed_limit";
    public static final String UnknownEx  = "Z001" + "-" + "unknown";
    
}
