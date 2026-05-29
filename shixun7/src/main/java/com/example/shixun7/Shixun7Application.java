package com.example.shixun7;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.shixun7.mapper")
@EnableKnife4j
public class Shixun7Application {
    public static void main(String[] args) {
        SpringApplication.run(Shixun7Application.class, args);
    }
}