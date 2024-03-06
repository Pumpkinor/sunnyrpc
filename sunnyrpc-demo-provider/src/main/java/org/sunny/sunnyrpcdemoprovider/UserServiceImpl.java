package org.sunny.sunnyrpcdemoprovider;

import org.springframework.stereotype.Component;
import org.sunny.sunnyprcdemoapi.domian.User;
import org.sunny.sunnyprcdemoapi.interfaces.UserService;
import org.sunny.sunnyrpccore.annotation.SunnyProvider;

@SunnyProvider
@Component
public class UserServiceImpl implements UserService {
    @Override
    public User getUserById(String id) {
        return new User("1","bob",12);
    }
}
