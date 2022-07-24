package com.github.xini1.apigateway;

import com.github.xini1.apigateway.controller.*;
import com.github.xini1.apigateway.service.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.annotation.*;

/**
 * @author Maxim Tereshchenko
 */
@EnableAutoConfiguration
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class);
    }

    @Bean
    UsersService usersService(
            @Value("${application.rpc.users.address}") String address,
            @Value("${application.rpc.users.port}") int port
    ) {
        return new UsersService(address, port);
    }

    @Bean
    UsersController usersController(UsersService usersService) {
        return new UsersController(usersService);
    }

    @Bean
    OrdersWriteController ordersWriteController(
            UsersService usersService,
            @Value("${application.rpc.orders.write.address}") String address,
            @Value("${application.rpc.orders.write.port}") int port
    ) {
        return new OrdersWriteController(usersService, new OrdersWriteService(address, port));
    }

    @Bean
    OrdersReadController ordersReadController(
            UsersService usersService,
            @Value("${application.rpc.orders.read.address}") String address,
            @Value("${application.rpc.orders.read.port}") int port
    ) {
        return new OrdersReadController(usersService, new OrdersReadService(address, port));
    }
}
