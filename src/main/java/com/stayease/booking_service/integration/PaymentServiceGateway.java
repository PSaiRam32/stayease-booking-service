package com.stayease.booking_service.integration;


import com.stayease.booking_service.config.PaymentClient;
import com.stayease.booking_service.dto.request.PaymentOrderRequest;
import com.stayease.booking_service.dto.response.ApiResponse;
import com.stayease.booking_service.dto.response.PaymentOrderResponse;
import com.stayease.booking_service.dto.response.RefundResponse;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceGateway {

    private final PaymentClient paymentClient;

    @Retry(name = "payment-service")
    @CircuitBreaker(name = "payment-service", fallbackMethod = "createPaymentOrderFallback")
    @Bulkhead(name = "payment-service",
            type = Bulkhead.Type.SEMAPHORE,fallbackMethod = "createPaymentOrderFallback"
    )
    public ApiResponse<PaymentOrderResponse> createPaymentOrder(PaymentOrderRequest request) {
        log.info("Calling Payment Service");
        return paymentClient.createPaymentOrder(request);
    }

    public ApiResponse<PaymentOrderResponse> createPaymentOrderFallback(PaymentOrderRequest request,Exception ex){
        log.error("Payment Service unavailable", ex);
        throw new RuntimeException("Payment Service is temporarily unavailable."+ex);
    }

    @Retry(name = "payment-service")
    @CircuitBreaker(name = "payment-service", fallbackMethod = "refundBookingFallback")
    public ApiResponse<RefundResponse> refundBooking(Long bookingId) {
        return paymentClient.refundBooking(bookingId);
    }

    public ApiResponse<RefundResponse> refundBookingFallback(Long bookingId,Exception ex){
        log.error("Refund failed", ex);
        throw new RuntimeException("Payment Service is temporarily unavailable."+ex);
    }

    @Retry(name = "payment-service")
    @CircuitBreaker(name = "payment-service", fallbackMethod = "getPaymentFallback")
    public ApiResponse<PaymentOrderResponse> getPaymentByBookingId(Long bookingId) {
        return paymentClient.getPaymentByBookingId(bookingId);
    }

    public ApiResponse<PaymentOrderResponse> getPaymentFallback(Long bookingId,Exception ex){
        log.error("Payment lookup failed", ex);
        throw new RuntimeException("Payment Service is temporarily unavailable."+ex);
    }
}