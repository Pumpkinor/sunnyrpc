package com.sunny.sunnyrpcdemoconsumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.sunny.sunnyprcdemoapi.domian.User;
import org.sunny.sunnyprcdemoapi.interfaces.OrderService;
import org.sunny.sunnyprcdemoapi.interfaces.UserService;
import org.sunny.sunnyrpccore.annotation.SunnyConsumer;
import org.sunny.sunnyrpccore.consumer.ConsumerConfig;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@Import({ConsumerConfig.class})
@RestController
@Slf4j
public class SunnyrpcDemoConsumerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SunnyrpcDemoConsumerApplication.class, args);
    }
    
    @SunnyConsumer
    UserService userService;
    @SunnyConsumer
    OrderService orderService;    
    
    @RequestMapping("/")
    public User invoke(@RequestParam int id){
        User user = userService.getUserById(String.valueOf(id));
        log.info(String.valueOf(user));
        return user;
    }
    
    @Bean
    public ApplicationRunner consumer_runner(){
        return x ->{
            log.info("Case 14. >>===[测试参数和返回值都是User[]类型]===");
            User[] users = new User[]{
                    new User("100", "KK100",22),
                    new User("101", "KK101",23)};
            Arrays.stream(userService.findUsers(users)).forEach(e->log.info(String.valueOf(e)));
            
            //            // 测试参数和返回值都是List类型
            log.info("Case 11. >>===[测试参数和返回值都是List类型]===");
            List<User> list = userService.getList(List.of(
                    new User("100", "KK100",100),
                    new User("101", "KK101",12)));
            list.forEach(e->log.info(String.valueOf(e)));

            // 测试参数和返回值都是Map类型
            log.info("Case 12. >>===[测试参数和返回值都是Map类型]===");
            Map<String, User> map = new HashMap<>();
            map.put("A200", new User("200", "KK200",20));
            map.put("A201", new User("201", "KK201",21));
            userService.getMap(map).forEach(
                    (k,v) -> log.info(k + " -> " + v)
            );
            
            log.info("Case 13. >>===[测试参数和返回值都是Boolean/boolean类型]===");
            log.info("userService.getFlag(false) = " + userService.getFlag(false));
            
            
            log.info(" ===> userService.getLongIds()");
            for (long id : userService.getIds()) {
                log.info(String.valueOf(id));
            }
            log.info(" ===> userService.getLongIds()");
            for (long id : userService.getLongIds()) {
                log.info(String.valueOf(id));
            }
            
            log.info(" ===> userService.getLongIds()");
            for (long id : userService.getIds(new int[]{4,5,6})) {
                log.info(String.valueOf(id));
            }
            Long id1 = userService.getID(222L);
            log.info(String.valueOf(id1));
            Long id2 = userService.getID(222f);
            log.info(String.valueOf(id2));
            Long id3 = userService.getID(new User("111","bob",12));
            log.info(String.valueOf(id3));
            User user = userService.getUserById("1");
            log.info(String.valueOf(user));
            Integer id = userService.getID(222);
            log.info(String.valueOf(id));
            String name = userService.getName("Tomas");
            log.info(name);
            name = userService.getName(2222,"Tomas");
            log.info(name);
//           TODO multiple interfaces how to test it
//            Order order = orderService.getOrderById(404);
//            log.info(order);
        };
    }
}
