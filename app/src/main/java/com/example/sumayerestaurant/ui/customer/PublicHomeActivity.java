package com.example.sumayerestaurant.ui.customer;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.example.sumayerestaurant.R;
import com.example.sumayerestaurant.data.local.CartManager;
import com.example.sumayerestaurant.data.model.MenuItem;
import com.example.sumayerestaurant.data.repository.MenuRepository;
import com.example.sumayerestaurant.ui.adapter.PublicMenuAdapter;
import com.example.sumayerestaurant.util.ImageHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Customer home screen - the first screen shown when the app opens.
 * Dark food-delivery style UI with a hero card, search bar, category pills,
 * photo grid and bottom navigation (Home / Search / Cart / Profile).
 */
public class PublicHomeActivity extends AppCompatActivity {

    private RecyclerView menuRecyclerView;
    private ProgressBar progressBar;
    private TextView errorTextView;
    private TextView emptyTextView;
    private View emptyLayout;
    private ChipGroup categoryChipGroup;
    private TextView sectionTitleTextView;
    private EditText searchEditText;
    private View filterButton;
    private ImageView filterIcon;
    private ImageView heroImage;
    private TextView heroBadge;
    private TextView heroTitle;
    private TextView heroPrep;
    private TextView heroPrice;
    private View heroAddButton;
    private BottomNavigationView bottomNavigation;

    private PublicMenuAdapter menuAdapter;
    private MenuRepository menuRepository;
    private CartManager cartManager;

