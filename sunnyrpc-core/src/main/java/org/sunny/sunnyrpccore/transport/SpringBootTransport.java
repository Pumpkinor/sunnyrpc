package org.sunny.sunnyrpccore.transport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sunny.sunnyrpccore.api.RpcRequest;
import org.sunny.sunnyrpccore.api.RpcResponse;
import org.sunny.sunnyrpccore.provider.ProviderInvoker;

@RestController
public class SpringBootTransport {
    
    @Autowired
    ProviderInvoker providerInvoker;
    
    // 使用http+json来进行通信和序列化
    @RequestMapping("/sunnyrpc")
    public RpcResponse<Object> invoke(@RequestBody RpcRequest request) {
        // 通过request获取服务名、方法名和参数 来调用对应的方法
        return providerInvoker.invokeRequest(request);
    }
}
