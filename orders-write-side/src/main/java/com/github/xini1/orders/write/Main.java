package com.github.xini1.orders.write;

import com.github.xini1.orders.write.application.SpringConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;

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
