package org.sunny.sunnyrpccore.consumer.http;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.sunny.sunnyrpccore.api.RpcRequest;
import org.sunny.sunnyrpccore.api.RpcResponse;
import org.sunny.sunnyrpccore.consumer.HttpInvoker;
import org.sunny.sunnyrpccore.exception.RpcException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
@Slf4j
public class OkHttpInvoker implements HttpInvoker {
    
    final static MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    OkHttpClient client;

    public OkHttpInvoker(int timeout) {
        client = new OkHttpClient.Builder()
                .connectionPool(new ConnectionPool(16, 60, TimeUnit.SECONDS))
                .readTimeout(timeout, TimeUnit.MILLISECONDS)
                .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public  RpcResponse<?> post(final RpcRequest rpcRequest, final String url) {
        String reqJson = JSON.toJSONString(rpcRequest);
        log.debug("call url is >>>>>>>>> " + url);
        log.debug("reqJson is >>>>>>>>> " + reqJson);
        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(reqJson, JSON_TYPE))
                .build();
        String respJson;
        try {
            respJson = client.newCall(request).execute().body().string();
        } catch (IOException e) {
            throw new RpcException(e, RpcException.UnknownEx);
        }
        log.debug("respJson is >>>>>>>>> " + respJson);
        return JSON.parseObject(respJson,RpcResponse.class);
    }
}
