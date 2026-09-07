package com.example.sumayerestaurant.ui.qr;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sumayerestaurant.data.api.RetrofitClient;
import com.example.sumayerestaurant.data.model.Order;
import com.example.sumayerestaurant.data.model.OrderItemRequest;
import com.example.sumayerestaurant.data.model.QrMenuItem;
import com.example.sumayerestaurant.data.model.QrOrderRequest;
import com.example.sumayerestaurant.data.model.QrTableMenu;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Customer-only, password-free flow launched by sumaye://order/{opaque-table-token}. */
public class QrOrderActivity extends AppCompatActivity {
    private final Map<Long, Integer> quantities = new LinkedHashMap<>();
    private final NumberFormat currency = NumberFormat.getNumberInstance(Locale.US);

    private String token;
    private String requestId;
    private TextView heading;
    private LinearLayout menu;
    private EditText notes;
    private Button submit;
    private ProgressBar progress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildScreen();
        openLink(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        openLink(intent);
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 32, 32, 48);
        scroll.addView(root);

        heading = text("Inapakia menyu...", 22);
        root.addView(heading);
        TextView guidance = text("Chagua vyakula kisha tuma oda yako moja kwa moja jikoni.", 15);
        guidance.setPadding(0, 8, 0, 20);
        root.addView(guidance);

        progress = new ProgressBar(this);
        progress.setIndeterminate(true);
        root.addView(progress);

        menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        root.addView(menu);

        notes = new EditText(this);
        notes.setHint("Maelekezo ya oda (si lazima)");
        notes.setMinLines(2);
        notes.setGravity(Gravity.TOP);
        LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        noteParams.topMargin = 24;
        root.addView(notes, noteParams);

        submit = new Button(this);
        submit.setText("Tuma Oda");
        submit.setEnabled(false);
        LinearLayout.LayoutParams submitParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        submitParams.topMargin = 16;
        root.addView(submit, submitParams);
        submit.setOnClickListener(v -> submitOrder());
        setContentView(scroll);
    }

    private void openLink(Intent intent) {
        Uri data = intent == null ? null : intent.getData();
        List<String> segments = data == null ? null : data.getPathSegments();
        if (data == null || !"sumaye".equals(data.getScheme()) || !"order".equals(data.getHost())
                || segments == null || segments.size() != 1 || segments.get(0).length() < 32) {
            invalidLink();
            return;
        }
        token = segments.get(0);
        quantities.clear();
        requestId = UUID.randomUUID().toString();
        loadMenu();
    }

    private void loadMenu() {
        progress.setVisibility(View.VISIBLE);
        submit.setEnabled(false);
        menu.removeAllViews();
        RetrofitClient.getApiService(this).getQrMenu(token).enqueue(new Callback<QrTableMenu>() {
            @Override public void onResponse(Call<QrTableMenu> call, Response<QrTableMenu> response) {
                progress.setVisibility(View.GONE);
                QrTableMenu body = response.body();
                if (!response.isSuccessful() || body == null) {
                    unavailable();
                    return;
                }
                heading.setText(body.getRestaurantName() + " • " + body.getBranchName()
                        + "\nMeza " + body.getTableNumber());
                renderMenu(body.getMenuItems());
            }
            @Override public void onFailure(Call<QrTableMenu> call, Throwable error) {
                progress.setVisibility(View.GONE);
                heading.setText("Menyu haikupatikana");
                toast("Hakuna muunganisho wa mtandao. Tafadhali jaribu tena.");
            }
        });
    }

    private void renderMenu(List<QrMenuItem> items) {
        if (items == null || items.isEmpty()) {
            menu.addView(text("Hakuna chakula kinachopatikana kwa sasa.", 16));
            return;
        }
        for (QrMenuItem item : items) {
            menu.addView(menuRow(item));
        }
    }

    private View menuRow(QrMenuItem item) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(0, 18, 0, 18);
        row.addView(text(item.getName() + " — TZS " + currency.format(item.getPrice()), 18));
        if (item.getDescription() != null && !item.getDescription().isBlank()) {
            row.addView(text(item.getDescription(), 14));
        }
        LinearLayout controls = new LinearLayout(this);
        controls.setGravity(Gravity.CENTER_VERTICAL);
        Button remove = new Button(this);
        remove.setText("−");
        TextView count = text("0", 18);
        count.setGravity(Gravity.CENTER);
        Button add = new Button(this);
        add.setText("+");
        controls.addView(remove);
        controls.addView(count, new LinearLayout.LayoutParams(96, LinearLayout.LayoutParams.WRAP_CONTENT));
        controls.addView(add);
        remove.setOnClickListener(v -> changeQuantity(item.getId(), -1, count));
        add.setOnClickListener(v -> changeQuantity(item.getId(), 1, count));
        row.addView(controls);
        return row;
    }

    private void changeQuantity(Long itemId, int change, TextView count) {
        int updated = Math.max(0, quantities.getOrDefault(itemId, 0) + change);
        if (updated == 0) quantities.remove(itemId); else quantities.put(itemId, updated);
        count.setText(String.valueOf(updated));
        submit.setEnabled(!quantities.isEmpty());
    }

    private void submitOrder() {
        List<OrderItemRequest> items = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            items.add(new OrderItemRequest(entry.getKey(), entry.getValue(), null));
        }
        if (items.isEmpty()) return;
        submit.setEnabled(false);
        submit.setText("Inatuma oda...");
        QrOrderRequest request = new QrOrderRequest(requestId, notes.getText().toString().trim(), items);
        RetrofitClient.getApiService(this).createQrOrder(token, request).enqueue(new Callback<Order>() {
            @Override public void onResponse(Call<Order> call, Response<Order> response) {
                if (response.isSuccessful() && response.body() != null) {
                    showSuccess(response.body());
                } else {
                    submit.setText("Tuma Oda");
                    submit.setEnabled(true);
                    toast("Oda haikutumwa. Hakikisha meza bado inapatikana.");
                }
            }
            @Override public void onFailure(Call<Order> call, Throwable error) {
                submit.setText("Tuma Oda");
                submit.setEnabled(true);
                toast("Hakuna muunganisho wa mtandao. Tafadhali jaribu tena.");
            }
        });
    }

    private TextView text(String value, int size) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        return view;
    }

    private void invalidLink() {
        heading.setText("Msimbo wa QR si sahihi");
        new AlertDialog.Builder(this).setMessage("Tafadhali skani QR code ya meza tena.")
                .setPositiveButton("Funga", (d, w) -> finish()).setCancelable(false).show();
    }

    private void unavailable() {
        heading.setText("Msimbo wa QR haupatikani");
        toast("Tafadhali muombe mhudumu akusaidie.");
    }

    private void showSuccess(Order order) {
        new AlertDialog.Builder(this)
                .setTitle("Oda imetumwa")
                .setMessage("Oda " + order.getOrderNumber() + " imetumwa jikoni. Jumla: TZS "
                        + currency.format(order.getTotalAmount()))
                .setPositiveButton("Sawa", (d, w) -> finish())
                .setCancelable(false)
                .show();
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
