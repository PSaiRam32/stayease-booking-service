package com.stayease.booking_service.config;

import com.stayease.booking_service.dto.ApiResponse;
import com.stayease.booking_service.dto.PaymentOrderRequestDTO;
import com.stayease.booking_service.dto.PaymentOrderResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "payment-service",
        url = "${services.payment.url}",
        configuration = FeignClientConfig.class
)
public interface PaymentClient {

    @PostMapping("/payments/order")
    ApiResponse<PaymentOrderResponseDTO> createPaymentOrder(
            @RequestBody PaymentOrderRequestDTO request
    );
}