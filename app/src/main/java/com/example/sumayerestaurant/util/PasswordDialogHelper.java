package com.example.sumayerestaurant.util;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.sumayerestaurant.data.api.RetrofitClient;
import com.example.sumayerestaurant.data.model.ChangePasswordRequest;
import com.example.sumayerestaurant.data.model.ResetPasswordRequest;
import com.example.sumayerestaurant.data.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PasswordDialogHelper {

    /**
     * Shows a dialog allowing any logged-in user to change their password.
     */
    public static void showChangePasswordDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("🔑 Badilisha Nenosiri");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        final EditText etCurrentPassword = new EditText(context);
        etCurrentPassword.setHint("Nenosiri la sasa");
        etCurrentPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etCurrentPassword);

        final EditText etNewPassword = new EditText(context);
        etNewPassword.setHint("Nenosiri jipya (angalau herufi 6)");
        etNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etNewPassword);

        final EditText etConfirmPassword = new EditText(context);
        etConfirmPassword.setHint("Rudia nenosiri jipya");
        etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etConfirmPassword);

        builder.setView(layout);

        builder.setPositiveButton("Hifadhi", null);
        builder.setNegativeButton("Ghairi", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Override to prevent auto-close on validation failure
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String current = etCurrentPassword.getText().toString().trim();
            String newPass = etNewPassword.getText().toString().trim();
            String confirm = etConfirmPassword.getText().toString().trim();

            if (current.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(context, "Tafadhali jaza sehemu zote.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPass.length() < 6) {
                Toast.makeText(context, "Nenosiri jipya lazima liwe na herufi 6 au zaidi.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirm)) {
                Toast.makeText(context, "Manenosiri mapya hayafanani.", Toast.LENGTH_SHORT).show();
                return;
            }

            ChangePasswordRequest request = new ChangePasswordRequest(current, newPass);
            RetrofitClient.getApiService(context).changePassword(request).enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        String msg = response.body() != null && response.body().containsKey("message")
                                ? response.body().get("message") : "Nenosiri limebadilishwa kwa mafanikio.";
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(context, "Nenosiri la sasa si sahihi au hitilafu imetokea.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    Toast.makeText(context, "Hitilafu ya mtandao: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    /**
     * Allows Admin/Manager to reset a staff member's password (Option A).
     */
    public static void showResetStaffPasswordDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("👤 Weka Upya Nenosiri la Mfanyakazi");

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48, 24, 48, 24);

        TextView label = new TextView(context);
        label.setText("Chagua mfanyakazi anayehitaji nenosiri jipya:");
        layout.addView(label);

        ProgressBar spinnerLoading = new ProgressBar(context);
        layout.addView(spinnerLoading);

        Spinner staffSpinner = new Spinner(context);
        staffSpinner.setVisibility(View.GONE);
        layout.addView(staffSpinner);

        EditText etNewPassword = new EditText(context);
        etNewPassword.setHint("Nenosiri jipya (mf. 123456 au nenosiri maalum)");
        etNewPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etNewPassword);

        builder.setView(layout);
        builder.setPositiveButton("Weka Upya", null);
        builder.setNegativeButton("Ghairi", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        final List<String> usernames = new ArrayList<>();
        final List<String> displayNames = new ArrayList<>();

        RetrofitClient.getApiService(context).getStaff().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                spinnerLoading.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    staffSpinner.setVisibility(View.VISIBLE);
                    for (User u : response.body()) {
                        usernames.add(u.getUsername());
                        String role = (u.getRoles() != null && !u.getRoles().isEmpty())
                                ? u.getRoles().iterator().next().replace("ROLE_", "") : "STAFF";
                        displayNames.add(u.getUsername() + " (" + u.getFirstName() + " - " + role + ")");
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, displayNames);
                    staffSpinner.setAdapter(adapter);
                } else {
                    Toast.makeText(context, "Imeshindikana kupakia orodha ya wafanyakazi.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                spinnerLoading.setVisibility(View.GONE);
                Toast.makeText(context, "Hitilafu ya mtandao: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (usernames.isEmpty() || staffSpinner.getSelectedItemPosition() < 0) {
                Toast.makeText(context, "Orodha ya wafanyakazi haijakamilika.", Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedUsername = usernames.get(staffSpinner.getSelectedItemPosition());
            String newPassword = etNewPassword.getText().toString().trim();

            if (newPassword.length() < 6) {
                Toast.makeText(context, "Nenosiri jipya lazima liwe na angalau herufi 6.", Toast.LENGTH_SHORT).show();
                return;
            }

            ResetPasswordRequest request = new ResetPasswordRequest(selectedUsername, newPassword);
            RetrofitClient.getApiService(context).resetPassword(request).enqueue(new Callback<Map<String, String>>() {
                @Override
                public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                    if (response.isSuccessful()) {
                        String msg = response.body() != null && response.body().containsKey("message")
                                ? response.body().get("message") : "Nenosiri limewekwa upya kwa mafanikio!";
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(context, "Imeshindikana kuweka upya nenosiri.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Map<String, String>> call, Throwable t) {
                    Toast.makeText(context, "Hitilafu ya mtandao: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    /**
     * Shows a helpful dialog on the login screen explaining the Option A reset flow.
     */
    public static void showForgotPasswordHelpDialog(Context context) {
        new AlertDialog.Builder(context)
                .setTitle("❓ Umesahau Nenosiri?")
                .setMessage("Je, umesahau nenosiri lako la kazi kwenye SUMAYE Karamu?\n\n" +
                        "Kwa sababu za kiusalama za mfumo wa mgahawa, tafadhali wasiliana na Meneja (Manager) au Msimamizi (Admin) wa tawi lako.\n\n" +
                        "Meneja ataingia kwenye mfumo na kukuwekea nenosiri jipya mara moja kupitia sehemu ya 'Weka Upya Nenosiri la Mfanyakazi'.")
                .setPositiveButton("Sawa, Nimeelewa", null)
                .show();
    }
}
