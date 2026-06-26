package com.parentkid.checkin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.parentkid.checkin.mapper")
public class ParentKidCheckinApplication {

    public static void main(String[] args) {
        SpringApplication.run(ParentKidCheckinApplication.class, args);
    }
}
