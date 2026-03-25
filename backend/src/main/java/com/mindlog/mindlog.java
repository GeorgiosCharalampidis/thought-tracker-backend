package com.mindlog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class mindlog {

    public static void main(String[] args) {
        SpringApplication.run(mindlog.class, args);
    }

}
