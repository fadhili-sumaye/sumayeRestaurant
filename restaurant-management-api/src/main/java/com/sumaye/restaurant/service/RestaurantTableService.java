package com.sumaye.restaurant.service;

import com.sumaye.restaurant.exception.ResourceNotFoundException;
import com.sumaye.restaurant.model.Branch;
import com.sumaye.restaurant.model.RestaurantTable;
import com.sumaye.restaurant.model.User;
import com.sumaye.restaurant.repository.BranchRepository;
import com.sumaye.restaurant.repository.RestaurantTableRepository;
import com.sumaye.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantTableService {
    private final RestaurantTableRepository tableRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;

    public List<RestaurantTable> getTablesByBranch(Long branchId) {
        return getTablesByBranch(branchId, null);
    }

    public List<RestaurantTable> getTablesByBranch(Long branchId, String username) {
        Branch branch = null;
        if (username != null) {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                boolean isGlobalRole = user.getRoles().stream().anyMatch(r ->
                        "ROLE_ADMIN".equals(r.getName()) || "ROLE_OWNER".equals(r.getName()));
                if (!isGlobalRole && user.getBranch() != null) {
                    branch = user.getBranch();
                }
            }
        }
        if (branch == null) {
            if (branchId != null) {
                branch = branchRepository.findById(branchId).orElse(null);
            }
            if (branch == null) {
                branch = branchRepository.findAll().stream().findFirst().orElse(null);
            }
        }
        if (branch == null) {
            return java.util.Collections.emptyList();
        }
        return tableRepository.findByBranchOrderByTableNumberAsc(branch);
    }

    public RestaurantTable getTable(Long id) {
        return tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Table not found"));
    }

    public RestaurantTable createTable(Long branchId, Integer tableNumber, Integer capacity, RestaurantTable.TableStatus status) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        RestaurantTable table = new RestaurantTable();
        table.setBranch(branch);
        table.setTableNumber(tableNumber);
        table.setCapacity(capacity);
        table.setStatus(status != null ? status : RestaurantTable.TableStatus.AVAILABLE);
        table.setCreatedAt(LocalDateTime.now());
        return tableRepository.save(table);
    }

    public RestaurantTable updateTable(Long id, Integer tableNumber, Integer capacity, RestaurantTable.TableStatus status) {
        RestaurantTable table = getTable(id);
        table.setTableNumber(tableNumber);
        table.setCapacity(capacity);
        table.setStatus(status != null ? status : RestaurantTable.TableStatus.AVAILABLE);
        table.setUpdatedAt(LocalDateTime.now());
        return tableRepository.save(table);
    }

    public RestaurantTable updateTableStatus(Long id, RestaurantTable.TableStatus status) {
        RestaurantTable table = getTable(id);
        table.setStatus(status);
        table.setUpdatedAt(LocalDateTime.now());
        return tableRepository.save(table);
    }

    public void deleteTable(Long id) {
        tableRepository.delete(getTable(id));
    }
}
