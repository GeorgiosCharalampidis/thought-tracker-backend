package com.rumino;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class rumino {

    public static void main(String[] args) {
        SpringApplication.run(rumino.class, args);
    }

}
