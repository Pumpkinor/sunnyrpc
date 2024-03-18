package org.sunny.sunnyrpccore.api;

import java.util.List;

public interface LoadBalancer<T> {
    T choose(List<T> providers);
    
    LoadBalancer Default = providers -> {
        if (providers == null || providers.isEmpty()){
            return null;
        }else {
            return providers.get(0);
        }
    };
}
