package org.sunny.sunnyrpccore.provider;

import org.sunny.sunnyrpccore.api.RpcRequest;
import org.sunny.sunnyrpccore.api.RpcResponse;
import org.sunny.sunnyrpccore.meta.ProviderMeta;
import org.sunny.sunnyrpccore.utils.TypeUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public class ProviderInvoker {
    private final ProviderBootstrap providerBootstrap;
    
    public ProviderInvoker(ProviderBootstrap providerBootstrap){
        this.providerBootstrap = providerBootstrap;
    }
    
    public RpcResponse<Object> invokeRequest(RpcRequest request) {
        RpcResponse<Object> rpcResponse = new RpcResponse<>();
        List<ProviderMeta> providerMetas = providerBootstrap.getSkeleton().get(request.getService());
        try {
            ProviderMeta providerMeta = findProviderMeta(providerMetas, request.getMethodSign());
            // TODO test providerMeta == null
            Method method = providerMeta.getMethod();
            Object[] actualParmas = processArgs(request.getParams(), method.getParameterTypes());
            Object result = method.invoke(providerMeta.getServiceImpl(), actualParmas);
            rpcResponse.setStatus(true);
            rpcResponse.setData(result);
            return rpcResponse;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            rpcResponse.setEx(new RuntimeException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e){
            e.printStackTrace();
            rpcResponse.setEx(new RuntimeException(e.getMessage()));
        }
        return rpcResponse;
    }
    
    private Object[] processArgs(final Object[] params, final Class<?>[] parameterTypes) {
        if (params == null || params.length == 0){
            return params;
        }
        Object[] actualParmas = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            actualParmas[i] = TypeUtils.cast(params[i], parameterTypes[i]);
        }
        return actualParmas;
    }
    
    private ProviderMeta findProviderMeta(final List<ProviderMeta> providerMetas, final String methodSign) {
        Optional<ProviderMeta> optional = providerMetas.stream()
                .filter(x -> x.getMethodSign().equals(methodSign)).findFirst();
        return optional.orElse(null);
    }
    
}
