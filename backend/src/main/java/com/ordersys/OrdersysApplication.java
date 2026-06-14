package com.ordersys;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ordersys.*.mapper")
public class OrdersysApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrdersysApplication.class, args);
    }
}
