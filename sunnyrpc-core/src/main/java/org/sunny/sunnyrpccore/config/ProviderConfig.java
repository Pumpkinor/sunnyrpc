package org.sunny.sunnyrpccore.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.sunny.sunnyrpccore.api.RegistryCenter;
import org.sunny.sunnyrpccore.provider.ProviderBootstrap;
import org.sunny.sunnyrpccore.provider.ProviderInvoker;
import org.sunny.sunnyrpccore.registry.nacos.NacosRegistryCenter;
import org.sunny.sunnyrpccore.registry.sunnyregistry.SunnyRegistryCenter;
import org.sunny.sunnyrpccore.registry.zk.ZkRegistryCenter;
import org.sunny.sunnyrpccore.transport.SpringBootTransport;

@Configuration
@Slf4j
@Import({AppConfigProperties.class,
        ProviderConfigProperties.class,
        ZkConfigProperties.class,
        SunnyRegistryConfigProperties.class,
        NacosConfigProperties.class,
        SpringBootTransport.class})
public class ProviderConfig {
    @Value("${server.port:9995}")
    private String port;

    @Autowired
    AppConfigProperties appConfigProperties;

    @Autowired
    ProviderConfigProperties providerConfigProperties;

    @Bean
    ProviderBootstrap providerBootstrap() {
        return new ProviderBootstrap(port, appConfigProperties, providerConfigProperties);
    }

    @Bean
    ProviderInvoker providerInvoker(@Autowired ProviderBootstrap providerBootstrap, @Autowired ProviderConfigProperties providerConfigProperties) {
        return new ProviderInvoker(providerBootstrap, providerConfigProperties);
    }

    //    根据配置文件的 sunnyrpc.registry.name来选择加载不同的注册中心
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "sunnyrpc.registry.name", havingValue = "zk")
    public RegistryCenter providerZkRegistryCenter(@Autowired ZkConfigProperties zkConfigProperties) {
        return new ZkRegistryCenter(zkConfigProperties);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "sunnyrpc.registry.name", havingValue = "sunny-registry")
    public RegistryCenter providerSunnyRegistry(@Autowired SunnyRegistryConfigProperties sunnyRegistryConfigProperties) {
        return new SunnyRegistryCenter(sunnyRegistryConfigProperties);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "sunnyrpc.registry.name", havingValue = "nacos")
    public RegistryCenter providerNacosRegistry(@Autowired NacosConfigProperties nacosConfigProperties) {
        return new NacosRegistryCenter(nacosConfigProperties);
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner providerBootstrap_runner(@Autowired ProviderBootstrap providerBootstrap) {
        //        ApplicationRunner会在springboot启动成功后执行 此时所有的spring上下文都已经初始化完成了
        return x -> {
            log.info("providerBootstrap_runner start");
            providerBootstrap.start();
            log.info("providerBootstrap_runner end");
        };
    }
}
