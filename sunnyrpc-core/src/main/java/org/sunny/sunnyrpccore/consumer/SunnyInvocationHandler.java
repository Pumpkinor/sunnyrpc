package org.sunny.sunnyrpccore.consumer;

import com.alibaba.fastjson.JSON;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;
import org.sunny.sunnyrpccore.api.RpcContext;
import org.sunny.sunnyrpccore.api.RpcRequest;
import org.sunny.sunnyrpccore.api.RpcResponse;
import org.sunny.sunnyrpccore.utils.MethodUtils;
import org.sunny.sunnyrpccore.utils.TypeUtils;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SunnyInvocationHandler implements InvocationHandler {
    final static MediaType JSONTYPE = MediaType.get("application/json; charset=utf-8");
    Class<?> service;
    RpcContext rpcContext;
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
        List<String> urls = rpcContext.getRouter().route(rpcContext.getProviders());
        String url = (String) rpcContext.getLoadBalancer().choose(urls);
        RpcResponse<?> rpcResponse = post(rpcRequest, url);
        
        if (rpcResponse.isStatus()){
            Object data = rpcResponse.getData();
            return TypeUtils.parseReturnData(method, data);
        }else {
            Exception ex = rpcResponse.getEx();
            System.out.println("exsssssss->>>>>>>>");
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
    
    OkHttpClient client = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(16,60,TimeUnit.SECONDS))
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .connectTimeout(1, TimeUnit.SECONDS)
            .build();
    
    private RpcResponse<?> post(final RpcRequest rpcRequest, final String url) throws IOException {
        String reqJson = JSON.toJSONString(rpcRequest);
        System.out.println("call url is >>>>>>>>> " + url);
        System.out.println("reqJson is >>>>>>>>> " + reqJson);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(reqJson, JSONTYPE))
                .build();
        String respJson = client.newCall(request).execute().body().string();
        System.out.println("respJson is >>>>>>>>> " + respJson);
        return JSON.parseObject(respJson,RpcResponse.class);
    }
}
