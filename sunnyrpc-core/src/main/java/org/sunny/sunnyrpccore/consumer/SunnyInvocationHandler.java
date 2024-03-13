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
import org.sunny.sunnyrpccore.utils.MethodUtils;

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
//        屏蔽一些方法
        String methodName = method.getName();
        if (MethodUtils.checkLocalMethod(methodName)){
            return null;
        }
        RpcRequest rpcRequest = new RpcRequest();
        rpcRequest.setService(service.getCanonicalName());
        rpcRequest.setMethodSign(MethodUtils.getMethodSign(method));
        rpcRequest.setParams(args);
        
        RpcResponse rpcResponse = post(rpcRequest);
        
        if (rpcResponse.isStatus()){
//            TODO 基本类型无法转换为json如何处理
            Object data = rpcResponse.getData();
            if(data instanceof JSONObject jsonResult) {
                return jsonResult.toJavaObject(method.getReturnType());
            } else {
                return data;
            }
        }else {
            Exception ex = rpcResponse.getEx();
            System.out.println("exsssssss->>>>>>>>");
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
    
    OkHttpClient client = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(16,60,TimeUnit.SECONDS))
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .connectTimeout(1, TimeUnit.SECONDS)
            .build();
    
    private RpcResponse post(final RpcRequest rpcRequest) throws IOException {
        String reqJson = JSON.toJSONString(rpcRequest);
        System.out.println("reqJson is >>>>>>>>> " + reqJson);
        Request request = new Request.Builder()
                .url("http://localhost:9999/")
                .post(RequestBody.create(reqJson, JSONTYPE))
                .build();
        String respJson = client.newCall(request).execute().body().string();
        System.out.println("respJson is >>>>>>>>> " + respJson);
        return JSON.parseObject(respJson,RpcResponse.class);
    }
}
