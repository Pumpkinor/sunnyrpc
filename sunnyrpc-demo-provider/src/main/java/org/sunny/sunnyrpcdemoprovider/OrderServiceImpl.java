package org.sunny.sunnyrpcdemoprovider;

import org.springframework.stereotype.Component;
import org.sunny.sunnyprcdemoapi.domian.Order;
import org.sunny.sunnyprcdemoapi.domian.User;
import org.sunny.sunnyprcdemoapi.interfaces.OrderService;
import org.sunny.sunnyprcdemoapi.interfaces.UserService;
import org.sunny.sunnyrpccore.annotation.SunnyProvider;

@SunnyProvider
@Component
public class OrderServiceImpl implements OrderService {
    @Override
    public Order getOrderById(Integer orderId) {
        return new Order(1L, 2.0F);
    }
}
