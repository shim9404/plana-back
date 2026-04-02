package com.example.plana;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // 자동 실행(지정된 시간 간격이나 특정 시점)
public class PlanaApplication {

	public static void main(String[] args) {
		SpringApplication.run(PlanaApplication.class, args);
	}

}
