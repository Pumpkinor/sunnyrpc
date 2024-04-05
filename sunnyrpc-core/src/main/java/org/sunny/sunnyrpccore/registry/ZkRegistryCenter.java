package org.sunny.sunnyrpccore.registry;

import com.alibaba.fastjson.JSON;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.jetbrains.annotations.NotNull;
import org.sunny.sunnyrpccore.api.RegistryCenter;
import org.sunny.sunnyrpccore.config.ZkConfigProperties;
import org.sunny.sunnyrpccore.exception.ZkException;
import org.sunny.sunnyrpccore.meta.InstanceMeta;
import org.sunny.sunnyrpccore.meta.ServiceMeta;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j

public class ZkRegistryCenter implements RegistryCenter {
    
    private ZkConfigProperties zkConfigProperties;
    public ZkRegistryCenter(ZkConfigProperties zkConfigProperties){
        this.zkConfigProperties = zkConfigProperties;
    }
    
    private CuratorFramework client = null;
    private TreeCache treeCache = null;
    @Override
    public void start() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3);
        client =  CuratorFrameworkFactory.builder()
                .connectString(zkConfigProperties.getServer())
                .namespace(zkConfigProperties.getRoot())
                .retryPolicy(retryPolicy)
                .build();
        client.start();
        log.info("zk client started");
    }
    
    @Override
    public void stop() {
        log.info("start close zk client"); 
        if (treeCache != null){
            treeCache.close();
        }
        client.close();
        log.info("zk client closed");
    }
    
    @Override
    public void register(final ServiceMeta serviceMeta, final InstanceMeta instanceMeta) {
//        service创建成持久节点
//        instance 创建成临时节点
        String servicePath = "/" + serviceMeta.toPath();
        
        try {
//            创建服务的持久化节点
            if (client.checkExists().forPath(servicePath) == null){
                client.create().withMode(CreateMode.PERSISTENT).forPath(servicePath, serviceMeta.toMetas().getBytes());
            }
//            创建instance的临时节点
            String instancePath = servicePath + "/" + instanceMeta.toPath();
            client.create().withMode(CreateMode.EPHEMERAL).forPath(instancePath,instanceMeta.toMetas().getBytes(StandardCharsets.UTF_8));
            log.info("register service: " + serviceMeta + " instance: " + instanceMeta);
        } catch (Exception e) {
            throw new ZkException(e);
        }
        
    }
    
    @Override
    public void unRegister(final ServiceMeta serviceMeta, final InstanceMeta instanceMeta) {
        log.info("start unRegister service: " + serviceMeta + " instance: " + instanceMeta);
        String servicePath = "/" + serviceMeta.toPath();
        try {
            if (client.checkExists().forPath(servicePath) == null){
                return;            
            }
            String instancePath = servicePath + "/" + instanceMeta.toPath();
            client.delete().quietly().forPath(instancePath);
            log.info("unRegistered service: " + serviceMeta + " instance: " + instanceMeta);
        } catch (Exception e) {
            throw new ZkException(e);
        }
        
    }
    
    @Override
    public List<InstanceMeta> fetchAll(final ServiceMeta serviceMeta) {
        String servicePath = "/" + serviceMeta.toPath();
        List<String> nodes;
        try {
            nodes = client.getChildren().forPath(servicePath);
            log.debug("fetch all instance from zk: ");
            nodes.forEach(System.out::println);
            return mapToInstanceMeta(nodes, servicePath);
        } catch (Exception e) {
            throw new ZkException(e);
        }
    }
    
    @NotNull
    private  List<InstanceMeta> mapToInstanceMeta(final List<String> nodes, final String servicePath) {
        return nodes.stream().map(x -> {
            String[] strArr = x.split("_");
            InstanceMeta instance = InstanceMeta.http(strArr[0], Integer.valueOf(strArr[1]));
            String nodePath = servicePath + "/" + x;
            byte[] bytes;
            try {
                bytes = client.getData().forPath(nodePath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Map<String,Object> params = JSON.parseObject(new String(bytes));
            log.info("instance: " + instance.toUrl() + "  metas:");
            params.forEach((k,v) -> {
                log.info(k + " -> " +v);
                instance.getParameters().put(k,v==null?null:v.toString());
            });
            return instance;
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
            log.debug("zk event subscribe : " + event);
            List<InstanceMeta> nodes = fetchAll(serviceMeta);
            changedListener.fire(new Event(nodes));
        });
        treeCache.start();
    }
}
