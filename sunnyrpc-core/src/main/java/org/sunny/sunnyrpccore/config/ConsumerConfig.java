package org.sunny.sunnyrpccore.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.sunny.sunnyrpccore.api.Filter;
import org.sunny.sunnyrpccore.api.LoadBalancer;
import org.sunny.sunnyrpccore.api.RegistryCenter;
import org.sunny.sunnyrpccore.api.Router;
import org.sunny.sunnyrpccore.api.RpcContext;
import org.sunny.sunnyrpccore.cluster.GrayRouter;
import org.sunny.sunnyrpccore.cluster.RoundRibonLoadBalancer;
import org.sunny.sunnyrpccore.consumer.ConsumerBootstrap;
import org.sunny.sunnyrpccore.filter.ParameterFilter;
import org.sunny.sunnyrpccore.meta.InstanceMeta;
import org.sunny.sunnyrpccore.registry.sunnyregistry.SunnyRegistryCenter;
import org.sunny.sunnyrpccore.registry.zk.ZkRegistryCenter;

import java.util.List;

@Configuration
@Slf4j
@Import({AppConfigProperties.class,ConsumerConfigProperties.class, SunnyRegistryConfigProperties.class, ZkConfigProperties.class})
public class ConsumerConfig {
    @Autowired
    AppConfigProperties appConfigProperties;
    
    @Autowired
    ConsumerConfigProperties consumerConfigProperties;
    
    @Autowired
    ZkConfigProperties zkConfigProperties;
    
    @Autowired
    SunnyRegistryConfigProperties sunnyRegistryConfigProperties;
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
    public Router<InstanceMeta> router() {
        return new GrayRouter(consumerConfigProperties.getGrayRatio());
    }

    
//    @Bean
//    public Filter cacheFilter(){
//        return new CacheFilter();
//    }
//    
//    @Bean
//    public Filter mockFilter(){
//        return new MockFilter();
//    }
    @Bean
    public Filter parameterFilter() {
        return new ParameterFilter();
    }
    
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "sunnyrpc.registry.name", havingValue = "zk")
    public RegistryCenter providerZkRegistryCenter(){
        return new ZkRegistryCenter(zkConfigProperties);
    }
    
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "sunnyrpc.registry.name", havingValue = "sunny-registry")
    public RegistryCenter providerSunnyRegistry(){
        return new SunnyRegistryCenter(sunnyRegistryConfigProperties);
    }
    @Bean
    public RpcContext createContext(@Autowired Router router,
                                    @Autowired LoadBalancer loadBalancer,
                                    @Autowired List<Filter> filters) {
        RpcContext context = new RpcContext();
        context.setRouter(router);
        context.setLoadBalancer(loadBalancer);
        context.setFilters(filters);
        context.getParameters().put("app.id", appConfigProperties.getId());
        context.getParameters().put("app.namespace", appConfigProperties.getNamespace());
        context.getParameters().put("app.env", appConfigProperties.getEnv());
        context.getParameters().put("consumer.retries", String.valueOf(consumerConfigProperties.getRetries()));
        context.getParameters().put("consumer.timeout", String.valueOf(consumerConfigProperties.getTimeout()));
        context.getParameters().put("consumer.faultLimit", String.valueOf(consumerConfigProperties.getFaultLimit()));
        context.getParameters().put("consumer.halfOpenInitialDelay", String.valueOf(consumerConfigProperties.getHalfOpenInitialDelay()));
        context.getParameters().put("consumer.halfOpenDelay", String.valueOf(consumerConfigProperties.getHalfOpenDelay()));
        return context;
    }
}
