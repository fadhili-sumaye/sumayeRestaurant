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
    @Query("select p.paymentMethod, p.provider, count(p), coalesce(sum(p.amount),0) from Payment p " +
            "where p.branch.id=:branchId and p.status='SUCCESS' and p.createdAt between :from and :to " +
            "group by p.paymentMethod, p.provider order by coalesce(sum(p.amount),0) desc")
    List<Object[]> paymentMethodStats(@Param("branchId") Long branchId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    @Query("select p.createdAt, p.amount, p.paymentMethod, p.provider from Payment p " +
            "where p.branch.id=:branchId and p.status='SUCCESS' and p.createdAt between :from and :to")
    List<Object[]> revenuesBetween(@Param("branchId") Long branchId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    @Query("select count(distinct p.order.id) from Payment p where p.branch.id=:branchId and p.status='SUCCESS' and p.createdAt between :from and :to")
    long countPaidOrdersBetween(@Param("branchId") Long branchId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    @Query("select p.cashier.username, count(p), coalesce(sum(p.amount),0) from Payment p " +
            "where p.branch.id=:branchId and p.status='SUCCESS' and p.createdAt between :from and :to and p.cashier is not null " +
            "group by p.cashier.username order by coalesce(sum(p.amount),0) desc")
    List<Object[]> cashierStatsBetween(@Param("branchId") Long branchId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
