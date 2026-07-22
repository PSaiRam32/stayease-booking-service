//package com.stayease.booking_service.config;
//
//import feign.Logger;
//import feign.Request;
//import feign.Retryer;
//import feign.codec.ErrorDecoder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.time.Duration;
//
//@Configuration
//public class FeignLoggingConfig {
//
//    //LoggingConfig
//    @Bean
//    Logger.Level feignLoggerLevel() {
//        return Logger.Level.FULL;
//    }
//
//    //TimeoutConfig
//    @Bean
//    Request.Options options() {
//        return new Request.Options(Duration.ofMillis(3000),Duration.ofMillis(5000),true);
//    }
//
//    //Retry Config
//    @Bean
//    public Retryer retryer() {
//        return new Retryer.Default(5L,10L,3);
//    }
//
//    @Bean
//    public ErrorDecoder feignErrorDecoder() {
//        return new FeignErrorDecoder();
//    }
//}