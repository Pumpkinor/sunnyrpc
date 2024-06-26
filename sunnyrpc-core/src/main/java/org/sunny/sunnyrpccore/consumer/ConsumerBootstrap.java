package org.sunny.sunnyrpccore.consumer;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.sunny.sunnyrpccore.annotation.SunnyConsumer;
import org.sunny.sunnyrpccore.api.RegistryCenter;
import org.sunny.sunnyrpccore.api.RpcContext;
import org.sunny.sunnyrpccore.meta.InstanceMeta;
import org.sunny.sunnyrpccore.meta.ServiceMeta;
import org.sunny.sunnyrpccore.utils.MethodUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
public class ConsumerBootstrap implements ApplicationContextAware, EnvironmentAware {
    ApplicationContext applicationContext;
    Environment environment;
//    stub作为一个缓存 在多个类需要将consumer的实例作为属性注入的时候 可以提高性能
    private final Map<String, Object> stub = new HashMap<>();
//    创建代理类并且注入
    public void start(){
        RegistryCenter rc = applicationContext.getBean(RegistryCenter.class);
        RpcContext rpcContext = applicationContext.getBean(RpcContext.class);
        
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (final String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            List<Field> fieldList = MethodUtils.findAnnotatedField(bean.getClass(), SunnyConsumer.class);
//            给每个带有注解的对象创建对应的代理对象
            fieldList.forEach(e->{
                log.info("field name -> "+e.getName());
                Class<?> service = e.getType();
                String serviceName = service.getCanonicalName();
                Object consumer = stub.get(serviceName);
                if (consumer == null){
//                    创建代理对象
                    consumer = createConsumerFromRegister(service, rpcContext, rc);
                    stub.put(serviceName, consumer);
                }
                e.setAccessible(true);
                try {
//                    设置代理对象
                    e.set(bean, consumer);
                } catch (IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }
    }
    
    private Object createConsumerFromRegister(final Class<?> service, final RpcContext rpcContext, final RegistryCenter rc) {
        String serviceName = service.getCanonicalName();
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .app(rpcContext.getParameters().get("app.id"))
                .namespace(rpcContext.getParameters().get("app.namespace"))
                .env(rpcContext.getParameters().get("app.env")).name(serviceName).build();
        List<InstanceMeta> nodes = rc.fetchAll(serviceMeta);
        rpcContext.setProviders(nodes);
//        添加订阅
        rc.subscribe(serviceMeta, event -> {
            List<InstanceMeta> providers = rpcContext.getProviders();
            providers.clear();
            providers.addAll(event.getData());
        });
        return createConsumer(service, rpcContext);
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
