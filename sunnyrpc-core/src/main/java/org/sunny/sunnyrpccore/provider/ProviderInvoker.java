package org.sunny.sunnyrpccore.provider;

import lombok.extern.slf4j.Slf4j;
import org.sunny.sunnyrpccore.api.RpcContext;
import org.sunny.sunnyrpccore.api.RpcRequest;
import org.sunny.sunnyrpccore.api.RpcResponse;
import org.sunny.sunnyrpccore.config.ProviderConfigProperties;
import org.sunny.sunnyrpccore.exception.RpcException;
import org.sunny.sunnyrpccore.governance.SlidingTimeWindow;
import org.sunny.sunnyrpccore.meta.ProviderMeta;
import org.sunny.sunnyrpccore.utils.TypeUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class ProviderInvoker {
    private final ProviderBootstrap providerBootstrap;
    private final ProviderConfigProperties providerConfigProperties;
    final Map<String, SlidingTimeWindow> windows = new HashMap<>();
    final Integer trafficControl;
    //    todo 改成map 去针对每个服务进行限流（配置文件也要针对改动）
    //    todo 多个实例 共享一个限流值 （把map放到redis）
    public ProviderInvoker(ProviderBootstrap providerBootstrap, ProviderConfigProperties providerConfigProperties){
        this.providerBootstrap = providerBootstrap;
        this.providerConfigProperties = providerConfigProperties;
        this.trafficControl = Integer.parseInt(providerConfigProperties.getMetas().getOrDefault("tc", "20"));
    }
    
    public RpcResponse<Object> invokeRequest(RpcRequest request) {
        log.debug(" ===> ProviderInvoker.invoke(request:{})", request);
        if(!request.getParams().isEmpty()) {
            request.getParams().forEach(RpcContext::setContextParameter);
        }
        RpcResponse<Object> rpcResponse = new RpcResponse<>();
        String service = request.getService();
        int trafficControl = Integer.parseInt(providerConfigProperties.getMetas().getOrDefault("tc", "20"));
        log.debug(" ===>> trafficControl:{} for {}", trafficControl, service);
        synchronized (windows) {
            SlidingTimeWindow window = windows.computeIfAbsent(service, k -> new SlidingTimeWindow());
            if (window.calcSum() >= trafficControl) {
                String trafficMsg = "service " + service + " invoked in 30s/[" +
                        window.getSum() + "] larger than tpsLimit = " + trafficControl;
                System.out.println(window);
                rpcResponse.setEx(new RpcException(trafficMsg, RpcException.ExceedLimitEx));
                return rpcResponse;
            }
            
            window.record(System.currentTimeMillis());
            log.debug("service {} in window with {}", service, window.getSum());
        }
        try{
            List<ProviderMeta> providerMetas = providerBootstrap.getSkeleton().get(service);
            ProviderMeta providerMeta = findProviderMeta(providerMetas, request.getMethodSign());
            // TODO test providerMeta == null
            Method method = providerMeta.getMethod();
            Object[] actualParmas = processArgs(request.getArgs(), method.getParameterTypes(), method.getGenericParameterTypes());
            Object result = method.invoke(providerMeta.getServiceImpl(), actualParmas);
            rpcResponse.setStatus(true);
            rpcResponse.setData(result);
            return rpcResponse;
        } catch (InvocationTargetException e) {
            rpcResponse.setEx(new RpcException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e){
            rpcResponse.setEx(new RpcException(e.getMessage()));
        }
        return rpcResponse;
    }
    
    private Object[] processArgs(final Object[] params, final Class<?>[] parameterTypes, final Type[] genericParameterTypes) {
        if (params == null || params.length == 0){
            return params;
        }
        Object[] actualParmas = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            actualParmas[i] = TypeUtils.castGeneric(params[i], parameterTypes[i], genericParameterTypes[i]);
        }
        return actualParmas;
    }
    
    private ProviderMeta findProviderMeta(final List<ProviderMeta> providerMetas, final String methodSign) {
        Optional<ProviderMeta> optional = providerMetas.stream()
                .filter(x -> x.getMethodSign().equals(methodSign)).findFirst();
        return optional.orElse(null);
    }
    
}
