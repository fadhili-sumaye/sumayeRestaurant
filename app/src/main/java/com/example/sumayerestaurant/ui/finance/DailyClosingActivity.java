package com.example.sumayerestaurant.ui.finance;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.sumayerestaurant.data.api.RetrofitClient;
import com.example.sumayerestaurant.data.local.TokenManager;
import com.example.sumayerestaurant.data.model.DailyClosing;
import com.example.sumayerestaurant.data.model.User;
import com.example.sumayerestaurant.util.ThemeUtil;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Records the physical cash count and closes one completed business day. */
public class DailyClosingActivity extends AppCompatActivity {
    private Long branchId;
    private EditText businessDate;
    private EditText actualCash;
    private EditText differenceReason;
    private EditText notes;
    private TextView summary;
    private Button closeButton;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        User user = new TokenManager(this).getUser();
        branchId = (user == null || user.getBranchId() == null) ? 1L : user.getBranchId();
        setTitle("Funga siku ya biashara");

        LinearLayout root = new LinearLayout(this);
        root.setPadding(32, 32, 32, 32);
        root.setOrientation(LinearLayout.VERTICAL);
        ThemeUtil.applyRootTheme(root);
        TextView guidance = new TextView(this);
        guidance.setText("Hesabu fedha taslimu zilizopo kabla ya kufunga siku. Tofauti yoyote lazima ielezwe.");
        ThemeUtil.styleLabel(guidance, 14); guidance.setTextColor(ThemeUtil.textSecondary(this));
        root.addView(guidance);
        businessDate = field("Tarehe (YYYY-MM-DD)");
        businessDate.setText(LocalDate.now().toString());
        actualCash = field("Fedha taslimu zilizohesabiwa (TZS)");
        actualCash.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        differenceReason = field("Sababu ya tofauti, kama ipo");
        notes = field("Maelezo ya ziada (si lazima)");
        notes.setMinLines(2);
        root.addView(businessDate);
        root.addView(actualCash);
        root.addView(differenceReason);
        root.addView(notes);
        closeButton = new Button(this);
        closeButton.setText("Thibitisha kufunga siku");
        ThemeUtil.stylePrimaryButton(closeButton);
        root.addView(closeButton);
        summary = new TextView(this);
        ThemeUtil.styleLabel(summary, 16); summary.setTextColor(ThemeUtil.greenBright(this));
        root.addView(summary);
        setContentView(root);
        closeButton.setOnClickListener(view -> confirm());
        loadExistingClosing();
    }

    private EditText field(String hint) {
        EditText field = new EditText(this);
        field.setHint(hint);
        ThemeUtil.styleEditText(field);
        return field;
    }

    private void loadExistingClosing() {
        if (branchId == null) {
            message("Tawi halijapatikana.");
            return;
        }
        RetrofitClient.getApiService(this).getDailyClosing(branchId, businessDate.getText().toString()).enqueue(new Callback<DailyClosing>() {
            @Override public void onResponse(Call<DailyClosing> call, Response<DailyClosing> response) {
                if (response.isSuccessful() && response.body() != null) showClosed(response.body());
            }
            @Override public void onFailure(Call<DailyClosing> call, Throwable throwable) { }
        });
    }

    private void confirm() {
        if (branchId == null || businessDate.getText().toString().trim().isEmpty()
                || actualCash.getText().toString().trim().isEmpty()) {
            message("Jaza tarehe na kiasi cha fedha taslimu.");
            return;
        }
        try {
            new BigDecimal(actualCash.getText().toString().trim());
            LocalDate.parse(businessDate.getText().toString().trim());
        } catch (Exception exception) {
            message("Weka tarehe na kiasi sahihi.");
            return;
        }
        new AlertDialog.Builder(this)
                .setMessage("Kufunga siku hakuwezi kurekebishwa. Una uhakika una kiasi sahihi cha fedha taslimu?")
                .setNegativeButton("Ghairi", null)
                .setPositiveButton("Funga siku", (dialog, which) -> submit())
                .show();
    }

    private void submit() {
        Map<String, Object> request = new HashMap<>();
        request.put("businessDate", businessDate.getText().toString().trim());
        request.put("actualCash", actualCash.getText().toString().trim());
        request.put("differenceReason", differenceReason.getText().toString().trim());
        request.put("notes", notes.getText().toString().trim());
        closeButton.setEnabled(false);
        RetrofitClient.getApiService(this).closeDay(branchId, request).enqueue(new Callback<DailyClosing>() {
            @Override public void onResponse(Call<DailyClosing> call, Response<DailyClosing> response) {
                closeButton.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    showClosed(response.body());
                    message("Siku imefungwa kwa mafanikio.");
                } else {
                    message("Imeshindikana kufunga siku. Hakikisha sababu ya tofauti imejazwa.");
                }
            }
            @Override public void onFailure(Call<DailyClosing> call, Throwable throwable) {
                closeButton.setEnabled(true);
                message("Hakuna mtandao. Tafadhali jaribu tena.");
            }
        });
    }

    private void showClosed(DailyClosing closing) {
        closeButton.setEnabled(false);
        actualCash.setEnabled(false);
        businessDate.setEnabled(false);
        differenceReason.setEnabled(false);
        notes.setEnabled(false);
        summary.setText("SIKU IMEFUNGWA\n\nMauzo: TZS " + closing.getTotalSales()
                + "\nCash: TZS " + closing.getCashSales()
                + "\nMobile money: TZS " + closing.getMobileMoneySales()
                + "\nKadi/Benki/Nyingine: TZS " + closing.getCardSales() + " / "
                + closing.getBankSales() + " / " + closing.getOtherSales()
                + "\nGharama: TZS " + closing.getExpenses()
                + "\nCash inayotarajiwa: TZS " + closing.getExpectedCash());
    }

    private void message(String value) {
        Toast.makeText(this, value, Toast.LENGTH_LONG).show();
    }
}
