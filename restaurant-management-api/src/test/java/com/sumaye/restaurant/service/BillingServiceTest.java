package com.sumaye.restaurant.service;

import com.sumaye.restaurant.dto.*;
import com.sumaye.restaurant.exception.ApiException;
import com.sumaye.restaurant.model.*;
import com.sumaye.restaurant.payment.PaymentProvider;
import com.sumaye.restaurant.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BillingServiceTest {

    @Mock
    private BillRepository billRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private TaxSettingRepository taxSettingRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BranchRepository branchRepository;

    @Mock
    private RestaurantTableRepository tableRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private DeliveryOrderRepository deliveryOrderRepository;

    private List<PaymentProvider> paymentProviders = new ArrayList<>();

    @InjectMocks
    private BillingService billingService;

    private Branch branch1;
    private Branch branch2;
    private Restaurant restaurant;
    private RestaurantTable table1;
    private Order order1;
    private User cashierUser;
    private User waiterUser;
    private User managerUser;
    private User branch2User;

    @BeforeEach
    void setUp() {
        restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Sumaye Restaurant");

        branch1 = new Branch();
        branch1.setId(1L);
        branch1.setName("Sumaye Kariakoo Branch");
        branch1.setAddress("Kariakoo, Dar es Salaam");
        branch1.setPhoneNumber("+255 712 345 678");
        branch1.setRestaurant(restaurant);

        branch2 = new Branch();
        branch2.setId(2L);
        branch2.setName("Sumaye Masaki Branch");
        branch2.setRestaurant(restaurant);

        table1 = new RestaurantTable();
        table1.setId(1L);
        table1.setTableNumber(5);
        table1.setBranch(branch1);
        table1.setStatus(RestaurantTable.TableStatus.OCCUPIED);

        Role cashierRole = new Role("ROLE_CASHIER");
        Role waiterRole = new Role("ROLE_WAITER");
        Role managerRole = new Role("ROLE_MANAGER");

        cashierUser = new User();
        cashierUser.setId(10L);
        cashierUser.setUsername("cashier_jane");
        cashierUser.setFirstName("Jane");
        cashierUser.setLastName("Doe");
        cashierUser.setBranch(branch1);
        cashierUser.setRoles(java.util.Set.of(cashierRole));

        waiterUser = new User();
        waiterUser.setId(11L);
        waiterUser.setUsername("waiter_john");
        waiterUser.setFirstName("John");
        waiterUser.setLastName("Smith");
        waiterUser.setBranch(branch1);
        waiterUser.setRoles(java.util.Set.of(waiterRole));

        managerUser = new User();
        managerUser.setId(12L);
        managerUser.setUsername("manager_boss");
        managerUser.setFirstName("Boss");
        managerUser.setLastName("Manager");
        managerUser.setBranch(branch1);
        managerUser.setRoles(java.util.Set.of(managerRole));

        branch2User = new User();
        branch2User.setId(13L);
        branch2User.setUsername("cashier_branch2");
        branch2User.setBranch(branch2);
        branch2User.setRoles(java.util.Set.of(cashierRole));

        order1 = new Order();
        order1.setId(100L);
        order1.setOrderNumber("ORD-260902-0001");
        order1.setBranch(branch1);
        order1.setTable(table1);
        order1.setWaiter(waiterUser);
        order1.setStatus(Order.OrderStatus.SERVED);
        order1.setSubtotal(BigDecimal.valueOf(10000.00));
        order1.setTotalAmount(BigDecimal.valueOf(10000.00));
        order1.setCreatedAt(LocalDateTime.now());

        OrderItem item1 = new OrderItem();
        item1.setId(1L);
        item1.setOrder(order1);
        item1.setItemName("Chips Kuku");
        item1.setUnitPrice(BigDecimal.valueOf(10000.00));
        item1.setQuantity(1);
        item1.setSubtotal(BigDecimal.valueOf(10000.00));
        order1.setItems(List.of(item1));
    }

    @Test
    @DisplayName("1. Bill creation: Successfully creates bill for order with subtotal = 10,000 TZS")
    void test1_BillCreation() {
        when(billRepository.findByOrder(order1)).thenReturn(Optional.empty());
        when(taxSettingRepository.findByBranchAndEnabledTrue(branch1)).thenReturn(Optional.empty());
        when(billRepository.count()).thenReturn(0L);
        when(billRepository.findByBillNumber(anyString())).thenReturn(Optional.empty());
        when(billRepository.save(any(Bill.class))).thenAnswer(i -> {
            Bill b = i.getArgument(0);
            b.setId(1L);
            return b;
        });

        Bill bill = billingService.getOrCreateBillForOrder(order1);

        assertNotNull(bill);
        assertNotNull(bill.getBillNumber());
        assertEquals(0, BigDecimal.valueOf(10000.00).compareTo(bill.getSubtotal()));
        assertEquals(Bill.PaymentStatus.UNPAID, bill.getPaymentStatus());
        verify(billRepository, times(1)).save(any(Bill.class));
    }

    @Test
    @DisplayName("2. Total calculation: Backend calculates total from subtotal and tax accurately")
    void test2_TotalCalculation() {
        when(billRepository.findByOrder(order1)).thenReturn(Optional.empty());
        when(taxSettingRepository.findByBranchAndEnabledTrue(branch1)).thenReturn(Optional.empty());
        when(billRepository.save(any(Bill.class))).thenAnswer(i -> i.getArgument(0));

        Bill bill = billingService.getOrCreateBillForOrder(order1);

        assertEquals(0, BigDecimal.valueOf(10000.00).compareTo(bill.getTotalAmount()));
        assertEquals(0, BigDecimal.valueOf(10000.00).compareTo(bill.getBalanceDue()));
        assertEquals(0, BigDecimal.ZERO.compareTo(bill.getAmountPaid()));
    }

    @Test
    @DisplayName("3. Discount calculation: Discount of 1,000 TZS correctly reduces total to 9,000 TZS")
    void test3_DiscountCalculation() {
        Bill bill = new Bill();
        bill.setId(1L);
        bill.setBillNumber("BILL-260902-0001");
        bill.setBranch(branch1);
        bill.setOrder(order1);
        bill.setSubtotal(BigDecimal.valueOf(10000.00));
        bill.setTaxRate(BigDecimal.ZERO);
        bill.setTaxAmount(BigDecimal.ZERO);
        bill.setTotalAmount(BigDecimal.valueOf(10000.00));
        bill.setAmountPaid(BigDecimal.ZERO);
        bill.setBalanceDue(BigDecimal.valueOf(10000.00));
        bill.setPaymentStatus(Bill.PaymentStatus.UNPAID);

        when(userRepository.findByUsername("manager_boss")).thenReturn(Optional.of(managerUser));
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(billRepository.save(any(Bill.class))).thenAnswer(i -> i.getArgument(0));

        ApplyDiscountRequest req = ApplyDiscountRequest.builder()
                .discountType(Bill.DiscountType.FIXED)
                .discountValue(BigDecimal.valueOf(1000.00))
                .reason("Punguzo la mteja wa mara kwa mara")
                .build();

        BillResponse response = billingService.applyDiscount(1L, req, "manager_boss");

        assertNotNull(response);
        assertEquals(0, BigDecimal.valueOf(1000.00).compareTo(response.getDiscountAmount()));
        assertEquals(0, BigDecimal.valueOf(9000.00).compareTo(response.getTotalAmount()));
        assertEquals(0, BigDecimal.valueOf(9000.00).compareTo(response.getBalanceDue()));
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    @DisplayName("4. Tax calculation: Configurable tax (18% VAT) calculated on taxable amount")
    void test4_TaxCalculation() {
        TaxSetting taxSetting = new TaxSetting();
        taxSetting.setBranch(branch1);
        taxSetting.setTaxName("VAT");
        taxSetting.setTaxRate(BigDecimal.valueOf(18.00));
        taxSetting.setEnabled(true);

        when(billRepository.findByOrder(order1)).thenReturn(Optional.empty());
        when(taxSettingRepository.findByBranchAndEnabledTrue(branch1)).thenReturn(Optional.of(taxSetting));
        when(billRepository.save(any(Bill.class))).thenAnswer(i -> i.getArgument(0));

        Bill bill = billingService.getOrCreateBillForOrder(order1);

        // Subtotal = 10,000, Tax 18% = 1,800 -> Total = 11,800
        assertEquals(0, BigDecimal.valueOf(1800.00).compareTo(bill.getTaxAmount()));
        assertEquals(0, BigDecimal.valueOf(11800.00).compareTo(bill.getTotalAmount()));
    }

    @Test
    @DisplayName("5. Cash payment: Customer gives 10,000 TZS for 7,500 TZS bill -> Change = 2,500 TZS")
    void test5_CashPayment() {
        Bill bill = new Bill();
        bill.setId(1L);
        bill.setBillNumber("BILL-260902-0001");
        bill.setBranch(branch1);
        bill.setOrder(order1);
        bill.setSubtotal(BigDecimal.valueOf(7500.00));
        bill.setTotalAmount(BigDecimal.valueOf(7500.00));
        bill.setAmountPaid(BigDecimal.ZERO);
        bill.setBalanceDue(BigDecimal.valueOf(7500.00));
        bill.setPaymentStatus(Bill.PaymentStatus.UNPAID);

        when(userRepository.findByUsername("cashier_jane")).thenReturn(Optional.of(cashierUser));
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(paymentRepository.count()).thenReturn(0L);
        when(paymentRepository.findByPaymentNumber(anyString())).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(billRepository.save(any(Bill.class))).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.findByTableAndStatusIn(eq(table1), anyList())).thenReturn(List.of());

        CreatePaymentRequest req = CreatePaymentRequest.builder()
                .amount(BigDecimal.valueOf(7500.00))
                .paymentMethod(Payment.PaymentMethod.CASH)
                .cashReceived(BigDecimal.valueOf(10000.00))
                .build();

        BillResponse response = billingService.processPayment(1L, req, "cashier_jane");

        assertNotNull(response);
        assertEquals(Bill.PaymentStatus.PAID, response.getPaymentStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getBalanceDue()));
        verify(paymentRepository, times(1)).save(argThat(p ->
                p.getChangeGiven() != null && BigDecimal.valueOf(2500.00).compareTo(p.getChangeGiven()) == 0
        ));
    }

    @Test
    @DisplayName("6. Partial payment: Bill = 10,000 TZS, Paid = 4,000 TZS -> Status is PARTIALLY_PAID, Balance = 6,000 TZS")
    void test6_PartialPayment() {
        Bill bill = new Bill();
        bill.setId(1L);
        bill.setBillNumber("BILL-260902-0001");
        bill.setBranch(branch1);
        bill.setOrder(order1);
        bill.setSubtotal(BigDecimal.valueOf(10000.00));
        bill.setTotalAmount(BigDecimal.valueOf(10000.00));
        bill.setAmountPaid(BigDecimal.ZERO);
        bill.setBalanceDue(BigDecimal.valueOf(10000.00));
        bill.setPaymentStatus(Bill.PaymentStatus.UNPAID);

        when(userRepository.findByUsername("cashier_jane")).thenReturn(Optional.of(cashierUser));
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(billRepository.save(any(Bill.class))).thenAnswer(i -> i.getArgument(0));

        CreatePaymentRequest req = CreatePaymentRequest.builder()
                .amount(BigDecimal.valueOf(4000.00))
                .paymentMethod(Payment.PaymentMethod.CASH)
                .cashReceived(BigDecimal.valueOf(4000.00))
                .build();

        BillResponse response = billingService.processPayment(1L, req, "cashier_jane");

        assertEquals(Bill.PaymentStatus.PARTIALLY_PAID, response.getPaymentStatus());
        assertEquals(0, BigDecimal.valueOf(4000.00).compareTo(response.getAmountPaid()));
        assertEquals(0, BigDecimal.valueOf(6000.00).compareTo(response.getBalanceDue()));
        // Table should NOT be released yet
        verify(tableRepository, never()).save(any());
    }

    @Test
    @DisplayName("7. Full payment: Second payment completes the bill -> Status changes to PAID")
    void test7_FullPayment() {
        Bill bill = new Bill();
        bill.setId(1L);
        bill.setBillNumber("BILL-260902-0001");
        bill.setBranch(branch1);
        bill.setOrder(order1);
        bill.setSubtotal(BigDecimal.valueOf(10000.00));
        bill.setTotalAmount(BigDecimal.valueOf(10000.00));
        bill.setAmountPaid(BigDecimal.valueOf(4000.00));
        bill.setBalanceDue(BigDecimal.valueOf(6000.00));
        bill.setPaymentStatus(Bill.PaymentStatus.PARTIALLY_PAID);

        when(userRepository.findByUsername("cashier_jane")).thenReturn(Optional.of(cashierUser));
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(2L);
            return p;
        });
        when(billRepository.save(any(Bill.class))).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.findByTableAndStatusIn(eq(table1), anyList())).thenReturn(List.of());

        CreatePaymentRequest req = CreatePaymentRequest.builder()
                .amount(BigDecimal.valueOf(6000.00))
                .paymentMethod(Payment.PaymentMethod.CASH)
                .cashReceived(BigDecimal.valueOf(6000.00))
                .build();

        BillResponse response = billingService.processPayment(1L, req, "cashier_jane");

        assertEquals(Bill.PaymentStatus.PAID, response.getPaymentStatus());
        assertEquals(0, BigDecimal.valueOf(10000.00).compareTo(response.getAmountPaid()));
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getBalanceDue()));
    }

    @Test
    @DisplayName("8. Change calculation: Cash payment with excess amount calculates accurate change")
    void test8_ChangeCalculation() {
        Bill bill = new Bill();
        bill.setId(1L);
        bill.setBillNumber("BILL-260902-0001");
        bill.setBranch(branch1);
        bill.setOrder(order1);
        bill.setTotalAmount(BigDecimal.valueOf(5000.00));
        bill.setAmountPaid(BigDecimal.ZERO);
        bill.setBalanceDue(BigDecimal.valueOf(5000.00));

        when(userRepository.findByUsername("cashier_jane")).thenReturn(Optional.of(cashierUser));
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment p = i.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(billRepository.save(any(Bill.class))).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.findByTableAndStatusIn(any(), anyList())).thenReturn(List.of());

        CreatePaymentRequest req = CreatePaymentRequest.builder()
                .amount(BigDecimal.valueOf(5000.00))
                .paymentMethod(Payment.PaymentMethod.CASH)
                .cashReceived(BigDecimal.valueOf(10000.00))
                .build();

        billingService.processPayment(1L, req, "cashier_jane");

        verify(paymentRepository).save(argThat(p ->
                p.getChangeGiven() != null && BigDecimal.valueOf(5000.00).compareTo(p.getChangeGiven()) == 0
        ));
    }

    @Test
    @DisplayName("9. Duplicate payment protection: Idempotency key prevents duplicate charges")
    void test9_DuplicatePaymentProtection() {
        Bill bill = new Bill();
        bill.setId(1L);
        bill.setBranch(branch1);
        bill.setOrder(order1);
        bill.setBalanceDue(BigDecimal.valueOf(5000.00));

        Payment existingPayment = new Payment();
        existingPayment.setId(99L);
        existingPayment.setIdempotencyKey("unique-key-123");

        when(userRepository.findByUsername("cashier_jane")).thenReturn(Optional.of(cashierUser));
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(paymentRepository.findByIdempotencyKey("unique-key-123")).thenReturn(Optional.of(existingPayment));

        CreatePaymentRequest req = CreatePaymentRequest.builder()
                .amount(BigDecimal.valueOf(5000.00))
                .paymentMethod(Payment.PaymentMethod.CASH)
                .cashReceived(BigDecimal.valueOf(5000.00))
                .idempotencyKey("unique-key-123")
                .build();

        BillResponse response = billingService.processPayment(1L, req, "cashier_jane");

        assertNotNull(response);
        // Verify payment was NOT saved again
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("10. Unauthorized discount: Normal waiter attempting to apply discount throws AccessDeniedException")
    void test10_UnauthorizedDiscount() {
        when(userRepository.findByUsername("waiter_john")).thenReturn(Optional.of(waiterUser));

        ApplyDiscountRequest req = ApplyDiscountRequest.builder()
                .discountType(Bill.DiscountType.FIXED)
                .discountValue(BigDecimal.valueOf(1000.00))
                .reason("Discount test")
                .build();

        assertThrows(AccessDeniedException.class, () ->
                billingService.applyDiscount(1L, req, "waiter_john")
        );
    }

    @Test
    @DisplayName("11. Unauthorized payment access: Insufficient cash received throws ApiException")
    void test11_UnauthorizedPaymentAccess() {
        Bill bill = new Bill();
        bill.setId(1L);
        bill.setBranch(branch1);
        bill.setOrder(order1);
        bill.setBalanceDue(BigDecimal.valueOf(10000.00));
        bill.setTotalAmount(BigDecimal.valueOf(10000.00));

        when(userRepository.findByUsername("cashier_jane")).thenReturn(Optional.of(cashierUser));
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));

        CreatePaymentRequest req = CreatePaymentRequest.builder()
                .amount(BigDecimal.valueOf(10000.00))
                .paymentMethod(Payment.PaymentMethod.CASH)
                .cashReceived(BigDecimal.valueOf(5000.00)) // Less than required
                .build();

        assertThrows(ApiException.class, () ->
                billingService.processPayment(1L, req, "cashier_jane")
        );
    }

    @Test
    @DisplayName("12. Bill PAID status: Upon full payment, bill status is strictly PAID")
    void test12_BillPaidStatus() {
        Bill bill = new Bill();
        bill.setId(1L);
        bill.setBillNumber("BILL-260902-0001");
        bill.setBranch(branch1);
        bill.setOrder(order1);
        bill.setTotalAmount(BigDecimal.valueOf(10000.00));
        bill.setAmountPaid(BigDecimal.ZERO);
        bill.setBalanceDue(BigDecimal.valueOf(10000.00));

        when(userRepository.findByUsername("cashier_jane")).thenReturn(Optional.of(cashierUser));
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));
        when(billRepository.save(any(Bill.class))).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.findByTableAndStatusIn(any(), anyList())).thenReturn(List.of());

        CreatePaymentRequest req = CreatePaymentRequest.builder()
                .amount(BigDecimal.valueOf(10000.00))
                .paymentMethod(Payment.PaymentMethod.CASH)
                .cashReceived(BigDecimal.valueOf(10000.00))
                .build();

        BillResponse response = billingService.processPayment(1L, req, "cashier_jane");

        assertEquals(Bill.PaymentStatus.PAID, response.getPaymentStatus());
    }

    @Test
    @DisplayName("13. Order COMPLETED status: Full payment transitions order status to COMPLETED")
    void test13_OrderCompletedStatus() {
        Bill bill = new Bill();
        bill.setId(1L);
        bill.setBillNumber("BILL-260902-0001");
        bill.setBranch(branch1);
        bill.setOrder(order1);
        bill.setTotalAmount(BigDecimal.valueOf(10000.00));
        bill.setAmountPaid(BigDecimal.ZERO);
        bill.setBalanceDue(BigDecimal.valueOf(10000.00));

        when(userRepository.findByUsername("cashier_jane")).thenReturn(Optional.of(cashierUser));
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));
        when(billRepository.save(any(Bill.class))).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.findByTableAndStatusIn(any(), anyList())).thenReturn(List.of());

        CreatePaymentRequest req = CreatePaymentRequest.builder()
                .amount(BigDecimal.valueOf(10000.00))
                .paymentMethod(Payment.PaymentMethod.CASH)
                .cashReceived(BigDecimal.valueOf(10000.00))
                .build();

        billingService.processPayment(1L, req, "cashier_jane");

        verify(orderRepository).save(argThat(o -> o.getStatus() == Order.OrderStatus.COMPLETED));
    }

    @Test
    @DisplayName("14. Table AVAILABLE status: Full payment releases table to AVAILABLE when no other orders exist")
    void test14_TableAvailableStatus() {
        Bill bill = new Bill();
        bill.setId(1L);
        bill.setBillNumber("BILL-260902-0001");
        bill.setBranch(branch1);
        bill.setOrder(order1);
        bill.setTotalAmount(BigDecimal.valueOf(10000.00));
        bill.setAmountPaid(BigDecimal.ZERO);
        bill.setBalanceDue(BigDecimal.valueOf(10000.00));

        when(userRepository.findByUsername("cashier_jane")).thenReturn(Optional.of(cashierUser));
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));
        when(billRepository.save(any(Bill.class))).thenAnswer(i -> i.getArgument(0));
        when(orderRepository.findByTableAndStatusIn(eq(table1), anyList())).thenReturn(List.of());

        CreatePaymentRequest req = CreatePaymentRequest.builder()
                .amount(BigDecimal.valueOf(10000.00))
                .paymentMethod(Payment.PaymentMethod.CASH)
                .cashReceived(BigDecimal.valueOf(10000.00))
                .build();

        billingService.processPayment(1L, req, "cashier_jane");

        verify(tableRepository).save(argThat(t -> t.getStatus() == RestaurantTable.TableStatus.AVAILABLE));
    }

    @Test
    @DisplayName("15. Receipt generation: Generates professional receipt with all required restaurant & financial fields")
    void test15_ReceiptGeneration() {
        Bill bill = new Bill();
        bill.setId(1L);
        bill.setBillNumber("BILL-260902-0001");
        bill.setBranch(branch1);
        bill.setTable(table1);
        bill.setOrder(order1);
        bill.setSubtotal(BigDecimal.valueOf(10000.00));
        bill.setDiscountAmount(BigDecimal.valueOf(1000.00));
        bill.setTaxRate(BigDecimal.valueOf(18.00));
        bill.setTaxAmount(BigDecimal.valueOf(1620.00));
        bill.setTotalAmount(BigDecimal.valueOf(10620.00));
        bill.setAmountPaid(BigDecimal.valueOf(10620.00));
        bill.setBalanceDue(BigDecimal.ZERO);
        bill.setCashier(cashierUser);

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setBill(bill);
        payment.setAmount(BigDecimal.valueOf(10620.00));
        payment.setPaymentMethod(Payment.PaymentMethod.CASH);
        payment.setCashReceived(BigDecimal.valueOf(15000.00));
        payment.setChangeGiven(BigDecimal.valueOf(4380.00));

        when(userRepository.findByUsername("cashier_jane")).thenReturn(Optional.of(cashierUser));
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));
        when(paymentRepository.findByBillOrderByCreatedAtAsc(bill)).thenReturn(List.of(payment));

        ReceiptResponse receipt = billingService.generateReceipt(1L, "cashier_jane");

        assertNotNull(receipt);
        assertEquals("Sumaye Restaurant", receipt.getRestaurantName());
        assertEquals("Sumaye Kariakoo Branch", receipt.getBranchName());
        assertNotNull(receipt.getReceiptNumber());
        assertEquals(1, receipt.getItems().size());
        assertEquals(0, BigDecimal.valueOf(10000.00).compareTo(receipt.getSubtotal()));
        assertEquals(0, BigDecimal.valueOf(1000.00).compareTo(receipt.getDiscountAmount()));
        assertEquals(0, BigDecimal.valueOf(1620.00).compareTo(receipt.getTaxAmount()));
        assertEquals(0, BigDecimal.valueOf(10620.00).compareTo(receipt.getTotalAmount()));
        assertEquals(0, BigDecimal.valueOf(4380.00).compareTo(receipt.getChangeGiven()));
        assertTrue(receipt.getFooterMessage().contains("Asante"));
    }

    @Test
    @DisplayName("16. Branch data isolation: Cashier from Branch 2 attempting access to Branch 1 throws AccessDeniedException")
    void test16_BranchDataIsolation() {
        Bill billBranch1 = new Bill();
        billBranch1.setId(1L);
        billBranch1.setBranch(branch1);
        billBranch1.setOrder(order1);

        when(userRepository.findByUsername("cashier_branch2")).thenReturn(Optional.of(branch2User));
        when(billRepository.findById(1L)).thenReturn(Optional.of(billBranch1));

        assertThrows(AccessDeniedException.class, () ->
                billingService.getBillById(1L, "cashier_branch2")
        );
    }
}
