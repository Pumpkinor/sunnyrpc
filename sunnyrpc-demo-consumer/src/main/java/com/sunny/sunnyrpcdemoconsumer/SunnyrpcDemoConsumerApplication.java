package com.sunny.sunnyrpcdemoconsumer;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.sunny.sunnyprcdemoapi.domian.Order;
import org.sunny.sunnyprcdemoapi.domian.User;
import org.sunny.sunnyprcdemoapi.interfaces.OrderService;
import org.sunny.sunnyprcdemoapi.interfaces.UserService;
import org.sunny.sunnyrpccore.annotation.SunnyConsumer;
import org.sunny.sunnyrpccore.consumer.ConsumerConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootApplication
@Import({ConsumerConfig.class})
public class SunnyrpcDemoConsumerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SunnyrpcDemoConsumerApplication.class, args);
    }
    
    @SunnyConsumer
    UserService userService;
    @SunnyConsumer
    OrderService orderService;    
    @Bean
    public ApplicationRunner consumer_runner(){
        return x ->{
//            // 测试参数和返回值都是List类型
            System.out.println("Case 11. >>===[测试参数和返回值都是List类型]===");
            List<User> list = userService.getList(List.of(
                    new User("100", "KK100",100),
                    new User("101", "KK101",12)));
            list.forEach(System.out::println);

            // 测试参数和返回值都是Map类型
            System.out.println("Case 12. >>===[测试参数和返回值都是Map类型]===");
            Map<String, User> map = new HashMap<>();
            map.put("A200", new User("200", "KK200",20));
            map.put("A201", new User("201", "KK201",21));
            userService.getMap(map).forEach(
                    (k,v) -> System.out.println(k + " -> " + v)
            );
            
            System.out.println("Case 13. >>===[测试参数和返回值都是Boolean/boolean类型]===");
            System.out.println("userService.getFlag(false) = " + userService.getFlag(false));
            
            
            System.out.println(" ===> userService.getLongIds()");
            for (long id : userService.getIds()) {
                System.out.println(id);
            }
            System.out.println(" ===> userService.getLongIds()");
            for (long id : userService.getLongIds()) {
                System.out.println(id);
            }
            
            System.out.println(" ===> userService.getLongIds()");
            for (long id : userService.getIds(new int[]{4,5,6})) {
                System.out.println(id);
            }
            Long id1 = userService.getID(222L);
            System.out.println(id1);
            Long id2 = userService.getID(222f);
            System.out.println(id2);
            Long id3 = userService.getID(new User("111","bob",12));
            System.out.println(id3);
            User user = userService.getUserById("1");
            System.out.println(user);
            Integer id = userService.getID(222);
            System.out.println(id);
            String name = userService.getName("Tomas");
            System.out.println(name);
            name = userService.getName(2222,"Tomas");
            System.out.println(name);
//           TODO multiple interfaces how to test it
            Order order = orderService.getOrderById(404);
            System.out.println(order);
        };
    }
}
