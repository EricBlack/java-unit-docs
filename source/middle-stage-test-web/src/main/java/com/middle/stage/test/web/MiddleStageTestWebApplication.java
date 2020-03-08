package com.middle.stage.test.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.middle.stage.test.*")
public class MiddleStageTestWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiddleStageTestWebApplication.class, args);
    }

}