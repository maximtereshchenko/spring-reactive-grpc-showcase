package com.github.xini1.apigateway;

import com.github.xini1.apigateway.router.OrdersReadRouterFunction;
import com.github.xini1.apigateway.router.OrdersWriteRouterFunction;
import com.github.xini1.apigateway.router.UsersRouterFunction;
import com.github.xini1.apigateway.service.OrdersReadService;
import com.github.xini1.apigateway.service.OrdersWriteService;
import com.github.xini1.apigateway.service.UsersService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

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
    RouterFunction<ServerResponse> usersRouterFunction(UsersService usersService) {
        return new UsersRouterFunction(usersService);
    }

    @Bean
    RouterFunction<ServerResponse> ordersWriteRouterFunction(
            UsersService usersService,
            @Value("${application.rpc.orders.write.address}") String address,
            @Value("${application.rpc.orders.write.port}") int port
    ) {
        return new OrdersWriteRouterFunction(usersService, new OrdersWriteService(address, port));
    }

    @Bean
    RouterFunction<ServerResponse> ordersReadRouterFunction(
            UsersService usersService,
            @Value("${application.rpc.orders.read.address}") String address,
            @Value("${application.rpc.orders.read.port}") int port
    ) {
        return new OrdersReadRouterFunction(usersService, new OrdersReadService(address, port));
    }
}
