package com.aues.library.service.impl;

import com.aues.library.controller.BookController;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CloudinaryService cloudinaryService;
    private final EmailService emailService;
    private final PayPalService payPalService;
    private final CurrencyConversionService currencyConversionService;
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository, OrderRepository orderRepository,
                              PayPalService payPalService, CloudinaryService cloudinaryService,
                              EmailService emailService, CurrencyConversionService currencyConversionService) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.payPalService = payPalService;
        this.cloudinaryService = cloudinaryService;
        this.emailService = emailService;
        this.currencyConversionService = currencyConversionService;
    }

    @Override
    @Transactional
    public Map<String, String> processPayment(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Order not found with ID: " + orderId));

        BigDecimal amountInKZT = order.getTotalAmount();
        BigDecimal amountInUSD = currencyConversionService.convertToUSD(amountInKZT);

        String cancelUrl = "http://localhost:5173/payment/failure";
        String successUrl = "http://localhost:5173/payment/success";

        try {
            // Создаем платеж через PayPal
            com.paypal.api.payments.Payment payPalPayment = payPalService.createPayment(
                    amountInUSD.doubleValue(),
                    "USD",
                    "paypal",
                    "sale",
                    "Payment for Order " + orderId,
                    cancelUrl,
                    successUrl
            );

            // Извлекаем URL для подтверждения оплаты
            String approvalUrl = payPalPayment.getLinks().stream()
                    .filter(link -> link.getRel().equals("approval_url"))
                    .map(com.paypal.api.payments.Links::getHref)
                    .findFirst()
                    .orElseThrow(() -> new PaymentProcessingException("Approval URL not found"));

            logger.info("Attempting to create PayPal payment with amount: " + amountInUSD);
            logger.info("PayPal payment created with ID: " + payPalPayment.getId());
            payPalPayment.getLinks().forEach(link -> logger.info("Link rel: " + link.getRel() + ", href: " + link.getHref()));

            // Сохраняем запись о платеже в вашей системе
            com.aues.library.model.Payment paymentRecord = new com.aues.library.model.Payment();
            paymentRecord.setOrder(order);
            paymentRecord.setPaymentDate(new Date());
            paymentRecord.setAmount(amountInUSD);
            paymentRecord.setTransactionId(payPalPayment.getId());
            paymentRecord.setPaymentStatus("PENDING");
            paymentRepository.save(paymentRecord);

            // Отправляем ссылку для завершения оплаты на email пользователя
            emailService.sendEmail(
                    userEmail,
                    "Оплата заказа",
                    "Перейдите по ссылке для завершения оплаты: " + approvalUrl
            );

            // Возвращаем approvalUrl на фронтенд
            Map<String, String> response = new HashMap<>();
            response.put("approvalUrl", approvalUrl);  // Убедитесь, что это именно approvalUrl
            return response;

        } catch (PayPalRESTException e) {
            throw new PaymentProcessingException("Error creating PayPal payment", e);
        }
    }


    @Override
    @Transactional
    public void handlePaymentNotification(String paymentId, String payerId) {
        try {
            // Execute payment using PayPal service
            com.paypal.api.payments.Payment payPalPayment = payPalService.executePayment(paymentId, payerId);
            com.aues.library.model.Payment paymentRecord = paymentRepository.findByTransactionId(paymentId)
                    .orElseThrow(() -> new PaymentNotFoundException("Payment with transaction ID " + paymentId + " not found"));

            logger.info("Executing PayPal payment with paymentId: {} and payerId: {}", paymentId, payerId);

            if ("approved".equalsIgnoreCase(payPalPayment.getState())) {
                paymentRecord.setPaymentStatus("PAID");
                paymentRepository.save(paymentRecord);

                // Download PDFs for each book copy using Cloudinary metadata
                List<byte[]> pdfs = paymentRecord.getOrder().getCartItems().stream()
                        .map(cartItem -> {
                            String fullPdfUrl = cartItem.getBookCopy().getFullPdf();
                            try {
                                byte[] pdfData = cloudinaryService.downloadFileByUrl(fullPdfUrl);
                                if (pdfData == null || pdfData.length == 0) {
                                    logger.warn("Downloaded PDF is empty for BookCopy ID: {}", fullPdfUrl);
                                    return null;  // Skip empty files
                                }
                                return pdfData;
                            } catch (Exception e) {
                                logger.error("Failed to download file for BookCopy ID: {}", fullPdfUrl, e);
                                return null;  // Skip files with download issues
                            }
                        })
                        .filter(Objects::nonNull)  // Remove any null entries
                        .collect(Collectors.toList());

                // Send email with attachments if PDFs are available
                if (!pdfs.isEmpty()) {
                    try {
                        emailService.sendEmailWithMultiplePdfAttachments(
                                paymentRecord.getOrder().getUser().getEmail(),
                                "Your Purchased Books",
                                "Thank you for your purchase! Please find attached the full PDFs of the books you bought.",
                                pdfs,
                                "BookPurchase"
                        );
                        logger.info("Email with attachments sent successfully to {}", paymentRecord.getOrder().getUser().getEmail());
                    } catch (Exception e) {
                        logger.error("Failed to send email with attachments to user: {}", paymentRecord.getOrder().getUser().getEmail(), e);
                    }
                } else {
                    logger.warn("No PDFs available for attachment. Email will not include any files.");
                }
            } else {
                paymentRecord.setPaymentStatus("CANCELED");
                paymentRepository.save(paymentRecord);
                logger.info("Payment status set to CANCELED for transaction ID: {}", paymentId);
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
