package org.sunny.sunnyrpcdemoprovider;

import org.springframework.stereotype.Component;
import org.sunny.sunnyprcdemoapi.domian.Order;
import org.sunny.sunnyprcdemoapi.interfaces.OrderService;
import org.sunny.sunnyrpccore.annotation.SunnyProvider;

@SunnyProvider
@Component
public class OrderServiceImpl implements OrderService {
    @Override
    public Order getOrderById(Integer orderId) {
        if (orderId == 404){
            throw new RuntimeException("404 exception");
        }
        return new Order(1L, 2.0F);
    }
}
