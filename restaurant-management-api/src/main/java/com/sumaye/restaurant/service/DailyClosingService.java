package com.sumaye.restaurant.service;

import com.sumaye.restaurant.dto.DailyClosingRequest;
import com.sumaye.restaurant.exception.ApiException;
import com.sumaye.restaurant.exception.ResourceNotFoundException;
import com.sumaye.restaurant.model.AuditLog;
import com.sumaye.restaurant.model.Branch;
import com.sumaye.restaurant.model.CashReconciliation;
import com.sumaye.restaurant.model.DailyClosing;
import com.sumaye.restaurant.model.Payment;
import com.sumaye.restaurant.model.User;
import com.sumaye.restaurant.repository.AuditLogRepository;
import com.sumaye.restaurant.repository.BranchRepository;
import com.sumaye.restaurant.repository.CashReconciliationRepository;
import com.sumaye.restaurant.repository.DailyClosingRepository;
import com.sumaye.restaurant.repository.ExpenseRepository;
import com.sumaye.restaurant.repository.PaymentRepository;
import com.sumaye.restaurant.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DailyClosingService {
    private final DailyClosingRepository closings;
    private final CashReconciliationRepository reconciliations;
    private final BranchRepository branches;
    private final UserRepository users;
    private final PaymentRepository payments;
    private final ExpenseRepository expenses;
    private final AuditLogRepository audit;

    @Transactional
    public DailyClosing close(Long branchId, DailyClosingRequest request, String username) {
        if (request.getBusinessDate().isAfter(LocalDate.now())) {
            throw new ApiException("Huwezi kufunga siku ya baadaye");
        }
        if (closings.findByBranchIdAndBusinessDate(branchId, request.getBusinessDate()).isPresent()) {
            throw new ApiException("Siku hii tayari imefungwa");
        }

        Branch branch = branches.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Tawi halikupatikana"));
        User user = users.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Mtumiaji hakupatikana"));
        LocalDateTime from = request.getBusinessDate().atStartOfDay();
        LocalDateTime to = request.getBusinessDate().plusDays(1).atStartOfDay().minusNanos(1);
        BigDecimal cashSales = totalForMethod(branchId, Payment.PaymentMethod.CASH, from, to);
        BigDecimal mobileMoneySales = totalForMethod(branchId, Payment.PaymentMethod.MOBILE_MONEY, from, to);
        BigDecimal cardSales = totalForMethod(branchId, Payment.PaymentMethod.CARD, from, to);
        BigDecimal bankSales = totalForMethod(branchId, Payment.PaymentMethod.BANK, from, to);
        BigDecimal otherSales = totalForMethod(branchId, Payment.PaymentMethod.OTHER, from, to);
        BigDecimal totalSales = payments.totalSuccessful(branchId, from, to);
        BigDecimal totalExpenses = expenses.total(branchId, request.getBusinessDate(), request.getBusinessDate());
        BigDecimal cashExpenses = expenses.totalByPaymentMethod(branchId, Payment.PaymentMethod.CASH,
                request.getBusinessDate(), request.getBusinessDate());
        BigDecimal expectedCash = cashSales.subtract(cashExpenses);
        BigDecimal difference = request.getActualCash().subtract(expectedCash);
        if (difference.compareTo(BigDecimal.ZERO) != 0
                && (request.getDifferenceReason() == null || request.getDifferenceReason().isBlank())) {
            throw new ApiException("Sababu ya tofauti ya fedha inahitajika");
        }

        DailyClosing closing = new DailyClosing();
        closing.setBranch(branch);
        closing.setBusinessDate(request.getBusinessDate());
        closing.setTotalSales(totalSales);
        closing.setCashSales(cashSales);
        closing.setMobileMoneySales(mobileMoneySales);
        closing.setCardSales(cardSales);
        closing.setBankSales(bankSales);
        closing.setOtherSales(otherSales);
        closing.setExpenses(totalExpenses);
        closing.setExpectedCash(expectedCash);
        closing.setClosedBy(user);
        closing.setNotes(blankToNull(request.getNotes()));
        DailyClosing saved = closings.save(closing);

        CashReconciliation reconciliation = new CashReconciliation();
        reconciliation.setDailyClosing(saved);
        reconciliation.setExpectedCash(expectedCash);
        reconciliation.setActualCash(request.getActualCash());
        reconciliation.setDifference(difference);
        reconciliation.setDifferenceReason(blankToNull(request.getDifferenceReason()));
        reconciliation.setRecordedBy(user);
        reconciliations.save(reconciliation);
        audit.save(AuditLog.builder().action("DAILY_CLOSING_CREATED").entityType("DailyClosing")
                .entityId(saved.getId()).performedBy(user).branch(branch)
                .details("Kufunga siku " + request.getBusinessDate() + "; mauzo TZS " + totalSales
                        + "; tofauti ya fedha TZS " + difference)
                .createdAt(LocalDateTime.now()).build());
        return saved;
    }

    public DailyClosing get(Long branchId, LocalDate businessDate) {
        return closings.findByBranchIdAndBusinessDate(branchId, businessDate)
                .orElseThrow(() -> new ResourceNotFoundException("Kufunga siku hakukupatikana"));
    }

    private BigDecimal totalForMethod(Long branchId, Payment.PaymentMethod method, LocalDateTime from, LocalDateTime to) {
        return payments.totalSuccessfulByMethod(branchId, method, from, to);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
