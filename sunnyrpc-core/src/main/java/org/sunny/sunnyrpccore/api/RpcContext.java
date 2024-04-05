package org.sunny.sunnyrpccore.api;

import lombok.Data;
import org.sunny.sunnyrpccore.meta.InstanceMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class RpcContext {
    private Router<InstanceMeta> router;
    private LoadBalancer<InstanceMeta> loadBalancer;
    private List<Filter> filters;
    private List<InstanceMeta> providers;
    private Map<String, String> parameters = new HashMap<>();
    
    // kkrpc.color = gray
    // kkrpc.gtrace_id
    // gw -> service1 ->  service2(跨线程传递) ...
    // http headers
//    使用ThreadLocal来避免线程间数据干扰
    public static ThreadLocal<Map<String,String>> ContextParameters = ThreadLocal.withInitial(HashMap::new);
    
    public static void setContextParameter(String key, String value) {
        ContextParameters.get().put(key, value);
    }
    
    public static String getContextParameter(String key) {
        return ContextParameters.get().get(key);
    }
    
    public static void removeContextParameter(String key) {
        ContextParameters.get().remove(key);
    }
    
}
