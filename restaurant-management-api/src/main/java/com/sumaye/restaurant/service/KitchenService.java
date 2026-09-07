package com.sumaye.restaurant.service;

import com.sumaye.restaurant.dto.KitchenOrderItemResponse;
import com.sumaye.restaurant.dto.KitchenOrderResponse;
import com.sumaye.restaurant.dto.RealTimeEvent;
import com.sumaye.restaurant.exception.ApiException;
import com.sumaye.restaurant.exception.ResourceNotFoundException;
import com.sumaye.restaurant.model.*;
import com.sumaye.restaurant.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class KitchenService {

    private final KitchenOrderRepository kitchenOrderRepository;
    private final KitchenOrderItemRepository kitchenOrderItemRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final RestaurantTableRepository tableRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional(readOnly = true)
    public List<KitchenOrderResponse> getActiveKitchenOrders(String username) {
        User user = getUser(username);
        Branch branch = getBranchForUser(user);

        List<KitchenOrder.KitchenOrderStatus> activeStatuses = List.of(
                KitchenOrder.KitchenOrderStatus.NEW,
                KitchenOrder.KitchenOrderStatus.ACCEPTED,
                KitchenOrder.KitchenOrderStatus.PREPARING,
                KitchenOrder.KitchenOrderStatus.READY
        );

        List<KitchenOrder> activeOrders = kitchenOrderRepository.findByBranchAndStatusInOrderByCreatedAtAsc(branch, activeStatuses);
        return activeOrders.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public KitchenOrderResponse getKitchenOrderById(Long id, String username) {
        KitchenOrder kitchenOrder = kitchenOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KOT haikupatikana: ID " + id));
        return mapToResponse(kitchenOrder);
    }

    @Transactional
    public KitchenOrderResponse acceptKitchenOrder(Long id, String username) {
        KitchenOrder kitchenOrder = kitchenOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KOT haikupatikana: ID " + id));

        if (kitchenOrder.getStatus() == KitchenOrder.KitchenOrderStatus.CANCELLED) {
            throw new ApiException("Huwezi kupokea oda iliyoghairiwa.");
        }

        User user = getUser(username);
        kitchenOrder.setStatus(KitchenOrder.KitchenOrderStatus.ACCEPTED);
        kitchenOrder.setAcceptedAt(LocalDateTime.now());
        kitchenOrder.setKitchenUser(user);
        kitchenOrder.setUpdatedAt(LocalDateTime.now());

        Order order = kitchenOrder.getOrder();
        order.setStatus(Order.OrderStatus.ACCEPTED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        KitchenOrder saved = kitchenOrderRepository.save(kitchenOrder);
        KitchenOrderResponse response = mapToResponse(saved);

        broadcastEvent("KOT_STATUS_UPDATED", saved, "Oda #" + order.getOrderNumber() + " imepokelewa jikoni.", response);
        return response;
    }

    @Transactional
    public KitchenOrderResponse startPreparingKitchenOrder(Long id, String username) {
        KitchenOrder kitchenOrder = kitchenOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KOT haikupatikana: ID " + id));

        if (kitchenOrder.getStatus() == KitchenOrder.KitchenOrderStatus.CANCELLED) {
            throw new ApiException("Huwezi kuandaa oda iliyoghairiwa.");
        }

        User user = getUser(username);
        kitchenOrder.setStatus(KitchenOrder.KitchenOrderStatus.PREPARING);
        kitchenOrder.setPreparingAt(LocalDateTime.now());
        kitchenOrder.setKitchenUser(user);
        kitchenOrder.setUpdatedAt(LocalDateTime.now());

        // Update items status
        if (kitchenOrder.getItems() != null) {
            for (KitchenOrderItem item : kitchenOrder.getItems()) {
                if (item.getStatus() == KitchenOrderItem.KitchenOrderItemStatus.NEW) {
                    item.setStatus(KitchenOrderItem.KitchenOrderItemStatus.PREPARING);
                    item.setUpdatedAt(LocalDateTime.now());
                }
            }
        }

        Order order = kitchenOrder.getOrder();
        order.setStatus(Order.OrderStatus.PREPARING);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        KitchenOrder saved = kitchenOrderRepository.save(kitchenOrder);
        KitchenOrderResponse response = mapToResponse(saved);

        broadcastEvent("KOT_STATUS_UPDATED", saved, "Oda #" + order.getOrderNumber() + " inaandaliwa sasa.", response);
        return response;
    }

    @Transactional
    public KitchenOrderResponse markKitchenOrderReady(Long id, String username) {
        KitchenOrder kitchenOrder = kitchenOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KOT haikupatikana: ID " + id));

        if (kitchenOrder.getStatus() == KitchenOrder.KitchenOrderStatus.CANCELLED) {
            throw new ApiException("Huwezi kukamilisha oda iliyoghairiwa.");
        }

        kitchenOrder.setStatus(KitchenOrder.KitchenOrderStatus.READY);
        kitchenOrder.setReadyAt(LocalDateTime.now());
        kitchenOrder.setUpdatedAt(LocalDateTime.now());

        // Update items status to READY
        if (kitchenOrder.getItems() != null) {
            for (KitchenOrderItem item : kitchenOrder.getItems()) {
                item.setStatus(KitchenOrderItem.KitchenOrderItemStatus.READY);
                item.setUpdatedAt(LocalDateTime.now());
            }
        }

        Order order = kitchenOrder.getOrder();
        order.setStatus(Order.OrderStatus.READY);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        KitchenOrder saved = kitchenOrderRepository.save(kitchenOrder);
        KitchenOrderResponse response = mapToResponse(saved);

        String readyMessage = "Oda #" + order.getOrderNumber() + " iko tayari!";
        broadcastEvent("ORDER_READY", saved, readyMessage, response);

        return response;
    }

    @Transactional
    public KitchenOrderResponse cancelKitchenOrder(Long id, String reason, String username) {
        KitchenOrder kitchenOrder = kitchenOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("KOT haikupatikana: ID " + id));

        User user = getUser(username);
        kitchenOrder.setStatus(KitchenOrder.KitchenOrderStatus.CANCELLED);
        kitchenOrder.setCancelledAt(LocalDateTime.now());
        kitchenOrder.setNotes(reason);
        kitchenOrder.setUpdatedAt(LocalDateTime.now());

        if (kitchenOrder.getItems() != null) {
            for (KitchenOrderItem item : kitchenOrder.getItems()) {
                item.setStatus(KitchenOrderItem.KitchenOrderItemStatus.CANCELLED);
                item.setUpdatedAt(LocalDateTime.now());
            }
        }

        Order order = kitchenOrder.getOrder();
        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setCancellationReason(reason != null ? reason : "Imeghairiwa na jiko");
        order.setCancelledBy(user);
        order.setCancelledAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        // Release table if occupied
        RestaurantTable table = order.getTable();
        if (table != null) {
            List<Order> activeOrders = orderRepository.findByTableAndStatusIn(
                    table,
                    List.of(Order.OrderStatus.NEW, Order.OrderStatus.SENT_TO_KITCHEN, Order.OrderStatus.ACCEPTED, Order.OrderStatus.PREPARING, Order.OrderStatus.READY)
            ).stream().filter(o -> !o.getId().equals(order.getId())).collect(Collectors.toList());

            if (activeOrders.isEmpty()) {
                table.setStatus(RestaurantTable.TableStatus.AVAILABLE);
                table.setUpdatedAt(LocalDateTime.now());
                tableRepository.save(table);
            }
        }

        KitchenOrder saved = kitchenOrderRepository.save(kitchenOrder);
        KitchenOrderResponse response = mapToResponse(saved);

        broadcastEvent("ORDER_CANCELLED", saved, "Oda #" + order.getOrderNumber() + " imeghairiwa: " + reason, response);
        return response;
    }

    public void broadcastNewOrder(KitchenOrder kitchenOrder) {
        KitchenOrderResponse response = mapToResponse(kitchenOrder);
        broadcastEvent("NEW_KOT", kitchenOrder, "Oda mpya #" + kitchenOrder.getOrder().getOrderNumber() + " imewasili!", response);
    }

    private void broadcastEvent(String eventType, KitchenOrder kot, String message, Object payload) {
        try {
            Long branchId = kot.getBranch() != null ? kot.getBranch().getId() : 1L;
            String waiterUsername = (kot.getOrder() != null && kot.getOrder().getWaiter() != null)
                    ? kot.getOrder().getWaiter().getUsername()
                    : null;

            RealTimeEvent event = RealTimeEvent.builder()
                    .eventType(eventType)
                    .branchId(branchId)
                    .orderId(kot.getOrder() != null ? kot.getOrder().getId() : null)
                    .orderNumber(kot.getOrder() != null ? kot.getOrder().getOrderNumber() : null)
                    .waiterUsername(waiterUsername)
                    .status(kot.getStatus().name())
                    .message(message)
                    .payload(payload)
                    .timestamp(LocalDateTime.now())
                    .build();

            // Broadcast to Kitchen Topic for this Branch
            messagingTemplate.convertAndSend("/topic/branches/" + branchId + "/kitchen", event);

            // Broadcast to specific Waiter Topic
            if (waiterUsername != null) {
                messagingTemplate.convertAndSend("/topic/waiters/" + waiterUsername, event);
            }

            // Broadcast to general branch orders topic
            messagingTemplate.convertAndSend("/topic/branches/" + branchId + "/orders", event);

            log.info("Broadcasted {} event for order #{} to branch {}", eventType, event.getOrderNumber(), branchId);
        } catch (Exception e) {
            log.error("Failed to broadcast real-time event: {}", e.getMessage(), e);
        }
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Mtumiaji hakupatikana"));
    }

    private Branch getBranchForUser(User user) {
        if (user.getBranch() != null) {
            return user.getBranch();
        }
        return branchRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new ApiException("Tawi halikupatikana"));
    }

    public KitchenOrderResponse mapToResponse(KitchenOrder kot) {
        List<KitchenOrderItemResponse> itemResponses = kot.getItems() != null
                ? kot.getItems().stream().map(i -> KitchenOrderItemResponse.builder()
                .id(i.getId())
                .orderItemId(i.getOrderItem() != null ? i.getOrderItem().getId() : null)
                .itemName(i.getItemName())
                .quantity(i.getQuantity())
                .specialInstructions(i.getSpecialInstructions())
                .status(i.getStatus())
                .build()).collect(Collectors.toList())
                : List.of();

        Order order = kot.getOrder();

        return KitchenOrderResponse.builder()
                .id(kot.getId())
                .orderId(order != null ? order.getId() : null)
                .orderNumber(order != null ? order.getOrderNumber() : null)
                .branchId(kot.getBranch() != null ? kot.getBranch().getId() : null)
                .branchName(kot.getBranch() != null ? kot.getBranch().getName() : null)
                .tableId(order != null && order.getTable() != null ? order.getTable().getId() : null)
                .tableNumber(order != null && order.getTable() != null ? order.getTable().getTableNumber() : null)
                .waiterName(order != null && order.getWaiter() != null ? (order.getWaiter().getFirstName() + " " + order.getWaiter().getLastName()).trim() : null)
                .orderType(order != null ? order.getOrderType() : null)
                .status(kot.getStatus())
                .notes(kot.getNotes())
                .items(itemResponses)
                .createdAt(kot.getCreatedAt())
                .acceptedAt(kot.getAcceptedAt())
                .preparingAt(kot.getPreparingAt())
                .readyAt(kot.getReadyAt())
                .cancelledAt(kot.getCancelledAt())
                .kitchenUserName(kot.getKitchenUser() != null ? kot.getKitchenUser().getUsername() : null)
                .build();
    }
}
