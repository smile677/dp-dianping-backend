package com.smile67;

import com.baomidou.mybatisplus.core.toolkit.AES;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.smile67.mapper")
@SpringBootApplication
public class DpDianPingApplication {

    public static void main(String[] args) {
        args = new String[]{"--mpw.key=d1fc9fe46a4b3b6e"};
        SpringApplication.run(DpDianPingApplication.class, args);
    }

}
