package com.sumaye.restaurant.dto;

import com.sumaye.restaurant.model.Payment;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {
    @NotNull(message = "Kiasi cha malipo kinahitajika.")
    @DecimalMin(value = "0.01", message = "Kiasi cha malipo lazima kiwe zaidi ya 0.")
    private BigDecimal amount;

    @NotNull(message = "Njia ya malipo inahitajika.")
    private Payment.PaymentMethod paymentMethod;

    private Payment.PaymentProvider provider;

    private String transactionReference;

    private BigDecimal cashReceived;

    private String idempotencyKey;

    private String notes;
}
