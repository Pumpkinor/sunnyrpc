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
}
