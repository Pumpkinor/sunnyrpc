package org.sunny.sunnyrpccore.registry.sunnyregistry;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.sunny.sunnyrpccore.api.RegistryCenter;
import org.sunny.sunnyrpccore.config.SunnyRegistryConfigProperties;
import org.sunny.sunnyrpccore.meta.InstanceMeta;
import org.sunny.sunnyrpccore.meta.ServiceMeta;
import org.sunny.sunnyrpccore.registry.ChangedListener;
import org.sunny.sunnyrpccore.registry.Event;
import org.sunny.sunnyrpccore.utils.http.HttpInvoker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j

public class SunnyRegistryCenter implements RegistryCenter {
    private static final String REG_PATH = "/reg";
    private static final String UNREG_PATH = "/unreg";
    private static final String FINDALL_PATH = "/findAll";
    private static final String VERSION_PATH = "/version";
    private static final String RENEWS_PATH = "/renews";
    
    private SunnyRegistryConfigProperties sunnyRegistryConfigProperties;
    
    private String server;
    
    private final Map<String, Long> VERSIONS = new HashMap<>();
    private final MultiValueMap<InstanceMeta, ServiceMeta> RENEWS = new LinkedMultiValueMap<>();
    
    private final SunnyRegistryHeathChecker healthChecker = new SunnyRegistryHeathChecker();
    
    public SunnyRegistryCenter(SunnyRegistryConfigProperties sunnyRegistryConfigProperties){
        this.sunnyRegistryConfigProperties = sunnyRegistryConfigProperties;
        this.server = sunnyRegistryConfigProperties.getServerList().get(0);
    }
    
    @Override
    public void start() {
        healthChecker.start();
        providerCheck();
        log.info("SunnyRegistry client started");
    }
    
    @Override
    public void stop() {
        healthChecker.stop();
        log.info("SunnyRegistry client closed");
    }
      
    public void providerCheck() {
//        开启了一个定时任务 来定时renew心跳
        healthChecker.providerCheck(() -> {
            RENEWS.keySet().forEach(
                    instance -> {
                        try {
                            Long timestamp = HttpInvoker.httpPost(JSON.toJSONString(instance),
                                    renewsPath(RENEWS.get(instance)), Long.class);
                            log.info(" ====>>>> [SunnyRegistry] : renew instance {} at {}", instance, timestamp);
                        }catch (Exception ex){
                            log.error(" ====>>>> [SunnyRegistry] : renew Exception:", ex);
                        }
                      
                    }
            );
        });
    }

    @Override
    public void register(final ServiceMeta serviceMeta, final InstanceMeta instanceMeta) {
        log.info(" ====>>>> [SunnyRegistry] : register instance {} to {}", instanceMeta.toUrl(), instanceMeta.toPath());
        InstanceMeta inst = HttpInvoker.httpPost(JSON.toJSONString(instanceMeta), regPath(serviceMeta), InstanceMeta.class);
        RENEWS.add(instanceMeta, serviceMeta);
        log.info(" ====>>>> [SunnyRegistry] : registered {}", inst);
    }
   
    @Override
    public void unRegister(final ServiceMeta serviceMeta, final InstanceMeta instanceMeta) {
        log.info(" ====>>>> [SunnyRegistry] : unregister instance {} to {}", instanceMeta.toUrl(), serviceMeta.toPath());
        InstanceMeta inst = HttpInvoker.httpPost(JSON.toJSONString(instanceMeta), unregPath(serviceMeta), InstanceMeta.class);
        RENEWS.remove(instanceMeta, serviceMeta);
        log.info(" ====>>>> [SunnyRegistry] : unregistered {}", inst);
    }
   
    @Override
    public List<InstanceMeta> fetchAll(final ServiceMeta serviceMeta) {
        log.info(" ====>>>> [SunnyRegistry] : find all instances for {}", serviceMeta.toPath());
        List<InstanceMeta> instances = HttpInvoker.httpGet(findAllPath(serviceMeta), new TypeReference<List<InstanceMeta>>() {});
        log.info(" ====>>>> [SunnyRegistry] : findAll = {}", instances);
        return instances;
    }
    
    @SneakyThrows
    @Override
    public void subscribe(final ServiceMeta serviceMeta, final ChangedListener changedListener) {
        healthChecker.check(() -> {
            String versionPath = versionPath(serviceMeta);
            Long newVersion = HttpInvoker.httpGet(versionPath, Long.class);
            Long version = VERSIONS.getOrDefault(serviceMeta.toPath(), -1L);
            log.debug(" ====>>>> [{}] newVersion:{} oldVersion:{}", serviceMeta.toPath(), newVersion, version);
            if (newVersion > version) {
                log.info(" ====>>>> version changed [{}] newVersion:{} oldVersion:{}", serviceMeta.toPath(), newVersion, version);
                List<InstanceMeta> instances = fetchAll(serviceMeta);
                log.info(" ====>>>> version {} fetch all and fire: {}", newVersion, instances);
                changedListener.fire(new Event(instances));
                VERSIONS.put(serviceMeta.toPath(), newVersion);
            }
        });
    }
    
    private String findAllPath(ServiceMeta service) {
        return server + "/findAll?service=" + service.toPath();
    }
    
    private String versionPath(ServiceMeta service) {
        return server + "/version?service=" + service.toPath();
    }
    
    private String regPath(ServiceMeta service) {
        return path(REG_PATH, service);
    }
    private String unregPath(ServiceMeta service) {
        return path(UNREG_PATH, service);
    }
    
    private String renewsPath(List<ServiceMeta> serviceList) {
        return path(RENEWS_PATH, serviceList);
    }
    private String path(String context, ServiceMeta service) {
        return server + context + "?service=" + service.toPath();
    }
    private String path(String context, List<ServiceMeta> serviceList) {
        StringBuffer sb = new StringBuffer();
        for (ServiceMeta service : serviceList) {
            sb.append(service.toPath()).append(",");
        }
        String services = sb.toString();
        if(services.endsWith(",")) services = services.substring(0, services.length() - 1);
        log.info(" ====>>>> [SunnyRegistry] : renew instance for {}", services);
        return server + context + "?services=" + services;
    }
}
