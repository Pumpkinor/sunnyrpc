package org.sunny.sunnyrpccore.filter;


import org.sunny.sunnyrpccore.api.Filter;
import org.sunny.sunnyrpccore.api.RpcContext;
import org.sunny.sunnyrpccore.api.RpcRequest;
import org.sunny.sunnyrpccore.api.RpcResponse;

import java.util.Map;

/**
 * 处理上下文参数.
 */
public class ParameterFilter implements Filter {
    @Override
    public Object preFilter(RpcRequest request) {
//        请求前使用RpcContext.ContextParameters设置到rpcRequest
        Map<String, String> params = RpcContext.ContextParameters.get();
        if(!params.isEmpty()) {
            request.getParams().putAll(params);
        }
        return null;
    }

    @Override
    public Object postFilter(RpcRequest request, RpcResponse response, Object result) {
//        请求完成后 清空RpcContext.ContextParameters的ThreadLocal数据
        RpcContext.ContextParameters.get().clear();
        return null;
    }
}
