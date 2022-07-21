package com.github.xini1.orders.write;

import com.github.xini1.orders.write.application.*;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.annotation.*;

/**
 * @author Maxim Tereshchenko
 */
@EnableAutoConfiguration
@Import(SpringConfiguration.class)
public final class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class);
    }
}
