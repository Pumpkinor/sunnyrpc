package org.sunny.sunnyrpcdemoprovider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.sunny.sunnyprcdemoapi.interfaces.UserService;
import org.sunny.sunnyrpccore.api.RpcRequest;
import org.sunny.sunnyrpccore.api.RpcResponse;
import org.sunny.sunnyrpccore.config.ProviderConfig;
import org.sunny.sunnyrpccore.provider.ProviderInvoker;

import java.util.HashMap;

@SpringBootApplication
@RestController
@Import({ProviderConfig.class})
@Slf4j
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
    @Autowired
    UserService userService;
    @RequestMapping("/setports/")
    public String setPorts(@RequestParam("ports") String ports){
        userService.setTimeOutPorts(ports);
        return "ok : " + ports;
    }

    @Bean
    ApplicationRunner providerRunner() {
        return args -> {
            RpcRequest request = new RpcRequest("org.sunny.sunnyprcdemoapi.interfaces.UserService", "getUserById@1_class java.lang.String", new String[]{"1"}, new HashMap<>());
            log.info("return is : " + providerInvoker.invokeRequest(request));
        };
    }
}
