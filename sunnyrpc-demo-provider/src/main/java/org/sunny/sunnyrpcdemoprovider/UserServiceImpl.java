package org.sunny.sunnyrpcdemoprovider;

import org.springframework.stereotype.Component;
import org.sunny.sunnyprcdemoapi.domian.Order;
import org.sunny.sunnyprcdemoapi.domian.User;
import org.sunny.sunnyprcdemoapi.interfaces.UserAddrService;
import org.sunny.sunnyprcdemoapi.interfaces.UserService;
import org.sunny.sunnyrpccore.annotation.SunnyProvider;

@SunnyProvider
@Component
public class UserServiceImpl implements UserService, UserAddrService
{
    @Override
    public User getUserById(String id) {
        return new User("1","bob",12);
    }
    
    @Override
    public Integer getID(final Integer id) {
        return id;
    }
    
    @Override
    public String getName(final Integer id, final String name) {
        return id + ":" + name;
    }
    
    @Override
    public String getName(final String name) {
        return name;
    }
    
    @Override
    public Order getOrderById(final Integer orderId) {
        return null;
    }
}
