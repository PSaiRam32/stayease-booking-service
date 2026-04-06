package com.stayease.booking_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@EnableFeignClients(basePackages = "com.stayease")
@SpringBootApplication
public class BookingServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(BookingServiceApplication.class, args);
		System.out.println("Booking Service is up and running");
	}

}
