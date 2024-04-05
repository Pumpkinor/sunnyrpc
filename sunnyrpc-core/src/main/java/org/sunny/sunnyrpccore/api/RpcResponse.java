package org.sunny.sunnyrpccore.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sunny.sunnyrpccore.exception.RpcException;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcResponse<T> {
    private boolean status;
    private T data;
    private RpcException ex;
}
