//package com.stayease.booking_service.config;
//
//import com.stayease.booking_service.exception.BusinessException;
//import feign.Response;
//import feign.codec.ErrorDecoder;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//@Slf4j
//@Component
//public class FeignErrorDecoder implements ErrorDecoder {
//
//    @Override
//    public Exception decode(String methodKey, Response response) {
//        int status = response.status();
//        String message = "Feign client error from " + methodKey + " with status: " + status;
//        log.error("Feign client error - Method: {}, Status: {}, Reason: {}",
//                methodKey, status, response.reason());
//
//        switch (status) {
//            case 400:
//                return new BusinessException("Bad Request from downstream service: " + response.reason());
//            case 404:
//                return new BusinessException("Resource not found in downstream service: " + response.reason());
//            case 409:
//                return new BusinessException("Conflict in downstream service: " + response.reason());
//            case 500:
//                return new BusinessException("Internal server error in downstream service: " + response.reason());
//            case 503:
//                return new BusinessException("Service unavailable: " + response.reason());
//            default:
//                return new BusinessException(message);
//        }
//    }
//}