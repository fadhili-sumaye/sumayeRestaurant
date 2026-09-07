package com.sumaye.restaurant.repository;

import com.sumaye.restaurant.model.Bill;
import com.sumaye.restaurant.model.Branch;
import com.sumaye.restaurant.model.Order;
import com.sumaye.restaurant.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentNumber(String paymentNumber);
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    List<Payment> findByBillOrderByCreatedAtAsc(Bill bill);
    List<Payment> findByOrderOrderByCreatedAtAsc(Order order);
    List<Payment> findByBranchOrderByCreatedAtDesc(Branch branch);
    @Query("select coalesce(sum(p.amount),0) from Payment p where p.branch.id=:branchId and p.status='SUCCESS' and p.createdAt between :from and :to")
    BigDecimal totalSuccessful(@Param("branchId") Long branchId,@Param("from") LocalDateTime from,@Param("to") LocalDateTime to);
    @Query("select coalesce(sum(p.amount),0) from Payment p where p.branch.id=:branchId and p.status='SUCCESS' and p.paymentMethod=:method and p.createdAt between :from and :to")
    BigDecimal totalSuccessfulByMethod(@Param("branchId") Long branchId,@Param("method") Payment.PaymentMethod method,@Param("from") LocalDateTime from,@Param("to") LocalDateTime to);
}
