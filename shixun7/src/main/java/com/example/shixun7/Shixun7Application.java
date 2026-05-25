package com.example.shixun7;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.shixun7.mapper")  // 添加这一行，指定 mapper 包路径
public class Shixun7Application {

    public static void main(String[] args) {
        SpringApplication.run(Shixun7Application.class, args);
    }
}
