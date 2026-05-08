package com.irms.table;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TableServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TableServiceApplication.class, args);
    }
}
