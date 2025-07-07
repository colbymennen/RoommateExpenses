package com.colby.roommate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication      // <-- this enables component scanning in com.colby.roommate and sub-packages
public class RoommateServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RoommateServiceApplication.class, args);
    }
}
