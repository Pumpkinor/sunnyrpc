package org.sunny.sunnyrpccore.consumer.http;

import com.alibaba.fastjson.JSON;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.sunny.sunnyrpccore.api.RpcRequest;
import org.sunny.sunnyrpccore.api.RpcResponse;
import org.sunny.sunnyrpccore.consumer.HttpInvoker;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class OkHttpInvoker implements HttpInvoker {
    
    final static MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client;

    public OkHttpInvoker() {
        client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
                .readTimeout(1, TimeUnit.SECONDS)
                .writeTimeout(1, TimeUnit.SECONDS)
                .connectTimeout(1, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public  RpcResponse<?> post(final RpcRequest rpcRequest, final String url) {
        String reqJson = JSON.toJSONString(rpcRequest);
        System.out.println("call url is >>>>>>>>> " + url);
        System.out.println("reqJson is >>>>>>>>> " + reqJson);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(reqJson, JSON_TYPE))
                .build();
        String respJson;
        try {
            respJson = client.newCall(request).execute().body().string();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        System.out.println("respJson is >>>>>>>>> " + respJson);
        return JSON.parseObject(respJson,RpcResponse.class);
    }
}
