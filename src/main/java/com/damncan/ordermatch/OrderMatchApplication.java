package com.damncan.ordermatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;

@SpringBootApplication(scanBasePackages = "com.damncan.ordermatch")
@PropertySources({
        @PropertySource("classpath:application.properties")
})
public class OrderMatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderMatchApplication.class, args);
    }
}
