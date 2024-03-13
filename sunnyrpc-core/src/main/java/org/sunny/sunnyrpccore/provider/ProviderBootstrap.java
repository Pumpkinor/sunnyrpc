package org.sunny.sunnyrpccore.provider;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.sunny.sunnyrpccore.annotation.SunnyProvider;
import org.sunny.sunnyrpccore.api.RpcRequest;
import org.sunny.sunnyrpccore.api.RpcResponse;
import org.sunny.sunnyrpccore.meta.ProviderMeta;
import org.sunny.sunnyrpccore.utils.MethodUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ProviderBootstrap implements ApplicationContextAware {
    ApplicationContext applicationContext;

    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();

    @PostConstruct
    public void initProviders() {
        // 获取所有被SunnyProvider注解的provider 这里需要的是类的信息 不是其实例化的对象
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(SunnyProvider.class);
        // 这里的key是bean的名字 不是接口的名字
        providers.forEach((key, value) -> System.out.println(key + " : " + value));
        // 需要将bean的名字转化为接口名
        providers.values().forEach(this::genInterface);
        System.out.println("sunnyrpc-demo-provider start");

    }

    public RpcResponse invokeRequest(RpcRequest request) {
        RpcResponse rpcResponse = new RpcResponse();
        List<ProviderMeta> providerMetas = skeleton.get(request.getService());
        try {
            ProviderMeta providerMeta = findProviderMeta(providerMetas, request.getMethodSign());
            // TODO test providerMeta == null
            Method method = providerMeta.getMethod();
            Object result = method.invoke(providerMeta.getServiceImpl(), request.getParams());
            rpcResponse.setStatus(true);
            rpcResponse.setData(result);
            return rpcResponse;
        } catch (InvocationTargetException  e) {
            rpcResponse.setEx(new RuntimeException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException e){
            rpcResponse.setEx(new RuntimeException(e.getMessage()));
        }
        return rpcResponse;
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
        Class<?> itfer = e.getClass().getInterfaces()[0];
        Method[] methods = itfer.getMethods();
        for (Method method : methods) {
            if (MethodUtils.checkLocalMethod(method)) {
                continue;
            }
            createProvider(itfer, e, method);
        }
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
