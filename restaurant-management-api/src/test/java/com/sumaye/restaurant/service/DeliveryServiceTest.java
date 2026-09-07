package com.sumaye.restaurant.service;

import com.sumaye.restaurant.dto.AssignDeliveryRequest;
import com.sumaye.restaurant.exception.ApiException;
import com.sumaye.restaurant.model.*;
import com.sumaye.restaurant.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeliveryServiceTest {

    @Mock
    private DeliveryOrderRepository deliveries;

    @Mock
    private DeliveryAssignmentRepository assignments;

    @Mock
    private DeliveryFeeSettingRepository feeSettings;

    @Mock
    private OrderRepository orders;

    @Mock
    private CustomerRepository customers;

    @Mock
    private BranchRepository branches;

    @Mock
    private UserRepository users;

    @Mock
    private SimpMessagingTemplate messaging;

    @InjectMocks
    private DeliveryService deliveryService;

    private Branch branch;
    private User manager;
    private User rider1;
    private User rider2;
    private Order order;
    private Customer customer;
    private DeliveryOrder delivery;

    @BeforeEach
    void setUp() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(1L);
        restaurant.setName("Sumaye Restaurant");

        branch = new Branch();
        branch.setId(1L);
        branch.setName("Downtown Branch");
        branch.setRestaurant(restaurant);

        Role managerRole = new Role("ROLE_MANAGER");
        Role deliveryRole = new Role("ROLE_DELIVERY");

        manager = new User();
        manager.setId(2L);
        manager.setUsername("manager");
        manager.setBranch(branch);
        manager.setRoles(Set.of(managerRole));
        manager.setActive(true);

        rider1 = new User();
        rider1.setId(10L);
        rider1.setUsername("juma_delivery");
        rider1.setFirstName("Juma");
        rider1.setBranch(branch);
        rider1.setRoles(Set.of(deliveryRole));
        rider1.setActive(true);

        rider2 = new User();
        rider2.setId(11L);
        rider2.setUsername("hamisi_delivery");
        rider2.setFirstName("Hamisi");
        rider2.setBranch(branch);
        rider2.setRoles(Set.of(deliveryRole));
        rider2.setActive(true);

        customer = new Customer();
        customer.setId(5L);
        customer.setFullName("John Doe");
        customer.setPhoneNumber("+255712345678");

        order = new Order();
        order.setId(1025L);
        order.setOrderNumber("ORD-1025");
        order.setBranch(branch);
        order.setTotalAmount(new BigDecimal("35000.00"));

        delivery = new DeliveryOrder();
        delivery.setId(100L);
        delivery.setBranch(branch);
        delivery.setOrder(order);
        delivery.setCustomer(customer);
        delivery.setDeliveryAddress("Masaki, Dar es Salaam");
        delivery.setStatus(DeliveryOrder.Status.READY);
    }

    @Test
    @DisplayName("Manager successfully assigns delivery staff, setting status to ASSIGNED and recording audit")
    void testManagerAssignsDeliveryStaff() {
        when(deliveries.findById(100L)).thenReturn(Optional.of(delivery));
        when(users.findById(10L)).thenReturn(Optional.of(rider1));
        when(users.findByUsername("manager")).thenReturn(Optional.of(manager));
        when(deliveries.save(any(DeliveryOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AssignDeliveryRequest req = new AssignDeliveryRequest();
        req.setRiderId(10L);
        req.setNotes("Peleka haraka kwa mteja");

        DeliveryOrder result = deliveryService.assign(1L, 100L, req, "manager");

        assertNotNull(result);
        assertEquals(DeliveryOrder.Status.ASSIGNED, result.getStatus());
        assertEquals(rider1, result.getRider());
        assertNotNull(result.getAssignedAt());

        // Verify assignment audit saved with assignedBy
        verify(assignments, times(1)).save(argThat(assignment ->
                assignment.getRider().getId().equals(10L) &&
                assignment.getAssignedBy().getId().equals(2L) &&
                "Peleka haraka kwa mteja".equals(assignment.getNotes()) &&
                assignment.getStatus() == DeliveryOrder.Status.ASSIGNED
        ));

        // Verify real-time notification sent to rider's topic
        verify(messaging, times(1)).convertAndSend(eq("/topic/riders/juma_delivery"), any(Object.class));
    }

    @Test
    @DisplayName("Assigned rider transitions ASSIGNED to OUT_FOR_DELIVERY")
    void testRiderStartsDelivery() {
        delivery.setStatus(DeliveryOrder.Status.ASSIGNED);
        delivery.setRider(rider1);

        when(deliveries.findById(100L)).thenReturn(Optional.of(delivery));
        when(users.findByUsername("juma_delivery")).thenReturn(Optional.of(rider1));
        when(deliveries.save(any(DeliveryOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeliveryOrder result = deliveryService.status(1L, 100L, DeliveryOrder.Status.OUT_FOR_DELIVERY, "juma_delivery");

        assertEquals(DeliveryOrder.Status.OUT_FOR_DELIVERY, result.getStatus());
        assertNotNull(result.getPickedUpAt());
    }

    @Test
    @DisplayName("Assigned rider transitions OUT_FOR_DELIVERY to DELIVERED")
    void testRiderCompletesDelivery() {
        delivery.setStatus(DeliveryOrder.Status.OUT_FOR_DELIVERY);
        delivery.setRider(rider1);

        when(deliveries.findById(100L)).thenReturn(Optional.of(delivery));
        when(users.findByUsername("juma_delivery")).thenReturn(Optional.of(rider1));
        when(deliveries.save(any(DeliveryOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DeliveryOrder result = deliveryService.status(1L, 100L, DeliveryOrder.Status.DELIVERED, "juma_delivery");

        assertEquals(DeliveryOrder.Status.DELIVERED, result.getStatus());
        assertNotNull(result.getDeliveredAt());
    }

    @Test
    @DisplayName("Prevent IDOR: Delivery staff cannot update another delivery staff's order")
    void testPreventIdorOtherRiderOrder() {
        delivery.setStatus(DeliveryOrder.Status.ASSIGNED);
        delivery.setRider(rider1); // Assigned to Juma

        when(deliveries.findById(100L)).thenReturn(Optional.of(delivery));
        when(users.findByUsername("hamisi_delivery")).thenReturn(Optional.of(rider2)); // Hamisi tries to update

        ApiException ex = assertThrows(ApiException.class, () ->
                deliveryService.status(1L, 100L, DeliveryOrder.Status.OUT_FOR_DELIVERY, "hamisi_delivery")
        );

        assertTrue(ex.getMessage().contains("usiyopangiwa"));
        verify(deliveries, never()).save(any());
    }

    @Test
    @DisplayName("Prevent unauthorized status transitions: Delivery staff cannot jump directly to DELIVERED from READY")
    void testRiderCannotSkipStatuses() {
        delivery.setStatus(DeliveryOrder.Status.READY);
        delivery.setRider(rider1);

        when(deliveries.findById(100L)).thenReturn(Optional.of(delivery));
        when(users.findByUsername("juma_delivery")).thenReturn(Optional.of(rider1));

        assertThrows(ApiException.class, () ->
                deliveryService.status(1L, 100L, DeliveryOrder.Status.DELIVERED, "juma_delivery")
        );

        verify(deliveries, never()).save(any());
    }
}
