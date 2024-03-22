package org.sunny.sunnyrpccore.consumer;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.sunny.sunnyrpccore.annotation.SunnyConsumer;
import org.sunny.sunnyrpccore.api.LoadBalancer;
import org.sunny.sunnyrpccore.api.RegistryCenter;
import org.sunny.sunnyrpccore.api.Router;
import org.sunny.sunnyrpccore.api.RpcContext;
import org.sunny.sunnyrpccore.utils.MethodUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConsumerBootstrap implements ApplicationContextAware, EnvironmentAware {
    ApplicationContext applicationContext;
    Environment environment;
    
    private final Map<String, Object> stub = new HashMap<>();
//    创建代理类并且注入
    public void start(){
        RpcContext  rpcContext = new RpcContext();
        RegistryCenter rc = applicationContext.getBean(RegistryCenter.class);
        Router router = applicationContext.getBean(Router.class);
        LoadBalancer loadBalancer = applicationContext.getBean(LoadBalancer.class);
        rpcContext.setRouter(router);
        rpcContext.setLoadBalancer(loadBalancer);
        
        
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (final String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            List<Field> fieldList = MethodUtils.findAnnotatedField(bean.getClass(), SunnyConsumer.class);
//            给每个带有注解的对象创建对应的代理对象
            fieldList.stream().forEach(e->{
                System.out.println("field name -> "+e.getName());
                Class<?> service = e.getType();
                String serviceName = service.getCanonicalName();
                Object consumer = stub.get(serviceName);
                if (consumer == null){
                    consumer = createConsumerFromRegister(service, rpcContext, rc);
                }
                e.setAccessible(true);
                try {
                    e.set(bean, consumer);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
    }
    
    private Object createConsumerFromRegister(final Class<?> service, final RpcContext rpcContext, final RegistryCenter rc) {
        String serviceName = service.getCanonicalName();
        List<String> nodes = rc.fetchAll(serviceName);
        rpcContext.setProviders(mapUrls(nodes));
//        添加订阅
        rc.subscribe(serviceName, event -> {
            List<String> providers = rpcContext.getProviders();
            providers.clear();
            providers.addAll(mapUrls(event.getData()));
        });
        return createConsumer(service, rpcContext);
    }
    
    private List<String> mapUrls(List<String> nodes){
        return nodes.stream()
                .map(e-> "http://" + e.replace("_",":") + "/")
                .collect(Collectors.toList());
    }
    
    private Object createConsumer(final Class<?> service, final RpcContext rpcContext) {
//        jdk 动态代理
        return Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service}, new SunnyInvocationHandler(service, rpcContext));
    }
    
    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public void setEnvironment(@NotNull final Environment environment) {
        this.environment = environment;
    }
}
