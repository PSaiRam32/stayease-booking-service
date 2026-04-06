package com.stayease.booking_service.config;

import com.stayease.booking_service.dto.PaymentRequestDTO;
import com.stayease.booking_service.dto.PaymentResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "payment-service",
        url = "${services.payment.url}",
        configuration = FeignClientConfig.class
)
public interface PaymentClient {

    @PostMapping("/payments/initiate")
    PaymentResponseDTO initiatePayment(@RequestBody PaymentRequestDTO request);
}