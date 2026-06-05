package com.fleet.dispatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FleetDispatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(FleetDispatchApplication.class, args);
    }
}
