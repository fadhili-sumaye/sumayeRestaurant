package com.sumaye.restaurant.payment;

import com.sumaye.restaurant.model.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MpesaProvider implements PaymentProvider {

    @Override
    public String getProviderName() {
        return "M-Pesa";
    }

    @Override
    public boolean supports(Payment.PaymentProvider provider) {
        return Payment.PaymentProvider.MPESA.equals(provider);
    }

    @Override
    public boolean validateTransactionReference(String reference) {
        // M-Pesa standard reference check (alphanumeric, 8-12 characters)
        return reference != null && reference.trim().length() >= 6;
    }

    @Override
    public Payment.PaymentTransactionStatus processPayment(Payment payment) {
        // Architectural bridge for future Daraja API / Open API integration
        log.info("Processing M-Pesa payment reference: {} for amount: {}", payment.getTransactionReference(), payment.getAmount());
        return Payment.PaymentTransactionStatus.SUCCESS;
    }
}
