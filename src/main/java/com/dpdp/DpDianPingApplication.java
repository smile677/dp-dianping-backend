package com.dpdp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.dpdp.mapper")
@SpringBootApplication
public class DpDianPingApplication {

    public static void main(String[] args) {
        args = new String[]{"--mpw.key=97d73d2b0466533b"};
        SpringApplication.run(DpDianPingApplication.class, args);
    }

}
