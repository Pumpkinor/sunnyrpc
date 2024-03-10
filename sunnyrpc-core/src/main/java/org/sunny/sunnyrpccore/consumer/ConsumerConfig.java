package org.sunny.sunnyrpccore.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class ConsumerConfig {
    @Bean
    ConsumerBootstrap createConsumerBootstrap(){
        return new ConsumerBootstrap();
    }
    
    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner consumerBootstrap_runner(@Autowired ConsumerBootstrap consumerBootstrap){
//        ApplicationRunner会在springboot启动成功后执行 此时所有的spring上下文都已经初始化完成了
        return x ->{
            System.out.println("consumerBootstrap_runner start");
            consumerBootstrap.start();
            System.out.println("consumerBootstrap_runner end");
        };
    }
}
