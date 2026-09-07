package com.sumaye.restaurant.service;

import com.sumaye.restaurant.dto.QrLinkResponse;
import com.sumaye.restaurant.dto.QrMenuItemResponse;
import com.sumaye.restaurant.dto.QrOrderRequest;
import com.sumaye.restaurant.dto.QrTableMenuResponse;
import com.sumaye.restaurant.dto.OrderResponse;
import com.sumaye.restaurant.exception.ApiException;
import com.sumaye.restaurant.exception.ResourceNotFoundException;
import com.sumaye.restaurant.model.MenuItem;
import com.sumaye.restaurant.model.RestaurantTable;
import com.sumaye.restaurant.repository.MenuItemRepository;
import com.sumaye.restaurant.repository.RestaurantTableRepository;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Public QR access is limited to an opaque, high-entropy table token and the customer menu/order flow. */
@Service
@RequiredArgsConstructor
public class QrOrderingService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private final RestaurantTableRepository tables;
    private final MenuItemRepository menuItems;
    private final BranchAccessService branchAccess;
    private final OrderService orderService;

    @Transactional(readOnly = true)
    public QrTableMenuResponse getMenu(String token) {
        RestaurantTable table = getTableByToken(token);
        List<QrMenuItemResponse> items = menuItems.findByBranchAndAvailable(table.getBranch(), true).stream()
                .map(this::menuItem)
                .toList();
        return QrTableMenuResponse.builder()
                .restaurantName(table.getBranch().getRestaurant().getName())
                .branchName(table.getBranch().getName())
                .tableNumber(table.getTableNumber())
                .menuItems(items)
                .build();
    }

    @Transactional
    public OrderResponse createOrder(String token, QrOrderRequest request) {
        RestaurantTable table = getTableByTokenForOrder(token);
        if (table.getStatus() != RestaurantTable.TableStatus.AVAILABLE) {
            throw new ApiException("Meza hii haipatikani kwa sasa. Tafadhali muombe mhudumu akusaidie.");
        }
        return orderService.createQrOrder(table, request);
    }

    @Transactional
    public QrLinkResponse getOrCreateLink(Long tableId, String username) {
        RestaurantTable table = tables.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Meza haikupatikana"));
        branchAccess.requireAccess(username, table.getBranch().getId());
        if (table.getQrToken() == null || table.getQrToken().isBlank()) {
            table.setQrToken(newUniqueToken());
            tables.save(table);
        }
        return linkFor(table);
    }

    /** Replaces a leaked or damaged QR code and immediately invalidates the old link. */
    @Transactional
    public QrLinkResponse rotateLink(Long tableId, String username) {
        RestaurantTable table = tables.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("Meza haikupatikana"));
        branchAccess.requireAccess(username, table.getBranch().getId());
        table.setQrToken(newUniqueToken());
        tables.save(table);
        return linkFor(table);
    }

    private RestaurantTable getTableByToken(String token) {
        if (token == null || token.length() < 32) {
            throw new ResourceNotFoundException("Msimbo wa QR haupatikani");
        }
        return tables.findByQrToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Msimbo wa QR haupatikani"));
    }

    private RestaurantTable getTableByTokenForOrder(String token) {
        if (token == null || token.length() < 32) {
            throw new ResourceNotFoundException("Msimbo wa QR haupatikani");
        }
        return tables.findByQrTokenForUpdate(token)
                .orElseThrow(() -> new ResourceNotFoundException("Msimbo wa QR haupatikani"));
    }

    private QrLinkResponse linkFor(RestaurantTable table) {
        return new QrLinkResponse("sumaye://order/" + table.getQrToken());
    }

    private String newUniqueToken() {
        String token;
        do {
            token = newToken();
        } while (tables.findByQrToken(token).isPresent());
        return token;
    }

    private String newToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private QrMenuItemResponse menuItem(MenuItem item) {
        return QrMenuItemResponse.builder()
                .id(item.getId())
                .categoryId(item.getCategory().getId())
                .categoryName(item.getCategory().getName())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .imageUrl(item.getImageUrl())
                .preparationTimeMinutes(item.getPreparationTimeMinutes())
                .build();
    }
}
