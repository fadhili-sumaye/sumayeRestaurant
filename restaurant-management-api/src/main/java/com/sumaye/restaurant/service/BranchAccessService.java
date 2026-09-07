package com.sumaye.restaurant.service;

import com.sumaye.restaurant.exception.ApiException;
import com.sumaye.restaurant.exception.ResourceNotFoundException;
import com.sumaye.restaurant.model.Branch;
import com.sumaye.restaurant.model.User;
import com.sumaye.restaurant.repository.BranchRepository;
import com.sumaye.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service @RequiredArgsConstructor
public class BranchAccessService {
    private final UserRepository users;
    private final BranchRepository branches;

    public User requireAccess(String username, Long branchId) {
        User user = users.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Mtumiaji hakupatikana"));
        Branch branch = branches.findById(branchId).orElseThrow(() -> new ResourceNotFoundException("Tawi halikupatikana"));
        boolean management = user.getRoles().stream().map(r -> r.getName()).anyMatch(r -> r.endsWith("OWNER") || r.endsWith("ADMIN") || r.endsWith("MANAGER"));
        boolean sameRestaurant = user.getRestaurant() != null && user.getRestaurant().getId().equals(branch.getRestaurant().getId());
        boolean sameBranch = user.getBranch() != null && user.getBranch().getId().equals(branchId);
        if (!user.isActive() || (!sameBranch && !(management && sameRestaurant))) throw new ApiException("Huruhusiwi kufikia tawi hili");
        return user;
    }
}
