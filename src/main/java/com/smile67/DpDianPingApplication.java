package com.smile67;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.smile67.mapper")
@SpringBootApplication
public class DpDianPingApplication {

    public static void main(String[] args) {
        args = new String[]{"--mpw.key=fb7c084da403650d"};
        SpringApplication.run(DpDianPingApplication.class, args);
    }

}
