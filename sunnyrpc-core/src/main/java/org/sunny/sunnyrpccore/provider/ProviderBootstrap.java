package org.sunny.sunnyrpccore.provider;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.sunny.sunnyrpccore.annotation.SunnyProvider;
import org.sunny.sunnyrpccore.api.RegistryCenter;
import org.sunny.sunnyrpccore.api.RpcRequest;
import org.sunny.sunnyrpccore.api.RpcResponse;
import org.sunny.sunnyrpccore.meta.ProviderMeta;
import org.sunny.sunnyrpccore.utils.MethodUtils;
import org.sunny.sunnyrpccore.utils.TypeUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ProviderBootstrap implements ApplicationContextAware {
    ApplicationContext applicationContext;
    
    private RegistryCenter rc;
    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();
    private String instance;
    @Value("${server.port}")
    private String port;
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
        this.instance = ip + "_" + port;
        skeleton.keySet().forEach(this::registerService);
        System.out.println("sunnyrpc-demo-provider start");
    }
    
    @PreDestroy
    public void stop(){
        skeleton.keySet().forEach(this::unRegisterService);
    }
    
    private void unRegisterService(String service) {
        System.out.println("start todo unRegisterService");
        rc.unRegister(service, instance);
    }
    
    private void registerService(String service) {
        RegistryCenter rc = applicationContext.getBean(RegistryCenter.class);
        rc.register(service, instance);
    }
    
    public RpcResponse invokeRequest(RpcRequest request) {
        RpcResponse rpcResponse = new RpcResponse();
        List<ProviderMeta> providerMetas = skeleton.get(request.getService());
        try {
            ProviderMeta providerMeta = findProviderMeta(providerMetas, request.getMethodSign());
            // TODO test providerMeta == null
            Method method = providerMeta.getMethod();
            Object[] actualParmas = processArgs(request.getParams(), method.getParameterTypes());
            Object result = method.invoke(providerMeta.getServiceImpl(), actualParmas);
            rpcResponse.setStatus(true);
            rpcResponse.setData(result);
            return rpcResponse;
        } catch (InvocationTargetException  e) {
            e.printStackTrace();
            rpcResponse.setEx(new RuntimeException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e){
            e.printStackTrace();
            rpcResponse.setEx(new RuntimeException(e.getMessage()));
        }
        return rpcResponse;
    }
    
    private Object[] processArgs(final Object[] params, final Class<?>[] parameterTypes) {
        if (params == null || params.length == 0){
            return params;
        }
        Object[] actualParmas = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            actualParmas[i] = TypeUtils.cast(params[i], parameterTypes[i]);
        }
        return actualParmas;
    }
    
    private ProviderMeta findProviderMeta(final List<ProviderMeta> providerMetas, final String methodSign) {
        Optional<ProviderMeta> optional = providerMetas.stream()
                .filter(x -> x.getMethodSign().equals(methodSign)).findFirst();
        return optional.orElse(null);
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

    private void genInterface(Object e) {
        final Class<?>[] interfaces = e.getClass().getInterfaces();
        Arrays.stream(interfaces).forEach(itfer -> {
            Method[] methods = itfer.getMethods();
            for (Method method : methods) {
                if (MethodUtils.checkLocalMethod(method)) {
                    continue;
                }
                createProvider(itfer, e, method);
            }
        });
    }
    
    private void createProvider(final Class<?> itfer, final Object e, final Method method) {
        final ProviderMeta providerMeta = new ProviderMeta();
        providerMeta.setMethod(method);
        providerMeta.setServiceImpl(e);
        providerMeta.setMethodSign(MethodUtils.getMethodSign(method));
        System.out.println(" create a provider: " + providerMeta);
        skeleton.add(itfer.getCanonicalName(), providerMeta);
    }
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
