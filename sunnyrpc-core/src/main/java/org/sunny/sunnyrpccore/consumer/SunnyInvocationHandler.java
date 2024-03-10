package org.sunny.sunnyrpccore.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.sunny.sunnyrpccore.api.RpcRequest;
import org.sunny.sunnyrpccore.api.RpcResponse;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

public class SunnyInvocationHandler implements InvocationHandler {
    final static MediaType JSONTYPE = MediaType.get("application/json; charset=utf-8");
    Class<?> service;
    
    public SunnyInvocationHandler(Class<?> clazz){
        this.service = clazz;
    }
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setService(service.getCanonicalName());
        rpcRequest.setMethod(method.getName());
        rpcRequest.setParams(args);
        
        RpcResponse rpcResponse = post(rpcRequest);
        
        if (rpcResponse.isStatus()){
            JSONObject jsonData = (JSONObject) rpcResponse.getData();
            return jsonData.toJavaObject(method.getReturnType());
        }
        return null;
    }
    
    OkHttpClient client = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(16,60,TimeUnit.SECONDS))
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .connectTimeout(1, TimeUnit.SECONDS)
            .build();
    
    private RpcResponse post(final RpcRequest rpcRequest) throws IOException {
        String reqJson = JSON.toJSONString(rpcRequest);
        Request request = new Request.Builder()
                .url("http://localhost:9999/")
                .post(RequestBody.create(reqJson, JSONTYPE))
                .build();
        String respJson = client.newCall(request).execute().body().string();
        return JSON.parseObject(respJson,RpcResponse.class);
    }
}
