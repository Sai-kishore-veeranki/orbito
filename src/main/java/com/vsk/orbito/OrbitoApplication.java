package com.vsk.orbito;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync       // enables @Async on email sending
@EnableScheduling  // enables @Scheduled jobs
public class OrbitoApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrbitoApplication.class, args);
	}

}
