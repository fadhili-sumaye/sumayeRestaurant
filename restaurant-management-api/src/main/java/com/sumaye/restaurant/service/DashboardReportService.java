package com.sumaye.restaurant.service;

import com.sumaye.restaurant.dto.OwnerBestSeller;
import com.sumaye.restaurant.dto.OwnerCategorySales;
import com.sumaye.restaurant.dto.OwnerDashboardResponse;
import com.sumaye.restaurant.dto.OwnerExpenseCategoryStat;
import com.sumaye.restaurant.dto.OwnerExpenseStats;
import com.sumaye.restaurant.dto.OwnerInventoryItem;
import com.sumaye.restaurant.dto.OwnerInventorySummary;
import com.sumaye.restaurant.dto.OwnerMonthlyPerformance;
import com.sumaye.restaurant.dto.OwnerOrderStats;
import com.sumaye.restaurant.dto.OwnerPaymentMethodStat;
import com.sumaye.restaurant.dto.OwnerRiderRow;
import com.sumaye.restaurant.dto.OwnerSeriesPoint;
import com.sumaye.restaurant.dto.OwnerStaffCountRow;
import com.sumaye.restaurant.dto.OwnerStaffPaymentRow;
import com.sumaye.restaurant.dto.OwnerStaffPerformance;
import com.sumaye.restaurant.dto.OwnerStaffSalesRow;
import com.sumaye.restaurant.dto.OwnerSummaryResponse;
import com.sumaye.restaurant.exception.ApiException;
import com.sumaye.restaurant.model.InventoryTransaction;
import com.sumaye.restaurant.model.Order;
import com.sumaye.restaurant.model.Payment;
import com.sumaye.restaurant.repository.DeliveryOrderRepository;
import com.sumaye.restaurant.repository.ExpenseRepository;
import com.sumaye.restaurant.repository.InventoryStockRepository;
import com.sumaye.restaurant.repository.InventoryTransactionRepository;
import com.sumaye.restaurant.repository.KitchenOrderRepository;
import com.sumaye.restaurant.repository.OrderItemRepository;
import com.sumaye.restaurant.repository.OrderRepository;
import com.sumaye.restaurant.repository.PaymentRepository;
import com.sumaye.restaurant.repository.WastageRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardReportService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final Set<String> ACTIVE_STATUSES =
            Set.of("NEW", "SENT_TO_KITCHEN", "ACCEPTED", "PREPARING", "READY", "SERVED");

    private final BranchAccessService branchAccess;
    private final PaymentRepository payments;
    private final ExpenseRepository expenses;
    private final OrderRepository orders;
    private final OrderItemRepository orderItems;
    private final InventoryStockRepository stock;
    private final InventoryTransactionRepository inventoryTransactions;
    private final WastageRecordRepository wastage;
    private final KitchenOrderRepository kitchenOrders;
    private final DeliveryOrderRepository deliveryOrders;

    public enum SeriesGroup { DAY, WEEK, MONTH }

    public OwnerDashboardResponse dashboard(Long branchId, String username, LocalDate from, LocalDate to, String groupBy) {
        branchAccess.requireAccess(username, branchId);
        LocalDate start = from == null ? LocalDate.now() : from;
        LocalDate end = to == null ? start : to;
        if (end.isBefore(start)) {
            throw new ApiException("Tarehe ya mwisho haiwezi kuwa kabla ya tarehe ya kuanzia");
        }
        SeriesGroup group = parseGroup(groupBy);
        LocalDateTime startDt = start.atStartOfDay();
        LocalDateTime endDt = end.plusDays(1).atStartOfDay().minusNanos(1);

        return OwnerDashboardResponse.builder()
                .summary(summary(branchId, startDt, endDt, start, end))
                .salesSeries(salesSeries(branchId, startDt, endDt, start, end, group))
                .payments(paymentStats(branchId, startDt, endDt))
                .expenses(expenseStats(branchId, start, end))
                .orders(orderStats(branchId, startDt, endDt))
                .bestSellers(bestSellers(branchId, startDt, endDt, 5))
                .inventory(inventory(branchId, startDt, endDt))
                .staff(staff(branchId, startDt, endDt))
                .monthly(monthly(branchId, start))
                .periodLabel(periodLabel(start, end, group))
                .generatedAt(LocalDateTime.now())
                .build();
    }

    public byte[] exportCsv(Long branchId, String username, LocalDate from, LocalDate to, String groupBy) {
        return buildCsv(dashboard(branchId, username, from, to, groupBy));
    }

    public byte[] exportPdf(Long branchId, String username, LocalDate from, LocalDate to, String groupBy) {
        return buildPdf(dashboard(branchId, username, from, to, groupBy));
    }

    private SeriesGroup parseGroup(String groupBy) {
        try {
            return groupBy == null ? SeriesGroup.DAY : SeriesGroup.valueOf(groupBy.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException("Kipindi cha mkusanyiko hakijulikani (DAY, WEEK au MONTH)");
        }
    }

    private OwnerSummaryResponse summary(Long branchId, LocalDateTime from, LocalDateTime to, LocalDate dFrom, LocalDate dTo) {
        BigDecimal sales = payments.totalSuccessful(branchId, from, to);
        BigDecimal expenseAmt = expenses.total(branchId, dFrom, dTo);
        BigDecimal diff = sales.subtract(expenseAmt);
        BigDecimal profit = diff.signum() < 0 ? ZERO : diff;
        BigDecimal loss = diff.signum() < 0 ? diff.abs() : ZERO;

        long total = 0;
        long pending = 0;
        long completed = 0;
        long cancelled = 0;
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (Object[] row : orders.countByStatusBetween(branchId, from, to)) {
            String st = ((Order.OrderStatus) row[0]).name();
            long c = ((Number) row[1]).longValue();
            total += c;
            byStatus.put(st, byStatus.getOrDefault(st, 0L) + c);
            if (ACTIVE_STATUSES.contains(st)) pending += c;
            if ("COMPLETED".equals(st)) completed += c;
            if ("CANCELLED".equals(st)) cancelled += c;
        }

        long paidOrders = payments.countPaidOrdersBetween(branchId, from, to);
        BigDecimal avgOrder = paidOrders == 0 ? ZERO : sales.divide(BigDecimal.valueOf(paidOrders), 2, RoundingMode.HALF_UP);

        BigDecimal food = ZERO;
        BigDecimal beverage = ZERO;
        BigDecimal other = ZERO;
        List<OwnerCategorySales> byCategory = new ArrayList<>();
        for (Object[] row : orderItems.revenueByCategory(branchId, from, to)) {
            String cat = (String) row[0];
            BigDecimal amt = (BigDecimal) row[1];
            byCategory.add(OwnerCategorySales.builder().category(cat == null ? "Vingine" : cat).sales(amt).build());
            if (cat == null) {
                other = other.add(amt);
            } else if (isFoodCategory(cat)) {
                food = food.add(amt);
            } else if (isBeverageCategory(cat)) {
                beverage = beverage.add(amt);
            } else {
                other = other.add(amt);
            }
        }
        byCategory.sort((a, b) -> b.getSales().compareTo(a.getSales()));

        return OwnerSummaryResponse.builder()
                .sales(sales)
                .expenses(expenseAmt)
                .profit(profit)
                .loss(loss)
                .totalOrders(total)
                .pendingOrders(pending)
                .completedOrders(completed)
                .cancelledOrders(cancelled)
                .avgOrderValue(avgOrder)
                .foodSales(food)
                .beverageSales(beverage)
                .otherSales(other)
                .lowStockItems(stock.countLowStockByBranchId(branchId))
                .salesByCategory(byCategory)
                .build();
    }

    private List<OwnerSeriesPoint> salesSeries(Long branchId, LocalDateTime from, LocalDateTime to,
                                               LocalDate dFrom, LocalDate dTo, SeriesGroup group) {
        long days = ChronoUnit.DAYS.between(dFrom, dTo) + 1;
        if (group == SeriesGroup.DAY && days > 90) group = SeriesGroup.MONTH;
        if (group == SeriesGroup.WEEK && days > 370) group = SeriesGroup.MONTH;

        Map<String, BigDecimal[]> acc = new TreeMap<>();
        LocalDate cursor = dFrom;
        while (!cursor.isAfter(dTo)) {
            acc.computeIfAbsent(bucketKey(cursor, group), k -> new BigDecimal[]{ZERO, ZERO});
            cursor = nextBucket(cursor, group);
        }
        for (Object[] row : payments.revenuesBetween(branchId, from, to)) {
            LocalDateTime ts = (LocalDateTime) row[0];
            BigDecimal amt = (BigDecimal) row[1];
            BigDecimal[] a = acc.get(bucketKey(ts.toLocalDate(), group));
            if (a == null) a = acc.computeIfAbsent(bucketKey(ts.toLocalDate(), group), k -> new BigDecimal[]{ZERO, ZERO});
            a[0] = a[0].add(amt);
        }
        for (Object[] row : expenses.expensesBetween(branchId, dFrom, dTo)) {
            LocalDate d = (LocalDate) row[0];
            BigDecimal amt = (BigDecimal) row[1];
            BigDecimal[] a = acc.computeIfAbsent(bucketKey(d, group), k -> new BigDecimal[]{ZERO, ZERO});
            a[1] = a[1].add(amt);
        }
        List<OwnerSeriesPoint> result = new ArrayList<>();
        acc.forEach((key, a) -> result.add(OwnerSeriesPoint.builder()
                .label(key)
                .revenue(a[0])
                .expenses(a[1])
                .profit(a[0].subtract(a[1]))
                .build()));
        return result;
    }

    private String bucketKey(LocalDate d, SeriesGroup group) {
        switch (group) {
            case WEEK:
                return d.minusDays(d.getDayOfWeek().getValue() - 1L).toString();
            case MONTH:
                return YearMonth.from(d).atDay(1).toString();
            default:
                return d.toString();
        }
    }

    private LocalDate nextBucket(LocalDate d, SeriesGroup group) {
        switch (group) {
            case WEEK:
                return d.plusDays(7);
            case MONTH:
                return YearMonth.from(d).plusMonths(1).atDay(1);
            default:
                return d.plusDays(1);
        }
    }

    private List<OwnerPaymentMethodStat> paymentStats(Long branchId, LocalDateTime from, LocalDateTime to) {
        List<OwnerPaymentMethodStat> result = new ArrayList<>();
        for (Object[] row : payments.paymentMethodStats(branchId, from, to)) {
            String method = ((Payment.PaymentMethod) row[0]).name();
            String provider = row[1] == null ? null : ((Payment.PaymentProvider) row[1]).name();
            long count = ((Number) row[2]).longValue();
            BigDecimal total = (BigDecimal) row[3];
            result.add(OwnerPaymentMethodStat.builder()
                    .paymentMethod(method)
                    .provider(provider)
                    .count(count)
                    .total(total)
                    .build());
        }
        return result;
    }

    private OwnerExpenseStats expenseStats(Long branchId, LocalDate from, LocalDate to) {
        BigDecimal total = expenses.total(branchId, from, to);
        long count = expenses.countBetween(branchId, from, to);
        List<OwnerExpenseCategoryStat> byCategory = new ArrayList<>();
        for (Object[] row : expenses.totalByCategory(branchId, from, to)) {
            Long categoryId = ((Number) row[0]).longValue();
            String name = (String) row[1];
            BigDecimal amount = (BigDecimal) row[2];
            BigDecimal percent = total.signum() == 0 ? ZERO
                    : amount.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);
            byCategory.add(OwnerExpenseCategoryStat.builder()
                    .categoryId(categoryId)
                    .categoryName(name)
                    .amount(amount)
                    .percent(percent)
                    .build());
        }
        return OwnerExpenseStats.builder().total(total).count(count).byCategory(byCategory).build();
    }

    private OwnerOrderStats orderStats(Long branchId, LocalDateTime from, LocalDateTime to) {
        long total = 0;
        long completed = 0;
        long pending = 0;
        long cancelled = 0;
        Map<String, Long> byStatus = new LinkedHashMap<>();
        for (Object[] row : orders.countByStatusBetween(branchId, from, to)) {
            String st = ((Order.OrderStatus) row[0]).name();
            long c = ((Number) row[1]).longValue();
            total += c;
            byStatus.put(st, c);
            if ("COMPLETED".equals(st)) completed += c;
            if ("CANCELLED".equals(st)) cancelled += c;
            if (ACTIVE_STATUSES.contains(st)) pending += c;
        }

        Map<Integer, Integer> hourCount = new TreeMap<>();
        for (LocalDateTime ts : orders.findCreatedAtsBetween(branchId, from, to)) {
            hourCount.merge(ts.getHour(), 1, Integer::sum);
        }
        Integer busyHour = null;
        int max = 0;
        for (Map.Entry<Integer, Integer> e : hourCount.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
                busyHour = e.getKey();
            }
        }
        String busyLabel = busyHour == null ? null : String.format("%02d:00 - %02d:00", busyHour, (busyHour + 1) % 24);

        return OwnerOrderStats.builder()
                .total(total)
                .completed(completed)
                .pending(pending)
                .cancelled(cancelled)
                .byStatus(byStatus)
                .busyHour(busyHour)
                .busyHourLabel(busyLabel)
                .build();
    }

    private List<OwnerBestSeller> bestSellers(Long branchId, LocalDateTime from, LocalDateTime to, int limit) {
        List<OwnerBestSeller> result = new ArrayList<>();
        for (Object[] row : orderItems.bestSellers(branchId, from, to)) {
            Long menuItemId = ((Number) row[0]).longValue();
            String name = (String) row[1];
            String category = (String) row[2];
            long qty = ((Number) row[3]).longValue();
            BigDecimal revenue = (BigDecimal) row[4];
            result.add(OwnerBestSeller.builder()
                    .menuItemId(menuItemId)
                    .name(name)
                    .categoryName(category)
                    .quantity(qty)
                    .revenue(revenue)
                    .build());
            if (result.size() >= limit) break;
        }
        return result;
    }

    private OwnerInventorySummary inventory(Long branchId, LocalDateTime from, LocalDateTime to) {
        Object[] was = wastage.countAndQuantityBetween(branchId, from, to);
        long wastageCount = ((Number) was[0]).longValue();
        BigDecimal wastageQty = was[1] == null ? ZERO : (BigDecimal) was[1];

        List<OwnerInventoryItem> recent = inventoryTransactions.findByBranchIdOrderByCreatedAtDesc(branchId, PageRequest.of(0, 10))
                .stream().map(t -> OwnerInventoryItem.builder()
                        .id(t.getId())
                        .ingredientName(t.getIngredient() == null ? null : t.getIngredient().getName())
                        .transactionType(t.getTransactionType() == null ? null : t.getTransactionType().name())
                        .quantityChange(t.getQuantityChange())
                        .unit(t.getUnit() == null ? null : t.getUnit().name())
                        .notes(t.getNotes())
                        .createdAt(t.getCreatedAt())
                        .build()).collect(Collectors.toList());

        return OwnerInventorySummary.builder()
                .totalItems(stock.countByBranchId(branchId))
                .lowStock(stock.countLowStockByBranchId(branchId))
                .outOfStock(stock.countOutOfStockByBranchId(branchId))
                .stockValue(stock.stockValueByBranchId(branchId))
                .wastageCount(wastageCount)
                .wastageQuantity(wastageQty)
                .recentTransactions(recent)
                .build();
    }

    private OwnerStaffPerformance staff(Long branchId, LocalDateTime from, LocalDateTime to) {
        List<OwnerStaffSalesRow> waiters = new ArrayList<>();
        for (Object[] row : orders.waiterStatsBetween(branchId, from, to)) {
            waiters.add(OwnerStaffSalesRow.builder()
                    .username((String) row[0])
                    .orders(((Number) row[1]).longValue())
                    .sales((BigDecimal) row[2])
                    .build());
        }
        List<OwnerStaffPaymentRow> cashiers = new ArrayList<>();
        for (Object[] row : payments.cashierStatsBetween(branchId, from, to)) {
            cashiers.add(OwnerStaffPaymentRow.builder()
                    .username((String) row[0])
                    .count(((Number) row[1]).longValue())
                    .total((BigDecimal) row[2])
                    .build());
        }
        List<OwnerStaffCountRow> kitchen = new ArrayList<>();
        for (Object[] row : kitchenOrders.preparedByUserBetween(branchId, from, to)) {
            kitchen.add(OwnerStaffCountRow.builder()
                    .username((String) row[0])
                    .count(((Number) row[1]).longValue())
                    .build());
        }
        List<OwnerRiderRow> riders = new ArrayList<>();
        for (Object[] row : deliveryOrders.riderStatsBetween(branchId, from, to)) {
            riders.add(OwnerRiderRow.builder()
                    .username((String) row[0])
                    .delivered(((Number) row[1]).longValue())
                    .deliveryFees((BigDecimal) row[2])
                    .build());
        }
        return OwnerStaffPerformance.builder()
                .waiters(waiters)
                .cashiers(cashiers)
                .kitchen(kitchen)
                .riders(riders)
                .kitchenOrdersTotal(kitchenOrders.countBetween(branchId, from, to))
                .deliveriesCompleted(deliveryOrders.completedCountBetween(branchId, from, to))
                .build();
    }

    private OwnerMonthlyPerformance monthly(Long branchId, LocalDate start) {
        YearMonth ym = YearMonth.from(start);
        YearMonth prev = ym.minusMonths(1);
        LocalDateTime mStart = ym.atDay(1).atStartOfDay();
        LocalDateTime mEnd = ym.atEndOfMonth().plusDays(1).atStartOfDay().minusNanos(1);
        LocalDateTime pStart = prev.atDay(1).atStartOfDay();
        LocalDateTime pEnd = prev.atEndOfMonth().plusDays(1).atStartOfDay().minusNanos(1);

        BigDecimal sales = payments.totalSuccessful(branchId, mStart, mEnd);
        BigDecimal previousSales = payments.totalSuccessful(branchId, pStart, pEnd);
        BigDecimal exp = expenses.total(branchId, ym.atDay(1), ym.atEndOfMonth());
        BigDecimal prevExp = expenses.total(branchId, prev.atDay(1), prev.atEndOfMonth());
        long thisOrders = countOrders(branchId, mStart, mEnd);
        long prevOrders = countOrders(branchId, pStart, pEnd);

        List<OwnerBestSeller> monthBest = bestSellers(branchId, mStart, mEnd, 1);
        String bestFood = monthBest.isEmpty() ? null : monthBest.get(0).getName();
        String bestCategory = monthBest.isEmpty() ? null : monthBest.get(0).getCategoryName();

        return OwnerMonthlyPerformance.builder()
                .monthLabel(ym.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + ym.getYear())
                .sales(sales)
                .previousSales(previousSales)
                .salesGrowthPercent(growth(previousSales, sales))
                .orders(thisOrders)
                .previousOrders(prevOrders)
                .ordersGrowthPercent(growth(BigDecimal.valueOf(prevOrders), BigDecimal.valueOf(thisOrders)))
                .expenses(exp)
                .previousExpenses(prevExp)
                .profit(sales.subtract(exp))
                .previousProfit(previousSales.subtract(prevExp))
                .bestSellingFood(bestFood)
                .bestSellingCategory(bestCategory)
                .build();
    }

    private long countOrders(Long branchId, LocalDateTime from, LocalDateTime to) {
        long total = 0;
        for (Object[] row : orders.countByStatusBetween(branchId, from, to)) {
            total += ((Number) row[1]).longValue();
        }
        return total;
    }

    private BigDecimal growth(BigDecimal prev, BigDecimal curr) {
        if (prev.signum() == 0) {
            return curr.signum() == 0 ? ZERO : BigDecimal.valueOf(100);
        }
        return curr.subtract(prev).multiply(BigDecimal.valueOf(100)).divide(prev, 2, RoundingMode.HALF_UP);
    }

    private String periodLabel(LocalDate from, LocalDate to, SeriesGroup group) {
        StringBuilder sb = new StringBuilder();
        for (LocalDate d = from; !d.isAfter(to); d = nextBucket(d, group)) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(d);
        }
        return sb.length() == 0 ? from.toString() : sb.toString();
    }

    private boolean isFoodCategory(String name) {
        String n = name == null ? "" : name.toUpperCase();
        return n.contains("FOOD") || n.contains("MAIN") || n.contains("STARTER") || n.contains("SIDE") || n.contains("UGALI") || n.contains("RICE");
    }

    private boolean isBeverageCategory(String name) {
        String n = name == null ? "" : name.toUpperCase();
        return n.contains("DRINK") || n.contains("BEVERAGE") || n.contains("JUICE") || n.contains("SODA") || n.contains("SOFT") || n.contains("WATER") || n.contains("SMOOTHIE");
    }

    // ------------------------------------------------------------------
    // CSV export
    // ------------------------------------------------------------------

    private byte[] buildCsv(OwnerDashboardResponse d) {
        StringBuilder sb = new StringBuilder("\uFEFF");
        sb.append(board("MUHTASARI WA BIASHARA", d.getPeriodLabel(), d.getGeneratedAt())).append('\n');

        sectionHeader(sb, "Muhtasari");
        csvRow(sb, "Mauzo", "Gharama", "Faida", "Hasara", "Wastani wa Oda");
        csvRow(sb, money(d.getSummary().getSales()), money(d.getSummary().getExpenses()),
                money(d.getSummary().getProfit()), money(d.getSummary().getLoss()),
                money(d.getSummary().getAvgOrderValue()));
        csvRow(sb, "Oda Zote", "Oda Zinazoendelea", "Oda Zimekamilika", "Oda Zimeghairiwa", "Bidhaa zenye Stoo ya Chini");
        csvRow(sb, String.valueOf(d.getSummary().getTotalOrders()), String.valueOf(d.getSummary().getPendingOrders()),
                String.valueOf(d.getSummary().getCompletedOrders()), String.valueOf(d.getSummary().getCancelledOrders()),
                String.valueOf(d.getSummary().getLowStockItems()));

        sectionHeader(sb, "Mauzo kwa Aina");
        csvRow(sb, "Vyakula", "Vinywaji", "Vingine");
        csvRow(sb, money(d.getSummary().getFoodSales()), money(d.getSummary().getBeverageSales()), money(d.getSummary().getOtherSales()));

        sectionHeader(sb, "Mauzo kwa Kipindi");
        csvRow(sb, "Kipindi", "Mapato", "Gharama", "Faida");
        for (OwnerSeriesPoint p : d.getSalesSeries()) {
            csvRow(sb, p.getLabel(), money(p.getRevenue()), money(p.getExpenses()), money(p.getProfit()));
        }

        sectionHeader(sb, "Malipo kwa Njia");
        csvRow(sb, "Njia", "Mtoa Huduma", "Idadi", "Kiasi");
        for (OwnerPaymentMethodStat p : d.getPayments()) {
            csvRow(sb, p.getPaymentMethod(), p.getProvider() == null ? "" : p.getProvider(),
                    String.valueOf(p.getCount()), money(p.getTotal()));
        }

        sectionHeader(sb, "Gharama kwa Kundi");
        csvRow(sb, "Jumla ya Gharama", String.valueOf(d.getExpenses().getCount()));
        csvRow(sb, "Kundi", "Kiasi", "Asilimia");
        for (OwnerExpenseCategoryStat c : d.getExpenses().getByCategory()) {
            csvRow(sb, c.getCategoryName(), money(c.getAmount()), c.getPercent().toPlainString() + "%");
        }

        sectionHeader(sb, "Maagizo kwa Hali");
        csvRow(sb, "Jumla", "Zinazoendelea", "Zimekamilika", "Zimeghairiwa", "Saa Yenye Shughuli Nyingi");
        csvRow(sb, String.valueOf(d.getOrders().getTotal()), String.valueOf(d.getOrders().getPending()),
                String.valueOf(d.getOrders().getCompleted()), String.valueOf(d.getOrders().getCancelled()),
                d.getOrders().getBusyHourLabel() == null ? "" : d.getOrders().getBusyHourLabel());

        sectionHeader(sb, "Vitoweo Vinavyouzwa Zaidi");
        csvRow(sb, "Chakula", "Kundi", "Idadi", "Mapato");
        for (OwnerBestSeller b : d.getBestSellers()) {
            csvRow(sb, b.getName(), b.getCategoryName() == null ? "" : b.getCategoryName(),
                    String.valueOf(b.getQuantity()), money(b.getRevenue()));
        }

        sectionHeader(sb, "Hesabu ya Stoo");
        csvRow(sb, "Jumla ya Vipengee", "Chini ya Kiwango", "Binafu", "Thamani ya Stoo", "Taka (Idadi)", "Kiasi cha Taka");
        csvRow(sb, String.valueOf(d.getInventory().getTotalItems()), String.valueOf(d.getInventory().getLowStock()),
                String.valueOf(d.getInventory().getOutOfStock()), money(d.getInventory().getStockValue()),
                String.valueOf(d.getInventory().getWastageCount()), d.getInventory().getWastageQuantity().toPlainString());

        sectionHeader(sb, "Utendaji wa Wahudumu (Waiters)");
        csvRow(sb, "Mtumiaji", "Oda", "Mauzo");
        for (OwnerStaffSalesRow r : d.getStaff().getWaiters()) {
            csvRow(sb, r.getUsername(), String.valueOf(r.getOrders()), money(r.getSales()));
        }
        sectionHeader(sb, "Utendaji wa Wahesabu (Cashiers)");
        csvRow(sb, "Mtumiaji", "Malipo", "Kiasi");
        for (OwnerStaffPaymentRow r : d.getStaff().getCashiers()) {
            csvRow(sb, r.getUsername(), String.valueOf(r.getCount()), money(r.getTotal()));
        }
        sectionHeader(sb, "Maandalizi ya Jikoni (Kitchen)");
        csvRow(sb, "Jumla ya Tiba za Jikoni", String.valueOf(d.getStaff().getKitchenOrdersTotal()));
        csvRow(sb, "Mpishi", "Idadi");
        for (OwnerStaffCountRow r : d.getStaff().getKitchen()) {
            csvRow(sb, r.getUsername(), String.valueOf(r.getCount()));
        }
        sectionHeader(sb, "Usambazaji wa Bidhaa (Delivery)");
        csvRow(sb, "Zilizopelekwa", String.valueOf(d.getStaff().getDeliveriesCompleted()));
        csvRow(sb, "Mpelekaji", "Zilizofika", "Ada ya Usafirishaji");
        for (OwnerRiderRow r : d.getStaff().getRiders()) {
            csvRow(sb, r.getUsername(), String.valueOf(r.getDelivered()), money(r.getDeliveryFees()));
        }

        sectionHeader(sb, "Ulinganisho wa Mwezi");
        OwnerMonthlyPerformance m = d.getMonthly();
        csvRow(sb, "Kipengele", m.getMonthLabel(), "Mwezi Uliopita", "Mabadiliko (%)");
        csvRow(sb, "Mauzo", money(m.getSales()), money(m.getPreviousSales()), m.getSalesGrowthPercent().toPlainString());
        csvRow(sb, "Maagizo", String.valueOf(m.getOrders()), String.valueOf(m.getPreviousOrders()), m.getOrdersGrowthPercent().toPlainString());
        csvRow(sb, "Gharama", money(m.getExpenses()), money(m.getPreviousExpenses()), "-");
        csvRow(sb, "Faida", money(m.getProfit()), money(m.getPreviousProfit()), "-");
        csvRow(sb, "Chakula Kinachouzwa Zaidi", m.getBestSellingFood() == null ? "" : m.getBestSellingFood(), "-", "-");

        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    private String board(String title, String period, LocalDateTime generatedAt) {
        return title + " | Kipindi: " + period + " | Imezalishwa: " + generatedAt;
    }

    private void sectionHeader(StringBuilder sb, String name) {
        sb.append('\n').append(name).append('\n');
    }

    private void csvRow(StringBuilder sb, String... cols) {
        for (int i = 0; i < cols.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(csv(cols[i] == null ? "" : cols[i]));
        }
        sb.append('\n');
    }

    private String csv(String value) {
        if (value.indexOf(',') < 0 && value.indexOf('"') < 0 && value.indexOf('\n') < 0) return value;
        return '"' + value.replace("\"", "\"\"") + '"';
    }

    private static String money(BigDecimal v) {
        if (v == null) return "0";
        java.text.DecimalFormat f = new java.text.DecimalFormat("#,##0.00");
        return f.format(v);
    }

    // ------------------------------------------------------------------
    // PDF export
    // ------------------------------------------------------------------

    private byte[] buildPdf(OwnerDashboardResponse d) {
        try {
            return docPdfBytes(d);
        } catch (Exception e) {
            log.error("PDF export failed", e);
            throw new ApiException("Imeshindwa kuunda faili ya PDF: " + e.getMessage());
        }
    }

    private byte[] docPdfBytes(OwnerDashboardResponse d) throws Exception {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            com.lowagie.text.Document doc = new com.lowagie.text.Document();
            com.lowagie.text.pdf.PdfWriter.getInstance(doc, out);
            doc.open();
            com.lowagie.text.Font titleFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 16);
            com.lowagie.text.Font headingFont = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 11,
                    com.lowagie.text.Font.BOLD, new java.awt.Color(0x00, 0xC8, 0x53));
            com.lowagie.text.Font normal = com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA, 9);

            doc.add(new com.lowagie.text.Paragraph("MUHTASARI WA BIASHARA", titleFont));
            doc.add(new com.lowagie.text.Paragraph("Kipindi: " + d.getPeriodLabel() + " | Imezalishwa: " + d.getGeneratedAt(), normal));
            doc.add(new com.lowagie.text.Chunk(" "));

            table(doc, headingFont, normal, "Muhtasari",
                    new String[]{"Kipengele", "Thamani (TZS)"},
                    new String[][]{
                            {"Mauzo", money(d.getSummary().getSales())},
                            {"Gharama", money(d.getSummary().getExpenses())},
                            {"Faida", money(d.getSummary().getProfit())},
                            {"Hasara", money(d.getSummary().getLoss())},
                            {"Wastani wa Oda", money(d.getSummary().getAvgOrderValue())},
                            {"Oda Zote", String.valueOf(d.getSummary().getTotalOrders())},
                            {"Oda Zinazoendelea", String.valueOf(d.getSummary().getPendingOrders())},
                            {"Oda Zimekamilika", String.valueOf(d.getSummary().getCompletedOrders())},
                            {"Oda Zimeghairiwa", String.valueOf(d.getSummary().getCancelledOrders())},
                            {"Bidhaa zenye Stoo ya Chini", String.valueOf(d.getSummary().getLowStockItems())}
                    });

            table(doc, headingFont, normal, "Mauzo kwa Aina",
                    new String[]{"Aina", "Kiasi (TZS)"},
                    new String[][]{
                            {"Vyakula", money(d.getSummary().getFoodSales())},
                            {"Vinywaji", money(d.getSummary().getBeverageSales())},
                            {"Vingine", money(d.getSummary().getOtherSales())}
                    });

            java.util.List<com.lowagie.text.Phrase[]> salesRows = new ArrayList<>();
            for (OwnerSeriesPoint p : d.getSalesSeries()) {
                salesRows.add(new com.lowagie.text.Phrase[]{new com.lowagie.text.Phrase(p.getLabel(), normal),
                        new com.lowagie.text.Phrase(money(p.getRevenue()), normal),
                        new com.lowagie.text.Phrase(money(p.getExpenses()), normal),
                        new com.lowagie.text.Phrase(money(p.getProfit()), normal)});
            }
            tableHeaderOnly(doc, headingFont, normal, "Mauzo kwa Kipindi",
                    new String[]{"Kipindi", "Mapato", "Gharama", "Faida"}, salesRows);

            java.util.List<com.lowagie.text.Phrase[]> payRows = new ArrayList<>();
            for (OwnerPaymentMethodStat p : d.getPayments()) {
                payRows.add(new com.lowagie.text.Phrase[]{new com.lowagie.text.Phrase(p.getPaymentMethod(), normal),
                        new com.lowagie.text.Phrase(p.getProvider() == null ? "-" : p.getProvider(), normal),
                        new com.lowagie.text.Phrase(String.valueOf(p.getCount()), normal),
                        new com.lowagie.text.Phrase(money(p.getTotal()), normal)});
            }
            tableHeaderOnly(doc, headingFont, normal, "Malipo kwa Njia",
                    new String[]{"Njia", "Mtoa Huduma", "Idadi", "Kiasi"}, payRows);

            java.util.List<com.lowagie.text.Phrase[]> expRows = new ArrayList<>();
            for (OwnerExpenseCategoryStat c : d.getExpenses().getByCategory()) {
                expRows.add(new com.lowagie.text.Phrase[]{new com.lowagie.text.Phrase(c.getCategoryName(), normal),
                        new com.lowagie.text.Phrase(money(c.getAmount()), normal),
                        new com.lowagie.text.Phrase(c.getPercent().toPlainString() + "%", normal)});
            }
            tableHeaderOnly(doc, headingFont, normal, "Gharama kwa Kundi",
                    new String[]{"Kundi", "Kiasi", "Asilimia"}, expRows);
            doc.add(new com.lowagie.text.Paragraph("Jumla ya Gharama: " + money(d.getExpenses().getTotal())
                    + " (Gharama " + d.getExpenses().getCount() + ")", normal));

            java.util.List<com.lowagie.text.Phrase[]> bestRows = new ArrayList<>();
            for (OwnerBestSeller b : d.getBestSellers()) {
                bestRows.add(new com.lowagie.text.Phrase[]{new com.lowagie.text.Phrase(b.getName(), normal),
                        new com.lowagie.text.Phrase(b.getCategoryName() == null ? "-" : b.getCategoryName(), normal),
                        new com.lowagie.text.Phrase(String.valueOf(b.getQuantity()), normal),
                        new com.lowagie.text.Phrase(money(b.getRevenue()), normal)});
            }
            tableHeaderOnly(doc, headingFont, normal, "Vitoweo Vinavyouzwa Zaidi",
                    new String[]{"Chakula", "Kundi", "Idadi", "Mapato"}, bestRows);

            table(doc, headingFont, normal, "Hesabu ya Stoo",
                    new String[]{"Kipengele", "Thamani"},
                    new String[][]{
                            {"Jumla ya Vipengee", String.valueOf(d.getInventory().getTotalItems())},
                            {"Chini ya Kiwango", String.valueOf(d.getInventory().getLowStock())},
                            {"Binafu", String.valueOf(d.getInventory().getOutOfStock())},
                            {"Thamani ya Stoo", money(d.getInventory().getStockValue())},
                            {"Taka (Idadi)", String.valueOf(d.getInventory().getWastageCount())},
                            {"Kiasi cha Taka", d.getInventory().getWastageQuantity().toPlainString()}
                    });

            table(doc, headingFont, normal, "Ulinganisho wa Mwezi",
                    new String[]{"Kipengele", "Mwezi Huu", "Mwezi Uliopita", "Mabadiliko (%)"},
                    new String[][]{
                            {"Mauzo", money(d.getMonthly().getSales()), money(d.getMonthly().getPreviousSales()),
                                    d.getMonthly().getSalesGrowthPercent().toPlainString()},
                            {"Maagizo", String.valueOf(d.getMonthly().getOrders()),
                                    String.valueOf(d.getMonthly().getPreviousOrders()),
                                    d.getMonthly().getOrdersGrowthPercent().toPlainString()},
                            {"Gharama", money(d.getMonthly().getExpenses()), money(d.getMonthly().getPreviousExpenses()), "-"},
                            {"Faida", money(d.getMonthly().getProfit()), money(d.getMonthly().getPreviousProfit()), "-"},
                            {"Chakula Kinachouzwa Zaidi",
                                    d.getMonthly().getBestSellingFood() == null ? "-" : d.getMonthly().getBestSellingFood(), "-", "-"}
                    });

            doc.close();
            return out.toByteArray();
        }

        private void table(com.lowagie.text.Document doc, com.lowagie.text.Font heading,
                           com.lowagie.text.Font normal, String title, String[] headers, String[][] rows) throws com.lowagie.text.DocumentException {
            doc.add(new com.lowagie.text.Paragraph(title, heading));
            com.lowagie.text.pdf.PdfPTable t = new com.lowagie.text.pdf.PdfPTable(headers.length);
            t.setWidthPercentage(100);
            for (String h : headers) t.addCell(headerCell(h));
            for (String[] row : rows) {
                for (String v : row) t.addCell(new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(v, normal)));
            }
            doc.add(t);
            doc.add(new com.lowagie.text.Chunk(" "));
        }

        private void tableHeaderOnly(com.lowagie.text.Document doc, com.lowagie.text.Font heading,
                                     com.lowagie.text.Font normal, String title, String[] headers,
                                     java.util.List<com.lowagie.text.Phrase[]> rows) throws com.lowagie.text.DocumentException {
            doc.add(new com.lowagie.text.Paragraph(title, heading));
            com.lowagie.text.pdf.PdfPTable t = new com.lowagie.text.pdf.PdfPTable(headers.length);
            t.setWidthPercentage(100);
            for (String h : headers) t.addCell(headerCell(h));
            for (com.lowagie.text.Phrase[] row : rows) {
                for (com.lowagie.text.Phrase v : row) t.addCell(new com.lowagie.text.pdf.PdfPCell(v));
            }
            doc.add(t);
            doc.add(new com.lowagie.text.Chunk(" "));
        }

        private com.lowagie.text.pdf.PdfPCell headerCell(String text) {
            com.lowagie.text.pdf.PdfPCell c = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(text,
                    com.lowagie.text.FontFactory.getFont(com.lowagie.text.FontFactory.HELVETICA_BOLD, 9,
                            com.lowagie.text.Font.BOLD, java.awt.Color.WHITE)));
            c.setBackgroundColor(new java.awt.Color(0x00, 0xC8, 0x53));
            return c;
        }
}
