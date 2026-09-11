package com.example.sumayerestaurant.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import com.example.sumayerestaurant.util.ImageHelper;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartViewHolder> {

    public interface OnCartItemInstructionClickListener {
        void onEditInstruction(CartItem cartItem);
    }

    private final Context context;
    private final List<CartItem> cartItems = new ArrayList<>();
    private final CartManager cartManager;
    private final OnCartItemInstructionClickListener instructionClickListener;
    private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.US);

    public CartItemAdapter(Context context, OnCartItemInstructionClickListener instructionClickListener) {
        this.context = context;
        this.instructionClickListener = instructionClickListener;
        this.cartManager = CartManager.getInstance();
    }

    public void setItems(List<CartItem> items) {
        this.cartItems.clear();
        if (items != null) {
            this.cartItems.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.tvName.setText(item.getMenuItem().getName());
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
        holder.tvSubtotal.setText(String.format("TZS %s", currencyFormat.format(item.getSubtotal())));

        int fallbackRes = ImageHelper.getFoodFallbackDrawable(item.getMenuItem().getImageUrl(), item.getMenuItem().getName());
        String resolved = ImageHelper.resolveImageUrl(item.getMenuItem().getImageUrl());
        Object loadTarget = (resolved != null && !resolved.isEmpty()) ? resolved : fallbackRes;

        Glide.with(context)
                .load(loadTarget)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(new CenterCrop(), new RoundedCorners(12))
                .placeholder(fallbackRes)
                .error(fallbackRes)
                .into(holder.ivCartItemimage);

        if (!TextUtils.isEmpty(item.getSpecialInstructions())) {
            holder.tvInstructions.setText("✍️ " + item.getSpecialInstructions());
            holder.tvInstructions.setTextColor(context.getColor(R.color.error_color));
        } else {
            holder.tvInstructions.setText("✍️ Weka maelekezo ya pekee");
            holder.tvInstructions.setTextColor(context.getColor(R.color.primary));
        }

        holder.tvInstructions.setOnClickListener(v -> {
            if (instructionClickListener != null) {
                instructionClickListener.onEditInstruction(item);
            }
        });

        holder.btnPlus.setOnClickListener(v -> {
            cartManager.updateQuantity(item.getMenuItem().getId(), item.getQuantity() + 1);
        });

        holder.btnMinus.setOnClickListener(v -> {
            cartManager.updateQuantity(item.getMenuItem().getId(), item.getQuantity() - 1);
        });

        holder.btnRemove.setOnClickListener(v -> {
            cartManager.removeItem(item.getMenuItem().getId());
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivCartItemimage;
        TextView tvName;
        TextView tvInstructions;
        TextView tvQuantity;
        TextView tvSubtotal;
        ImageButton btnMinus;
        ImageButton btnPlus;
        ImageButton btnRemove;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCartItemimage = itemView.findViewById(R.id.ivCartItemImage);
            tvName = itemView.findViewById(R.id.tvCartItemName);
            tvInstructions = itemView.findViewById(R.id.tvCartItemInstructions);
            tvQuantity = itemView.findViewById(R.id.tvCartQuantity);
            tvSubtotal = itemView.findViewById(R.id.tvCartItemSubtotal);
            btnMinus = itemView.findViewById(R.id.btnCartMinus);
            btnPlus = itemView.findViewById(R.id.btnCartPlus);
            btnRemove = itemView.findViewById(R.id.btnRemoveCartItem);
        }
    }
}
