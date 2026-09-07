package com.sumaye.restaurant.service;

import com.sumaye.restaurant.dto.ExpenseCategoryRequest;
import com.sumaye.restaurant.dto.ExpenseRequest;
import com.sumaye.restaurant.exception.ApiException;
import com.sumaye.restaurant.exception.ResourceNotFoundException;
import com.sumaye.restaurant.model.AuditLog;
import com.sumaye.restaurant.model.Branch;
import com.sumaye.restaurant.model.Expense;
import com.sumaye.restaurant.model.ExpenseCategory;
import com.sumaye.restaurant.model.User;
import com.sumaye.restaurant.repository.AuditLogRepository;
import com.sumaye.restaurant.repository.BranchRepository;
import com.sumaye.restaurant.repository.ExpenseCategoryRepository;
import com.sumaye.restaurant.repository.ExpenseRepository;
import com.sumaye.restaurant.repository.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpenseRepository expenses;
    private final ExpenseCategoryRepository categories;
    private final BranchRepository branches;
    private final UserRepository users;
    private final AuditLogRepository audit;

    @Transactional
    public Expense create(Long branchId, ExpenseRequest request, String username) {
        Branch branch = branch(branchId);
        ExpenseCategory category = categories.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Kundi la gharama halikupatikana"));
        if (!category.getBranch().getId().equals(branchId) || !category.isActive()) {
            throw new ApiException("Kundi hili halipatikani kwa tawi hili");
        }
        User user = user(username);
        Expense expense = new Expense();
        expense.setBranch(branch);
        expense.setCategory(category);
        expense.setDescription(request.getDescription().trim());
        expense.setAmount(request.getAmount());
        expense.setPaymentMethod(request.getPaymentMethod());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setReferenceNumber(blankToNull(request.getReferenceNumber()));
        expense.setNotes(blankToNull(request.getNotes()));
        expense.setRecordedBy(user);
        Expense saved = expenses.save(expense);
        recordAudit("EXPENSE_CREATED", saved.getId(), user, branch,
                "Gharama TZS " + saved.getAmount() + " imehifadhiwa");
        return saved;
    }

    @Transactional
    public ExpenseCategory createCategory(Long branchId, ExpenseCategoryRequest request, String username) {
        Branch branch = branch(branchId);
        String name = request.getName().trim();
        if (categories.existsByBranchIdAndNameIgnoreCase(branchId, name)) {
            throw new ApiException("Kundi hili la gharama tayari lipo");
        }
        ExpenseCategory category = new ExpenseCategory();
        category.setBranch(branch);
        category.setName(name);
        category.setDescription(blankToNull(request.getDescription()));
        ExpenseCategory saved = categories.save(category);
        recordAudit("EXPENSE_CATEGORY_CREATED", saved.getId(), user(username), branch,
                "Kundi la gharama " + saved.getName() + " limehifadhiwa");
        return saved;
    }

    @Transactional
    public void voidExpense(Long id, String reason, String username) {
        if (reason == null || reason.isBlank()) {
            throw new ApiException("Sababu ya kubatilisha inahitajika");
        }
        Expense expense = expenses.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gharama haikupatikana"));
        if (expense.getStatus() == Expense.Status.VOIDED) {
            throw new ApiException("Gharama hii tayari imebatilishwa");
        }
        User user = user(username);
        expense.setStatus(Expense.Status.VOIDED);
        expense.setVoidReason(reason.trim());
        expense.setVoidedBy(user);
        expense.setVoidedAt(LocalDateTime.now());
        expense.setUpdatedAt(LocalDateTime.now());
        expenses.save(expense);
        recordAudit("EXPENSE_VOIDED", expense.getId(), user, expense.getBranch(),
                "Gharama TZS " + expense.getAmount() + " imebatilishwa: " + expense.getVoidReason());
    }

    private Branch branch(Long branchId) {
        return branches.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Tawi halikupatikana"));
    }

    private User user(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Mtumiaji hakupatikana"));
    }

    private void recordAudit(String action, Long entityId, User user, Branch branch, String details) {
        audit.save(AuditLog.builder().action(action).entityType("Expense").entityId(entityId)
                .performedBy(user).branch(branch).details(details).createdAt(LocalDateTime.now()).build());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
