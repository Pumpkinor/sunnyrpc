package org.sunny.sunnyrpccore.api;

public interface Filter {
    Object preFilter(RpcRequest rpcRequest);
    
    Object postFilter(RpcRequest rpcRequest, RpcResponse rpcResponse, Object result);
    
    Filter Default = new Filter() {
        @Override
        public RpcResponse preFilter(final RpcRequest rpcRequest) {
            return null;
        }
        
        @Override
        public RpcResponse postFilter(final RpcRequest rpcRequest, final RpcResponse rpcResponse, final Object result) {
            return rpcResponse;
        }
    };
    
}
