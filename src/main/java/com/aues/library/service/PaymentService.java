package com.aues.library.service;

import com.aues.library.model.Payment;

import java.util.List;

public interface PaymentService {
    Payment createPayment(Long orderId, Payment payment);
    Payment getPaymentById(Long id);
    Payment getPaymentByTransactionId(String transactionId);
    Payment getPaymentByOrderId(Long orderId);
    List<Payment> getAllPayments();
    Payment updatePaymentStatus(Long id, String paymentStatus);
    void deletePayment(Long id);
    Payment processPayment(Long orderId, String userEmail);
    void handlePaymentNotification(String paymentId, String payerId); // Updated to accept both parameters
}

