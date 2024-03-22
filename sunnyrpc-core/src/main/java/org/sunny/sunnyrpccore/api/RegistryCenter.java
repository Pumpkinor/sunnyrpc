package org.sunny.sunnyrpccore.api;

import org.sunny.sunnyrpccore.meta.InstanceMeta;
import org.sunny.sunnyrpccore.meta.ServiceMeta;
import org.sunny.sunnyrpccore.registry.ChangedListener;

import java.util.ArrayList;
import java.util.List;

public interface RegistryCenter {
    void start();
    
    void stop();
    
//    provider use
    void register(ServiceMeta serviceMeta, InstanceMeta instanceMeta);
    
    void unRegister(ServiceMeta serviceMeta, InstanceMeta instanceMeta);
    
//    consumer use
    
    List<InstanceMeta> fetchAll(ServiceMeta serviceMeta);
    
    void subscribe(ServiceMeta serviceMeta, ChangedListener changedListener);
//    void heartbeat();
    
    class StaticRegisterCenter implements RegistryCenter {
        List<String> providers;
        
        public StaticRegisterCenter(List<String> providers){
            this.providers = providers;
        }
        @Override
        public void start() {
            
        }
        
        @Override
        public void stop() {
            
        }
        
        @Override
        public void register(final ServiceMeta serviceMeta, final InstanceMeta instanceMeta) {
            
        }
        
        @Override
        public void unRegister(final ServiceMeta serviceMeta, final InstanceMeta instanceMeta) {
            
        }
        
        @Override
        public List<InstanceMeta> fetchAll(final ServiceMeta serviceMeta) {
            List<InstanceMeta> list = new ArrayList<>();
            return list;
        }
        
        @Override
        public void subscribe(final ServiceMeta serviceMeta, final ChangedListener changedListener) {
            
        }
    }

}
