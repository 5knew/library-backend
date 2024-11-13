package com.aues.library.controller;

import com.aues.library.exceptions.PaymentNotFoundException;
import com.aues.library.exceptions.PaymentProcessingException;
import com.aues.library.model.Payment;
import com.aues.library.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Process the main payment flow
    @PostMapping("/process")
    public ResponseEntity<?> processPayment(@RequestParam String userEmail,
                                            @RequestParam Long userId, @RequestBody List<Long> cartItemIds) {
        try {
            Map<String, String> response = paymentService.processPayment(userEmail, userId, cartItemIds);
            return new ResponseEntity<>(response, HttpStatus.CREATED); // Возвращаем JSON с approvalUrl
        } catch (PaymentProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Payment processing error: " + e.getMessage());
        }
    }


    // Process payment notification from PayPal, with paymentId and payerId
    @PostMapping("/notification")
    public ResponseEntity<?> handlePaymentNotification(@RequestParam String paymentId, @RequestParam String payerId) {
        try {
            paymentService.handlePaymentNotification(paymentId, payerId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (PaymentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Payment with ID " + paymentId + " not found.");
        } catch (PaymentProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error processing payment notification: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestParam Long orderId, @RequestBody Payment payment) {
        try {
            Payment createdPayment = paymentService.createPayment(orderId, payment);
            return new ResponseEntity<>(createdPayment, HttpStatus.CREATED);
        } catch (PaymentProcessingException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        try {
            Payment payment = paymentService.getPaymentById(id);
            return new ResponseEntity<>(payment, HttpStatus.OK);
        } catch (PaymentNotFoundException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/payment/success")
    public String paymentSuccess(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId) {
        try {
            paymentService.handlePaymentNotification(paymentId, payerId);
            return "redirect:/payment-success"; // Переадресация на страницу успеха на клиенте
        } catch (PaymentProcessingException e) {
            return "redirect:/payment-failure"; // Переадресация на страницу неудачной оплаты
        }
    }

    @GetMapping("/payment/failure")
    public String paymentFailure() {
        return "redirect:/payment-failure"; // Переадресация на страницу неудачной оплаты
    }


    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<Payment> getPaymentByTransactionId(@PathVariable String transactionId) {
        try {
            Payment payment = paymentService.getPaymentByTransactionId(transactionId);
            return new ResponseEntity<>(payment, HttpStatus.OK);
        } catch (PaymentNotFoundException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Payment> getPaymentByOrderId(@PathVariable Long orderId) {
        try {
            Payment payment = paymentService.getPaymentByOrderId(orderId);
            return new ResponseEntity<>(payment, HttpStatus.OK);
        } catch (PaymentNotFoundException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        List<Payment> payments = paymentService.getAllPayments();
        return payments.isEmpty() ? new ResponseEntity<>(HttpStatus.NO_CONTENT) : new ResponseEntity<>(payments, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePaymentStatus(@PathVariable Long id, @RequestParam String paymentStatus) {
        try {
            Payment updatedPayment = paymentService.updatePaymentStatus(id, paymentStatus);
            return new ResponseEntity<>(updatedPayment, HttpStatus.OK);
        } catch (PaymentNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Payment with ID " + id + " not found.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deletePayment(@PathVariable Long id) {
        try {
            paymentService.deletePayment(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (PaymentNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
