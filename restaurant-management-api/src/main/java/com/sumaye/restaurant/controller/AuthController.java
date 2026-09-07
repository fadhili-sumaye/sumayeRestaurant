package com.sumaye.restaurant.controller;

import com.sumaye.restaurant.dto.ChangePasswordRequest;
import com.sumaye.restaurant.dto.LoginRequest;
import com.sumaye.restaurant.dto.LoginResponse;
import com.sumaye.restaurant.dto.ResetPasswordRequest;
import com.sumaye.restaurant.model.Role;
import com.sumaye.restaurant.model.User;
import com.sumaye.restaurant.repository.RoleRepository;
import com.sumaye.restaurant.repository.UserRepository;
import com.sumaye.restaurant.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * POST /api/auth/login
     * Body: { "username": "...", "password": "..." }
     * Returns: JWT token + user info
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtTokenProvider.generateToken(authentication);

            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found after authentication"));

            if (!user.isActive()) {
                Map<String, Object> error = new HashMap<>();
                error.put("message", "Akaunti yako haijawashwa. Wasiliana na msimamizi.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            LoginResponse response = LoginResponse.builder()
                    .accessToken(token)
                    .tokenType("Bearer")
                    .expiresIn(86400000L)
                    .user(LoginResponse.UserInfo.fromUser(user))
                    .build();

            log.info("Login successful for user: {} with roles: {}",
                    user.getUsername(),
                    user.getRoles().stream().map(Role::getName).toList());

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for user: {}", request.getUsername());
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Jina la mtumiaji au nenosiri si sahihi.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    /**
     * GET /api/auth/health
     * Public endpoint — no auth required
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "restaurant-management-api");
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/change-password
     * Any authenticated user can change their own password.
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request, Principal principal) {
        if (principal == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Hujaingia kwenye mfumo.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Mtumiaji hajapatikana"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            Map<String, Object> error = new HashMap<>();
            error.put("message", "Nenosiri la sasa si sahihi.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password successfully changed for user: {}", user.getUsername());
        Map<String, String> res = new HashMap<>();
        res.put("message", "Nenosiri limebadilishwa kwa mafanikio.");
        return ResponseEntity.ok(res);
    }

    /**
     * POST /api/auth/reset-password
     * ADMIN, OWNER, or MANAGER can reset password for any staff member.
     */
    @PostMapping("/reset-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request, Principal principal) {
        User targetUser = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Mtumiaji '" + request.getUsername() + "' hajapatikana."));

        targetUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(targetUser);

        log.info("Password for user {} was reset by {}", targetUser.getUsername(), principal.getName());
        Map<String, String> res = new HashMap<>();
        res.put("message", "Nenosiri la mtumiaji " + targetUser.getUsername() + " limewekwa upya kwa mafanikio.");
        return ResponseEntity.ok(res);
    }

    /**
     * GET /api/auth/staff
     * Lists all staff members for password management.
     */
    @GetMapping("/staff")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'MANAGER')")
    public ResponseEntity<List<LoginResponse.UserInfo>> getStaff() {
        List<LoginResponse.UserInfo> staff = userRepository.findAll().stream()
                .map(LoginResponse.UserInfo::fromUser)
                .toList();
        return ResponseEntity.ok(staff);
    }
}
