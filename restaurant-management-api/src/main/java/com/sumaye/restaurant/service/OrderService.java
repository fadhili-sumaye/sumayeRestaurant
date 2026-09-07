package com.sumaye.restaurant.service;

import com.sumaye.restaurant.dto.*;
import com.sumaye.restaurant.exception.ApiException;
import com.sumaye.restaurant.exception.ResourceNotFoundException;
import com.sumaye.restaurant.model.*;
import com.sumaye.restaurant.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final KitchenOrderRepository kitchenOrderRepository;
    private final KitchenOrderItemRepository kitchenOrderItemRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final RestaurantTableRepository tableRepository;
    private final MenuItemRepository menuItemRepository;
    @Lazy
    private final KitchenService kitchenService;
    @Lazy
    private final InventoryService inventoryService;
    @Lazy
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request, String username) {
        User waiter = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Mtumiaji hakupatikana"));

        if (!waiter.isActive()) {
            throw new ApiException("Akaunti yako haijawashwa");
        }

        Branch branch = waiter.getBranch();
        if (branch == null) {
            // Fallback to first branch if not set on user
            branch = branchRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new ApiException("Tawi halikupatikana"));
        }

        // Idempotency check: prevent duplicate submissions
        if (request.getClientRequestId() != null && !request.getClientRequestId().isBlank()) {
            Optional<Order> existingOrder = orderRepository.findByClientRequestId(request.getClientRequestId());
            if (existingOrder.isPresent()) {
                log.info("Idempotent request received: returning existing order {}", existingOrder.get().getOrderNumber());
                return mapToResponse(existingOrder.get());
            }
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new ApiException("Oda yako haina bidhaa.");
        }

        RestaurantTable table = null;
        if (request.getOrderType() == Order.OrderType.DINE_IN) {
            if (request.getTableId() == null) {
                throw new ApiException("Tafadhali chagua meza kwa oda ya kula hapa.");
            }
            table = tableRepository.findById(request.getTableId())
                    .orElseThrow(() -> new ResourceNotFoundException("Meza haikupatikana."));

            if (!table.getBranch().getId().equals(branch.getId())) {
                throw new ApiException("Meza hii haipo kwenye tawi lako.");
            }

            if (table.getStatus() != RestaurantTable.TableStatus.AVAILABLE) {
                throw new ApiException("Meza hii tayari ina mteja au haipatikani.");
            }
        }

        // Build Order
        Order order = new Order();
        order.setBranch(branch);
        order.setTable(table);
        order.setWaiter(waiter);
        order.setOrderType(request.getOrderType() != null ? request.getOrderType() : Order.OrderType.DINE_IN);
        order.setStatus(Order.OrderStatus.SENT_TO_KITCHEN);
        order.setNotes(request.getNotes());
        order.setClientRequestId(request.getClientRequestId());
        order.setCreatedAt(LocalDateTime.now());
        order.setOrderNumber(generateOrderNumber(branch.getId()));

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (OrderItemRequest itemReq : request.getItems()) {
            if (itemReq.getMenuItemId() == null) {
                throw new ApiException("Kitambulisho cha chakula kinahitajika.");
            }
            if (itemReq.getQuantity() == null || itemReq.getQuantity() <= 0) {
                throw new ApiException("Idadi ya chakula lazima iwe angalau 1.");
            }

            MenuItem menuItem = menuItemRepository.findById(itemReq.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Chakula hakikupatikana: ID " + itemReq.getMenuItemId()));

            if (!menuItem.isAvailable()) {
                throw new ApiException("Chakula '" + menuItem.getName() + "' hakipatikani kwa sasa.");
            }

            BigDecimal itemPrice = menuItem.getPrice(); // ALWAYS use CURRENT DB price
            BigDecimal itemSubtotal = itemPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItem(menuItem);
            orderItem.setItemName(menuItem.getName());
            orderItem.setUnitPrice(itemPrice);
            orderItem.setQuantity(itemReq.getQuantity());
            orderItem.setSubtotal(itemSubtotal);
            orderItem.setSpecialInstructions(itemReq.getSpecialInstructions());
            orderItem.setCreatedAt(LocalDateTime.now());

            orderItems.add(orderItem);
        }

        order.setSubtotal(subtotal);
        order.setTax(BigDecimal.ZERO);
        order.setDiscount(BigDecimal.ZERO);
        order.setTotalAmount(subtotal);
        order.setItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        // Update table status to OCCUPIED
        if (table != null) {
            table.setStatus(RestaurantTable.TableStatus.OCCUPIED);
            table.setUpdatedAt(LocalDateTime.now());
            tableRepository.save(table);
        }

        // Create Kitchen Order for Phase 5 real-time tracking
        KitchenOrder kitchenOrder = new KitchenOrder();
        kitchenOrder.setOrder(savedOrder);
        kitchenOrder.setBranch(branch);
        kitchenOrder.setStatus(KitchenOrder.KitchenOrderStatus.NEW);
        kitchenOrder.setNotes(request.getNotes());
        kitchenOrder.setCreatedAt(LocalDateTime.now());

        List<KitchenOrderItem> kitchenItems = new ArrayList<>();
        for (OrderItem oi : savedOrder.getItems()) {
            KitchenOrderItem koi = new KitchenOrderItem();
            koi.setKitchenOrder(kitchenOrder);
            koi.setOrderItem(oi);
            koi.setItemName(oi.getItemName());
            koi.setQuantity(oi.getQuantity());
            koi.setSpecialInstructions(oi.getSpecialInstructions());
            koi.setStatus(KitchenOrderItem.KitchenOrderItemStatus.NEW);
            koi.setCreatedAt(LocalDateTime.now());
            kitchenItems.add(koi);
        }
        kitchenOrder.setItems(kitchenItems);

        KitchenOrder savedKot = kitchenOrderRepository.save(kitchenOrder);

        // Broadcast to Kitchen in real time
        kitchenService.broadcastNewOrder(savedKot);

        // Notify the management dashboard in real time so the Owner sees a new order
        messagingTemplate.convertAndSend("/topic/branches/" + branch.getId() + "/management",
                RealTimeEvent.builder()
                        .eventType("ORDER_CREATED")
                        .branchId(branch.getId())
                        .orderId(savedOrder.getId())
                        .orderNumber(savedOrder.getOrderNumber())
                        .waiterUsername(username)
                        .status(savedOrder.getStatus().name())
                        .message("Oda mpya #" + savedOrder.getOrderNumber() + " imeingizwa")
                        .timestamp(LocalDateTime.now())
                        .build());

        // Phase 7: reserve stock atomically with the order/KOT.  A stock failure
        // rolls back the complete request so no kitchen ticket is created without stock.
        inventoryService.deductStockForOrder(savedOrder);

        return mapToResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String username) {
        User waiter = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Mtumiaji hakupatikana"));

        List<Order> orders = orderRepository.findByWaiterOrderByCreatedAtDesc(waiter);
        return orders.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Creates a customer QR order through the same persistence, KOT broadcast,
     * and inventory deduction workflow used by staff orders.
     */
    @Transactional
    public OrderResponse createQrOrder(RestaurantTable table, QrOrderRequest request) {
        Optional<Order> existing = orderRepository.findByClientRequestId(request.getClientRequestId());
        if (existing.isPresent()) {
            Order prior = existing.get();
            if (prior.getOrderSource() == Order.OrderSource.QR
                    && prior.getTable() != null && prior.getTable().getId().equals(table.getId())) {
                return mapToResponse(prior);
            }
            throw new ApiException("Ombi hili tayari limetumika. Tafadhali anzisha oda upya.");
        }

        Order order = new Order();
        order.setBranch(table.getBranch());
        order.setTable(table);
        order.setOrderType(Order.OrderType.DINE_IN);
        order.setOrderSource(Order.OrderSource.QR);
        order.setCustomerSessionId(UUID.randomUUID().toString());
        order.setStatus(Order.OrderStatus.SENT_TO_KITCHEN);
        order.setNotes(request.getNotes() == null || request.getNotes().isBlank()
                ? "Oda ya QR" : "[QR] " + request.getNotes().trim());
        order.setClientRequestId(request.getClientRequestId());
        order.setCreatedAt(LocalDateTime.now());
        order.setOrderNumber(generateOrderNumber(table.getBranch().getId()));

        BigDecimal subtotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest itemRequest : request.getItems()) {
            if (itemRequest.getMenuItemId() == null || itemRequest.getQuantity() == null || itemRequest.getQuantity() <= 0) {
                throw new ApiException("Chagua chakula na idadi sahihi.");
            }
            MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Chakula hakikupatikana"));
            if (!menuItem.isAvailable() || !menuItem.getBranch().getId().equals(table.getBranch().getId())) {
                throw new ApiException("Chakula ulichochagua hakipatikani kwa meza hii.");
            }
            BigDecimal itemSubtotal = menuItem.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            subtotal = subtotal.add(itemSubtotal);
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItem(menuItem);
            orderItem.setItemName(menuItem.getName());
            orderItem.setUnitPrice(menuItem.getPrice());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setSubtotal(itemSubtotal);
            orderItem.setSpecialInstructions(itemRequest.getSpecialInstructions());
            orderItem.setCreatedAt(LocalDateTime.now());
            orderItems.add(orderItem);
        }
        order.setSubtotal(subtotal);
        order.setTax(BigDecimal.ZERO);
        order.setDiscount(BigDecimal.ZERO);
        order.setTotalAmount(subtotal);
        order.setItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        table.setStatus(RestaurantTable.TableStatus.OCCUPIED);
        table.setUpdatedAt(LocalDateTime.now());
        tableRepository.save(table);

        KitchenOrder kitchenOrder = new KitchenOrder();
        kitchenOrder.setOrder(savedOrder);
        kitchenOrder.setBranch(table.getBranch());
        kitchenOrder.setStatus(KitchenOrder.KitchenOrderStatus.NEW);
        kitchenOrder.setNotes("[QR] " + (request.getNotes() == null ? "" : request.getNotes().trim()));
        kitchenOrder.setCreatedAt(LocalDateTime.now());
        List<KitchenOrderItem> kitchenItems = new ArrayList<>();
        for (OrderItem orderItem : savedOrder.getItems()) {
            KitchenOrderItem kitchenItem = new KitchenOrderItem();
            kitchenItem.setKitchenOrder(kitchenOrder);
            kitchenItem.setOrderItem(orderItem);
            kitchenItem.setItemName(orderItem.getItemName());
            kitchenItem.setQuantity(orderItem.getQuantity());
            kitchenItem.setSpecialInstructions(orderItem.getSpecialInstructions());
            kitchenItem.setStatus(KitchenOrderItem.KitchenOrderItemStatus.NEW);
            kitchenItem.setCreatedAt(LocalDateTime.now());
            kitchenItems.add(kitchenItem);
        }
        kitchenOrder.setItems(kitchenItems);
        KitchenOrder savedKot = kitchenOrderRepository.save(kitchenOrder);
        kitchenService.broadcastNewOrder(savedKot);
        messagingTemplate.convertAndSend("/topic/branches/" + table.getBranch().getId() + "/management",
                RealTimeEvent.builder()
                        .eventType("ORDER_CREATED")
                        .branchId(table.getBranch().getId())
                        .orderId(savedOrder.getId())
                        .orderNumber(savedOrder.getOrderNumber())
                        .status(savedOrder.getStatus().name())
                        .message("Oda mpya ya QR #" + savedOrder.getOrderNumber() + " imeingizwa")
                        .timestamp(LocalDateTime.now())
                        .build());
        inventoryService.deductStockForOrder(savedOrder);
        return mapToResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getBranchOrders(Long branchId, String username) {
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Tawi halikupatikana"));
        List<Order> orders = orderRepository.findByBranchOrderByCreatedAtDesc(branch);
        return orders.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id, String username) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Oda haikupatikana: ID " + id));
        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long id, CancelOrderRequest request, String username) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Oda haikupatikana: ID " + id));

        if (order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new ApiException("Oda hii tayari ilikwishaghairiwa.");
        }

        if (order.getStatus() == Order.OrderStatus.COMPLETED) {
            throw new ApiException("Huwezi kughairi oda iliyokamilika.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Mtumiaji hakupatikana"));

        order.setStatus(Order.OrderStatus.CANCELLED);
        order.setCancellationReason(request != null ? request.getReason() : "Imeghairiwa na mhudumu");
        order.setCancelledBy(user);
        order.setCancelledAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        // Reversal is part of the cancellation transaction and always leaves an audit trail.
        inventoryService.reverseStockForOrder(order);

        // Release table if occupied
        RestaurantTable table = order.getTable();
        if (table != null) {
            List<Order> activeOrdersOnTable = orderRepository.findByTableAndStatusIn(
                    table,
                    List.of(Order.OrderStatus.NEW, Order.OrderStatus.SENT_TO_KITCHEN, Order.OrderStatus.ACCEPTED, Order.OrderStatus.PREPARING, Order.OrderStatus.READY, Order.OrderStatus.SERVED)
            ).stream().filter(o -> !o.getId().equals(order.getId())).collect(Collectors.toList());

            if (activeOrdersOnTable.isEmpty()) {
                table.setStatus(RestaurantTable.TableStatus.AVAILABLE);
                table.setUpdatedAt(LocalDateTime.now());
                tableRepository.save(table);
            }
        }

        // Cancel Kitchen Order if exists
        kitchenOrderRepository.findByOrder(order).ifPresent(ko -> {
            ko.setStatus(KitchenOrder.KitchenOrderStatus.CANCELLED);
            ko.setCancelledAt(LocalDateTime.now());
            ko.setUpdatedAt(LocalDateTime.now());
            kitchenOrderRepository.save(ko);
        });

        Order updatedOrder = orderRepository.save(order);
        return mapToResponse(updatedOrder);
    }

    private synchronized String generateOrderNumber(Long branchId) {
        long count = orderRepository.count() + 1;
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String candidate = String.format("ORD-%s-%04d", datePrefix, count % 10000);
        
        while (orderRepository.findByOrderNumber(candidate).isPresent()) {
            count++;
            candidate = String.format("ORD-%s-%04d", datePrefix, count % 10000);
        }
        return candidate;
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream().map(item -> OrderItemResponse.builder()
                .id(item.getId())
                .menuItemId(item.getMenuItem() != null ? item.getMenuItem().getId() : null)
                .itemName(item.getItemName())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .specialInstructions(item.getSpecialInstructions())
                .createdAt(item.getCreatedAt())
                .build()).collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .branchId(order.getBranch() != null ? order.getBranch().getId() : null)
                .branchName(order.getBranch() != null ? order.getBranch().getName() : null)
                .tableId(order.getTable() != null ? order.getTable().getId() : null)
                .tableNumber(order.getTable() != null ? order.getTable().getTableNumber() : null)
                .waiterId(order.getWaiter() != null ? order.getWaiter().getId() : null)
                .waiterName(order.getWaiter() != null ? (order.getWaiter().getFirstName() + " " + order.getWaiter().getLastName()).trim() : null)
                .orderType(order.getOrderType())
                .orderSource(order.getOrderSource())
                .status(order.getStatus())
                .subtotal(order.getSubtotal())
                .tax(order.getTax())
                .discount(order.getDiscount())
                .totalAmount(order.getTotalAmount())
                .notes(order.getNotes())
                .clientRequestId(order.getClientRequestId())
                .cancellationReason(order.getCancellationReason())
                .cancelledByName(order.getCancelledBy() != null ? order.getCancelledBy().getUsername() : null)
                .cancelledAt(order.getCancelledAt())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
