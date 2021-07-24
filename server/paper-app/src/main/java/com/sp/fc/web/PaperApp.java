package com.sp.fc.web;

import com.sp.fc.web.config.DBInit;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = {
        "com.sp.fc.config",
        "com.sp.fc.web",
        "com.sp.fc.site"
})
public class PaperApp {
    public static void main(String[] args) {
        SpringApplication.run(PaperApp.class,args);
    }


}
