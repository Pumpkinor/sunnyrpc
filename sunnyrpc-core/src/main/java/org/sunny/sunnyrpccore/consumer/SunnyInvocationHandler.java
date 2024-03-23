package org.sunny.sunnyrpccore.consumer;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.sunny.sunnyrpccore.api.RpcContext;
import org.sunny.sunnyrpccore.api.RpcRequest;
import org.sunny.sunnyrpccore.api.RpcResponse;
import org.sunny.sunnyrpccore.consumer.http.OkHttpInvoker;
import org.sunny.sunnyrpccore.meta.InstanceMeta;
import org.sunny.sunnyrpccore.utils.MethodUtils;
import org.sunny.sunnyrpccore.utils.TypeUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
@Slf4j
public class SunnyInvocationHandler implements InvocationHandler {
    Class<?> service;
    RpcContext rpcContext;
    HttpInvoker httpInvoker = new OkHttpInvoker();
    
    public SunnyInvocationHandler(Class<?> clazz, RpcContext rpcContext){
        this.service = clazz;
        this.rpcContext = rpcContext;
    }
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
//        屏蔽一些方法
        String methodName = method.getName();
        if (MethodUtils.checkLocalMethod(methodName)){
            return null;
        }
        RpcRequest rpcRequest = getRpcRequest(method, args);
        List<InstanceMeta> instances = rpcContext.getRouter().route(rpcContext.getProviders());
        InstanceMeta instance = rpcContext.getLoadBalancer().choose(instances);
        log.debug("loadBalancer.choose(instances) ==> " + instance);
        RpcResponse<?> rpcResponse = httpInvoker.post(rpcRequest, instance.toUrl());
        if (rpcResponse.isStatus()){
            Object data = rpcResponse.getData();
            return TypeUtils.parseReturnData(method, data);
        }else {
            Exception ex = rpcResponse.getEx();
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
    
    @NotNull
    private RpcRequest getRpcRequest(final Method method, final Object[] args) {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setService(service.getCanonicalName());
        rpcRequest.setMethodSign(MethodUtils.getMethodSign(method));
        rpcRequest.setParams(args);
        return rpcRequest;
    }
}
