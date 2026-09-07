package com.sumaye.restaurant.service;

import com.sumaye.restaurant.dto.CreateStaffRequest;
import com.sumaye.restaurant.dto.StaffResponse;
import com.sumaye.restaurant.exception.ApiException;
import com.sumaye.restaurant.exception.ResourceNotFoundException;
import com.sumaye.restaurant.model.Branch;
import com.sumaye.restaurant.model.Role;
import com.sumaye.restaurant.model.User;
import com.sumaye.restaurant.repository.BranchRepository;
import com.sumaye.restaurant.repository.RoleRepository;
import com.sumaye.restaurant.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<StaffResponse> getStaff(Long branchId, String roleFilter, String requesterUsername) {
        User requester = getRequester(requesterUsername);
        boolean isManager = isManagerOnly(requester);

        Long effectiveBranchId = branchId;
        if (isManager) {
            if (requester.getBranch() == null) {
                throw new ApiException("Meneja hana tawi lililopangwa.");
            }
            effectiveBranchId = requester.getBranch().getId();
        }

        final Long filterBranchId = effectiveBranchId;
        final String normalizedRole = (roleFilter != null && !roleFilter.trim().isEmpty())
                ? (roleFilter.startsWith("ROLE_") ? roleFilter.trim() : "ROLE_" + roleFilter.trim())
                : null;

        return userRepository.findAll().stream()
                .filter(u -> filterBranchId == null || (u.getBranch() != null && u.getBranch().getId().equals(filterBranchId)))
                .filter(u -> normalizedRole == null || u.getRoles().stream().anyMatch(r -> r.getName().equalsIgnoreCase(normalizedRole)))
                .map(StaffResponse::fromUser)
                .collect(Collectors.toList());
    }

    @Transactional
    public StaffResponse createStaff(CreateStaffRequest request, String requesterUsername) {
        User requester = getRequester(requesterUsername);
        boolean isManager = isManagerOnly(requester);

        String username = request.getUsername().trim();
        if (userRepository.existsByUsername(username)) {
            throw new ApiException("Jina la mtumiaji '" + username + "' tayari linatumika.");
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            if (userRepository.existsByEmail(request.getEmail().trim())) {
                throw new ApiException("Barua pepe '" + request.getEmail() + "' tayari inatumika.");
            }
        }

        String rName = request.getRole().trim();
        if (!rName.startsWith("ROLE_")) {
            rName = "ROLE_" + rName.toUpperCase();
        }
        final String roleName = rName;

        if (isManager && ("ROLE_OWNER".equalsIgnoreCase(roleName) || "ROLE_ADMIN".equalsIgnoreCase(roleName))) {
            throw new ApiException("Meneja hawezi kuunda mtumiaji wa ngazi ya OWNER au ADMIN.");
        }

        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));

        Long branchId = request.getBranchId();
        if (isManager) {
            branchId = requester.getBranch().getId();
        }

        Branch branch = null;
        if (branchId != null) {
            branch = branchRepository.findById(branchId)
                    .orElseThrow(() -> new ResourceNotFoundException("Tawi halikupatikana."));
        } else if (requester.getBranch() != null) {
            branch = requester.getBranch();
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setFirstName(request.getFirstName().trim());
        newUser.setLastName(request.getLastName().trim());
        newUser.setEmail(request.getEmail() != null ? request.getEmail().trim() : username + "@sumaye.com");
        newUser.setBranch(branch);
        newUser.setRestaurant(branch != null ? branch.getRestaurant() : requester.getRestaurant());
        newUser.setActive(true);
        newUser.setRoles(new HashSet<>(Set.of(role)));
        newUser.setCreatedAt(LocalDateTime.now());

        User saved = userRepository.save(newUser);
        log.info("New staff created: {} with role {} by {}", saved.getUsername(), role.getName(), requesterUsername);
        return StaffResponse.fromUser(saved);
    }

    @Transactional
    public StaffResponse toggleActive(Long staffId, String requesterUsername) {
        User requester = getRequester(requesterUsername);
        User target = userRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Mtumiaji hakupatikana."));

        if (target.getId().equals(requester.getId())) {
            throw new ApiException("Huwezi kubadilisha hali ya akaunti yako mwenyewe.");
        }

        boolean isManager = isManagerOnly(requester);
        if (isManager) {
            boolean targetIsPrivileged = target.getRoles().stream()
                    .anyMatch(r -> "ROLE_OWNER".equals(r.getName()) || "ROLE_ADMIN".equals(r.getName()));
            if (targetIsPrivileged) {
                throw new ApiException("Meneja hawezi kurekebisha akaunti ya OWNER au ADMIN.");
            }
            if (target.getBranch() == null || !target.getBranch().getId().equals(requester.getBranch().getId())) {
                throw new ApiException("Huruhusiwi kurekebisha wafanyakazi wa tawi lingine.");
            }
        }

        target.setActive(!target.isActive());
        target.setUpdatedAt(LocalDateTime.now());
        User saved = userRepository.save(target);
        log.info("User {} active state toggled to {} by {}", saved.getUsername(), saved.isActive(), requesterUsername);
        return StaffResponse.fromUser(saved);
    }

    private User getRequester(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Mtumiaji anayeomba hakupatikana."));
    }

    private boolean isManagerOnly(User user) {
        boolean isOwnerOrAdmin = user.getRoles().stream()
                .anyMatch(r -> "ROLE_OWNER".equals(r.getName()) || "ROLE_ADMIN".equals(r.getName()));
        return !isOwnerOrAdmin;
    }
}
