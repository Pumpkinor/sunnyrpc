package org.sunny.sunnyprcdemoapi.interfaces;

import org.sunny.sunnyprcdemoapi.domian.User;

public interface UserService {
    User getUserById(String name);
    
    Integer getID(Integer id);
    String getName(Integer id, String name);
    
    String getName(String name);
}
