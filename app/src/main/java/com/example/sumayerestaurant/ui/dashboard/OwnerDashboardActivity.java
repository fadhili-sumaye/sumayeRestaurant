package com.example.sumayerestaurant.ui.dashboard;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.sumayerestaurant.R;
import com.example.sumayerestaurant.data.local.TokenManager;
import com.example.sumayerestaurant.data.model.OwnerBestSeller;
import com.example.sumayerestaurant.data.model.OwnerDashboard;
import com.example.sumayerestaurant.data.model.OwnerExpenseCategoryStat;
import com.example.sumayerestaurant.data.model.OwnerInventoryItem;
import com.example.sumayerestaurant.data.model.OwnerMonthlyPerformance;
import com.example.sumayerestaurant.data.model.OwnerPaymentMethodStat;
import com.example.sumayerestaurant.data.model.OwnerRiderRow;
import com.example.sumayerestaurant.data.model.OwnerStaffCountRow;
import com.example.sumayerestaurant.data.model.OwnerStaffPaymentRow;
import com.example.sumayerestaurant.data.model.OwnerStaffSalesRow;
import com.example.sumayerestaurant.data.model.OwnerSummary;
import com.example.sumayerestaurant.data.model.User;
import com.example.sumayerestaurant.data.repository.OwnerRepository;
import com.example.sumayerestaurant.data.websocket.StompClient;
import com.example.sumayerestaurant.data.websocket.WebSocketManager;
import com.example.sumayerestaurant.ui.customer.CustomerActivity;
import com.example.sumayerestaurant.ui.delivery.DeliveryActivity;
import com.example.sumayerestaurant.ui.finance.DailyClosingActivity;
import com.example.sumayerestaurant.ui.finance.ExpenseActivity;
import com.example.sumayerestaurant.ui.finance.ReportActivity;
import com.example.sumayerestaurant.ui.inventory.InventoryDashboardActivity;
import com.example.sumayerestaurant.ui.login.LoginActivity;
import com.example.sumayerestaurant.ui.reservation.ReservationActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OwnerDashboardActivity extends AppCompatActivity {

    private static final String[] PERIOD_LABELS = {"Leo", "Jana", "Wiki Hii", "Mwezi Huu", "Mwezi Uliopita", "Kipindi"};

    private static final int COLOR_ACCENT = 0xFFFF6B00;
    private static final int COLOR_RED = 0xFFFF5252;
    private static final int COLOR_AMBER = 0xFFFFD740;
    private static final int COLOR_WHITE = 0xFFFFFFFF;
    private static final int COLOR_MUTED = 0xFFBDBDBD;

    private TokenManager tokenManager;
    private User user;
    private Long branchId;
    private OwnerRepository repository;

    private LinearLayout contentContainer;
    private LinearLayout chipContainer;
    private TextView statusText;
    private ProgressBar loadingBar;
    private final List<MaterialButton> chips = new ArrayList<>();

    private LocalDate fromDate = LocalDate.now();
    private LocalDate toDate = LocalDate.now();
    private String groupBy = "DAY";
    private int selectedChip = 0;
    private boolean wsSubscribed = false;

    private final DecimalFormat moneyFmt = new DecimalFormat("#,##0.##");
    private final DecimalFormat pctFmt = new DecimalFormat("0.#");

    private static class Stat {
        final String label;
        final String value;
        final int color;
        Stat(String label, String value, int color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_owner);

        tokenManager = new TokenManager(this);
        user = (User) getIntent().getSerializableExtra("user");
        if (user == null) {
            user = tokenManager.getUser();
        }
        if (user != null) {
            branchId = user.getBranchId();
        }

        repository = new OwnerRepository(this);
        contentContainer = findViewById(R.id.contentContainer);
        chipContainer = findViewById(R.id.chipContainer);
        statusText = findViewById(R.id.statusText);
        loadingBar = findViewById(R.id.loadingBar);

        setupHeader();
        buildChips();
        setupBottomNav();
        loadDashboard();
        connectRealTime();
    }

    private void setupHeader() {
        TextView welcomeTextView = findViewById(R.id.welcomeTextView);
        TextView roleTextView = findViewById(R.id.roleTextView);

        String firstName = (user != null && user.getFirstName() != null) ? user.getFirstName() : "Mmiliki";
        welcomeTextView.setText("Karibu, " + firstName + "!");
        roleTextView.setText("Dashibodi ya Mmiliki");

        findViewById(R.id.btnResetStaffPassword).setOnClickListener(v ->
                com.example.sumayerestaurant.util.PasswordDialogHelper.showResetStaffPasswordDialog(this));
        com.example.sumayerestaurant.util.RoleMenuUtil.attach(this,
                findViewById(R.id.btnRoleMenu), this::logout);

        statusText.setOnClickListener(v -> loadDashboard());
    }

    // ---------------- Period chips ----------------

    private void buildChips() {
        chips.clear();
        for (int i = 0; i < PERIOD_LABELS.length; i++) {
            final int index = i;
            MaterialButton b = new MaterialButton(this);
            b.setText(PERIOD_LABELS[i]);
            b.setAllCaps(false);
            b.setTextSize(12);
            b.setMinHeight(dp(38));
            b.setMinimumWidth(0);
            b.setCornerRadius(dp(18));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(38), 1f);
            lp.setMargins(0, 0, dp(5), 0);
            chipContainer.addView(b, lp);
            b.setOnClickListener(v -> onChipTap(index));
            chips.add(b);
        }
        highlightChip(0);
    }

    private void onChipTap(int index) {
        LocalDate now = LocalDate.now();
        switch (index) {
            case 0:
                fromDate = now;
                toDate = now;
                groupBy = "DAY";
                break;
            case 1:
                fromDate = now.minusDays(1);
                toDate = now;
                groupBy = "DAY";
                break;
            case 2:
                fromDate = now.with(DayOfWeek.MONDAY);
                toDate = now;
                groupBy = "DAY";
                break;
            case 3:
                fromDate = YearMonth.from(now).atDay(1);
                toDate = now;
                groupBy = "DAY";
                break;
            case 4: {
                YearMonth prev = YearMonth.from(now).minusMonths(1);
                fromDate = prev.atDay(1);
                toDate = prev.atEndOfMonth();
                groupBy = "DAY";
                break;
            }
            case 5:
                pickStartDate();
                return;
        }
        highlightChip(index);
        loadDashboard();
    }

    private void pickStartDate() {
        new DatePickerDialog(this, (v, y, m, d) ->
                        pickEndDate(LocalDate.of(y, m + 1, d)),
                fromDate.getYear(), fromDate.getMonthValue() - 1, fromDate.getDayOfMonth()).show();
    }

    private void pickEndDate(final LocalDate start) {
        new DatePickerDialog(this, (v, y, m, d) -> {
            LocalDate end = LocalDate.of(y, m + 1, d);
            if (end.isBefore(start)) {
                Toast.makeText(this, "Tarehe ya mwisho haiwezi kuwa kabla ya mwanzo.", Toast.LENGTH_SHORT).show();
                return;
            }
            fromDate = start;
            toDate = end;
            long days = ChronoUnit.DAYS.between(start, end);
            groupBy = days <= 45 ? "DAY" : (days <= 240 ? "WEEK" : "MONTH");
            highlightChip(-1);
            loadDashboard();
        }, toDate.getYear(), toDate.getMonthValue() - 1, toDate.getDayOfMonth()).show();
    }

    private void highlightChip(int index) {
        selectedChip = index;
        for (int i = 0; i < chips.size(); i++) {
            MaterialButton b = chips.get(i);
            boolean active = i == index;
            b.setBackgroundTintList(ColorStateList.valueOf(active ? COLOR_ACCENT : 0xFF1E1E1E));
            b.setTextColor(active ? 0xFF050505 : COLOR_ACCENT);
            b.setStrokeColor(ColorStateList.valueOf(COLOR_ACCENT));
        }
    }

    // ---------------- Data loading ----------------

    private void loadDashboard() {
        if (branchId == null) {
            loadingBar.setVisibility(View.GONE);
            statusText.setText("Hakuna tawi husika kwa akaunti hii.");
            return;
        }
        loadingBar.setVisibility(View.VISIBLE);
        statusText.setText("Inapakia takwimu...");
        repository.getDashboard(branchId, fromDate.toString(), toDate.toString(), groupBy,
                new OwnerRepository.OwnerCallback<OwnerDashboard>() {
                    @Override
                    public void onSuccess(OwnerDashboard dashboard) {
                        loadingBar.setVisibility(View.GONE);
                        statusText.setText("Imesasishwa " + new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date())
                                + " (gusa kusasisha)");
                        render(dashboard);
                    }

                    @Override
                    public void onError(String message) {
                        loadingBar.setVisibility(View.GONE);
                        statusText.setText(message);
                        Toast.makeText(OwnerDashboardActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ---------------- Rendering ----------------

    private void render(OwnerDashboard d) {
        contentContainer.removeAllViews();
        if (d == null) {
            addEmptyNote("Hakuna data");
            return;
        }

        if (d.getPeriodLabel() != null && !d.getPeriodLabel().isEmpty()) {
            addSectionHeader("KIPINDI: " + d.getPeriodLabel().toUpperCase(Locale.US));
        }

        addSectionHeader("MUHTASARI");
        OwnerSummary s = d.getSummary();
        if (s != null) {
            List<Stat> stats = new ArrayList<>();
            stats.add(new Stat("Mauzo", money(s.getSales()), COLOR_ACCENT));
            stats.add(new Stat("Gharama", money(s.getExpenses()), COLOR_AMBER));
            BigDecimal profit = s.getProfit();
            if (profit != null && profit.signum() < 0) {
                stats.add(new Stat("Faida / Hasara", "-" + money(profit.abs()), COLOR_RED));
            } else {
                stats.add(new Stat("Faida / Hasara", money(profit), COLOR_ACCENT));
            }
            stats.add(new Stat("Maagizo Yote", String.valueOf(s.getTotalOrders()), COLOR_WHITE));
            stats.add(new Stat("Yanayoendelea", String.valueOf(s.getPendingOrders()), COLOR_AMBER));
            stats.add(new Stat("Yaliyokamilika", String.valueOf(s.getCompletedOrders()), COLOR_ACCENT));
            stats.add(new Stat("Yaliyoghairiwa", String.valueOf(s.getCancelledOrders()), COLOR_RED));
            stats.add(new Stat("Wastani wa Oda", money(s.getAvgOrderValue()), COLOR_MUTED));
            stats.add(new Stat("Chakula", money(s.getFoodSales()), COLOR_WHITE));
            stats.add(new Stat("Vinywaji", money(s.getBeverageSales()), COLOR_WHITE));
            stats.add(new Stat("Bidhaa/Engine", money(s.getOtherSales()), COLOR_WHITE));
            stats.add(new Stat("Stoo ya Chini", String.valueOf(s.getLowStockItems()), COLOR_AMBER));
            addStatGrid(stats);

            if (s.getSalesByCategory() != null && !s.getSalesByCategory().isEmpty()) {
                addSectionHeader("MAUZO KWA KATEGORIA");
                for (com.example.sumayerestaurant.data.model.OwnerCategorySales c : s.getSalesByCategory()) {
                    if (c.getCategory() != null) {
                        addValueRow(c.getCategory(), money(c.getSales()), COLOR_ACCENT);
                    }
                }
            }
        }

        // Sales chart
        addSectionHeader("MAUZO KWA KIPINDI");
        if (d.getSalesSeries() != null && !d.getSalesSeries().isEmpty()) {
            List<BarChartView.BarEntry> salesEntries = new ArrayList<>();
            for (com.example.sumayerestaurant.data.model.OwnerSeriesPoint p : d.getSalesSeries()) {
                BigDecimal r = p.getRevenue() != null ? p.getRevenue() : BigDecimal.ZERO;
                salesEntries.add(new BarChartView.BarEntry(p.getLabel(), r.floatValue(), COLOR_ACCENT));
            }
            BarChartView salesChart = new BarChartView(this);
            salesChart.setEntries(salesEntries);
            contentContainer.addView(salesChart, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(190)));
        } else {
            addEmptyNote("Hakuna mauzo kwa kipindi hiki");
        }

        // P&L chart
        addSectionHeader("FAIDA / HASARA");
        if (s != null) {
            List<BarChartView.BarEntry> pnlEntries = new ArrayList<>();
            BigDecimal revenue = s.getSales() != null ? s.getSales() : BigDecimal.ZERO;
            BigDecimal expenses = s.getExpenses() != null ? s.getExpenses() : BigDecimal.ZERO;
            BigDecimal profit = s.getProfit() != null ? s.getProfit() : BigDecimal.ZERO;
            pnlEntries.add(new BarChartView.BarEntry("Mauzo", revenue.floatValue(), COLOR_ACCENT));
            pnlEntries.add(new BarChartView.BarEntry("Gharama", expenses.floatValue(), COLOR_AMBER));
            pnlEntries.add(new BarChartView.BarEntry("Faida", profit.floatValue(),
                    profit.signum() < 0 ? COLOR_RED : 0));
            BarChartView pnlChart = new BarChartView(this);
            pnlChart.setEntries(pnlEntries);
            contentContainer.addView(pnlChart, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(190)));
        }

        // Payments
        addSectionHeader("MALIPO KWA NJIA");
        if (d.getPayments() != null && !d.getPayments().isEmpty()) {
            for (OwnerPaymentMethodStat p : d.getPayments()) {
                addValueRow(paymentName(p.getPaymentMethod(), p.getProvider()) + " (" + p.getCount() + ")",
                        money(p.getTotal()), COLOR_ACCENT);
            }
        } else {
            addEmptyNote("Hakuna malipo kwa kipindi hiki");
        }

        // Expenses by category
        if (d.getExpenses() != null) {
            addSectionHeader("GHARAMA KWA KUNDI");
            addValueRow("Jumla ya Gharama", money(d.getExpenses().getTotal()), COLOR_AMBER);
            addValueRow("Idadi ya Rekodi", String.valueOf(d.getExpenses().getCount()), COLOR_WHITE);
            if (d.getExpenses().getByCategory() != null) {
                for (OwnerExpenseCategoryStat c : d.getExpenses().getByCategory()) {
                    addPercentRow(c.getCategoryName() != null ? c.getCategoryName() : "Nyingine",
                            c.getAmount(), c.getPercent());
                }
            }
        }

        // Orders by status
        if (d.getOrders() != null) {
            addSectionHeader("MAAGIZO KWA HALI");
            addValueRow("Jumla", String.valueOf(d.getOrders().getTotal()), COLOR_WHITE);
            addValueRow("Zinazoendelea", String.valueOf(d.getOrders().getPending()), COLOR_AMBER);
            addValueRow("Zimekamilika", String.valueOf(d.getOrders().getCompleted()), COLOR_ACCENT);
            addValueRow("Zimeghairiwa", String.valueOf(d.getOrders().getCancelled()), COLOR_RED);
            if (d.getOrders().getByStatus() != null) {
                for (Map.Entry<String, Long> e : d.getOrders().getByStatus().entrySet()) {
                    if (e.getKey() == null) continue;
                    addValueRow(statusSwahili(e.getKey()), String.valueOf(e.getValue()), COLOR_MUTED);
                }
            }
            if (d.getOrders().getBusyHourLabel() != null) {
                addValueRow("Saa yenye shughuli nyingi", d.getOrders().getBusyHourLabel(), COLOR_ACCENT);
            }
        }

        // Best sellers
        addSectionHeader("VITOWEO VINAVYOUZWA ZAIDI");
        if (d.getBestSellers() != null && !d.getBestSellers().isEmpty()) {
            int rank = 1;
            for (OwnerBestSeller b : d.getBestSellers()) {
                String name = b.getName() != null ? b.getName() : ("#N/A-" + b.getMenuItemId());
                if (b.getCategoryName() != null) name += " · " + b.getCategoryName();
                addValueRow(rank + ". " + name + " (" + b.getQuantity() + ")",
                        money(b.getRevenue()), COLOR_ACCENT);
                rank++;
            }
        } else {
            addEmptyNote("Hakuna mauzo kwa kipindi hiki");
        }

        // Inventory
        if (d.getInventory() != null) {
            addSectionHeader("HALI YA STOO");
            List<Stat> invStats = new ArrayList<>();
            invStats.add(new Stat("Jumla ya Vipengee", String.valueOf(d.getInventory().getTotalItems()), COLOR_WHITE));
            invStats.add(new Stat("Chini ya Kiwango", String.valueOf(d.getInventory().getLowStock()), COLOR_AMBER));
            invStats.add(new Stat("Zilizokwisha", String.valueOf(d.getInventory().getOutOfStock()), COLOR_RED));
            invStats.add(new Stat("Thamani ya Stoo", money(d.getInventory().getStockValue()), COLOR_WHITE));
            invStats.add(new Stat("Matukio ya Taka", String.valueOf(d.getInventory().getWastageCount()), COLOR_AMBER));
            invStats.add(new Stat("Kiasi cha Taka", qty(d.getInventory().getWastageQuantity()), COLOR_AMBER));
            addStatGrid(invStats);

            if (d.getInventory().getRecentTransactions() != null && !d.getInventory().getRecentTransactions().isEmpty()) {
                addSectionHeader("MUHARAKATO WA HIVI KARIBUNI");
                int shown = 0;
                for (OwnerInventoryItem it : d.getInventory().getRecentTransactions()) {
                    if (shown >= 8) break;
                    String left = (it.getIngredientName() != null ? it.getIngredientName() : "")
                            + " · " + statusSwahili(it.getTransactionType())
                            + ": " + qty(it.getQuantityChange()) + (it.getUnit() != null ? " " + it.getUnit() : "");
                    addValueRow(left, timeShort(it.getCreatedAt()), COLOR_MUTED);
                    shown++;
                }
            }
        }

        // Staff performance
        if (d.getStaff() != null) {
            addSectionHeader("UTENDAJI WA WAHAKIKATI (WAITERS)");
            if (d.getStaff().getWaiters() == null || d.getStaff().getWaiters().isEmpty()) {
                addEmptyNote("Hakuna data");
            } else {
                for (OwnerStaffSalesRow w : d.getStaff().getWaiters()) {
                    addValueRow((w.getUsername() != null ? w.getUsername() : "-") + " (" + w.getOrders() + " oda)",
                            money(w.getSales()), COLOR_ACCENT);
                }
            }

            addSectionHeader("UTENDAJI WA WAKASHA");
            if (d.getStaff().getCashiers() == null || d.getStaff().getCashiers().isEmpty()) {
                addEmptyNote("Hakuna data");
            } else {
                for (OwnerStaffPaymentRow c : d.getStaff().getCashiers()) {
                    addValueRow((c.getUsername() != null ? c.getUsername() : "-") + " (" + c.getCount() + ")",
                            money(c.getTotal()), COLOR_ACCENT);
                }
            }

            addSectionHeader("JIKONI (KDS)");
            if (d.getStaff().getKitchen() == null || d.getStaff().getKitchen().isEmpty()) {
                addValueRow("Jumla ya Maagizo ya Jikoni", String.valueOf(d.getStaff().getKitchenOrdersTotal()), COLOR_WHITE);
            } else {
                for (OwnerStaffCountRow k : d.getStaff().getKitchen()) {
                    addValueRow(k.getUsername() != null ? k.getUsername() : "-",
                            String.valueOf(k.getCount()), COLOR_WHITE);
                }
            }

            addSectionHeader("WASAMBAZAJI (RIDERS)");
            if (d.getStaff().getRiders() != null && !d.getStaff().getRiders().isEmpty()) {
                for (OwnerRiderRow r : d.getStaff().getRiders()) {
                    addValueRow((r.getUsername() != null ? r.getUsername() : "-") + " (" + r.getDelivered() + ")",
                            money(r.getDeliveryFees()), COLOR_ACCENT);
                }
                addValueRow("Usafirishaji Uliokamilika", String.valueOf(d.getStaff().getDeliveriesCompleted()), COLOR_ACCENT);
            } else {
                addEmptyNote("Hakuna usafirishaji kwa kipindi hiki");
            }
        }

        // Monthly comparison
        if (d.getMonthly() != null && d.getMonthly().getMonthLabel() != null) {
            addSectionHeader("ULINGANISHO NA MWEZI ULIOPITA");
            OwnerMonthlyPerformance m = d.getMonthly();
            addValueRow("Mauzo: " + m.getMonthLabel(), money(m.getSales()), COLOR_ACCENT);
            addValueRow("Mauzo (Mwezi Uliopita)", money(m.getPreviousSales()), COLOR_MUTED);
            addValueRow("Mwendo wa Mauzo", pctSigned(m.getSalesGrowthPercent()),
                    m.getSalesGrowthPercent() != null && m.getSalesGrowthPercent().signum() < 0 ? COLOR_RED : COLOR_ACCENT);
            addValueRow("Maagizo / Ulinganisho", m.getOrders() + " vs " + m.getPreviousOrders(), COLOR_WHITE);
            addValueRow("Gharama", money(m.getExpenses()), COLOR_AMBER);
            addValueRow("Faida", money(m.getProfit()), COLOR_ACCENT);
            if (m.getBestSellingFood() != null) {
                addValueRow("Kitoweo Kikuu", m.getBestSellingFood(), COLOR_WHITE);
            }
            if (m.getBestSellingCategory() != null) {
                addValueRow("Kategoria Bora", m.getBestSellingCategory(), COLOR_WHITE);
            }
        }

        addSectionHeader("RIPOTI NA UHAMISHO (EXPORT)");
        MaterialButton csvBtn = new MaterialButton(this);
        csvBtn.setText("Pakua Ripoti kama Excel (CSV)");
        csvBtn.setAllCaps(false);
        csvBtn.setTextSize(13);
        csvBtn.setBackgroundTintList(ColorStateList.valueOf(0xFF1E1E1E));
        csvBtn.setTextColor(COLOR_ACCENT);
        csvBtn.setStrokeColor(ColorStateList.valueOf(COLOR_ACCENT));
        csvBtn.setStrokeWidth(1);
        csvBtn.setCornerRadius(dp(12));
        csvBtn.setOnClickListener(v -> exportReport(false));
        contentContainer.addView(csvBtn, fullWidthMargin(8));

        MaterialButton pdfBtn = new MaterialButton(this);
        pdfBtn.setText("Pakua Ripoti kama PDF");
        pdfBtn.setAllCaps(false);
        pdfBtn.setTextSize(13);
        pdfBtn.setBackgroundTintList(ColorStateList.valueOf(COLOR_ACCENT));
        pdfBtn.setTextColor(0xFF050505);
        pdfBtn.setCornerRadius(dp(12));
        pdfBtn.setOnClickListener(v -> exportReport(true));
        contentContainer.addView(pdfBtn, fullWidthMargin(6));

        addSectionHeader("MENU ZINGINE");
        addModuleButtons();
    }

    private LinearLayout.LayoutParams fullWidthMargin(int topDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(topDp);
        return lp;
    }

    // ---------------- Export ----------------

    private void exportReport(final boolean pdf) {
        if (branchId == null) return;
        loadingBar.setVisibility(View.VISIBLE);
        statusText.setText(pdf ? "Inatayarisha PDF..." : "Inatayarisha CSV...");
        repository.exportReport(branchId, fromDate.toString(), toDate.toString(),
                pdf ? "pdf" : "csv", new OwnerRepository.ExportCallback() {
                    @Override
                    public void onSuccess(File file) {
                        loadingBar.setVisibility(View.GONE);
                        statusText.setText("Ripoti imetayarishwa.");
                        shareFile(file);
                    }

                    @Override
                    public void onError(String message) {
                        loadingBar.setVisibility(View.GONE);
                        statusText.setText(message);
                        Toast.makeText(OwnerDashboardActivity.this, message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void shareFile(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(this, "com.example.sumayerestaurant.fileprovider", file);
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType(file.getName().endsWith(".pdf") ? "application/pdf" : "text/csv");
            i.putExtra(Intent.EXTRA_STREAM, uri);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(i, "Tuma ripoti"));
        } catch (Exception e) {
            Toast.makeText(this, "Imeshindwa kushiriki faili: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ---------------- Module navigation ----------------

    private void addModuleButtons() {
        String[] labels = {
                "Ripoti", "Kufunga Siku", "Gharama", "Stoo",
                "Kash & Billing", "Huduma Meza", "Jikoni (KDS)", "Wateja",
                "Mihimili (Reservation)", "Usafirishaji"
        };
        Class<?>[] targets = {
                ReportActivity.class, DailyClosingActivity.class, ExpenseActivity.class,
                InventoryDashboardActivity.class, CashierDashboardActivity.class,
                WaiterDashboardActivity.class, KitchenDashboardActivity.class,
                CustomerActivity.class, ReservationActivity.class, DeliveryActivity.class
        };

        for (int i = 0; i < labels.length; i += 2) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            row.setGravity(Gravity.TOP);
            addModuleButton(row, labels[i], targets[i], true);
            if (i + 1 < labels.length) {
                addModuleButton(row, labels[i + 1], targets[i + 1], false);
            }
            contentContainer.addView(row);
        }
    }

    private void addModuleButton(LinearLayout row, String label, final Class<?> target, boolean start) {
        MaterialButton b = new MaterialButton(this);
        b.setText(label);
        b.setTextSize(11);
        b.setAllCaps(false);
        b.setMinHeight(dp(44));
        b.setBackgroundTintList(ColorStateList.valueOf(0xFF1E1E1E));
        b.setTextColor(COLOR_ACCENT);
        b.setStrokeColor(ColorStateList.valueOf(COLOR_ACCENT));
        b.setStrokeWidth(1);
        b.setCornerRadius(dp(10));
        b.setOnClickListener(v -> {
            Intent intent = new Intent(OwnerDashboardActivity.this, target);
            if (target == CashierDashboardActivity.class || target == WaiterDashboardActivity.class
                    || target == KitchenDashboardActivity.class) {
                intent.putExtra("user", user);
            }
            startActivity(intent);
        });
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(46), 1f);
        lp.setMarginStart(start ? dp(6) : dp(3));
        lp.setMarginEnd(start ? dp(3) : dp(6));
        lp.topMargin = dp(4);
        lp.bottomMargin = dp(4);
        row.addView(b, lp);
    }

    // ---------------- UI helpers ----------------

    private void addSectionHeader(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(COLOR_ACCENT);
        t.setTextSize(13);
        t.setTypeface(t.getTypeface(), Typeface.BOLD);
        t.setPadding(0, dp(14), 0, dp(6));
        contentContainer.addView(t);
    }

    private void addEmptyNote(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextColor(COLOR_MUTED);
        t.setTextSize(13);
        t.setGravity(Gravity.CENTER);
        t.setPadding(0, dp(8), 0, dp(8));
        contentContainer.addView(t);
    }

    private void addValueRow(String left, String right, int color) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(3), 0, dp(3));
        TextView l = new TextView(this);
        l.setText(left);
        l.setTextColor(COLOR_WHITE);
        l.setTextSize(13);
        TextView r = new TextView(this);
        r.setText(right);
        r.setTextColor(color);
        r.setTextSize(14);
        r.setTypeface(r.getTypeface(), Typeface.BOLD);
        row.addView(l, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(r, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        contentContainer.addView(row);
    }

    private void addPercentRow(String category, BigDecimal amount, BigDecimal percent) {
        LinearLayout wrap = new LinearLayout(this);
        wrap.setOrientation(LinearLayout.VERTICAL);
        wrap.setPadding(0, dp(4), 0, dp(4));
        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView l = new TextView(this);
        l.setText(category);
        l.setTextColor(COLOR_WHITE);
        l.setTextSize(13);
        TextView r = new TextView(this);
        r.setText(money(amount));
        r.setTextColor(COLOR_AMBER);
        r.setTextSize(13);
        r.setTypeface(r.getTypeface(), Typeface.BOLD);
        top.addView(l, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(r, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        wrap.addView(top);

        LinearLayout barRow = new LinearLayout(this);
        barRow.setOrientation(LinearLayout.HORIZONTAL);
        barRow.setGravity(Gravity.CENTER_VERTICAL);
        barRow.setPadding(0, dp(4), 0, 0);
        ProgressBar bar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        bar.setMax(100);
        int pct = percent == null ? 0 : (int) Math.round(percent.doubleValue());
        bar.setProgress(pct);
        bar.setProgressTintList(ColorStateList.valueOf(COLOR_ACCENT));
        bar.setProgressBackgroundTintList(ColorStateList.valueOf(0xFF303030));
        TextView pctTv = new TextView(this);
        pctTv.setText(pct + "%");
        pctTv.setTextColor(COLOR_MUTED);
        pctTv.setTextSize(11);
        pctTv.setPadding(dp(8), 0, 0, 0);
        barRow.addView(bar, new LinearLayout.LayoutParams(0, dp(6), 1f));
        barRow.addView(pctTv, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        wrap.addView(barRow);
        contentContainer.addView(wrap);
    }

    private void addStatGrid(List<Stat> stats) {
        if (stats == null || stats.isEmpty()) return;
        for (int i = 0; i < stats.size(); i += 2) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.TOP);
            addStatCard(row, stats.get(i), true);
            if (i + 1 < stats.size()) {
                addStatCard(row, stats.get(i + 1), false);
            }
            contentContainer.addView(row, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }
    }

    private void addStatCard(LinearLayout row, Stat s, boolean start) {
        MaterialCardView card = new MaterialCardView(this);
        card.setCardBackgroundColor(0xFF1E1E1E);
        card.setStrokeWidth(1);
        card.setStrokeColor(ColorStateList.valueOf(0xFF242424));
        card.setRadius(dp(12));
        card.setUseCompatPadding(true);
        card.setPreventCornerOverlap(true);
        card.setCardElevation(0f);

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(dp(12), dp(12), dp(12), dp(12));
        TextView v = new TextView(this);
        v.setText(s.value != null ? s.value : "-");
        v.setTextColor(s.color);
        v.setTextSize(17);
        v.setTypeface(v.getTypeface(), Typeface.BOLD);
        TextView l = new TextView(this);
        l.setText(s.label);
        l.setTextColor(COLOR_MUTED);
        l.setTextSize(10);
        l.setPadding(0, dp(4), 0, 0);
        inner.addView(v);
        inner.addView(l);
        card.addView(inner);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        lp.topMargin = dp(5);
        lp.bottomMargin = dp(5);
        if (start) {
            lp.setMarginStart(0);
            lp.setMarginEnd(dp(6));
        } else {
            lp.setMarginStart(dp(6));
            lp.setMarginEnd(0);
        }
        row.addView(card, lp);
    }

    // ---------------- Real-time ----------------

    private void connectRealTime() {
        if (wsSubscribed || branchId == null) return;
        wsSubscribed = true;
        WebSocketManager wsm = WebSocketManager.getInstance(this);
        wsm.connect();
        StompClient.MessageListener reload = (destination, payload) -> runOnUiThread(this::loadDashboard);
        wsm.subscribe("/topic/branches/" + branchId + "/orders", reload);
        wsm.subscribe("/topic/branches/" + branchId + "/cashier", reload);
        wsm.subscribe("/topic/branches/" + branchId + "/inventory", reload);
        wsm.subscribe("/topic/branches/" + branchId + "/management", reload);
    }

    @Override
    protected void onDestroy() {
        if (branchId != null) {
            WebSocketManager wsm = WebSocketManager.getInstance(this);
            wsm.unsubscribe("/topic/branches/" + branchId + "/orders");
            wsm.unsubscribe("/topic/branches/" + branchId + "/cashier");
            wsm.unsubscribe("/topic/branches/" + branchId + "/inventory");
            wsm.unsubscribe("/topic/branches/" + branchId + "/management");
        }
        super.onDestroy();
    }

    // ---------------- Formatting ----------------

    private String money(BigDecimal v) {
        if (v == null) return "TZS 0";
        return "TZS " + moneyFmt.format(v);
    }

    private String qty(BigDecimal v) {
        if (v == null) return "0";
        BigDecimal abs = v.abs();
        BigDecimal val = v;
        String prefix = v.signum() < 0 ? "-" : "";
        if (abs.doubleValue() >= 1000000.0) {
            return prefix + moneyFmt.format(v.divide(new BigDecimal("1000000"), 1, java.math.RoundingMode.HALF_UP)) + "M";
        }
        return val.toString();
    }

    private String pctSigned(BigDecimal v) {
        if (v == null) return "";
        double d = v.doubleValue();
        return (d > 0 ? "+" : "") + pctFmt.format(d) + "%";
    }

    private String timeShort(String iso) {
        if (iso == null || iso.isEmpty()) return "";
        try {
            String raw = iso.length() > 19 ? iso.substring(0, 19) : iso;
            LocalDateTime dt;
            if (raw.length() == 19) {
                dt = LocalDateTime.parse(raw);
            } else {
                dt = LocalDate.parse(raw).atStartOfDay();
            }
            return dt.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
        } catch (Exception e) {
            return iso;
        }
    }

    private String paymentName(String method, String provider) {
        if (method == null) return "Nyingine";
        String m = method.toUpperCase(Locale.US);
        String p = provider != null ? provider.toUpperCase(Locale.US) : "";
        if (m.equals("MOBILE_MONEY")) {
            if (p.contains("MPESA")) return "M-Pesa";
            if (p.contains("AIRTEL")) return "Airtel Money";
            if (p.contains("TIGO")) return "Tigo Pesa";
            if (p.contains("HALOPESA")) return "HaloPesa";
            return "Pesa ya Simu";
        }
        if (m.equals("CASH")) return "Taslimu";
        if (m.equals("CARD")) return "Kadi";
        if (m.equals("BANK_TRANSFER") || m.equals("BANK")) return "Benki";
        if (m.equals("OTHER")) return "Nyingine";
        return m;
    }

    private String statusSwahili(String s) {
        if (s == null) return "";
        switch (s.toUpperCase(Locale.US)) {
            case "PENDING": return "Inasubiri";
            case "PAYMENT_PENDING": return "Malipo yanasubiri";
            case "PAID": return "Imelipwa";
            case "PREPARING":
            case "IN_KITCHEN": return "Inatayarishwa";
            case "READY": return "Iko tayari";
            case "SERVED": return "Imehudumiwa";
            case "COMPLETED": return "Imekamilika";
            case "CANCELLED": return "Imeghairiwa";
            case "STOCK_IN": return "Ongezeko la stoo";
            case "STOCK_OUT": return "Matumizi ya stoo";
            case "WASTAGE": return "Taka";
            case "WASTE": return "Taka";
            case "ADJUSTMENT": return "Marekebisho";
            case "PURCHASE": return "Ununuzi";
            default: return s;
        }
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_reports) {
                startActivity(new Intent(this, ReportActivity.class));
                return true;
            } else if (id == R.id.nav_expenses) {
                startActivity(new Intent(this, ExpenseActivity.class));
                return true;
            } else if (id == R.id.nav_inventory) {
                startActivity(new Intent(this, InventoryDashboardActivity.class));
                return true;
            }
            return true;
        });
    }

    private void logout() {
        tokenManager.clearAll();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private int dp(float v) {
        return Math.round(getResources().getDisplayMetrics().density * v);
    }
}