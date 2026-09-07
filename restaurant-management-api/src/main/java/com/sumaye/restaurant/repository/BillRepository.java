package com.sumaye.restaurant.repository;

import com.sumaye.restaurant.model.Bill;
import com.sumaye.restaurant.model.Branch;
import com.sumaye.restaurant.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findByOrder(Order order);
    Optional<Bill> findByBillNumber(String billNumber);
    List<Bill> findByBranchOrderByCreatedAtDesc(Branch branch);
    List<Bill> findByBranchAndPaymentStatusInOrderByCreatedAtDesc(Branch branch, List<Bill.PaymentStatus> statuses);
    List<Bill> findByBranchAndBillRequestedTrueAndPaymentStatusNot(Branch branch, Bill.PaymentStatus paymentStatus);
}
