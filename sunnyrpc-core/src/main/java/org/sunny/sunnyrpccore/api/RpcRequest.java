package org.sunny.sunnyrpccore.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest {
    private String service; // 服务名 org.sunny.sunnyprcdemoapi.interfaces.UserService
    private String methodSign;  // 方法名 getUserByid@2_id_name
    private Object[] args;    // 参数 id name
    // 跨调用方需要传递的参数
//    private Map<String,String> params = new HashMap<>();
}
