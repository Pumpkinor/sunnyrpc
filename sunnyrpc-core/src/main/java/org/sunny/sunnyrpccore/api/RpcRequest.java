package org.sunny.sunnyrpccore.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest {
    private String service; // 服务名 org.sunny.sunnyprcdemoapi.interfaces.UserService
    private String method;  // 方法名 getUserByid
    private Object[] params;    // 参数 id
}
