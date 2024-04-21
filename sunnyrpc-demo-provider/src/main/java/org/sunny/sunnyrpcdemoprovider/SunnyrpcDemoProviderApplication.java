package org.sunny.sunnyrpcdemoprovider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.sunny.sunnyprcdemoapi.interfaces.UserService;
import org.sunny.sunnyrpccore.annotation.EnableSunnyRPC;

@SpringBootApplication
@RestController
@EnableSunnyRPC
@Slf4j
public class SunnyrpcDemoProviderApplication {

    public static void main(String[] args) {
        SpringApplication.run(SunnyrpcDemoProviderApplication.class, args);
    }
    
    @Autowired
    UserService userService;
    @RequestMapping("/setports/")
    public String setPorts(@RequestParam("ports") String ports){
        userService.setTimeOutPorts(ports);
        return "ok : " + ports;
    }
    
//    @Autowired
//    ProviderConfigProperties providerConfigProperties;
//    
//    @Bean
//    ApplicationRunner providerRun(@Autowired ApplicationContext context) {
//        return x -> {
//            
//            System.out.println(" =====> providerProperties.getMetas()");
//            providerConfigProperties.getMetas().forEach((k,v)->System.out.println(k+":"+v));
//            testAll();
//        };
//    }
//    
//    @Autowired
//    SpringBootTransport transport;
//    private void testAll() {
//        // test 5 for traffic control
//        RpcRequest request = new RpcRequest();
//        request.setService("org.sunny.sunnyprcdemoapi.interfaces.UserService");
//        request.setMethodSign("getUserById@1_String");
//        request.setArgs(new Object[]{100});
//        System.out.println("Provider Case 5. >>===[复杂测试：测试流量并发控制]===");
//                for (int i = 0; i < 120; i++) {
//                    try {
//                        Thread.sleep(1000);
//                        RpcResponse<Object> r = transport.invoke(request);
//                        System.out.println(i + " ***>>> " +r.getData());
//                    } catch (RpcException e) {
//                        // ignore
//                        System.out.println(i + " ***>>> " +e.getMessage() + " -> " + e.getErrorCode());
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//
//
//    }
    
}
