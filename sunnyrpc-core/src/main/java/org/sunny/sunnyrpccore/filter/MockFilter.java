package org.sunny.sunnyrpccore.filter;

import lombok.SneakyThrows;
import org.sunny.sunnyrpccore.api.Filter;
import org.sunny.sunnyrpccore.api.RpcRequest;
import org.sunny.sunnyrpccore.api.RpcResponse;
import org.sunny.sunnyrpccore.utils.MethodUtils;
import org.sunny.sunnyrpccore.utils.MockUtils;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * can use to find bug in services chain
 */
public class MockFilter implements Filter {
    @SneakyThrows
    @Override
    public Object preFilter(RpcRequest request) {
        Class service = Class.forName(request.getService());
        Method method = findMethod(service, request.getMethodSign());
        Class clazz = method.getReturnType();
        return MockUtils.mock(clazz);
    }

    private Method findMethod(Class service, String methodSign) {
        return Arrays.stream(service.getMethods())
                .filter(method -> !MethodUtils.checkLocalMethod(method))
                .filter(method -> methodSign.equals(MethodUtils.getMethodSign(method)))
                .findFirst().orElse(null);
    }

    @Override
    public Object postFilter(RpcRequest request, RpcResponse response, Object result) {
        return null;
    }
}
