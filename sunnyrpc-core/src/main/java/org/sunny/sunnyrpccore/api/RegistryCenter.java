package org.sunny.sunnyrpccore.api;

import java.util.List;

public interface RegistryCenter {
    void start();
    
    void stop();
    
//    provider use
    void register(String service, String instance);
    
    void unRegister(String service, String instance);
    
//    consumer use
    
    List<String> fetchAll(String service);
    
//    void subscribe();
    
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
        public void register(final String service, final String instance) {
            
        }
        
        @Override
        public void unRegister(final String service, final String instance) {
            
        }
        
        @Override
        public List<String> fetchAll(final String service) {
            return providers;
        }
    }

}