    private final List<MenuItem> allMenuItems = new ArrayList<>();
    private String selectedCategory = "Vyote";
    private boolean availableOnly = false;
    private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_home);

        // Initialize views
        menuRecyclerView = findViewById(R.id.menuRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        errorTextView = findViewById(R.id.errorTextView);
        emptyTextView = findViewById(R.id.emptyTextView);
        emptyLayout = findViewById(R.id.emptyLayout);
        categoryChipGroup = findViewById(R.id.categoryChipGroup);
        sectionTitleTextView = findViewById(R.id.sectionTitleTextView);
        searchEditText = findViewById(R.id.searchEditText);
        filterButton = findViewById(R.id.filterButton);
        filterIcon = findViewById(R.id.filterIcon);
        heroImage = findViewById(R.id.heroImage);
        heroBadge = findViewById(R.id.heroBadge);
        heroTitle = findViewById(R.id.heroTitle);
        heroPrep = findViewById(R.id.heroPrep);
        heroPrice = findViewById(R.id.heroPrice);
        heroAddButton = findViewById(R.id.heroAddButton);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        cartManager = CartManager.getInstance();
        menuRepository = new MenuRepository(this);

        // Setup RecyclerView with Pinterest-style staggered 2-column grid
        menuAdapter = new PublicMenuAdapter(this, new PublicMenuAdapter.ItemClickListener() {
            @Override
            public void onItemClick(MenuItem item) {
                FoodDetailActivity.start(PublicHomeActivity.this, item);
            }

            @Override
            public void onAddClick(MenuItem item) {
                if (!item.isAvailable()) {
                    Toast.makeText(PublicHomeActivity.this, "Chakula hiki haipatikani leo", Toast.LENGTH_SHORT).show();
                    return;
                }
                cartManager.addItem(item, 1, null);
                Toast.makeText(PublicHomeActivity.this, "Imeongezwa kwenye kikapu", Toast.LENGTH_SHORT).show();
            }
        });
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        menuRecyclerView.setLayoutManager(layoutManager);
        menuRecyclerView.setAdapter(menuAdapter);
        menuRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@androidx.annotation.NonNull android.graphics.Rect outRect, @androidx.annotation.NonNull android.view.View view, @androidx.annotation.NonNull RecyclerView parent, @androidx.annotation.NonNull RecyclerView.State state) {
                outRect.set(6, 6, 6, 6);
            }
        });

        setupSearch();
        setupHero();
        setupBottomNavigation();
        setupFilterButton();

        // Load menu from backend
        loadPublicMenu();
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupHero() {
        heroAddButton.setOnClickListener(v -> {
            MenuItem heroItem = getHeroItem();
            if (heroItem == null) return;
            if (!heroItem.isAvailable()) {
                Toast.makeText(this, "Chakula hiki haipatikani leo", Toast.LENGTH_SHORT).show();
                return;
            }
            cartManager.addItem(heroItem, 1, null);
            Toast.makeText(this, "Imeongezwa kwenye kikapu", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupFilterButton() {
        filterButton.setOnClickListener(v -> {
            availableOnly = !availableOnly;
            updateFilterButtonStyle();
            applyFilters();
        });
    }

    private void updateFilterButtonStyle() {
        if (availableOnly) {
            filterButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFF6B00));
            filterIcon.setColorFilter(0xFF050505);
        } else {
            filterButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getColor(R.color.food_accent_soft)));
            filterIcon.setColorFilter(getColor(R.color.food_accent));
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            }
            if (id == R.id.nav_search) {
                searchEditText.requestFocus();
            } else if (id == R.id.nav_cart) {
                startActivity(new android.content.Intent(this, CustomerCartActivity.class));
            } else if (id == R.id.nav_profile) {
                startActivity(new android.content.Intent(this, CustomerProfileActivity.class));
            }
            // Keep Home highlighted since the other tabs open separate screens.
            bottomNavigation.getMenu().findItem(R.id.nav_home).setChecked(true);
            return true;
        });
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
                        allMenuItems.clear();
                        allMenuItems.addAll(result);

                        // Build category chips from actual data
                        buildCategoryChips(result);

                        // Apply initial filter
                        applyFilters();
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
            updateChipStyle(chip, category);

            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedCategory = category;
                    applyFilters();
                    updateAllChipStyles();
                }
            });

            categoryChipGroup.addView(chip);
        }

        // Set initial "All" as checked
        if (categoryChipGroup.getChildCount() > 0) {
            ((Chip) categoryChipGroup.getChildAt(0)).setChecked(true);
        }
    }

    private void updateAllChipStyles() {
        for (int i = 0; i < categoryChipGroup.getChildCount(); i++) {
            updateChipStyle((Chip) categoryChipGroup.getChildAt(i),
                    ((Chip) categoryChipGroup.getChildAt(i)).getText().toString());
        }
    }

    private void updateChipStyle(Chip chip, String category) {
        if (category.equals(selectedCategory)) {
            chip.setChipBackgroundColorResource(R.color.food_accent);
            chip.setTextColor(getColor(R.color.food_on_accent));
        } else {
            chip.setChipBackgroundColorResource(R.color.card_secondary);
            chip.setTextColor(getColor(R.color.text_primary));
        }
    }

    private void applyFilters() {
        String query = searchEditText.getText().toString().trim().toLowerCase();
        List<MenuItem> base = new ArrayList<>();
        for (MenuItem item : allMenuItems) {
            boolean categoryMatches = "Vyote".equals(selectedCategory)
                    || (item.getCategory() != null && selectedCategory.equals(item.getCategory().getName()));
            if (!categoryMatches) continue;

            boolean queryMatches = query.isEmpty()
                    || (item.getName() != null && item.getName().toLowerCase().contains(query))
                    || (item.getCategory() != null && item.getCategory().getName() != null
                        && item.getCategory().getName().toLowerCase().contains(query));
            if (!queryMatches) continue;

            if (availableOnly && !item.isAvailable()) continue;

            base.add(item);
        }

        if ("Vyote".equals(selectedCategory)) {
            sectionTitleTextView.setText(query.isEmpty() ? "Menu" : "Matokeo ya Utafutaji");
        } else {
            sectionTitleTextView.setText(query.isEmpty() ? selectedCategory : "Matokeo ya Utafutaji");
        }

        if (base.isEmpty()) {
            emptyLayout.setVisibility(View.VISIBLE);
            menuRecyclerView.setVisibility(View.GONE);
            heroImage.setImageResource(R.drawable.placeholder_food);
            heroTitle.setText("Bado hakuna chakula");
            heroPrep.setText("Jaribu utafutaji mwingine.");
            heroPrice.setText("");
            heroBadge.setVisibility(View.GONE);
        } else {
            emptyLayout.setVisibility(View.GONE);
            menuRecyclerView.setVisibility(View.VISIBLE);
            menuAdapter.setItems(base);
            bindHero(base.get(0));
        }
    }

    private MenuItem getHeroItem() {
        List<MenuItem> items = menuAdapter.getItems();
        if (items.isEmpty()) return null;
        for (MenuItem item : items) {
            if (item.isAvailable()) return item;
        }
        return items.get(0);
    }

    private void bindHero(MenuItem item) {
        heroTitle.setText(item.getName());
        heroPrice.setText(String.format("TZS %s", currencyFormat.format(item.getPrice())));
        heroBadge.setVisibility(View.VISIBLE);

        if (item.getPreparationTimeMinutes() != null && item.getPreparationTimeMinutes() > 0) {
            String cat = item.getCategory() != null && item.getCategory().getName() != null
                    ? item.getCategory().getName() : "Sumaye Special";
            heroPrep.setText(String.format("%s · Dakika %d", cat, item.getPreparationTimeMinutes()));
        } else {
            heroPrep.setText(item.getCategory() != null && item.getCategory().getName() != null
                    ? item.getCategory().getName() : "");
        }

        int fallbackRes = ImageHelper.getFoodFallbackDrawable(item.getImageUrl(), item.getName());
        String resolved = ImageHelper.resolveImageUrl(item.getImageUrl());
        Object loadTarget = (resolved != null && !resolved.isEmpty()) ? resolved : fallbackRes;

        Glide.with(this)
                .load(loadTarget)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(new CenterCrop())
                .placeholder(fallbackRes)
                .error(fallbackRes)
                .into(heroImage);

        heroImage.setOnClickListener(v -> FoodDetailActivity.start(this, item));
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