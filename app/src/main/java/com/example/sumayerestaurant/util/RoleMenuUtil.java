package com.example.sumayerestaurant.util;

import android.app.Activity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import com.example.sumayerestaurant.R;
import com.google.android.material.appbar.MaterialToolbar;

public class RoleMenuUtil {

    public static void attach(Activity activity, ImageButton menuButton, Runnable onLogout) {
        if (menuButton == null) return;
        menuButton.setOnClickListener(v -> showMenu(activity, menuButton, onLogout));
    }

    public static void attachToToolbar(Activity activity, MaterialToolbar toolbar, Runnable onLogout) {
        if (toolbar == null) return;
        toolbar.setNavigationOnClickListener(v -> showMenu(activity, v, onLogout));
    }

    public static void showMenu(Activity activity, View anchor, Runnable onLogout) {
        PopupMenu popup = new PopupMenu(activity, anchor);
        popup.getMenuInflater().inflate(R.menu.menu_role_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_change_password) {
                PasswordDialogHelper.showChangePasswordDialog(activity);
                return true;
            } else if (id == R.id.action_logout) {
                if (onLogout != null) onLogout.run();
                return true;
            }
            return false;
        });
        popup.show();
    }
}