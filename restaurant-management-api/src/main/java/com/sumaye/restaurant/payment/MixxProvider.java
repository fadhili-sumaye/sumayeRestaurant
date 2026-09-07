package com.sumaye.restaurant.payment;

import com.sumaye.restaurant.model.Payment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MixxProvider implements PaymentProvider {

    @Override
    public String getProviderName() {
        return "Mixx by Yas";
    }

    @Override
    public boolean supports(Payment.PaymentProvider provider) {
        return Payment.PaymentProvider.MIXX.equals(provider);
    }

    @Override
    public boolean validateTransactionReference(String reference) {
        return reference != null && reference.trim().length() >= 6;
    }

    @Override
    public Payment.PaymentTransactionStatus processPayment(Payment payment) {
        log.info("Processing Mixx payment reference: {} for amount: {}", payment.getTransactionReference(), payment.getAmount());
        return Payment.PaymentTransactionStatus.SUCCESS;
    }
}
