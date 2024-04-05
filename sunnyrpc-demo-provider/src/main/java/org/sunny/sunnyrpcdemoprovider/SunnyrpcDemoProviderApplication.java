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
}
