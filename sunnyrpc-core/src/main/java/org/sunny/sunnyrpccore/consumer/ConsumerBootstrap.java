package org.sunny.sunnyrpccore.consumer;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.sunny.sunnyrpccore.annotation.SunnyConsumer;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsumerBootstrap implements ApplicationContextAware {
    ApplicationContext applicationContext;
    
    private Map<String, Object> stub = new HashMap<>();
//    创建代理类并且注入
    public void start(){
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        for (final String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            List<Field> fieldList = findAnnotatedField(bean.getClass());
//            给每个带有注解的对象创建对应的代理对象
            fieldList.stream().forEach(e->{
                System.out.println("field name -> "+e.getName());
                Class<?> service = e.getType();
                String serviceName = service.getCanonicalName();
                Object consumer = stub.get(serviceName);
                if (consumer == null){
                    consumer = createConsumer(service);
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
    
    private Object createConsumer(final Class<?> service) {
//        jdk 动态代理
        return Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service}, new SunnyInvocationHandler(service));
    }
    
    private List<Field> findAnnotatedField(Class<?> aClass){
        List<Field> results = new ArrayList<>();
        while(aClass != null){
            //        spring 管理的bean 大部分是经过增强后的类 增强后的类是原始类的子类 因此使用getDeclaredFields无法获取其原始父类的属性
            Field[] fields = aClass.getDeclaredFields();
            for (final Field field : fields) {
                if (field.isAnnotationPresent(SunnyConsumer.class)){
                    results.add(field);
                }
            }
            aClass = aClass.getSuperclass();
        }
        return results;
    }
    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
