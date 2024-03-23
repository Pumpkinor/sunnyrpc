package org.sunny.sunnyrpccore.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.sunny.sunnyrpccore.api.RegistryCenter;
import org.sunny.sunnyrpccore.registry.ZkRegistryCenter;

@Configuration
@Slf4j
public class ProviderConfig {

    @Bean
    ProviderBootstrap providerBootstrap() {
        return new ProviderBootstrap();
    }
    
    @Bean
    ProviderInvoker providerInvoker(@Autowired ProviderBootstrap providerBootstrap) {
        return new ProviderInvoker(providerBootstrap);
    }
    @Bean(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter provider_rc(){
        return new ZkRegistryCenter();
    }
    
    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner providerBootstrap_runner(@Autowired ProviderBootstrap providerBootstrap){
        //        ApplicationRunner会在springboot启动成功后执行 此时所有的spring上下文都已经初始化完成了
        return x ->{
            log.info("providerBootstrap_runner start");
            providerBootstrap.start();
            log.info("providerBootstrap_runner end");
        };
    }
}
