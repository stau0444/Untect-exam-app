package com.sp.fc.paper;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = {
        "com.sp.fc.paper.repository"
})

@EntityScan(basePackages = {
        "com.sp.fc.paper.domain",
        "com.sp.fc.user.domain"
})
@SpringBootApplication
public class PaperApp {

    public static void main(String[] args) {
        SpringApplication.run(PaperApp.class,args);
    }
}

