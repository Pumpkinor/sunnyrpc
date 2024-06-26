package org.sunny.sunnyrpccore.registry.nacos;

import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.sunny.sunnyrpccore.api.RegistryCenter;
import org.sunny.sunnyrpccore.config.NacosConfigProperties;
import org.sunny.sunnyrpccore.meta.InstanceMeta;
import org.sunny.sunnyrpccore.meta.ServiceMeta;
import org.sunny.sunnyrpccore.registry.ChangedListener;
import org.sunny.sunnyrpccore.registry.Event;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j

public class NacosRegistryCenter implements RegistryCenter {
    private NacosConfigProperties nacosConfigProperties;

    private NamingService client;

    public NacosRegistryCenter(NacosConfigProperties nacosConfigProperties) {
        this.nacosConfigProperties = nacosConfigProperties;
    }

    @SneakyThrows
    @Override
    public void start() {
        if (client != null) {
            log.info("nacosServer alive ... ");
            return;
        }

        // @see com.alibaba.nacos.api.PropertyKeyConst
        Properties properties = new Properties();
        List<String> serverAddrs = nacosConfigProperties.getServerAddrs();
        if (serverAddrs == null || serverAddrs.size() == 0) {
            // "请填写注册中心连接信息"
            throw new RuntimeException("请填写注册中心连接信息");
        } else {
            String serverList = serverAddrs.stream().collect(Collectors.joining(","));
            // 指定 Nacos 地址
            properties.setProperty(PropertyKeyConst.SERVER_ADDR, serverList);
        }


        // 默认命名空间是空，可以不填写
        properties.put(PropertyKeyConst.NAMESPACE, nacosConfigProperties.getNameSpace());
        // 如果在云上开启鉴权可以传入应用身份
//        properties.put("ramRoleName", "$ramRoleName");
        if (nacosConfigProperties.getAccessKey() != null) {
            properties.put(PropertyKeyConst.ACCESS_KEY, nacosConfigProperties.getAccessKey());

        }
        if (nacosConfigProperties.getSecretKey() != null) {
            properties.put(PropertyKeyConst.SECRET_KEY, nacosConfigProperties.getSecretKey());
        }
        if (nacosConfigProperties.getUsername() != null) {
            properties.put(PropertyKeyConst.USERNAME, nacosConfigProperties.getUsername());
        }
        if (nacosConfigProperties.getPassword() != null) {
            properties.put(PropertyKeyConst.PASSWORD, nacosConfigProperties.getPassword());
        }
        log.info("nacosServer start ...");
        client = NamingFactory.createNamingService(properties);
        log.info(" ===> nacos client starting.");
    }

    @SneakyThrows
    @Override
    public void stop() {
        client.shutDown();
    }

    @SneakyThrows
    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        Instance nacosInstance = new Instance();
        nacosInstance.setServiceName(service.getName());
        nacosInstance.setInstanceId(service.getApp());
        nacosInstance.setIp(instance.getHost());
        nacosInstance.setPort(instance.getPort());
        nacosInstance.setClusterName(instance.getContext());
        nacosInstance.setMetadata(instance.getParameters());
        nacosInstance.setHealthy(true);
        client.registerInstance(service.getName(), nacosInstance);
        log.info(" ===> register to nacos: {}", instance);
    }

    @SneakyThrows
    @Override
    public void unRegister(ServiceMeta service, InstanceMeta instance) {
        client.deregisterInstance(service.getName(),
                instance.getHost(),
                instance.getPort(),
                instance.getContext());
        log.info(" ===> unregister from nacos: {}", instance);
    }

    @SneakyThrows
    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        List<Instance> nodes = client.selectInstances(service.getName(), true);
        return nodes.stream().map(instance -> {
            InstanceMeta meta = InstanceMeta.http(
                    instance.getIp(), instance.getPort(), instance.getClusterName());
            Map<String, String> params = instance.getMetadata();
            params.forEach((k, v) -> {
                log.debug("nacos Metas : key: {}, value: {}", k, v);
                meta.getParameters().put(k, v);
            });
            log.debug(" fetchAll instance: {}", meta.toUrl());
            return meta;
        }).collect(Collectors.toList());
    }

    @SneakyThrows
    @Override
    public void subscribe(ServiceMeta service, ChangedListener changedListener) {

        client.subscribe(service.getName(), event -> {
            // 节点变动，这里会感知到
            log.info("nacos subscribe event: {}", event);
            List<InstanceMeta> nodes = fetchAll(service);
            changedListener.fire(new Event(nodes));
        });

    }
}
