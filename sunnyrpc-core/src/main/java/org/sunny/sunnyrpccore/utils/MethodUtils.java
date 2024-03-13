package org.sunny.sunnyrpccore.utils;

import java.lang.reflect.Method;

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
}
