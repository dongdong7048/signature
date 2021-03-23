package com.signature;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication   //此註解表示啟動springboot應用
@MapperScan("com.signature.mapper") //此註解表示啟用 Mybatis Mapper，掃描的包是com.signature.mapper
public class SignatureApplication {

    //項目啟動的起始點
    public static void main(String[] args) {
        SpringApplication.run(SignatureApplication.class, args);
    }

}
