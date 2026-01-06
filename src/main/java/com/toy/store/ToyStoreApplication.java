package com.toy.store;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
		org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
@EnableScheduling
@MapperScan("com.toy.store.mapper")
public class ToyStoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(ToyStoreApplication.class, args);
	}

}
