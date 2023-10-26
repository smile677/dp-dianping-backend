package com.smile67;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.smile67.mapper")
@SpringBootApplication
public class DpDianPingApplication {

    public static void main(String[] args) {
        args = new String[]{"--mpw.key=97d73d2b0466533b"};
        SpringApplication.run(DpDianPingApplication.class, args);
    }

}
