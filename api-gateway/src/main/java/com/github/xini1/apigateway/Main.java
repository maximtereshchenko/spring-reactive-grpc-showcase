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
    UsersController usersController(
            @Value("${application.rpc.users.address}") String address,
            @Value("${application.rpc.users.port}") int port
    ) {
        return new UsersController(new UsersService(address, port));
    }
}
