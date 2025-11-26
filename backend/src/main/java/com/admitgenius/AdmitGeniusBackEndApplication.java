package com.admitgenius;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.admitgenius")
public class AdmitGeniusBackEndApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdmitGeniusBackEndApplication.class, args);
    }
}
