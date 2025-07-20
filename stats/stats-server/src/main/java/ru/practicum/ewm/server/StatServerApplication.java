package ru.practicum.ewm.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class StatServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(StatServerApplication.class, args);
    }
}