package com.sumaye.restaurant.service;

import com.sumaye.restaurant.dto.*;
import com.sumaye.restaurant.exception.ApiException;
import com.sumaye.restaurant.exception.ResourceNotFoundException;
import com.sumaye.restaurant.model.*;
import com.sumaye.restaurant.payment.PaymentProvider;
import com.sumaye.restaurant.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final TaxSettingRepository taxSettingRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final RestaurantTableRepository tableRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final List<PaymentProvider> paymentProviders;
    private final AuditLogRepository auditLogRepository;
    private final DeliveryOrderRepository deliveryOrderRepository;

    @Transactional
    public Bill getOrCreateBillForOrder(Order order) {
        Optional<Bill> existing = billRepository.findByOrder(order);
        if (existing.isPresent()) {
            return existing.get();
        }

        Branch branch = order.getBranch();
        BigDecimal foodSubtotal = order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal deliveryFee = deliveryOrderRepository.findByOrderId(order.getId())
                .map(DeliveryOrder::getDeliveryFee).orElse(BigDecimal.ZERO);
        BigDecimal subtotal = foodSubtotal.add(deliveryFee);

        BigDecimal taxRate = BigDecimal.ZERO;
        BigDecimal taxAmount = BigDecimal.ZERO;

        Optional<TaxSetting> activeTax = taxSettingRepository.findByBranchAndEnabledTrue(branch);
        if (activeTax.isPresent()) {
            taxRate = activeTax.get().getTaxRate();
            taxAmount = subtotal.multiply(taxRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        BigDecimal totalAmount = subtotal.add(taxAmount);

        Bill bill = new Bill();
        bill.setBillNumber(generateBillNumber(branch.getId()));
        bill.setOrder(order);
        bill.setBranch(branch);
        bill.setTable(order.getTable());
        bill.setSubtotal(subtotal);
        bill.setDeliveryFee(deliveryFee);
        bill.setDiscountType(Bill.DiscountType.NONE);
        bill.setDiscountAmount(BigDecimal.ZERO);
        bill.setTaxRate(taxRate);
        bill.setTaxAmount(taxAmount);
        bill.setTotalAmount(totalAmount);
        bill.setAmountPaid(BigDecimal.ZERO);
        bill.setBalanceDue(totalAmount);
        bill.setPaymentStatus(Bill.PaymentStatus.UNPAID);
        bill.setCreatedAt(LocalDateTime.now());

        return billRepository.save(bill);
    }

    @Transactional(readOnly = true)
    public List<BillResponse> getActiveBills(String username) {
        User user = getUser(username);
        Branch branch = getBranchForUser(user);

        List<Bill.PaymentStatus> activeStatuses = List.of(
                Bill.PaymentStatus.UNPAID,
                Bill.PaymentStatus.PARTIALLY_PAID,
                Bill.PaymentStatus.PAID
        );

        List<Bill> bills = billRepository.findByBranchAndPaymentStatusInOrderByCreatedAtDesc(branch, activeStatuses);
        return bills.stream().map(this::mapToBillResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BillResponse getBillById(Long id, String username) {
        User user = getUser(username);
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ankara haikupatikana: ID " + id));
        validateBranchAccess(user, bill.getBranch());
        return mapToBillResponse(bill);
    }

    @Transactional(readOnly = true)
    public BillResponse getBillByOrderId(Long orderId, String username) {
        User user = getUser(username);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Oda haikupatikana: ID " + orderId));
        validateBranchAccess(user, order.getBranch());
        Bill bill = getOrCreateBillForOrder(order);
        return mapToBillResponse(bill);
    }

    @Transactional
    public BillResponse requestBill(Long orderId, String username) {
        User user = getUser(username);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Oda haikupatikana: ID " + orderId));
        validateBranchAccess(user, order.getBranch());

        Bill bill = getOrCreateBillForOrder(order);
        bill.setBillRequested(true);
        bill.setBillRequestedAt(LocalDateTime.now());
        bill.setUpdatedAt(LocalDateTime.now());
        Bill savedBill = billRepository.save(bill);

        // Broadcast real-time event to cashier
        String tableName = (order.getTable() != null && order.getTable().getTableNumber() != null)
                ? "Meza " + order.getTable().getTableNumber()
                : "Oda #" + order.getOrderNumber();

        RealTimeEvent event = RealTimeEvent.builder()
                .eventType("BILL_REQUESTED")
                .branchId(order.getBranch().getId())
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .waiterUsername(order.getWaiter() != null ? order.getWaiter().getUsername() : null)
                .status("BILL_REQUESTED")
                .message(tableName + " imeomba Ankara.")
                .payload(mapToBillResponse(savedBill))
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/branches/" + order.getBranch().getId() + "/cashier", event);
        log.info("Broadcasted BILL_REQUESTED for order #{} to branch {}", order.getOrderNumber(), order.getBranch().getId());

        return mapToBillResponse(savedBill);
    }

    @Transactional
    public BillResponse applyDiscount(Long billId, ApplyDiscountRequest request, String username) {
        User user = getUser(username);
        verifyDiscountAuthorization(user);

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Ankara haikupatikana: ID " + billId));
        validateBranchAccess(user, bill.getBranch());

        if (bill.getPaymentStatus() == Bill.PaymentStatus.PAID) {
            throw new ApiException("Huwezi kubadilisha punguzo kwenye ankara iliyokwishalipwa.");
        }

        BigDecimal subtotal = bill.getSubtotal();
        BigDecimal discountAmount = BigDecimal.ZERO;

        if (request.getDiscountType() == Bill.DiscountType.PERCENTAGE) {
            if (request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new ApiException("Asilimia ya punguzo haiwezi kuzidi 100%.");
            }
            discountAmount = subtotal.multiply(request.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else if (request.getDiscountType() == Bill.DiscountType.FIXED) {
            discountAmount = request.getDiscountValue();
        }

        if (discountAmount.compareTo(subtotal) > 0) {
            throw new ApiException("Punguzo haliwezi kuzidi jumla ndogo ya ankara.");
        }

        BigDecimal taxableAmount = subtotal.subtract(discountAmount);
        BigDecimal taxAmount = BigDecimal.ZERO;
        if (bill.getTaxRate().compareTo(BigDecimal.ZERO) > 0) {
            taxAmount = taxableAmount.multiply(bill.getTaxRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }

        BigDecimal totalAmount = taxableAmount.add(taxAmount);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        BigDecimal balanceDue = totalAmount.subtract(bill.getAmountPaid());
        if (balanceDue.compareTo(BigDecimal.ZERO) < 0) {
            balanceDue = BigDecimal.ZERO;
        }

        bill.setDiscountType(request.getDiscountType());
        bill.setDiscountAmount(discountAmount);
        bill.setDiscountReason(request.getReason());
        bill.setDiscountAppliedBy(user);
        bill.setTaxAmount(taxAmount);
        bill.setTotalAmount(totalAmount);
        bill.setBalanceDue(balanceDue);
        bill.setUpdatedAt(LocalDateTime.now());

        Bill saved = billRepository.save(bill);

        // Audit Log for discount
        auditLogRepository.save(AuditLog.builder()
                .action("DISCOUNT_APPLIED")
                .entityType("Bill")
                .entityId(saved.getId())
                .performedBy(user)
                .branch(saved.getBranch())
                .details(String.format("Punguzo la TZS %s (%s) limewekwa kwenye Ankara #%s na %s. Sababu: %s",
                        discountAmount, request.getDiscountType(), saved.getBillNumber(), user.getUsername(), request.getReason()))
                .createdAt(LocalDateTime.now())
                .build());

        return mapToBillResponse(saved);
    }

    @Transactional
    public BillResponse processPayment(Long billId, CreatePaymentRequest request, String username) {
        User cashier = getUser(username);

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Ankara haikupatikana: ID " + billId));
        validateBranchAccess(cashier, bill.getBranch());

        if (bill.getPaymentStatus() == Bill.PaymentStatus.PAID) {
            throw new ApiException("Ankara hii tayari imeshalipwa kikamilifu.");
        }

        // Idempotency check
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            Optional<Payment> existing = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existing.isPresent()) {
                log.info("Duplicate payment request detected for idempotency key: {}", request.getIdempotencyKey());
                return mapToBillResponse(bill);
            }
        }

        BigDecimal paymentAmount = request.getAmount();
        if (paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException("Kiasi cha malipo lazima kiwe zaidi ya 0.");
        }

        if (paymentAmount.compareTo(bill.getBalanceDue()) > 0 && request.getPaymentMethod() != Payment.PaymentMethod.CASH) {
            throw new ApiException("Kiasi cha malipo (" + paymentAmount + ") kinazidi salio linalodaiwa (" + bill.getBalanceDue() + ").");
        }

        BigDecimal cashReceived = request.getCashReceived();
        BigDecimal changeGiven = BigDecimal.ZERO;

        if (request.getPaymentMethod() == Payment.PaymentMethod.CASH) {
            if (cashReceived == null || cashReceived.compareTo(paymentAmount) < 0) {
                throw new ApiException("Fedha haitoshi. Kiasi kinachotakiwa: " + paymentAmount + ", Kiasi kilichopokelewa: " + (cashReceived != null ? cashReceived : 0));
            }
            changeGiven = cashReceived.subtract(paymentAmount);
        } else if (request.getPaymentMethod() == Payment.PaymentMethod.MOBILE_MONEY) {
            if (request.getTransactionReference() == null || request.getTransactionReference().isBlank()) {
                throw new ApiException("Namba ya kumbukumbu ya muamala (Transaction Reference) inahitajika.");
            }
            Payment.PaymentProvider provider = request.getProvider() != null ? request.getProvider() : Payment.PaymentProvider.MPESA;
            for (PaymentProvider p : paymentProviders) {
                if (p.supports(provider)) {
                    if (!p.validateTransactionReference(request.getTransactionReference())) {
                        throw new ApiException("Namba ya muamala ya " + p.getProviderName() + " sio sahihi.");
                    }
                }
            }
        }

        // Create and save Payment
        Payment payment = new Payment();
        payment.setPaymentNumber(generatePaymentNumber(bill.getBranch().getId()));
        payment.setBill(bill);
        payment.setOrder(bill.getOrder());
        payment.setBranch(bill.getBranch());
        payment.setAmount(paymentAmount);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setProvider(request.getProvider() != null ? request.getProvider() : Payment.PaymentProvider.NONE);
        payment.setTransactionReference(request.getTransactionReference());
        payment.setCashReceived(cashReceived);
        payment.setChangeGiven(changeGiven);
        payment.setStatus(Payment.PaymentTransactionStatus.SUCCESS);
        payment.setCashier(cashier);
        payment.setIdempotencyKey(request.getIdempotencyKey());
        payment.setNotes(request.getNotes());
        payment.setCreatedAt(LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(payment);

        // Record Audit Log for payment
        auditLogRepository.save(AuditLog.builder()
                .action("PAYMENT_RECORDED")
                .entityType("Payment")
                .entityId(savedPayment.getId())
                .performedBy(cashier)
                .branch(bill.getBranch())
                .details(String.format("Malipo #%s ya TZS %s (Njia: %s) kwa Ankara #%s.",
                        savedPayment.getPaymentNumber(), paymentAmount, savedPayment.getPaymentMethod(), bill.getBillNumber()))
                .createdAt(LocalDateTime.now())
                .build());

        // Update Bill
        BigDecimal newAmountPaid = bill.getAmountPaid().add(paymentAmount);
        BigDecimal newBalanceDue = bill.getTotalAmount().subtract(newAmountPaid);
        if (newBalanceDue.compareTo(BigDecimal.ZERO) <= 0) {
            newBalanceDue = BigDecimal.ZERO;
            bill.setPaymentStatus(Bill.PaymentStatus.PAID);
        } else {
            bill.setPaymentStatus(Bill.PaymentStatus.PARTIALLY_PAID);
        }

        bill.setAmountPaid(newAmountPaid);
        bill.setBalanceDue(newBalanceDue);
        bill.setCashier(cashier);
        bill.setUpdatedAt(LocalDateTime.now());

        Bill savedBill = billRepository.save(bill);

        // Complete Order and Release Table if fully paid
        if (bill.getPaymentStatus() == Bill.PaymentStatus.PAID) {
            Order order = bill.getOrder();
            order.setStatus(Order.OrderStatus.COMPLETED);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            RestaurantTable table = order.getTable();
            if (table != null) {
                List<Order> activeTableOrders = orderRepository.findByTableAndStatusIn(
                        table,
                        List.of(Order.OrderStatus.NEW, Order.OrderStatus.SENT_TO_KITCHEN, Order.OrderStatus.ACCEPTED, Order.OrderStatus.PREPARING, Order.OrderStatus.READY, Order.OrderStatus.SERVED)
                ).stream().filter(o -> !o.getId().equals(order.getId())).collect(Collectors.toList());

                if (activeTableOrders.isEmpty()) {
                    table.setStatus(RestaurantTable.TableStatus.AVAILABLE);
                    table.setUpdatedAt(LocalDateTime.now());
                    tableRepository.save(table);
                    log.info("Table #{} released to AVAILABLE after full payment of order #{}", table.getTableNumber(), order.getOrderNumber());
                }
            }

            // Record Audit Log for completion
            auditLogRepository.save(AuditLog.builder()
                    .action("ORDER_COMPLETED_PAID")
                    .entityType("Order")
                    .entityId(order.getId())
                    .performedBy(cashier)
                    .branch(bill.getBranch())
                    .details(String.format("Ankara #%s imelipwa yote. Oda #%s imekamilika (COMPLETED) na Meza #%s imeachwa WAZI (AVAILABLE).",
                            bill.getBillNumber(), order.getOrderNumber(), table != null ? table.getTableNumber() : "N/A"))
                    .createdAt(LocalDateTime.now())
                    .build());

            // Real-time notification to waiter and cashier
            String completionMsg = "Malipo ya Oda #" + order.getOrderNumber() + " yamekamilika.";
            RealTimeEvent completedEvent = RealTimeEvent.builder()
                    .eventType("PAYMENT_COMPLETED")
                    .branchId(order.getBranch().getId())
                    .orderId(order.getId())
                    .orderNumber(order.getOrderNumber())
                    .waiterUsername(order.getWaiter() != null ? order.getWaiter().getUsername() : null)
                    .status("COMPLETED")
                    .message(completionMsg)
                    .payload(mapToBillResponse(savedBill))
                    .timestamp(LocalDateTime.now())
                    .build();

            if (order.getWaiter() != null) {
                messagingTemplate.convertAndSend("/topic/waiters/" + order.getWaiter().getUsername(), completedEvent);
            }
            messagingTemplate.convertAndSend("/topic/branches/" + order.getBranch().getId() + "/orders", completedEvent);
            messagingTemplate.convertAndSend("/topic/branches/" + order.getBranch().getId() + "/cashier", completedEvent);
        }

        return mapToBillResponse(savedBill);
    }

    @Transactional(readOnly = true)
    public ReceiptResponse generateReceipt(Long billId, String username) {
        User user = getUser(username);
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new ResourceNotFoundException("Ankara haikupatikana: ID " + billId));
        validateBranchAccess(user, bill.getBranch());

        Branch branch = bill.getBranch();
        Restaurant restaurant = branch.getRestaurant();
        Order order = bill.getOrder();

        List<ReceiptItemResponse> receiptItems = order.getItems().stream().map(i -> ReceiptItemResponse.builder()
                .itemName(i.getItemName())
                .quantity(i.getQuantity())
                .unitPrice(i.getUnitPrice())
                .subtotal(i.getSubtotal())
                .build()).collect(Collectors.toList());

        List<Payment> payments = paymentRepository.findByBillOrderByCreatedAtAsc(bill);
        List<PaymentResponse> paymentResponses = payments.stream().map(this::mapToPaymentResponse).collect(Collectors.toList());

        BigDecimal totalCashReceived = payments.stream()
                .filter(p -> p.getCashReceived() != null)
                .map(Payment::getCashReceived)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalChangeGiven = payments.stream()
                .filter(p -> p.getChangeGiven() != null)
                .map(Payment::getChangeGiven)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String paymentSummary = payments.stream()
                .map(p -> p.getPaymentMethod().name() + " (TZS " + p.getAmount() + ")")
                .collect(Collectors.joining(", "));

        String receiptNum = "REC-" + bill.getBillNumber().replace("BILL-", "");

        return ReceiptResponse.builder()
                .restaurantName(restaurant != null ? restaurant.getName() : "SUMAYE RESTAURANT")
                .branchName(branch.getName())
                .address(branch.getAddress() != null ? branch.getAddress() : "Dar es Salaam, Tanzania")
                .phone(branch.getPhoneNumber() != null ? branch.getPhoneNumber() : "+255 700 000 000")
                .tinNumber("123-456-789")
                .vrnNumber("40-001234-Z")
                .receiptNumber(receiptNum)
                .billNumber(bill.getBillNumber())
                .orderNumber(order.getOrderNumber())
                .tableNumber(bill.getTable() != null ? bill.getTable().getTableNumber() : null)
                .orderType(order.getOrderType().name())
                .waiterName(order.getWaiter() != null ? (order.getWaiter().getFirstName() + " " + order.getWaiter().getLastName()).trim() : null)
                .cashierName(bill.getCashier() != null ? (bill.getCashier().getFirstName() + " " + bill.getCashier().getLastName()).trim() : "Mweka Hazina")
                .items(receiptItems)
                .subtotal(bill.getSubtotal())
                .deliveryFee(bill.getDeliveryFee())
                .discountAmount(bill.getDiscountAmount())
                .taxRate(bill.getTaxRate())
                .taxAmount(bill.getTaxAmount())
                .totalAmount(bill.getTotalAmount())
                .amountPaid(bill.getAmountPaid())
                .changeGiven(totalChangeGiven)
                .balanceDue(bill.getBalanceDue())
                .paymentMethodsSummary(paymentSummary)
                .payments(paymentResponses)
                .receiptDate(LocalDateTime.now())
                .footerMessage("Asante kwa kutuhudumia. Karibu Tena!")
                .build();
    }

    public void validateBranchAccess(User user, Branch branch) {
        if (branch == null || user == null) return;
        boolean isMasterAdmin = user.getRoles().stream().anyMatch(r ->
                r.getName().equalsIgnoreCase("ROLE_OWNER") || r.getName().equalsIgnoreCase("ROLE_ADMIN"));
        if (isMasterAdmin) return;
        if (user.getBranch() != null && !user.getBranch().getId().equals(branch.getId())) {
            throw new AccessDeniedException("Huruhusiwi kupata au kubadilisha taarifa za kifedha za tawi lingine.");
        }
    }

    @Transactional(readOnly = true)
    public TaxSetting getTaxSettingForBranch(Long branchId, String username) {
        User user = getUser(username);
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Tawi halikupatikana"));
        validateBranchAccess(user, branch);
        return taxSettingRepository.findByBranchAndEnabledTrue(branch)
                .orElseGet(() -> {
                    TaxSetting defaultSetting = new TaxSetting();
                    defaultSetting.setBranch(branch);
                    defaultSetting.setTaxName("VAT");
                    defaultSetting.setTaxRate(BigDecimal.valueOf(18.00));
                    defaultSetting.setEnabled(false);
                    return defaultSetting;
                });
    }

    @Transactional
    public TaxSetting updateTaxSetting(Long branchId, String taxName, BigDecimal taxRate, Boolean enabled, String username) {
        User user = getUser(username);
        boolean canManage = user.getRoles().stream().anyMatch(r ->
                r.getName().equalsIgnoreCase("ROLE_OWNER") ||
                r.getName().equalsIgnoreCase("ROLE_ADMIN") ||
                r.getName().equalsIgnoreCase("ROLE_MANAGER"));
        if (!canManage) {
            throw new AccessDeniedException("Huna ruhusa ya kurekebisha mipangilio ya kodi.");
        }
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Tawi halikupatikana"));
        validateBranchAccess(user, branch);

        TaxSetting setting = taxSettingRepository.findByBranchAndEnabledTrue(branch)
                .orElseGet(() -> {
                    TaxSetting ts = new TaxSetting();
                    ts.setBranch(branch);
                    return ts;
                });

        if (taxName != null && !taxName.isBlank()) setting.setTaxName(taxName);
        if (taxRate != null) setting.setTaxRate(taxRate);
        if (enabled != null) setting.setEnabled(enabled);
        setting.setUpdatedAt(LocalDateTime.now());
        TaxSetting saved = taxSettingRepository.save(setting);

        auditLogRepository.save(AuditLog.builder()
                .action("TAX_SETTING_UPDATED")
                .entityType("TaxSetting")
                .entityId(saved.getId())
                .performedBy(user)
                .branch(branch)
                .details(String.format("Mipangilio ya kodi imesasishwa: %s kwa kiwango cha %s%% (Imewezeshwa: %s)",
                        saved.getTaxName(), saved.getTaxRate(), saved.isEnabled()))
                .createdAt(LocalDateTime.now())
                .build());

        return saved;
    }

    private void verifyDiscountAuthorization(User user) {
        boolean authorized = user.getRoles().stream().anyMatch(r ->
                r.getName().equalsIgnoreCase("ROLE_OWNER") ||
                        r.getName().equalsIgnoreCase("ROLE_ADMIN") ||
                        r.getName().equalsIgnoreCase("ROLE_MANAGER") ||
                        r.getName().equalsIgnoreCase("ROLE_CASHIER")
        );
        if (!authorized) {
            throw new AccessDeniedException("Huna ruhusa ya kutoa punguzo.");
        }
    }

    private synchronized String generateBillNumber(Long branchId) {
        long count = billRepository.count() + 1;
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String candidate = String.format("BILL-%s-%04d", datePrefix, count % 10000);
        while (billRepository.findByBillNumber(candidate).isPresent()) {
            count++;
            candidate = String.format("BILL-%s-%04d", datePrefix, count % 10000);
        }
        return candidate;
    }

    private synchronized String generatePaymentNumber(Long branchId) {
        long count = paymentRepository.count() + 1;
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        String candidate = String.format("PAY-%s-%04d", datePrefix, count % 10000);
        while (paymentRepository.findByPaymentNumber(candidate).isPresent()) {
            count++;
            candidate = String.format("PAY-%s-%04d", datePrefix, count % 10000);
        }
        return candidate;
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

    public BillResponse mapToBillResponse(Bill bill) {
        Order order = bill.getOrder();
        List<OrderItemResponse> orderItems = order != null ? order.getItems().stream().map(item -> OrderItemResponse.builder()
                .id(item.getId())
                .menuItemId(item.getMenuItem() != null ? item.getMenuItem().getId() : null)
                .itemName(item.getItemName())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .specialInstructions(item.getSpecialInstructions())
                .createdAt(item.getCreatedAt())
                .build()).collect(Collectors.toList()) : List.of();

        List<Payment> payments = paymentRepository.findByBillOrderByCreatedAtAsc(bill);
        List<PaymentResponse> paymentResponses = payments.stream().map(this::mapToPaymentResponse).collect(Collectors.toList());

        return BillResponse.builder()
                .id(bill.getId())
                .billNumber(bill.getBillNumber())
                .orderId(order != null ? order.getId() : null)
                .orderNumber(order != null ? order.getOrderNumber() : null)
                .branchId(bill.getBranch() != null ? bill.getBranch().getId() : null)
                .branchName(bill.getBranch() != null ? bill.getBranch().getName() : null)
                .tableId(bill.getTable() != null ? bill.getTable().getId() : null)
                .tableNumber(bill.getTable() != null ? bill.getTable().getTableNumber() : null)
                .waiterName(order != null && order.getWaiter() != null ? (order.getWaiter().getFirstName() + " " + order.getWaiter().getLastName()).trim() : null)
                .orderType(order != null ? order.getOrderType() : null)
                .orderItems(orderItems)
                .subtotal(bill.getSubtotal())
                .discountType(bill.getDiscountType())
                .discountAmount(bill.getDiscountAmount())
                .discountReason(bill.getDiscountReason())
                .discountAppliedByName(bill.getDiscountAppliedBy() != null ? bill.getDiscountAppliedBy().getUsername() : null)
                .taxRate(bill.getTaxRate())
                .taxAmount(bill.getTaxAmount())
                .totalAmount(bill.getTotalAmount())
                .amountPaid(bill.getAmountPaid())
                .balanceDue(bill.getBalanceDue())
                .paymentStatus(bill.getPaymentStatus())
                .cashierName(bill.getCashier() != null ? bill.getCashier().getUsername() : null)
                .billRequested(bill.isBillRequested())
                .billRequestedAt(bill.getBillRequestedAt())
                .payments(paymentResponses)
                .notes(bill.getNotes())
                .createdAt(bill.getCreatedAt())
                .updatedAt(bill.getUpdatedAt())
                .build();
    }

    public PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .paymentNumber(payment.getPaymentNumber())
                .billId(payment.getBill() != null ? payment.getBill().getId() : null)
                .billNumber(payment.getBill() != null ? payment.getBill().getBillNumber() : null)
                .orderId(payment.getOrder() != null ? payment.getOrder().getId() : null)
                .orderNumber(payment.getOrder() != null ? payment.getOrder().getOrderNumber() : null)
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .provider(payment.getProvider())
                .transactionReference(payment.getTransactionReference())
                .cashReceived(payment.getCashReceived())
                .changeGiven(payment.getChangeGiven())
                .status(payment.getStatus())
                .cashierName(payment.getCashier() != null ? payment.getCashier().getUsername() : null)
                .notes(payment.getNotes())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
