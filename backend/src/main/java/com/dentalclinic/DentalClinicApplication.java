package com.dentalclinic;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.dentalclinic.mapper")
public class DentalClinicApplication {
    public static void main(String[] args) {
        SpringApplication.run(DentalClinicApplication.class, args);
    }
}
