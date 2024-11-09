package com.aues.library.service.impl;

import com.paypal.api.payments.Payment;
import com.paypal.api.payments.PaymentExecution;
import com.paypal.api.payments.Transaction;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PayPalService {

    @Autowired
    private APIContext apiContext;

    public Payment createPayment(Double total, String currency, String method,
                                 String intent, String description, String cancelUrl, String successUrl) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setIntent(intent);

        // Configure Payment Method and Amount
        payment.setPayer(new com.paypal.api.payments.Payer().setPaymentMethod(method));
        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(new com.paypal.api.payments.Amount().setCurrency(currency).setTotal(String.format("%.2f", total)));

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);
        payment.setTransactions(transactions);

        // Set Redirect URLs
        com.paypal.api.payments.RedirectUrls redirectUrls = new com.paypal.api.payments.RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);
        payment.setRedirectUrls(redirectUrls);

        return payment.create(apiContext);
    }

    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(paymentId);
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);
        return payment.execute(apiContext, paymentExecution);
    }
}
