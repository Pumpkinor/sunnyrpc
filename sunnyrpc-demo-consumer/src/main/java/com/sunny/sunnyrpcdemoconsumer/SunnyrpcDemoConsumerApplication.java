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
            User user = userService.getUserById("1");
            System.out.println(user);
            Integer id = userService.getID(222);
            System.out.println(id);
            String name = userService.getName("Tomas");
            System.out.println(name);
            Order order = orderService.getOrderById(404);
            System.out.println(order);
        };
    }
}
