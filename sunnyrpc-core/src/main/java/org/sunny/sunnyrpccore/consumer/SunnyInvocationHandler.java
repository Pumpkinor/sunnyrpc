package org.sunny.sunnyrpccore.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.jetbrains.annotations.Nullable;
import org.sunny.sunnyrpccore.api.RpcContext;
import org.sunny.sunnyrpccore.api.RpcRequest;
import org.sunny.sunnyrpccore.api.RpcResponse;
import org.sunny.sunnyrpccore.utils.MethodUtils;
import org.sunny.sunnyrpccore.utils.TypeUtils;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SunnyInvocationHandler implements InvocationHandler {
    final static MediaType JSONTYPE = MediaType.get("application/json; charset=utf-8");
    Class<?> service;
    RpcContext rpcContext;
    List<String> providers;
    public SunnyInvocationHandler(Class<?> clazz, RpcContext rpcContext, List<String> providers){
        this.service = clazz;
        this.rpcContext = rpcContext;
        this.providers = providers;
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
        
        List<String> urls = rpcContext.getRouter().route(providers);
        String url = (String) rpcContext.getLoadBalancer().choose(urls);
        
        RpcResponse rpcResponse = post(rpcRequest, url);
        
        if (rpcResponse.isStatus()){
            Object data = rpcResponse.getData();
            Class<?> type = method.getReturnType();
            System.out.println("method.getReturnType() = " + type);
            return parseReturnData(method, data, type);
        }else {
            Exception ex = rpcResponse.getEx();
            System.out.println("exsssssss->>>>>>>>");
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
    
    
    @Nullable
    private static Object parseReturnData(final Method method, final Object data, final Class<?> type) {
//        思考要不要把这块合并到TypeUtils
        if (data instanceof JSONObject jsonResult) {
            if (Map.class.isAssignableFrom(type)) {
                Map resultMap = new HashMap();
                //                获取一个返回值的范型
                Type genericReturnType = method.getGenericReturnType();
                System.out.println("genericReturnType is : " + genericReturnType);
//                ParameterizedType是个什么玩意
                if (genericReturnType instanceof ParameterizedType parameterizedType) {
                    Class<?> keyType = (Class<?>)parameterizedType.getActualTypeArguments()[0];
                    Class<?> valueType = (Class<?>)parameterizedType.getActualTypeArguments()[1];
                    System.out.println("keyType  : " + keyType);
                    System.out.println("valueType: " + valueType);
                    jsonResult.forEach((key1, value1) -> {
                        Object key = TypeUtils.cast(key1, keyType);
                        Object value = TypeUtils.cast(value1, valueType);
                        resultMap.put(key, value);
                    });
                }
                return resultMap;
            }
            return jsonResult.toJavaObject(type);
        } else if (data instanceof JSONArray jsonArray) {
            Object[] array = jsonArray.toArray();
//  理论上来讲这一段和TypeUtils里的array那段是等效的
            if (type.isArray()) {
                Class<?> componentType = type.getComponentType();
                Object resultArray = Array.newInstance(componentType, array.length);
                for (int i = 0; i < array.length; i++) {
                    Array.set(resultArray, i, array[i]);
                }
                return resultArray;
            } else if (List.class.isAssignableFrom(type)) {
                List<Object> resultList = new ArrayList<>(array.length);
//                获取一个返回值的范型
                Type genericReturnType = method.getGenericReturnType();
                System.out.println("genericReturnType is : " + genericReturnType);
                if (genericReturnType instanceof ParameterizedType parameterizedType) {
                    Type actualType = parameterizedType.getActualTypeArguments()[0];
                    System.out.println("actualType is : " + actualType);
                    for (Object o : array) {
                        resultList.add(TypeUtils.cast(o, (Class<?>) actualType));
                    }
                } else {
                    resultList.addAll(Arrays.asList(array));
                }
                return resultList;
            } else {
                return null;
            }
        } else {
            return TypeUtils.cast(data, type);
        }
    }
    
    OkHttpClient client = new OkHttpClient.Builder()
            .connectionPool(new ConnectionPool(16,60,TimeUnit.SECONDS))
            .readTimeout(1, TimeUnit.SECONDS)
            .writeTimeout(1, TimeUnit.SECONDS)
            .connectTimeout(1, TimeUnit.SECONDS)
            .build();
    
    private RpcResponse post(final RpcRequest rpcRequest, final String url) throws IOException {
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
