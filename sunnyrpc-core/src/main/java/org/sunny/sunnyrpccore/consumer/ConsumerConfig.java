package org.sunny.sunnyrpccore.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.sunny.sunnyrpccore.api.Filter;
import org.sunny.sunnyrpccore.api.LoadBalancer;
import org.sunny.sunnyrpccore.api.RegistryCenter;
import org.sunny.sunnyrpccore.api.Router;
import org.sunny.sunnyrpccore.cluster.RoundRibonLoadBalancer;
import org.sunny.sunnyrpccore.filter.CacheFilter;
import org.sunny.sunnyrpccore.meta.InstanceMeta;
import org.sunny.sunnyrpccore.registry.ZkRegistryCenter;

@Configuration
@Slf4j
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
            log.info("consumerBootstrap_runner start");
            consumerBootstrap.start();
            log.info("consumerBootstrap_runner end");
        };
    }
    
    @Bean
    public LoadBalancer<InstanceMeta> loadBalancer(){
        return new RoundRibonLoadBalancer();
    }
    
    @Bean
    public Router<InstanceMeta> router(){
        return Router.Default;
    }
    
    @Bean
    public Filter cacheFilter(){
        return new CacheFilter();
    }
    
//    @Bean
//    public Filter mockFilter(){
//        return new MockFilter();
//    }
    
    @Bean(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter consumer_rc(){
        return new ZkRegistryCenter();
    }
}
