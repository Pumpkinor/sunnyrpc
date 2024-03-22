package org.sunny.sunnyrpccore.meta;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceMeta {

    private String app;
    private String namespace;
    private String env;
    private String name;
    // private String version;

    public String toPath() {
        return String.format("%s_%s_%s_%s", app, namespace, env, name);
    }
}
