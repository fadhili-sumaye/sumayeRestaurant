package com.sumaye.restaurant.repository;

import com.sumaye.restaurant.model.Expense;
import com.sumaye.restaurant.model.Payment;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    @EntityGraph(attributePaths = "category")
    List<Expense> findByBranchIdAndExpenseDateBetweenOrderByExpenseDateDesc(Long branchId, LocalDate from, LocalDate to);

    @Query("select coalesce(sum(e.amount), 0) from Expense e where e.branch.id = :branchId "
            + "and e.status = 'POSTED' and e.expenseDate between :from and :to")
    BigDecimal total(@Param("branchId") Long branchId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("select coalesce(sum(e.amount), 0) from Expense e where e.branch.id = :branchId "
            + "and e.status = 'POSTED' and e.paymentMethod = :method and e.expenseDate between :from and :to")
    BigDecimal totalByPaymentMethod(
            @Param("branchId") Long branchId,
            @Param("method") Payment.PaymentMethod method,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("select e.category.id, e.category.name, coalesce(sum(e.amount), 0) from Expense e "
            + "where e.branch.id = :branchId and e.status = 'POSTED' and e.expenseDate between :from and :to "
            + "group by e.category.id, e.category.name order by coalesce(sum(e.amount), 0) desc")
    List<Object[]> totalByCategory(@Param("branchId") Long branchId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("select e.expenseDate, e.amount from Expense e "
            + "where e.branch.id = :branchId and e.status = 'POSTED' and e.expenseDate between :from and :to")
    List<Object[]> expensesBetween(@Param("branchId") Long branchId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("select count(e) from Expense e where e.branch.id = :branchId and e.status = 'POSTED' and e.expenseDate between :from and :to")
    long countBetween(@Param("branchId") Long branchId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
