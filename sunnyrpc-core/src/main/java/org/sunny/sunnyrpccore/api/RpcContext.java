package org.sunny.sunnyrpccore.api;

import lombok.Data;
import org.sunny.sunnyrpccore.meta.InstanceMeta;

import java.util.List;

@Data
public class RpcContext {
    private Router<InstanceMeta> router;
    private LoadBalancer<InstanceMeta> loadBalancer;
    private List<Filter> filters;
    private List<InstanceMeta> providers;
}
