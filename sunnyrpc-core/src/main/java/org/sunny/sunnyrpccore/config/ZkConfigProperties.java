package org.sunny.sunnyrpccore.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "sunnyrpc.zk")
public class ZkConfigProperties {
    // for zk 
    private String server = "localhost:2181";

    private String root = "sunnyrpc";
}
