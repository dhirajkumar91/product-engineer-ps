package com.dhiraj.durable_reminders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class DurableRemindersApplication {

	public static void main(String[] args) {
		SpringApplication.run(DurableRemindersApplication.class, args);
	}

}
