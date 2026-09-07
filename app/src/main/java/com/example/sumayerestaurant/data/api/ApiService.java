package com.example.sumayerestaurant.data.api;

import com.example.sumayerestaurant.data.model.*;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

import java.util.List;
import java.util.Map;

public interface ApiService {
    // Auth
    @POST("/api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("/api/auth/change-password")
    Call<Map<String, String>> changePassword(@Body ChangePasswordRequest request);

    @POST("/api/auth/reset-password")
    Call<Map<String, String>> resetPassword(@Body ResetPasswordRequest request);

    @GET("/api/auth/staff")
    Call<List<User>> getStaff();

    // Tables
    @GET("/api/branches/{branchId}/tables")
    Call<List<RestaurantTable>> getTables(@Path("branchId") Long branchId);

    @GET("/api/tables/{id}")
    Call<RestaurantTable> getTable(@Path("id") Long id);

    // Categories
    @GET("/api/branches/{branchId}/categories")
    Call<List<MenuCategory>> getCategories(@Path("branchId") Long branchId);

    // Menu Items
    @GET("/api/branches/{branchId}/menu-items")
    Call<List<MenuItem>> getMenuItems(@Path("branchId") Long branchId);

    // Orders
    @POST("/api/orders")
    Call<Order> createOrder(@Body CreateOrderRequest request);

    // Public customer QR ordering (no staff authentication required).
    @GET("/api/public/qr/{token}")
    Call<QrTableMenu> getQrMenu(@Path("token") String token);

    @POST("/api/public/qr/{token}/orders")
    Call<Order> createQrOrder(@Path("token") String token, @Body QrOrderRequest request);

    @GET("/api/orders/my-orders")
    Call<List<Order>> getMyOrders();

    @GET("/api/orders/{id}")
    Call<Order> getOrderById(@Path("id") Long id);

    @POST("/api/orders/{id}/cancel")
    Call<Order> cancelOrder(@Path("id") Long id, @Body CancelOrderRequest request);

    // Kitchen Management (Phase 5)
    @GET("/api/kitchen/orders")
    Call<List<KitchenOrder>> getActiveKitchenOrders();

    @GET("/api/kitchen/orders/{id}")
    Call<KitchenOrder> getKitchenOrderById(@Path("id") Long id);

    @PUT("/api/kitchen/orders/{id}/accept")
    Call<KitchenOrder> acceptKitchenOrder(@Path("id") Long id);

    @PUT("/api/kitchen/orders/{id}/preparing")
    Call<KitchenOrder> startPreparingKitchenOrder(@Path("id") Long id);

    @PUT("/api/kitchen/orders/{id}/ready")
    Call<KitchenOrder> markKitchenOrderReady(@Path("id") Long id);

    @PUT("/api/kitchen/orders/{id}/cancel")
    Call<KitchenOrder> cancelKitchenOrder(@Path("id") Long id, @Body CancelOrderRequest request);

    // Billing & Payments (Phase 6)
    @GET("/api/bills/pending")
    Call<List<Bill>> getActiveBills();

    @GET("/api/bills/{id}")
    Call<Bill> getBillById(@Path("id") Long id);

    @GET("/api/bills/orders/{orderId}")
    Call<Bill> getBillByOrderId(@Path("orderId") Long orderId);

    @POST("/api/bills/orders/{orderId}/request")
    Call<Bill> requestBill(@Path("orderId") Long orderId);

    @POST("/api/bills/{id}/discount")
    Call<Bill> applyDiscount(@Path("id") Long id, @Body ApplyDiscountRequest request);

    @POST("/api/bills/{id}/payments")
    Call<Bill> processPayment(@Path("id") Long id, @Body CreatePaymentRequest request);

    @GET("/api/bills/{id}/receipt")
    Call<Receipt> getReceipt(@Path("id") Long id);

    // Inventory (Phase 7)
    @GET("/api/branches/{branchId}/inventory/stock")
    Call<List<InventoryStock>> getInventoryStock(@Path("branchId") Long branchId);

    @GET("/api/ingredients")
    Call<List<Ingredient>> getIngredients();

    @POST("/api/branches/{branchId}/wastage")
    Call<Object> recordWastage(@Path("branchId") Long branchId, @Body Map<String, Object> request);

    @POST("/api/branches/{branchId}/inventory/stock/adjust")
    Call<Object> adjustStock(@Path("branchId") Long branchId, @Body Map<String, Object> request);

    @POST("/api/recipes")
    Call<Object> saveRecipe(@Query("branchId") Long branchId, @Body Map<String, Object> request);

    @POST("/api/branches/{branchId}/purchases")
    Call<Object> createPurchase(@Path("branchId") Long branchId, @Body Map<String, Object> request);

    // Customers, reservations and delivery (Phase 8)
    @GET("/api/branches/{branchId}/customers")
    Call<PageResponse<Customer>> searchCustomers(@Path("branchId") Long branchId, @Query("q") String query, @Query("page") int page, @Query("size") int size);

    @POST("/api/branches/{branchId}/customers")
    Call<Customer> createCustomer(@Path("branchId") Long branchId, @Body Map<String, Object> request);

    @GET("/api/branches/{branchId}/reservations")
    Call<List<Object>> getReservations(@Path("branchId") Long branchId, @Query("date") String date);

    @POST("/api/branches/{branchId}/reservations")
    Call<Object> createReservation(@Path("branchId") Long branchId, @Body Map<String, Object> request);

    @PUT("/api/branches/{branchId}/reservations/{id}/status/{status}")
    Call<Object> updateReservationStatus(@Path("branchId") Long branchId, @Path("id") Long id, @Path("status") String status);

    @GET("/api/branches/{branchId}/deliveries")
    Call<List<DeliveryOrder>> getDeliveries(@Path("branchId") Long branchId);

    @GET("/api/branches/{branchId}/deliveries/my-deliveries")
    Call<List<DeliveryOrder>> getMyDeliveries(@Path("branchId") Long branchId);

    @POST("/api/branches/{branchId}/deliveries")
    Call<Object> createDelivery(@Path("branchId") Long branchId, @Body Map<String, Object> request);

    @PUT("/api/branches/{branchId}/deliveries/{id}/status/{status}")
    Call<Object> updateDeliveryStatus(@Path("branchId") Long branchId, @Path("id") Long id, @Path("status") String status);

    @PUT("/api/branches/{branchId}/deliveries/{id}/assign")
    Call<Object> assignDelivery(@Path("branchId") Long branchId, @Path("id") Long id, @Body Map<String, Object> request);

    @GET("/api/branches/{branchId}/expenses/categories") Call<List<ExpenseCategory>> getExpenseCategories(@Path("branchId") Long branchId);
    @POST("/api/branches/{branchId}/expenses") Call<Object> createExpense(@Path("branchId") Long branchId, @Body Map<String,Object> request);
    @POST("/api/branches/{branchId}/daily-closings") Call<DailyClosing> closeDay(@Path("branchId") Long branchId, @Body Map<String,Object> request);
    @GET("/api/branches/{branchId}/daily-closings/{businessDate}") Call<DailyClosing> getDailyClosing(@Path("branchId") Long branchId, @Path("businessDate") String businessDate);
    @GET("/api/branches/{branchId}/reports/profit-loss") Call<ProfitLoss> profitLoss(@Path("branchId") Long branchId,@Query("from") String from,@Query("to") String to);
}
