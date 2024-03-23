package org.sunny.sunnyrpccore.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
public class TypeUtils {
    
    @Nullable
    public static Object parseReturnData(final Method method, final Object data) {
        Class<?> type = method.getReturnType();
        log.debug("method.getReturnType() = " + type);
        if (data instanceof JSONObject jsonResult) {
            if (Map.class.isAssignableFrom(type)) {
                Map resultMap = new HashMap();
                //                获取一个返回值的范型
                Type genericReturnType = method.getGenericReturnType();
                log.debug("genericReturnType is : " + genericReturnType);
                //                ParameterizedType是个什么玩意
                if (genericReturnType instanceof ParameterizedType parameterizedType) {
                    Class<?> keyType = (Class<?>)parameterizedType.getActualTypeArguments()[0];
                    Class<?> valueType = (Class<?>)parameterizedType.getActualTypeArguments()[1];
                    log.debug("keyType  : " + keyType);
                    log.debug("valueType: " + valueType);
                    jsonResult.forEach((key1, value1) -> {
                        Object key = TypeUtils.cast(key1, keyType);
                        Object value = TypeUtils.cast(value1, valueType);
                        resultMap.put(key, value);
                    });
                }
                return resultMap;
            }
            return jsonResult.toJavaObject(type);
        } else if (data instanceof JSONArray jsonArray) {
            Object[] array = jsonArray.toArray();
            //  理论上来讲这一段和TypeUtils里的array那段是等效的
            if (type.isArray()) {
                Class<?> componentType = type.getComponentType();
                Object resultArray = Array.newInstance(componentType, array.length);
                for (int i = 0; i < array.length; i++) {
                    if (componentType.isPrimitive() || componentType.getPackageName().startsWith("java")) {
                        Array.set(resultArray, i, array[i]);
                    } else {
                        Object castObject = TypeUtils.cast(array[i], componentType);
                        Array.set(resultArray, i, castObject);
                    }                }
                return resultArray;
            } else if (List.class.isAssignableFrom(type)) {
                List<Object> resultList = new ArrayList<>(array.length);
                //                获取一个返回值的范型
                Type genericReturnType = method.getGenericReturnType();
                log.debug("genericReturnType is : " + genericReturnType);
                if (genericReturnType instanceof ParameterizedType parameterizedType) {
                    Type actualType = parameterizedType.getActualTypeArguments()[0];
                    log.debug("actualType is : " + actualType);
                    for (Object o : array) {
                        resultList.add(TypeUtils.cast(o, (Class<?>) actualType));
                    }
                } else {
                    resultList.addAll(Arrays.asList(array));
                }
                return resultList;
            } else {
                return null;
            }
        } else {
            return TypeUtils.cast(data, type);
        }
    }
    
    public static Object cast(Object origin, Class<?> type) {
        if(origin == null) return null;
        Class<?> aClass = origin.getClass();
        if(type.isAssignableFrom(aClass)) {
            return origin;
        }

        if(type.isArray()) {
            if(origin instanceof List list) {
                origin = list.toArray();
            }
            int length = Array.getLength(origin);
            Class<?> componentType = type.getComponentType();
            Object resultArray = Array.newInstance(componentType, length);
            for (int i = 0; i < length; i++) {
                if (componentType.isPrimitive() || componentType.getPackageName().startsWith("java")) {
                    Array.set(resultArray, i, Array.get(origin, i));
                } else {
                    //                数组里的类型不是基本类型 也不是jdk自带的包里的类 需要对每个元素先转一下类型
                    Object castObject = cast(Array.get(origin, i), componentType);
                    Array.set(resultArray, i, castObject);
                }            }
            return resultArray;
        }

        if (origin instanceof HashMap map) {
            JSONObject jsonObject = new JSONObject(map);
            return jsonObject.toJavaObject(type);
        }
        
        if (origin instanceof JSONObject jsonObject) {
            return jsonObject.toJavaObject(type);
        }

        if(type.equals(Integer.class) || type.equals(Integer.TYPE)) {
            return Integer.valueOf(origin.toString());
        } else if(type.equals(Long.class) || type.equals(Long.TYPE)) {
            return Long.valueOf(origin.toString());
        } else if(type.equals(Float.class) || type.equals(Float.TYPE)) {
            return Float.valueOf(origin.toString());
        } else if(type.equals(Double.class) || type.equals(Double.TYPE)) {
            return Double.valueOf(origin.toString());
        } else if(type.equals(Byte.class) || type.equals(Byte.TYPE)) {
            return Byte.valueOf(origin.toString());
        } else if(type.equals(Short.class) || type.equals(Short.TYPE)) {
            return Short.valueOf(origin.toString());
        } else if(type.equals(Character.class) || type.equals(Character.TYPE)) {
            return Character.valueOf(origin.toString().charAt(0));
        } else if(type.equals(Boolean.class) || type.equals(Boolean.TYPE)) {
            return Boolean.valueOf(origin.toString());
        }

        return null;


    }
}
