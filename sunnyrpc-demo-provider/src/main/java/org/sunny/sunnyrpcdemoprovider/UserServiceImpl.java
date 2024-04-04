package org.sunny.sunnyrpcdemoprovider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.sunny.sunnyprcdemoapi.domian.Order;
import org.sunny.sunnyprcdemoapi.domian.User;
import org.sunny.sunnyprcdemoapi.interfaces.UserAddrService;
import org.sunny.sunnyprcdemoapi.interfaces.UserService;
import org.sunny.sunnyrpccore.annotation.SunnyProvider;

import java.util.List;
import java.util.Map;

@SunnyProvider
@Component
public class UserServiceImpl implements UserService, UserAddrService
{
    @Autowired
    Environment environment;
    @Override
    public User getUserById(String id) {
        return new User("1","bob - port is : " + environment.getProperty("server.port") ,12);
    }
    
    @Override
    public Integer getID(final Integer id) {
        return id;
    }
    
    @Override
    public Long getID(final Long id) {
        return id;
    }
    
    @Override
    public Long getID(final User user) {
        return Long.valueOf(user.getId());
    }
    
    @Override
    public Long getID(final Float id) {
        return 222L;
    }
    
    @Override
    public String getName(final Integer id, final String name) {
        return id + ":" + name;
    }
    @Override
    public int[] getIds() {
        return new int[] {100,200,300};
    }
    
    @Override
    public long[] getLongIds() {
        return new long[]{1,2,3};
    }
    
    @Override
    public int[] getIds(int[] ids) {
        return ids;
    }
    
    @Override
    public String getName(final String name) {
        return name;
    }
    
    @Override
    public List<User> getList(final List<User> userList) {
        return userList;
    }
    @Override
    public User[] findUsers(User[] users) {
        return users;
    }
    
    @Override
    public Map<String, User> getMap(final Map<String, User> userMap) {
        return userMap;
    }
    
    @Override
    public Boolean getFlag(final boolean flag) {
        return flag;
    }
    
    private String timeOutPorts = "9997,9999";
    @Override
    public void setTimeOutPorts(final String timeOutPorts) {
        this.timeOutPorts = timeOutPorts;
    }
    
    @Override
    public User timeOut(final int timeOut) {
        String port = environment.getProperty("server.port");
        assert port != null;
        if (timeOutPorts.contains(port)){
            try {
                Thread.sleep(timeOut);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return new User("21","Pop - " + port,34);
    }
    
    @Override
    public Order getOrderById(final Integer orderId) {
        return null;
    }
    
    
}
