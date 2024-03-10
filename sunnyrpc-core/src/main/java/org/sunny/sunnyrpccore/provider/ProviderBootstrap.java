package org.sunny.sunnyrpccore.provider;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.sunny.sunnyrpccore.annotation.SunnyProvider;
import org.sunny.sunnyrpccore.api.RpcRequest;
import org.sunny.sunnyrpccore.api.RpcResponse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ProviderBootstrap implements ApplicationContextAware {
    ApplicationContext applicationContext;

    private Map<String, Object> skeleton = new HashMap<>();

    @PostConstruct
    public void initProviders() {
        // 获取所有被SunnyProvider注解的provider
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(SunnyProvider.class);
        // 这里的key是bean的名字 不是接口的名字
        providers.forEach((key, value) -> System.out.println(key + " : " + value));
        // 需要将bean的名字转化为接口名
        providers.values().forEach(e -> genInterface(e));
        System.out.println("sunnyrpc-demo-provider start");

    }

    public RpcResponse invokeRequest(RpcRequest request) {
        RpcResponse rpcResponse = new RpcResponse();
        Object bean = skeleton.get(request.getService());
        try {
            Method method = findMethod(bean.getClass(), request.getMethod());
            Object result = method.invoke(bean, request.getParams());
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
        skeleton.put(itfer.getCanonicalName(), e);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
