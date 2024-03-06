package org.sunny.sunnyprcdemoapi.interfaces;

import org.sunny.sunnyprcdemoapi.domian.Order;

public interface OrderService {
    Order getOrderById(Integer orderId);
}
