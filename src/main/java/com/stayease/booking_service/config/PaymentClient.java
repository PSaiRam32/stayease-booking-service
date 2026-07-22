package com.stayease.booking_service.config;

import com.stayease.booking_service.dto.response.ApiResponse;
import com.stayease.booking_service.dto.request.PaymentOrderRequest;
import com.stayease.booking_service.dto.response.PaymentOrderResponse;
import com.stayease.booking_service.dto.response.RefundResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name="payment-service",
        url="${services.payment.url}",
        configuration=FeignConfig.class
)
public interface PaymentClient{
    @PostMapping("/payments/order")
    PaymentOrderResponse createPaymentOrder(@RequestBody PaymentOrderRequest request);

    @PostMapping("/payments/booking/{bookingId}/refund")
    RefundResponse refundBooking(@PathVariable Long bookingId);

    @GetMapping("/payments/booking/{bookingId}")
    PaymentOrderResponse getPaymentByBookingId(@PathVariable Long bookingId);
}