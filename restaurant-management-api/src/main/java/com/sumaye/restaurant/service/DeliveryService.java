package com.sumaye.restaurant.service;

import com.sumaye.restaurant.dto.AssignDeliveryRequest;
import com.sumaye.restaurant.dto.DeliveryFeeSettingRequest;
import com.sumaye.restaurant.dto.DeliveryRequest;
import com.sumaye.restaurant.exception.ApiException;
import com.sumaye.restaurant.exception.ResourceNotFoundException;
import com.sumaye.restaurant.model.*;
import com.sumaye.restaurant.repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeliveryService {
    private final DeliveryOrderRepository deliveries;
    private final DeliveryAssignmentRepository assignments;
    private final DeliveryFeeSettingRepository feeSettings;
    private final OrderRepository orders;
    private final CustomerRepository customers;
    private final BranchRepository branches;
    private final UserRepository users;
    private final SimpMessagingTemplate messaging;

    @Transactional(readOnly = true)
    public List<DeliveryOrder> list(Long branchId, DeliveryOrder.Status status) {
        requireBranch(branchId);
        return status == null ? deliveries.findByBranchIdOrderByCreatedAtDesc(branchId)
                : deliveries.findByBranchIdAndStatus(branchId, status);
    }

    @Transactional(readOnly = true)
    public List<DeliveryOrder> listMyDeliveries(Long branchId, String username) {
        requireBranch(branchId);
        User user = users.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Mtumiaji hakupatikana"));
        return deliveries.findByBranchIdAndRiderIdOrderByCreatedAtDesc(branchId, user.getId());
    }

    @Transactional(readOnly = true)
    public DeliveryOrder getDeliveryById(Long branchId, Long id, String username) {
        DeliveryOrder delivery = findInBranch(branchId, id);
        User user = users.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Mtumiaji hakupatikana"));

        boolean isManagement = user.getRoles().stream()
                .anyMatch(r -> "ROLE_OWNER".equals(r.getName()) || "ROLE_ADMIN".equals(r.getName()) || "ROLE_MANAGER".equals(r.getName()));

        if (!isManagement) {
            // If the user is delivery staff, enforce strict ownership to prevent IDOR/BOLA
            if (delivery.getRider() == null || !delivery.getRider().getId().equals(user.getId())) {
                throw new ApiException("Huruhusiwi kuona taarifa za oda hii ya delivery");
            }
        }
        return delivery;
    }

    @Transactional(readOnly = true)
    public List<DeliveryAssignment> assignmentHistory(Long branchId, Long deliveryId) {
        findInBranch(branchId, deliveryId);
        return assignments.findByDeliveryOrderIdOrderByAssignedAtDesc(deliveryId);
    }

    @Transactional
    public DeliveryOrder create(Long branchId, DeliveryRequest request, String username) {
        Branch branch = requireBranch(branchId);
        Order order = orders.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Oda haikupatikana"));
        if (order.getOrderType() != Order.OrderType.DELIVERY || !order.getBranch().getId().equals(branchId)) {
            throw new ApiException("Oda hii si oda halali ya delivery ya tawi hili");
        }
        if (deliveries.findByOrderId(order.getId()).isPresent()) {
            throw new ApiException("Oda hii tayari ina delivery");
        }
        Customer customer = customers.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Mteja hakupatikana"));
        if (!customer.isActive() || !customer.getRestaurant().getId().equals(branch.getRestaurant().getId())) {
            throw new ApiException("Mteja si halali kwa mgahawa huu");
        }

        DeliveryOrder delivery = new DeliveryOrder();
        delivery.setBranch(branch);
        delivery.setOrder(order);
        delivery.setCustomer(customer);
        delivery.setDeliveryAddress(request.getDeliveryAddress().trim());
        delivery.setLocationNotes(request.getLocationNotes());
        delivery.setDeliveryFee(request.getDeliveryFee() != null ? request.getDeliveryFee() : defaultFee(branch));
        DeliveryOrder saved = deliveries.save(delivery);
        if (request.getRiderId() != null) {
            return assign(branchId, saved.getId(), request.getRiderId(), null, username);
        }
        broadcast(saved);
        return saved;
    }

    @Transactional
    public DeliveryOrder assign(Long branchId, Long deliveryId, AssignDeliveryRequest request, String assignerUsername) {
        return assign(branchId, deliveryId, request.getRiderId(), request.getNotes(), assignerUsername);
    }

    @Transactional
    public DeliveryOrder status(Long branchId, Long id, DeliveryOrder.Status next, String username) {
        DeliveryOrder delivery = findInBranch(branchId, id);
        User user = users.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Mtumiaji hakupatikana"));

        boolean isDeliveryStaff = user.getRoles().stream()
                .anyMatch(r -> "ROLE_DELIVERY".equals(r.getName()));
        boolean isKitchen = user.getRoles().stream()
                .anyMatch(r -> "ROLE_KITCHEN".equals(r.getName()));
        boolean isManagement = user.getRoles().stream()
                .anyMatch(r -> "ROLE_OWNER".equals(r.getName()) || "ROLE_ADMIN".equals(r.getName()) || "ROLE_MANAGER".equals(r.getName()));

        // Role-based status transition restrictions
        if (isDeliveryStaff && !isManagement) {
            if (delivery.getRider() == null || !delivery.getRider().getId().equals(user.getId())) {
                throw new ApiException("Huruhusiwi kubadilisha hali ya oda ya delivery usiyopangiwa");
            }
            if (next != DeliveryOrder.Status.OUT_FOR_DELIVERY && next != DeliveryOrder.Status.DELIVERED) {
                throw new ApiException("Mtoa huduma wa delivery anaruhusiwa kubadili kuelekea OUT_FOR_DELIVERY au DELIVERED tu");
            }
            if (next == DeliveryOrder.Status.OUT_FOR_DELIVERY && delivery.getStatus() != DeliveryOrder.Status.ASSIGNED && delivery.getStatus() != DeliveryOrder.Status.READY) {
                throw new ApiException("Oda lazima iwe tayari na ipangiwe kabla ya kuanza safari");
            }
            if (next == DeliveryOrder.Status.DELIVERED && delivery.getStatus() != DeliveryOrder.Status.OUT_FOR_DELIVERY) {
                throw new ApiException("Oda lazima iwe safarini (OUT_FOR_DELIVERY) kabla ya kuwekwa imewasilishwa");
            }
        }

        if (isKitchen && !isManagement) {
            if (next != DeliveryOrder.Status.PREPARING && next != DeliveryOrder.Status.READY) {
                throw new ApiException("Jiko linaruhusiwa kuweka PREPARING au READY tu");
            }
        }

        if (!canTransition(delivery.getStatus(), next)) {
            throw new ApiException("Hatua ya delivery kutoka " + delivery.getStatus() + " kwenda " + next + " si sahihi");
        }

        if (next == DeliveryOrder.Status.OUT_FOR_DELIVERY && delivery.getRider() == null) {
            throw new ApiException("Mtoa huduma lazima apewe oda kabla ya kuondoka");
        }

        LocalDateTime now = LocalDateTime.now();
        delivery.setStatus(next);
        delivery.setUpdatedAt(now);

        if (next == DeliveryOrder.Status.OUT_FOR_DELIVERY) {
            delivery.setPickedUpAt(now);
            updateLatestAssignment(delivery.getId(), now, false, DeliveryOrder.Status.OUT_FOR_DELIVERY);
        }
        if (next == DeliveryOrder.Status.DELIVERED) {
            delivery.setDeliveredAt(now);
            updateLatestAssignment(delivery.getId(), now, true, DeliveryOrder.Status.DELIVERED);
        }

        DeliveryOrder saved = deliveries.save(delivery);
        broadcast(saved);

        // Send real-time notification to rider
        if (saved.getRider() != null) {
            Map<String, Object> updatePayload = new HashMap<>();
            updatePayload.put("type", "STATUS_UPDATED");
            updatePayload.put("deliveryId", saved.getId());
            updatePayload.put("orderNumber", saved.getOrder() != null ? saved.getOrder().getOrderNumber() : "");
            updatePayload.put("status", saved.getStatus().name());
            messaging.convertAndSend("/topic/riders/" + saved.getRider().getUsername(), updatePayload);
        }

        return saved;
    }

    @Transactional(readOnly = true)
    public DeliveryFeeSetting feeSetting(Long branchId) {
        Branch branch = requireBranch(branchId);
        return feeSettings.findByBranchId(branchId).orElseGet(() -> defaultFeeSetting(branch));
    }

    @Transactional
    public DeliveryFeeSetting updateFeeSetting(Long branchId, DeliveryFeeSettingRequest request) {
        Branch branch = requireBranch(branchId);
        DeliveryFeeSetting setting = feeSettings.findByBranchId(branchId).orElseGet(() -> defaultFeeSetting(branch));
        setting.setFixedFee(request.getFixedFee());
        setting.setEnabled(request.getEnabled());
        setting.setUpdatedAt(LocalDateTime.now());
        return feeSettings.save(setting);
    }

    private DeliveryOrder assign(Long branchId, Long deliveryId, Long riderId, String notes, String assignerUsername) {
        DeliveryOrder delivery = findInBranch(branchId, deliveryId);
        if (delivery.getStatus() == DeliveryOrder.Status.DELIVERED
                || delivery.getStatus() == DeliveryOrder.Status.COMPLETED
                || delivery.getStatus() == DeliveryOrder.Status.CANCELLED) {
            throw new ApiException("Delivery iliyokamilika au kughairiwa haiwezi kupewa mtoa huduma");
        }

        User rider = users.findById(riderId)
                .orElseThrow(() -> new ResourceNotFoundException("Mtoa huduma hakupatikana"));
        if (!rider.isActive() || rider.getBranch() == null || !rider.getBranch().getId().equals(branchId)) {
            throw new ApiException("Mtoa huduma hayupo kwenye tawi hili au akaunti yake haijawashwa");
        }

        boolean hasDeliveryRole = rider.getRoles().stream()
                .anyMatch(r -> "ROLE_DELIVERY".equals(r.getName()));
        if (!hasDeliveryRole) {
            throw new ApiException("Mtumiaji aliyechaguliwa hana wajibu wa DELIVERY");
        }

        User assigner = null;
        if (assignerUsername != null) {
            assigner = users.findByUsername(assignerUsername).orElse(null);
        }

        LocalDateTime now = LocalDateTime.now();
        delivery.setRider(rider);
        delivery.setStatus(DeliveryOrder.Status.ASSIGNED);
        delivery.setAssignedAt(now);
        delivery.setUpdatedAt(now);

        DeliveryAssignment assignment = new DeliveryAssignment();
        assignment.setDeliveryOrder(delivery);
        assignment.setRider(rider);
        assignment.setAssignedBy(assigner);
        assignment.setAssignedAt(now);
        assignment.setNotes(notes);
        assignment.setStatus(DeliveryOrder.Status.ASSIGNED);
        assignments.save(assignment);

        DeliveryOrder saved = deliveries.save(delivery);

        // 1. Broadcast to branch deliveries topic
        broadcast(saved);

        // 2. Real-time notification to the specific delivery rider
        String orderNum = (saved.getOrder() != null && saved.getOrder().getOrderNumber() != null)
                ? saved.getOrder().getOrderNumber()
                : String.valueOf(saved.getId());
        String notificationMessage = "Umepewa oda #" + orderNum + " ya delivery.";

        Map<String, Object> riderNotification = new HashMap<>();
        riderNotification.put("type", "DELIVERY_ASSIGNED");
        riderNotification.put("message", notificationMessage);
        riderNotification.put("deliveryId", saved.getId());
        riderNotification.put("orderId", saved.getOrder() != null ? saved.getOrder().getId() : null);
        riderNotification.put("orderNumber", orderNum);
        riderNotification.put("customerName", saved.getCustomer() != null ? saved.getCustomer().getFullName() : "");
        riderNotification.put("customerPhone", saved.getCustomer() != null ? saved.getCustomer().getPhoneNumber() : "");
        riderNotification.put("address", saved.getDeliveryAddress());
        riderNotification.put("timestamp", now.toString());

        messaging.convertAndSend("/topic/riders/" + rider.getUsername(), riderNotification);
        log.info("Sent real-time delivery assignment notification to rider: {}", rider.getUsername());

        return saved;
    }

    private Branch requireBranch(Long branchId) {
        return branches.findById(branchId).orElseThrow(() -> new ResourceNotFoundException("Tawi halikupatikana"));
    }

    private DeliveryOrder findInBranch(Long branchId, Long id) {
        DeliveryOrder delivery = deliveries.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery haikupatikana"));
        if (!delivery.getBranch().getId().equals(branchId)) {
            throw new ResourceNotFoundException("Delivery haikupatikana");
        }
        return delivery;
    }

    private BigDecimal defaultFee(Branch branch) {
        return feeSettings.findByBranchId(branch.getId()).filter(DeliveryFeeSetting::isEnabled)
                .map(DeliveryFeeSetting::getFixedFee).orElse(BigDecimal.ZERO);
    }

    private DeliveryFeeSetting defaultFeeSetting(Branch branch) {
        DeliveryFeeSetting setting = new DeliveryFeeSetting();
        setting.setBranch(branch);
        return setting;
    }

    private boolean canTransition(DeliveryOrder.Status from, DeliveryOrder.Status to) {
        if (from == to) return true;
        return switch (from) {
            case PENDING -> to == DeliveryOrder.Status.CONFIRMED || to == DeliveryOrder.Status.CANCELLED;
            case CONFIRMED -> to == DeliveryOrder.Status.PREPARING || to == DeliveryOrder.Status.READY || to == DeliveryOrder.Status.ASSIGNED || to == DeliveryOrder.Status.CANCELLED;
            case PREPARING -> to == DeliveryOrder.Status.READY || to == DeliveryOrder.Status.ASSIGNED || to == DeliveryOrder.Status.CANCELLED;
            case READY -> to == DeliveryOrder.Status.ASSIGNED || to == DeliveryOrder.Status.OUT_FOR_DELIVERY || to == DeliveryOrder.Status.CANCELLED;
            case ASSIGNED -> to == DeliveryOrder.Status.OUT_FOR_DELIVERY || to == DeliveryOrder.Status.ASSIGNED || to == DeliveryOrder.Status.CANCELLED;
            case OUT_FOR_DELIVERY -> to == DeliveryOrder.Status.DELIVERED;
            case DELIVERED -> to == DeliveryOrder.Status.COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
    }

    private void updateLatestAssignment(Long deliveryId, LocalDateTime timestamp, boolean delivered, DeliveryOrder.Status status) {
        assignments.findByDeliveryOrderIdOrderByAssignedAtDesc(deliveryId).stream().findFirst().ifPresent(assignment -> {
            if (delivered) {
                assignment.setDeliveredAt(timestamp);
            } else {
                assignment.setPickedUpAt(timestamp);
            }
            assignment.setStatus(status);
            assignments.save(assignment);
        });
    }

    private void broadcast(DeliveryOrder delivery) {
        messaging.convertAndSend("/topic/branches/" + delivery.getBranch().getId() + "/deliveries", delivery);
    }
}
