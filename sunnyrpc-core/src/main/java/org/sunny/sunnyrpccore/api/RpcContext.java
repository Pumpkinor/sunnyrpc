package org.sunny.sunnyrpccore.api;

import lombok.Data;

import java.util.List;

@Data
public class RpcContext {
    private Router router;
    private LoadBalancer loadBalancer;
    private List<Filter> filters;
}
