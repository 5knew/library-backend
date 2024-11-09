package com.aues.library.service.impl;

import com.aues.library.exceptions.BookNotFoundException;
import com.aues.library.exceptions.PaymentNotFoundException;
import com.aues.library.exceptions.PaymentProcessingException;
import com.aues.library.model.Order;
import com.aues.library.model.Payment;
import com.aues.library.repository.OrderRepository;
import com.aues.library.repository.PaymentRepository;
import com.aues.library.service.EmailService;
import com.aues.library.service.PaymentService;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CloudinaryService cloudinaryService;
    private final EmailService emailService;
    private final PayPalService payPalService;

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository, OrderRepository orderRepository,
                              PayPalService payPalService, CloudinaryService cloudinaryService,
                              EmailService emailService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.payPalService = payPalService;
        this.cloudinaryService = cloudinaryService;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public com.aues.library.model.Payment processPayment(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Order not found with ID: " + orderId));

        BigDecimal amount = order.getTotalAmount();
        String cancelUrl = "https://yourdomain.com/payment-failure";
        String successUrl = "https://yourdomain.com/payment-success";

        try {
            // Use PayPalService to create a payment
            com.paypal.api.payments.Payment payPalPayment = payPalService.createPayment(
                    amount.doubleValue(),
                    "USD",
                    "paypal",
                    "sale",
                    "Payment for Order " + orderId,
                    cancelUrl,
                    successUrl
            );

            // Get the approval URL from the PayPal payment
            String paymentLink = payPalPayment.getLinks().stream()
                    .filter(link -> link.getRel().equals("approval_url"))
                    .map(com.paypal.api.payments.Links::getHref)
                    .findFirst()
                    .orElseThrow(() -> new PaymentProcessingException("Approval URL not found"));

            // Create and save a new Payment object in your system
            com.aues.library.model.Payment paymentRecord = new com.aues.library.model.Payment();
            paymentRecord.setOrder(order);
            paymentRecord.setPaymentDate(new Date());
            paymentRecord.setAmount(amount);
            paymentRecord.setTransactionId(payPalPayment.getId()); // PayPal's transaction ID
            paymentRecord.setPaymentStatus("PENDING");
            paymentRepository.save(paymentRecord);

            // Send the payment link to the user
            emailService.sendEmail(
                    userEmail,
                    "Оплата заказа",
                    "Перейдите по ссылке для завершения оплаты: " + paymentLink
            );

            return paymentRecord;

        } catch (PayPalRESTException e) {
            throw new PaymentProcessingException("Error creating PayPal payment", e);
        }
    }

    @Override
    @Transactional
    public void handlePaymentNotification(String paymentId, String payerId) {
        try {
            com.paypal.api.payments.Payment payPalPayment = payPalService.executePayment(paymentId, payerId);
            com.aues.library.model.Payment paymentRecord = paymentRepository.findByTransactionId(paymentId)
                    .orElseThrow(() -> new PaymentNotFoundException("Payment with transaction ID " + paymentId + " not found"));

            if ("approved".equalsIgnoreCase(payPalPayment.getState())) {
                paymentRecord.setPaymentStatus("PAID");
                paymentRepository.save(paymentRecord);

                // Send purchased items as attachments
                List<byte[]> pdfs = paymentRecord.getOrder().getCartItems().stream()
                        .map(cartItem -> {
                            String pdfUrl = cartItem.getBookCopy().getBook().getFullPdf();
                            return cloudinaryService.downloadFile(pdfUrl);
                        }).collect(Collectors.toList());

                emailService.sendEmailWithMultiplePdfAttachments(
                        paymentRecord.getOrder().getUser().getEmail(),
                        "Your Purchased Books",
                        "Thank you for your purchase! Please find attached the full PDFs of the books you bought.",
                        pdfs,
                        "BookPurchase"
                );
            } else {
                paymentRecord.setPaymentStatus("CANCELED");
                paymentRepository.save(paymentRecord);
            }

        } catch (PayPalRESTException e) {
            throw new PaymentProcessingException("Error completing PayPal payment", e);
        }
    }

    @Override
    @Transactional
    public Payment createPayment(Long orderId, Payment payment) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new PaymentProcessingException("Order with ID " + orderId + " not found"));

        payment.setOrder(order);
        payment.setPaymentDate(new Date());
        return paymentRepository.save(payment);
    }

    @Override
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment with ID " + id + " not found"));
    }

    @Override
    public Payment getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment with transaction ID " + transactionId + " not found"));
    }

    @Override
    public Payment getPaymentByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment for order ID " + orderId + " not found"));
    }

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    @Transactional
    public Payment updatePaymentStatus(Long id, String paymentStatus) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new PaymentNotFoundException("Payment with ID " + id + " not found"));
        payment.setPaymentStatus(paymentStatus);
        return paymentRepository.save(payment);
    }

    @Override
    public void deletePayment(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw new PaymentNotFoundException("Payment with ID " + id + " not found");
        }
        paymentRepository.deleteById(id);
    }
}
