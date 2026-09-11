package com.example.sumayerestaurant.ui.customer;

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
import com.example.sumayerestaurant.R;
import com.example.sumayerestaurant.data.local.CartManager;
import com.example.sumayerestaurant.data.model.CartItem;
import com.example.sumayerestaurant.ui.adapter.PublicMenuAdapter;
import com.example.sumayerestaurant.util.ImageHelper;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Cart line items with quantity controls and a remove button.
 */
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final Context context;
    private final CartManager cartManager;
    private final List<CartItem> items = new ArrayList<>();
    private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.US);

    public CartAdapter(Context context, CartManager cartManager) {
        this.context = context;
        this.cartManager = cartManager;
    }

    public void setItems(List<CartItem> cartItems) {
        this.items.clear();
        if (cartItems != null) {
            this.items.addAll(cartItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = items.get(position);
        com.example.sumayerestaurant.data.model.MenuItem menuItem = cartItem.getMenuItem();

        holder.cartItemName.setText(menuItem.getName());
        holder.cartItemPrice.setText(String.format("TZS %s", currencyFormat.format(cartItem.getSubtotal())));
        holder.cartItemQty.setText(String.valueOf(cartItem.getQuantity()));

        int fallbackRes = ImageHelper.getFoodFallbackDrawable(menuItem.getImageUrl(), menuItem.getName());
        String resolved = ImageHelper.resolveImageUrl(menuItem.getImageUrl());
        Object loadTarget = (resolved != null && !resolved.isEmpty()) ? resolved : fallbackRes;

        Glide.with(context)
                .load(loadTarget)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(new CenterCrop(), new RoundedCorners(14))
                .placeholder(fallbackRes)
                .error(fallbackRes)
                .into(holder.cartItemImage);

        holder.plusButton.setOnClickListener(v ->
                cartManager.updateQuantity(menuItem.getId(), cartItem.getQuantity() + 1));

        holder.minusButton.setOnClickListener(v ->
                cartManager.updateQuantity(menuItem.getId(), cartItem.getQuantity() - 1));

        holder.removeButton.setOnClickListener(v -> cartManager.removeItem(menuItem.getId()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView cartItemImage;
        TextView cartItemName;
        TextView cartItemPrice;
        TextView cartItemQty;
        View plusButton;
        View minusButton;
        View removeButton;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            cartItemImage = itemView.findViewById(R.id.cartItemImage);
            cartItemName = itemView.findViewById(R.id.cartItemName);
            cartItemPrice = itemView.findViewById(R.id.cartItemPrice);
            cartItemQty = itemView.findViewById(R.id.cartItemQty);
            plusButton = itemView.findViewById(R.id.plusButton);
            minusButton = itemView.findViewById(R.id.minusButton);
            removeButton = itemView.findViewById(R.id.removeButton);
        }
    }
}