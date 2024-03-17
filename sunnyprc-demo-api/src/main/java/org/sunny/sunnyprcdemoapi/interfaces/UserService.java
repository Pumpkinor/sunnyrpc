package org.sunny.sunnyprcdemoapi.interfaces;

import org.sunny.sunnyprcdemoapi.domian.User;

import java.util.List;
import java.util.Map;

public interface UserService {
    User getUserById(String name);
    
    Integer getID(Integer id);
    Long getID(Long id);
    
    Long getID(User user);
    
    Long getID(Float id);
    
    int[] getIds();
    long[] getLongIds();
    int[] getIds(int[] ids);
    
    String getName(Integer id, String name);
    
    String getName(String name);
    
    List<User> getList(List<User> userList);
    
    Map<String, User> getMap(Map<String, User> userMap);
    
    Boolean getFlag(boolean flag);
}
