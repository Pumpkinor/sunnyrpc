package org.sunny.sunnyrpccore.consumer;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sunny.sunnyrpccore.api.Filter;
import org.sunny.sunnyrpccore.api.RpcContext;
import org.sunny.sunnyrpccore.api.RpcRequest;
import org.sunny.sunnyrpccore.api.RpcResponse;
import org.sunny.sunnyrpccore.consumer.http.OkHttpInvoker;
import org.sunny.sunnyrpccore.exception.RpcException;
import org.sunny.sunnyrpccore.governance.SlidingTimeWindow;
import org.sunny.sunnyrpccore.meta.InstanceMeta;
import org.sunny.sunnyrpccore.utils.MethodUtils;
import org.sunny.sunnyrpccore.utils.TypeUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SunnyInvocationHandler implements InvocationHandler {
    Class<?> service;
    RpcContext rpcContext;
    HttpInvoker httpInvoker;
    
    final Set<InstanceMeta> isolatedProviders = new HashSet<>();
    final Set<InstanceMeta> openHalfProviders = new HashSet<>();
    final Map<String, SlidingTimeWindow> windowsMap = new HashMap<>();
    
    ScheduledExecutorService scheduledExecutorService;
    public SunnyInvocationHandler(Class<?> clazz, RpcContext rpcContext){
        this.service = clazz;
        this.rpcContext = rpcContext;
        this.httpInvoker= new OkHttpInvoker(Long.parseLong(rpcContext.getParameters().get("consumer.timeout")));
//        探活线程
        this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
        this.scheduledExecutorService.scheduleWithFixedDelay(this::openHalf, 10, 60, TimeUnit.SECONDS);
    }
    
    private void openHalf() {
        openHalfProviders.clear();
        openHalfProviders.addAll(isolatedProviders);
        log.debug("half open isolate provider {}" , openHalfProviders);
    }
    
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
//        屏蔽一些方法
        String methodName = method.getName();
        if (MethodUtils.checkLocalMethod(methodName)){
            return null;
        }
        RpcRequest rpcRequest = getRpcRequest(method, args);
        int retries = Integer.parseInt(rpcContext.getParameters().get("consumer.retries"));
        int retryTimes = 0;
        while (retries-retryTimes++ >= 0){
            try{
                for (final Filter filter : rpcContext.getFilters()) {
                    Object preResult = filter.preFilter(rpcRequest);
                    //          TODO  这个if 会导致无法链式执行filter吧
                    if (preResult != null){
                        log.debug(filter.getClass().getName() + "=====> preFilter " + preResult);
                        return preResult;
                    }
                }
                List<InstanceMeta> instances = null;
                InstanceMeta instance = null;
//              如果半开集合中存在provider需要进行探活 则优先进行探活处理
                synchronized (openHalfProviders){
                    if (openHalfProviders.isEmpty()){
                        instances = rpcContext.getRouter().route(rpcContext.getProviders());
                        instance = rpcContext.getLoadBalancer().choose(instances);
                        log.debug("loadBalancer.choose(instances) ==> " + instance);
                    }else {
                        instance = openHalfProviders.iterator().next();
                        openHalfProviders.remove(instance);
                        log.debug("half open choose(instances) ==> " + instance);
                    }
                }
                RpcResponse<?> rpcResponse;
                Object result;
                String url = instance.toUrl();
                try {
                    rpcResponse = httpInvoker.post(rpcRequest, url);
                    result = castReturnData(method, rpcResponse);
                }catch (Exception ex) {
//                    故障规则统计和隔离
//                    每次异常计数一次 统计30秒内的异常
                    synchronized (windowsMap){
                        SlidingTimeWindow window = windowsMap.computeIfAbsent(url, k -> new SlidingTimeWindow());
                        window.record(System.currentTimeMillis());
                        log.debug("instance {} in window with {}", url, window.getSum());
                        //                    发生了10次故障就进行隔离
                        if (window.getSum() >= 10){
                            isolate(instance);
                        }
                    }
                    throw ex;
                }
//                探活成功恢复
                synchronized (isolatedProviders){
                    List<InstanceMeta> providers = rpcContext.getProviders();
                    if (!providers.contains(instance)){
                        isolatedProviders.remove(instance);
                        providers.add(instance);
                        log.debug("reuse instance {} ", instance);
                        log.debug("isolatedProviders {} ", isolatedProviders);
                        log.debug("providers {} ", providers);
                    }
                }
                for (final Filter filter : rpcContext.getFilters()) {
                    Object postResult = filter.postFilter(rpcRequest, rpcResponse, result);
                    //          TODO  这个if 会导致无法链式执行filter吧
                    if(postResult != null) {
                        log.debug(filter.getClass().getName() + "=====> postFilter " + postResult);
                        return postResult;
                    }
                }
                return result;
            }catch (RuntimeException ex){
                if (!(ex.getCause() instanceof SocketTimeoutException)){
                    throw ex;
                }else {
                    log.info("retrying times : " + retryTimes);
                }
            }
        }
        return null;
    }
    
    private void isolate(final InstanceMeta instance) {
        log.debug("isolate instance {}" , instance);
        isolatedProviders.add(instance);
        log.debug("isolatedProviders {}" , isolatedProviders);
        rpcContext.getProviders().remove(instance);
        log.debug("usingProviders {}" , rpcContext.getProviders());
    }
    
    @Nullable
    private static Object castReturnData(final Method method, final RpcResponse<?> rpcResponse) {
        if (rpcResponse.isStatus()){
            Object data = rpcResponse.getData();
            return TypeUtils.parseReturnData(method, data);
        }else {
            RpcException exception = rpcResponse.getEx();
            if(exception != null) {
                log.error("response error.", exception);
                throw exception;
            }
            return null;
        }
    }
    
    @NotNull
    private RpcRequest getRpcRequest(final Method method, final Object[] args) {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setService(service.getCanonicalName());
        rpcRequest.setMethodSign(MethodUtils.getMethodSign(method));
        rpcRequest.setArgs(args);
        return rpcRequest;
    }
}
