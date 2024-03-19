package org.sunny.sunnyrpccore.registry;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.sunny.sunnyrpccore.api.RegistryCenter;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class ZkRegistryCenter implements RegistryCenter {
    private CuratorFramework client = null;
    
    @Override
    public void start() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3);
        client =  CuratorFrameworkFactory.builder()
                .connectString("8.141.93.226:2181")
                .namespace("sunnyrpc")
                .retryPolicy(retryPolicy)
                .build();
        client.start();
        System.out.println("zk client started");
    }
    
    @Override
    public void stop() {
        client.close();
        System.out.println("zk client closed");
    }
    
    @Override
    public void register(final String service, final String instance) {
//        service创建成持久节点
//        instance 创建成临时节点
        String servicePath = "/" + service;
        
        try {
//            创建服务的持久化节点
            if (client.checkExists().forPath(servicePath) == null){
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath,"service".getBytes(StandardCharsets.UTF_8));
            }
//            创建instance的临时节点
            String instancePath = servicePath + "/" + instance;
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath,"instance".getBytes(StandardCharsets.UTF_8));
            System.out.println("register service: " + service + " instance: " + instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }
    
    @Override
    public void unRegister(final String service, final String instance) {
        String servicePath = "/" + service;
        try {
            if (client.checkExists().forPath(servicePath) == null){
                return;            
            }
            String instancePath = servicePath + "/" + instance;
            client.delete().quietly().forPath(instancePath);
            System.out.println("unRegister service: " + service + " instance: " + instance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }
    
    @Override
    public List<String> fetchAll(final String service) {
        return null;
    }
}
