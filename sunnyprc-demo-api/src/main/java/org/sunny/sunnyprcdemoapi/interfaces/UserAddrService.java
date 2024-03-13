package org.sunny.sunnyprcdemoapi.interfaces;

import org.sunny.sunnyprcdemoapi.domian.Order;

public interface UserAddrService {
    Order getOrderById(Integer orderId);
}
