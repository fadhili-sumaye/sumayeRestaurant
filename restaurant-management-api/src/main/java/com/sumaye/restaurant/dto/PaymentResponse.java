package com.sumaye.restaurant.dto;

import com.sumaye.restaurant.model.Payment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long id;
    private String paymentNumber;
    private Long billId;
    private String billNumber;
    private Long orderId;
    private String orderNumber;
    private BigDecimal amount;
    private Payment.PaymentMethod paymentMethod;
    private Payment.PaymentProvider provider;
    private String transactionReference;
    private BigDecimal cashReceived;
    private BigDecimal changeGiven;
    private Payment.PaymentTransactionStatus status;
    private String cashierName;
    private String notes;
    private LocalDateTime createdAt;
}
