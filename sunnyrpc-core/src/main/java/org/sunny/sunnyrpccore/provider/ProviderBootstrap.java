package org.sunny.sunnyrpccore.provider;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.sunny.sunnyrpccore.annotation.SunnyProvider;
import org.sunny.sunnyrpccore.api.RegistryCenter;
import org.sunny.sunnyrpccore.meta.InstanceMeta;
import org.sunny.sunnyrpccore.meta.ProviderMeta;
import org.sunny.sunnyrpccore.meta.ServiceMeta;
import org.sunny.sunnyrpccore.utils.MethodUtils;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Map;

public class ProviderBootstrap implements ApplicationContextAware {
    ApplicationContext applicationContext;
    
    private RegistryCenter rc;
    
    @Getter
    private final MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();
    private InstanceMeta instanceMeta;
    
    @Value("${server.port}")
    private String port;
    
    @Value("${sunnyrpc.app.id}")
    private String app;
    
    @Value("${sunnyrpc.app.namespace}")
    private String namespace;
    
    @Value("${sunnyrpc.app.env}")
    private String env;
    @SneakyThrows
    @PostConstruct
    public void initProviders() {
        rc = applicationContext.getBean(RegistryCenter.class);
        // 获取所有被SunnyProvider注解的provider 这里需要的是类的信息 不是其实例化的对象
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(SunnyProvider.class);
        // 这里的key是bean的名字 不是接口的名字
        providers.forEach((key, value) -> System.out.println(key + " : " + value));
        // 需要将bean的名字转化为接口名
        providers.values().forEach(this::genInterface);
    }
    
    public void start() throws UnknownHostException {
//        注册instance到zk 需要等待spring应用完全启动至可用状态
        String ip = InetAddress.getLocalHost().getHostAddress();
//        InetAddress.getLocalHost().getHostAddress()在windows下没问题，在linux下是根据主机名在hosts文件对应的ip来获取IP地址的
//        String ip = InetAddress.getLocalHost().getHostAddress();
        instanceMeta = InstanceMeta.http(ip, Integer.valueOf(port));
        skeleton.keySet().forEach(this::registerService);
        System.out.println("sunnyrpc-demo-provider start");
    }
    
    @PreDestroy
    public void stop(){
        skeleton.keySet().forEach(this::unRegisterService);
    }
    
    private void unRegisterService(String service) {
        System.out.println("start todo unRegisterService");
        ServiceMeta serviceMeta = ServiceMeta.builder().app(app).namespace(namespace).env(env).name(service).build();
        rc.unRegister(serviceMeta, instanceMeta);
    }
    
    private void registerService(String service) {
        RegistryCenter rc = applicationContext.getBean(RegistryCenter.class);
        ServiceMeta serviceMeta = ServiceMeta.builder().app(app).namespace(namespace).env(env).name(service).build();
        rc.register(serviceMeta, instanceMeta);
    }
    
    private Method findMethod(Class<?> aClass, String method) {
        Method[] methods = aClass.getMethods();
        for (Method value : methods) {
            if (value.getName().equals(method)) {
                return value;
            }
        }
        return null;
    }

    private void genInterface(Object impl) {
        final Class<?>[] interfaces = impl.getClass().getInterfaces();
        Arrays.stream(interfaces).forEach(service -> {
            Arrays.stream(service.getMethods())
                    .filter(method -> !MethodUtils.checkLocalMethod(method))
                    .forEach(method -> createProvider(service,impl,method));
        });
    }
    
    private void createProvider(final Class<?> service, final Object impl, final Method method) {
        final ProviderMeta providerMeta = ProviderMeta.builder()
                .serviceImpl(impl)
                .method(method)
                .methodSign(MethodUtils.getMethodSign(method))
                .build();
        System.out.println(" create a provider: " + providerMeta);
        skeleton.add(service.getCanonicalName(), providerMeta);
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
    
}
