package org.sunny.sunnyrpccore.consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.sunny.sunnyrpccore.api.LoadBalancer;
import org.sunny.sunnyrpccore.api.RegistryCenter;
import org.sunny.sunnyrpccore.api.RegistryCenter.StaticRegisterCenter;
import org.sunny.sunnyrpccore.api.Router;
import org.sunny.sunnyrpccore.cluster.RoundRibonLoadBalancer;

import java.util.List;

@Configuration
public class ConsumerConfig {
    @Value("${sunnyrpc.providers}")
    String services;
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
    
    @Bean
    public LoadBalancer loadBalancer(){
        return new RoundRibonLoadBalancer();
    }
    
    @Bean
    public Router router(){
        return Router.Default;
    }
    
    @Bean(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter consumer_rc(){
        return new StaticRegisterCenter(List.of(services.split(",")));
    }
}
