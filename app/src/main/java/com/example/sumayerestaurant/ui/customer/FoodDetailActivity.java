package com.example.sumayerestaurant.ui.customer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.sumayerestaurant.R;
import com.example.sumayerestaurant.data.local.CartManager;
import com.example.sumayerestaurant.data.model.MenuItem;
import com.example.sumayerestaurant.ui.adapter.PublicMenuAdapter;
import com.example.sumayerestaurant.util.ImageHelper;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Food detail screen with a large hero image, floating back/favorite buttons,
 * quantity selector and an orange "Add to Cart" button.
 */
public class FoodDetailActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM = "extra_item";

    private CartManager cartManager;
    private MenuItem item;
    private int quantity = 1;
    private boolean favorite = false;
    private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.US);

    public static void start(Context context, MenuItem item) {
        Intent intent = new Intent(context, FoodDetailActivity.class);
        intent.putExtra(EXTRA_ITEM, item);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        cartManager = CartManager.getInstance();

        if (getIntent() != null && getIntent().hasExtra(EXTRA_ITEM)) {
            item = (MenuItem) getIntent().getSerializableExtra(EXTRA_ITEM);
        }
        if (item == null) {
            finish();
            return;
        }

        ImageView detailImage = findViewById(R.id.detailImage);
        TextView detailTitle = findViewById(R.id.detailTitle);
        TextView detailCategory = findViewById(R.id.detailCategory);
        TextView detailPrep = findViewById(R.id.detailPrep);
        TextView detailPrice = findViewById(R.id.detailPrice);
        TextView detailDescription = findViewById(R.id.detailDescription);
        TextView detailAvailability = findViewById(R.id.detailAvailability);
        TextView quantityTextView = findViewById(R.id.quantityTextView);
        View backButton = findViewById(R.id.backButton);
        View favButton = findViewById(R.id.favButton);
        ImageView heartIcon = findViewById(R.id.favHeartIcon);
        View minusButton = findViewById(R.id.minusButton);
        View plusButton = findViewById(R.id.plusButton);
        View addToCartButton = findViewById(R.id.addToCartButton);

        detailTitle.setText(item.getName());
        detailPrice.setText(String.format("TZS %s", currencyFormat.format(item.getPrice())));
        detailCategory.setText(item.getCategory() != null && item.getCategory().getName() != null
                ? item.getCategory().getName() : "Sumaye Special");

        if (item.getPreparationTimeMinutes() != null && item.getPreparationTimeMinutes() > 0) {
            detailPrep.setText(String.format("Dakika %d za maandalizi", item.getPreparationTimeMinutes()));
        } else {
            detailPrep.setText("Tayari kwa haraka");
        }

        detailDescription.setText(item.getDescription() != null && !item.getDescription().trim().isEmpty()
                ? item.getDescription()
                : "Chakula kizuri cha nyumbani kutoka jikoni la Sumaye Restaurant.");

        if (item.isAvailable()) {
            detailAvailability.setText("Inapatikana leo");
            detailAvailability.setTextColor(getColor(R.color.success_color));
        } else {
            detailAvailability.setText("Haipatikani leo");
            detailAvailability.setTextColor(getColor(R.color.error_color));
        }

        int fallbackRes = ImageHelper.getFoodFallbackDrawable(item.getImageUrl(), item.getName());
        String resolved = ImageHelper.resolveImageUrl(item.getImageUrl());
        Object loadTarget = (resolved != null && !resolved.isEmpty()) ? resolved : fallbackRes;

        Glide.with(this)
                .load(loadTarget)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(new CenterCrop(), new RoundedCorners(28))
                .placeholder(fallbackRes)
                .error(fallbackRes)
                .into(detailImage);

        backButton.setOnClickListener(v -> finish());

        favButton.setOnClickListener(v -> {
            favorite = !favorite;
            heartIcon.setColorFilter(favorite ? getColor(R.color.food_accent) : getColor(R.color.white));
            Toast.makeText(this, favorite ? "Imehifadhiwa kwenye pipendwa" : "Imeondolewa kwenye pipendwa", Toast.LENGTH_SHORT).show();
        });

        plusButton.setOnClickListener(v -> {
            if (quantity < 30) {
                quantity++;
                quantityTextView.setText(String.valueOf(quantity));
            }
        });

        minusButton.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                quantityTextView.setText(String.valueOf(quantity));
            }
        });

        addToCartButton.setOnClickListener(v -> {
            if (!item.isAvailable()) {
                Toast.makeText(this, "Chakula hiki haipatikani leo", Toast.LENGTH_SHORT).show();
                return;
            }
            cartManager.addItem(item, quantity, null);
            Toast.makeText(this, "Imeongezwa kwenye kikapu", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}