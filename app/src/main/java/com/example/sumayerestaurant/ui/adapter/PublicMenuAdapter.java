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
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.sumayerestaurant.R;
import com.example.sumayerestaurant.data.model.MenuItem;
import com.example.sumayerestaurant.util.Constants;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PublicMenuAdapter extends RecyclerView.Adapter<PublicMenuAdapter.MenuViewHolder> {

    private final Context context;
    private final List<MenuItem> menuItems = new ArrayList<>();
    private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.US);
    private int lastAnimatedPosition = -1;

    public PublicMenuAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<MenuItem> items) {
        this.menuItems.clear();
        if (items != null) {
            this.menuItems.addAll(items);
        }
        lastAnimatedPosition = -1;
        notifyDataSetChanged();
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

        // Category name
        if (item.getCategory() != null) {
            holder.itemCategoryTextView.setText(item.getCategory().getName());
            holder.itemCategoryTextView.setVisibility(View.VISIBLE);
        } else {
            holder.itemCategoryTextView.setVisibility(View.GONE);
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

        // Load image with crossfade animation and rounded corners
        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(resolveImageUrl(item.getImageUrl()))
                    .transform(new CenterCrop(), new RoundedCorners(24))
                    .placeholder(R.drawable.placeholder_food)
                    .error(R.drawable.placeholder_food)
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .into(holder.itemImage);
        } else {
            holder.itemImage.setImageResource(R.drawable.placeholder_food);
        }

        // Card entry animation
        animateCard(holder.itemView, position);
    }

    /**
     * The API stores relative paths (e.g. "/images/pilau-kuku.png") so data stays
     * portable across servers; Glide needs an absolute URL, so prefix the active
     * backend base URL when one is missing.
     */
    private String resolveImageUrl(String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url;
        }
        String base = Constants.BASE_URL;
        if (base.endsWith("/") && url.startsWith("/")) {
            return base.substring(0, base.length() - 1) + url;
        }
        if (!base.endsWith("/") && !url.startsWith("/")) {
            return base + "/" + url;
        }
        return base + url;
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
        TextView itemPriceTextView;
        TextView availabilityBadge;

        MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            itemNameTextView = itemView.findViewById(R.id.itemNameTextView);
            itemCategoryTextView = itemView.findViewById(R.id.itemCategoryTextView);
            itemPriceTextView = itemView.findViewById(R.id.itemPriceTextView);
            availabilityBadge = itemView.findViewById(R.id.availabilityBadge);
        }
    }
}
