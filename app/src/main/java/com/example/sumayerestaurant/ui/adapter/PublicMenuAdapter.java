package com.example.sumayerestaurant.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.sumayerestaurant.R;
import com.example.sumayerestaurant.data.model.MenuItem;
import com.example.sumayerestaurant.util.Constants;
import com.example.sumayerestaurant.util.ImageHelper;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Pinterest-style staggered grid card adapter for the public customer menu.
 * Cards show photo with varying heights, name, category, prep time and price
 * with a floating add-to-cart button.
 */
public class PublicMenuAdapter extends RecyclerView.Adapter<PublicMenuAdapter.MenuViewHolder> {

    public interface ItemClickListener {
        void onItemClick(MenuItem item);
        void onAddClick(MenuItem item);
    }

    private final Context context;
    private final ItemClickListener listener;
    private final List<MenuItem> menuItems = new ArrayList<>();
    private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.US);
    private int lastAnimatedPosition = -1;

    private static final int[] HEIGHT_VARIANTS = {120, 140, 160, 130, 150};

    public PublicMenuAdapter(Context context, ItemClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setItems(List<MenuItem> items) {
        this.menuItems.clear();
        if (items != null) {
            this.menuItems.addAll(items);
        }
        lastAnimatedPosition = -1;
        notifyDataSetChanged();
    }

    public List<MenuItem> getItems() {
        return new ArrayList<>(menuItems);
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_public_menu, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        MenuItem item = menuItems.get(position);

        holder.itemNameTextView.setText(item.getName());
        holder.itemPriceTextView.setText(String.format("TZS %s", currencyFormat.format(item.getPrice())));

        // Pinterest waterfall effect: vary image heights by position
        int variant = HEIGHT_VARIANTS[position % HEIGHT_VARIANTS.length];
        ViewGroup.LayoutParams params = holder.itemImage.getLayoutParams();
        params.height = (int) (variant * context.getResources().getDisplayMetrics().density);
        holder.itemImage.setLayoutParams(params);

        // Category name
        if (item.getCategory() != null && item.getCategory().getName() != null) {
            holder.itemCategoryTextView.setText(item.getCategory().getName());
            holder.itemCategoryTextView.setVisibility(View.VISIBLE);
        } else {
            holder.itemCategoryTextView.setVisibility(View.GONE);
        }

        // Prep time
        if (item.getPreparationTimeMinutes() != null && item.getPreparationTimeMinutes() > 0) {
            holder.itemPrepTextView.setText(String.format("Dakika %d", item.getPreparationTimeMinutes()));
        } else {
            holder.itemPrepTextView.setText("Tayari kwa haraka");
        }

        // Availability badge (overlay on image)
        if (item.isAvailable()) {
            holder.availabilityBadge.setText("Inapatikana");
            holder.availabilityBadge.setTextColor(context.getColor(R.color.white));
            holder.availabilityBadge.setBackgroundResource(R.drawable.bg_badge_available);
            holder.itemView.setAlpha(1.0f);
        } else {
            holder.availabilityBadge.setText("Haipatikani");
            holder.availabilityBadge.setTextColor(context.getColor(R.color.white));
            holder.availabilityBadge.setBackgroundResource(R.drawable.bg_badge_occupied);
            holder.itemView.setAlpha(0.7f);
        }

        // Load image with local high-res food photo fallback and crossfade animation
        int fallbackRes = ImageHelper.getFoodFallbackDrawable(item.getImageUrl(), item.getName());
        String resolved = resolveImageUrl(item.getImageUrl());
        Object loadTarget = (resolved != null && !resolved.isEmpty()) ? resolved : fallbackRes;

        Glide.with(context)
                .load(loadTarget)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(new CenterCrop(), new RoundedCorners(16))
                .placeholder(fallbackRes)
                .error(fallbackRes)
                .transition(DrawableTransitionOptions.withCrossFade(300))
                .into(holder.itemImage);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
        holder.addButton.setOnClickListener(v -> {
            if (listener != null) listener.onAddClick(item);
        });

        // Card entry animation
        animateCard(holder.itemView, position);
    }

    /**
     * Resolves the image URL using ImageHelper.
     */
    public static String resolveImageUrl(String url) {
        return ImageHelper.resolveImageUrl(url);
    }

    private void animateCard(View view, int position) {
        if (position > lastAnimatedPosition) {
            view.setAlpha(0f);
            view.setTranslationY(40f);
            view.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(350)
                    .setStartDelay(position * 50L)
                    .start();
            lastAnimatedPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    static class MenuViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemNameTextView;
        TextView itemCategoryTextView;
        TextView itemPrepTextView;
        TextView itemPriceTextView;
        TextView availabilityBadge;
        View addButton;

        MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            itemCategoryTextView = itemView.findViewById(R.id.itemCategoryTextView);
            itemPrepTextView = itemView.findViewById(R.id.itemPrepTextView);
            itemPriceTextView = itemView.findViewById(R.id.itemPriceTextView);
            availabilityBadge = itemView.findViewById(R.id.availabilityBadge);
            addButton = itemView.findViewById(R.id.addButton);
        }
    }
}