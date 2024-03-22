package org.sunny.sunnyrpcdemoprovider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.sunny.sunnyrpccore.api.RpcRequest;
import org.sunny.sunnyrpccore.api.RpcResponse;
import org.sunny.sunnyrpccore.provider.ProviderConfig;
import org.sunny.sunnyrpccore.provider.ProviderInvoker;

@SpringBootApplication
@RestController
@Import({ProviderConfig.class}) 
public class SunnyrpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SunnyrpcDemoProviderApplication.class, args);
    }
    
    @Autowired
    ProviderInvoker providerInvoker;

// 使用http+json来进行通信和序列化
    @RequestMapping("/")
    public RpcResponse<Object> invoke(@RequestBody RpcRequest request) {
        // 通过request获取服务名、方法名和参数 来调用对应的方法
        return providerInvoker.invokeRequest(request);
    }


    @Bean
    ApplicationRunner providerRunner() {
        return args -> {
            RpcRequest request = new RpcRequest("org.sunny.sunnyprcdemoapi.interfaces.UserService", "getUserById@1_class java.lang.String", new String[]{"1"});
            System.out.println("return is : " + providerInvoker.invokeRequest(request));
        };
    }
}
