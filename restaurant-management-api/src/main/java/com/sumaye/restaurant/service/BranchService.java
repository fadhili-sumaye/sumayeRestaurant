package com.sumaye.restaurant.service;

import com.sumaye.restaurant.exception.ResourceNotFoundException;
import com.sumaye.restaurant.model.Branch;
import com.sumaye.restaurant.model.Restaurant;
import com.sumaye.restaurant.repository.BranchRepository;
import com.sumaye.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchService {
    private final BranchRepository branchRepository;
    private final RestaurantRepository restaurantRepository;

    public List<Branch> getBranchesByRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        return branchRepository.findByRestaurant(restaurant);
    }

    public Branch getBranch(Long id) {
        return branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));
    }

    public Branch createBranch(Long restaurantId, String name, String location, String phoneNumber, String address) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));

        Branch branch = new Branch();
        branch.setRestaurant(restaurant);
        branch.setName(name);
        branch.setLocation(location);
        branch.setPhoneNumber(phoneNumber);
        branch.setAddress(address);
        branch.setActive(true);
        branch.setCreatedAt(LocalDateTime.now());
        return branchRepository.save(branch);
    }

    public Branch updateBranch(Long id, String name, String location, String phoneNumber, String address, boolean active) {
        Branch branch = getBranch(id);
        branch.setName(name);
        branch.setLocation(location);
        branch.setPhoneNumber(phoneNumber);
        branch.setAddress(address);
        branch.setActive(active);
        branch.setUpdatedAt(LocalDateTime.now());
        return branchRepository.save(branch);
    }

    public void deleteBranch(Long id) {
        Branch branch = getBranch(id);
        branchRepository.delete(branch);
    }

    public Branch toggleBranchStatus(Long id) {
        Branch branch = getBranch(id);
        branch.setActive(!branch.isActive());
        branch.setUpdatedAt(LocalDateTime.now());
        return branchRepository.save(branch);
    }
}
