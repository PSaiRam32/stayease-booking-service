package com.stayease.booking_service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = "com.stayease")
@SpringBootApplication
@Slf4j
public class BookingServiceApplication {
	public static void main(String[] args) {
		log.info("Starting Booking Service Application");
		SpringApplication.run(BookingServiceApplication.class, args);
		log.info("Booking Service Application Started Successfully on port 8085");
		log.info("API Documentation: http://localhost:8085/swagger-ui.html");
	}

}
