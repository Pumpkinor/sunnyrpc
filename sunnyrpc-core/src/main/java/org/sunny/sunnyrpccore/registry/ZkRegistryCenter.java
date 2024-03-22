package org.sunny.sunnyrpccore.registry;

import lombok.SneakyThrows;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.sunny.sunnyrpccore.api.RegistryCenter;
import org.sunny.sunnyrpccore.meta.InstanceMeta;
import org.sunny.sunnyrpccore.meta.ServiceMeta;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class ZkRegistryCenter implements RegistryCenter {
    @Value("${sunnyrpc.zkServer}")
    String servers;
    
    @Value("${sunnyrpc.zkRoot}")
    String root;
    
    private CuratorFramework client = null;
    private TreeCache treeCache = null;
    @Override
    public void start() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3);
        client =  CuratorFrameworkFactory.builder()
                .connectString(servers)
                .namespace(root)
                .retryPolicy(retryPolicy)
                .build();
        client.start();
        System.out.println("zk client started");
    }
    
    @Override
    public void stop() {
        System.out.println("start close zk client");
        treeCache.close();
        client.close();
        System.out.println("zk client closed");
    }
    
    @Override
    public void register(final ServiceMeta serviceMeta, final InstanceMeta instanceMeta) {
//        service创建成持久节点
//        instance 创建成临时节点
        String servicePath = "/" + serviceMeta.toPath();
        
        try {
//            创建服务的持久化节点
            if (client.checkExists().forPath(servicePath) == null){
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath,"service".getBytes(StandardCharsets.UTF_8));
            }
//            创建instance的临时节点
            String instancePath = servicePath + "/" + instanceMeta.toPath();
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath,"instance".getBytes(StandardCharsets.UTF_8));
            System.out.println("register service: " + serviceMeta + " instance: " + instanceMeta);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }
    
    @Override
    public void unRegister(final ServiceMeta serviceMeta, final InstanceMeta instanceMeta) {
        System.out.println("start unRegister service: " + serviceMeta + " instance: " + instanceMeta);
        String servicePath = "/" + serviceMeta.toPath();
        try {
            if (client.checkExists().forPath(servicePath) == null){
                return;            
            }
            String instancePath = servicePath + "/" + instanceMeta.toPath();
            client.delete().quietly().forPath(instancePath);
            System.out.println("unRegistered service: " + serviceMeta + " instance: " + instanceMeta);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
    }
    
    @Override
    public List<InstanceMeta> fetchAll(final ServiceMeta serviceMeta) {
        String servicePath = "/" + serviceMeta.toPath();
        List<String> nodes = null;
        try {
            nodes = client.getChildren().forPath(servicePath);
            System.out.println("fetch all instance from zk: ");
            nodes.forEach(System.out::println);
            return mapToInstanceMeta(nodes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @NotNull
    private static List<InstanceMeta> mapToInstanceMeta(final List<String> nodes) {
        return nodes.stream().map(x -> {
            String[] infos = x.split("_");
            return InstanceMeta.http(infos[0], Integer.valueOf(infos[1]));
        }).collect(Collectors.toList());
    }
    
    @SneakyThrows
    @Override
    public void subscribe(final ServiceMeta serviceMeta, final ChangedListener changedListener) {
        String servicePath = "/" + serviceMeta.toPath();
        treeCache = TreeCache.newBuilder(client,servicePath)
                .setCacheData(true).setMaxDepth(2).build();
        treeCache.getListenable().addListener((curator,event)->{
//            zk有任何节点变化 这里的代码就会执行
            System.out.println("zk event subscribe : " + event);
            List<InstanceMeta> nodes = fetchAll(serviceMeta);
            changedListener.fire(new Event(nodes));
        });
        treeCache.start();
    }
}
