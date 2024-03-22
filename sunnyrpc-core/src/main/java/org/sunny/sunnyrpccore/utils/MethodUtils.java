package org.sunny.sunnyrpccore.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MethodUtils {
    public static String getMethodSign(Method method){
        final String name = method.getName();
        final int parameterCount = method.getParameterCount();
        final Class<?>[] parameterTypes = method.getParameterTypes();
        StringBuilder sb = new StringBuilder(name);
        sb.append("@").append(parameterCount);
        for (final Class<?> parameterType : parameterTypes) {
            sb.append("_").append(parameterType);
        }
        return sb.toString();
    }
    
    public static boolean checkLocalMethod(final String method) {
        //本地方法不代理
        if ("toString".equals(method) ||
                "hashCode".equals(method) ||
                "notifyAll".equals(method) ||
                "equals".equals(method) ||
                "wait".equals(method) ||
                "getClass".equals(method) ||
                "notify".equals(method)) {
            return true;
        }
        return false;
    }
    
    public static boolean checkLocalMethod(final Method method) {
        return method.getDeclaringClass().equals(Object.class);
    }
    
    public static List<Field>  findAnnotatedField(Class<?> aClass, Class<? extends Annotation> annotationClass) {
        List<Field> results = new ArrayList<>(); 
        while (aClass != null) {
            //        spring 管理的bean 大部分是经过增强后的类 增强后的类是原始类的子类 因此使用getDeclaredFields无法获取其原始父类的属性
            Field[] fields = aClass.getDeclaredFields(); 
            for (final Field field : fields) {
                if (field.isAnnotationPresent(annotationClass)) {
                    results.add(field);
                }
            } aClass = aClass.getSuperclass();
        } 
        return results;
    }
}
