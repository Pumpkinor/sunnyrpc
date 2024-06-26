package org.sunny.sunnyrpccore.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "sunnyrpc.nacos-registry")
public class NacosConfigProperties {
    private List<String> serverAddrs;
    private String username;
    private String password;
    private String nameSpace;
    private String accessKey;
    private String secretKey;
}
