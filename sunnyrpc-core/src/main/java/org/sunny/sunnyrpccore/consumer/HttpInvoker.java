package org.sunny.sunnyrpccore.consumer;

import org.sunny.sunnyrpccore.api.RpcRequest;
import org.sunny.sunnyrpccore.api.RpcResponse;

public interface HttpInvoker {

    RpcResponse<?> post(RpcRequest rpcRequest, String url);

}
