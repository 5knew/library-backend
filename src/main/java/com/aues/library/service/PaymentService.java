package com.aues.library.service;

import com.aues.library.model.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface PaymentService {
    Payment createPayment(Long orderId, Payment payment);
    Payment getPaymentById(Long id);
    Payment getPaymentByTransactionId(String transactionId);
    Payment getPaymentByOrderId(Long orderId);
    List<Payment> getAllPayments();
    Payment updatePaymentStatus(Long id, String paymentStatus);
    void deletePayment(Long id);
    Map<String, String> processPayment(String userEmail, Long userId, List<Long> cartItemIds);
    void handlePaymentNotification(String paymentId, String payerId);
    Page<Payment> getFilteredPayments(String paymentStatus, String transactionId, Long orderId, Pageable pageable);
    // Updated to accept both parameters
}

