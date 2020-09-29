package com.example.hostinfo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.example.hostinfo.dao")
public class HostinfoApplication {

    public static void main(String[] args) {
        SpringApplication.run(HostinfoApplication.class, args);
    }

}
