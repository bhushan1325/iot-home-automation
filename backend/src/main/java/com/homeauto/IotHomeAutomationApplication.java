package com.homeauto;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IotHomeAutomationApplication {

    public static void main(String[] args) {
        SpringApplication.run(IotHomeAutomationApplication.class, args);
    }
}
