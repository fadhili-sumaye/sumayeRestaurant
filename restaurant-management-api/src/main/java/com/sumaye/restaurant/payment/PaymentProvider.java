package com.sumaye.restaurant.payment;

import com.sumaye.restaurant.model.Payment;

public interface PaymentProvider {
    String getProviderName();
    boolean supports(Payment.PaymentProvider provider);
    boolean validateTransactionReference(String reference);
    Payment.PaymentTransactionStatus processPayment(Payment payment);
}
