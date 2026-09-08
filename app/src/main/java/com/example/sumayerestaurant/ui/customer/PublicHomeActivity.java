package com.example.sumayerestaurant.ui.customer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sumayerestaurant.R;
import com.example.sumayerestaurant.data.local.TokenManager;
import com.example.sumayerestaurant.data.model.MenuItem;
import com.example.sumayerestaurant.data.repository.MenuRepository;
import com.example.sumayerestaurant.ui.adapter.PublicMenuAdapter;
import com.example.sumayerestaurant.ui.dashboard.DashboardActivity;
import com.example.sumayerestaurant.ui.login.LoginActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Public restaurant home screen - the first screen shown when the app opens.
 * Features a modern hero banner, category filter chips, and a grid of food/drink cards.
 * No authentication required to view this screen.
 * 
 * Hamburger menu (top-left) provides access to the staff Login screen.
 */
public class PublicHomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private NavigationView navigationView;
    private RecyclerView menuRecyclerView;
    private ProgressBar progressBar;
    private TextView errorTextView;
    private TextView emptyTextView;
    private View emptyLayout;
    private ChipGroup categoryChipGroup;
    private TextView sectionTitleTextView;

    private PublicMenuAdapter menuAdapter;
    private MenuRepository menuRepository;
    private TokenManager tokenManager;

    private List<MenuItem> allMenuItems = new ArrayList<>();
    private String selectedCategory = "Vyote"; // "All" in Swahili

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_home);

        // Initialize views
        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.navigationView);
        menuRecyclerView = findViewById(R.id.menuRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        errorTextView = findViewById(R.id.errorTextView);
        emptyTextView = findViewById(R.id.emptyTextView);
        emptyLayout = findViewById(R.id.emptyLayout);
        categoryChipGroup = findViewById(R.id.categoryChipGroup);
        sectionTitleTextView = findViewById(R.id.sectionTitleTextView);

        // Setup toolbar with hamburger icon
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("SUMAYE RESTAURANT");
        }

        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Setup drawer navigation
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_login) {
                drawerLayout.closeDrawer(GravityCompat.START);
                if (tokenManager.isLoggedIn()) {
                    startActivity(new Intent(this, DashboardActivity.class));
                } else {
                    startActivity(new Intent(this, LoginActivity.class));
                }
                return true;
            }
            return false;
        });

        // Initialize components
        tokenManager = new TokenManager(this);
        menuRepository = new MenuRepository(this);

        // Setup RecyclerView with 2-column grid
        menuAdapter = new PublicMenuAdapter(this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        menuRecyclerView.setLayoutManager(gridLayoutManager);
        menuRecyclerView.setAdapter(menuAdapter);

        // Update drawer menu based on login state
        updateDrawerMenu();

        // Load menu from backend
        loadPublicMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDrawerMenu();
    }

    private void updateDrawerMenu() {
        if (navigationView.getMenu() != null) {
            navigationView.getMenu().clear();
        }
        if (tokenManager.isLoggedIn()) {
            navigationView.getMenu().add(0, R.id.nav_login, 0, "Dashboard")
                    .setIcon(R.drawable.ic_login);
        } else {
            navigationView.getMenu().add(0, R.id.nav_login, 0, "Login")
                    .setIcon(R.drawable.ic_login);
        }
    }

    private void loadPublicMenu() {
        showLoading(true);
        hideError();
        emptyLayout.setVisibility(View.GONE);

        menuRepository.getPublicMenu(new MenuRepository.MenuCallback<List<MenuItem>>() {
            @Override
            public void onSuccess(List<MenuItem> result) {
                runOnUiThread(() -> {
                    showLoading(false);
                    if (result == null || result.isEmpty()) {
                        emptyLayout.setVisibility(View.VISIBLE);
                        menuRecyclerView.setVisibility(View.GONE);
                        categoryChipGroup.setVisibility(View.GONE);
                    } else {
                        emptyLayout.setVisibility(View.GONE);
                        menuRecyclerView.setVisibility(View.VISIBLE);
                        allMenuItems = result;

                        // Build category chips from actual data
                        buildCategoryChips(result);

                        // Apply initial filter
                        filterByCategory(selectedCategory);
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showError(message);
                });
            }
        });
    }

    private void buildCategoryChips(List<MenuItem> items) {
        categoryChipGroup.removeAllViews();

        // Collect unique categories
        Set<String> categories = new LinkedHashSet<>();
        categories.add("Vyote"); // "All"
        for (MenuItem item : items) {
            if (item.getCategory() != null && item.getCategory().getName() != null) {
                categories.add(item.getCategory().getName());
            }
        }

        // Create chips
        for (String category : categories) {
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setCheckable(true);
            chip.setChecked(category.equals(selectedCategory));
            chip.setCheckedIconVisible(false);
            chip.setChipBackgroundColorResource(
                    category.equals(selectedCategory) ? R.color.primary : R.color.card_secondary
            );
            chip.setTextColor(getColor(R.color.text_primary));

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedCategory = category;
                    filterByCategory(category);
                    updateChipStyles();
                }
            });

            categoryChipGroup.addView(chip);
        }

        // Set initial "All" as checked
        if (categoryChipGroup.getChildCount() > 0) {
            ((Chip) categoryChipGroup.getChildAt(0)).setChecked(true);
        }
    }

    private void updateChipStyles() {
        for (int i = 0; i < categoryChipGroup.getChildCount(); i++) {
            Chip chip = (Chip) categoryChipGroup.getChildAt(i);
            if (chip.getText().toString().equals(selectedCategory)) {
                chip.setChipBackgroundColorResource(R.color.primary);
                chip.setTextColor(getColor(R.color.black));
            } else {
                chip.setChipBackgroundColorResource(R.color.card_secondary);
                chip.setTextColor(getColor(R.color.text_primary));
            }
        }
    }

    private void filterByCategory(String category) {
        List<MenuItem> filtered;
        if ("Vyote".equals(category)) {
            filtered = allMenuItems;
            sectionTitleTextView.setText("Orodha ya Vyakula");
        } else {
            filtered = new ArrayList<>();
            for (MenuItem item : allMenuItems) {
                if (item.getCategory() != null && category.equals(item.getCategory().getName())) {
                    filtered.add(item);
                }
            }
            sectionTitleTextView.setText(category);
        }

        if (filtered.isEmpty()) {
            emptyLayout.setVisibility(View.VISIBLE);
            menuRecyclerView.setVisibility(View.GONE);
        } else {
            emptyLayout.setVisibility(View.GONE);
            menuRecyclerView.setVisibility(View.VISIBLE);
            menuAdapter.setItems(filtered);
        }
    }

    private void showLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void showError(String message) {
        errorTextView.setText(message);
        errorTextView.setVisibility(View.VISIBLE);
        menuRecyclerView.setVisibility(View.GONE);
    }

    private void hideError() {
        errorTextView.setVisibility(View.GONE);
    }
}
