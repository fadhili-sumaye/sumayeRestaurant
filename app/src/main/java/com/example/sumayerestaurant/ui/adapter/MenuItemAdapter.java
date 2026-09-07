package com.example.sumayerestaurant.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sumayerestaurant.R;
import com.example.sumayerestaurant.data.local.CartManager;
import com.example.sumayerestaurant.data.model.CartItem;
import com.example.sumayerestaurant.data.model.MenuItem;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MenuItemAdapter extends RecyclerView.Adapter<MenuItemAdapter.MenuViewHolder> {

    public interface OnItemInstructionClickListener {
        void onInstructionClick(MenuItem item, String currentInstructions);
    }

    private final Context context;
    private final List<MenuItem> menuItems = new ArrayList<>();
    private final CartManager cartManager;
    private final OnItemInstructionClickListener instructionClickListener;
    private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.US);

    public MenuItemAdapter(Context context, OnItemInstructionClickListener instructionClickListener) {
        this.context = context;
        this.instructionClickListener = instructionClickListener;
        this.cartManager = CartManager.getInstance();
    }

    public void setItems(List<MenuItem> items) {
        this.menuItems.clear();
        if (items != null) {
            this.menuItems.addAll(items);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_menu_item, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        MenuItem item = menuItems.get(position);

        holder.tvFoodName.setText(item.getName());
        holder.tvFoodDescription.setText(item.getDescription() != null ? item.getDescription() : "");
        holder.tvFoodDescription.setVisibility(TextUtils.isEmpty(item.getDescription()) ? View.GONE : View.VISIBLE);
        holder.tvFoodPrice.setText(String.format("TZS %s", currencyFormat.format(item.getPrice())));

        if (!item.isAvailable()) {
            holder.tvAvailabilityBadge.setVisibility(View.VISIBLE);
            holder.layoutQuantityControls.setVisibility(View.GONE);
            holder.tvAddSpecialNote.setVisibility(View.GONE);
            holder.tvSpecialInstructionsPreview.setVisibility(View.GONE);
            holder.cardMenuItem.setAlpha(0.6f);
            return;
        }

        holder.cardMenuItem.setAlpha(1.0f);
        holder.tvAvailabilityBadge.setVisibility(View.GONE);
        holder.layoutQuantityControls.setVisibility(View.VISIBLE);
        holder.tvAddSpecialNote.setVisibility(View.VISIBLE);

        // Find existing quantity and instructions in cart
        int currentQty = 0;
        String currentInstructions = "";
        for (CartItem ci : cartManager.getItems()) {
            if (ci.getMenuItem().getId().equals(item.getId())) {
                currentQty = ci.getQuantity();
                currentInstructions = ci.getSpecialInstructions();
                break;
            }
        }

        holder.tvQuantity.setText(String.valueOf(currentQty));

        if (!TextUtils.isEmpty(currentInstructions)) {
            holder.tvSpecialInstructionsPreview.setText("Maelezo: " + currentInstructions);
            holder.tvSpecialInstructionsPreview.setVisibility(View.VISIBLE);
            holder.tvAddSpecialNote.setText("✍️ Badili Maelezo");
        } else {
            holder.tvSpecialInstructionsPreview.setVisibility(View.GONE);
            holder.tvAddSpecialNote.setText("✍️ Weka Maelezo");
        }

        int finalCurrentQty = currentQty;
        String finalCurrentInstructions = currentInstructions;

        holder.btnPlus.setOnClickListener(v -> {
            cartManager.addItem(item, 1, finalCurrentInstructions);
            notifyItemChanged(holder.getAdapterPosition());
        });

        holder.btnMinus.setOnClickListener(v -> {
            if (finalCurrentQty > 0) {
                cartManager.updateQuantity(item.getId(), finalCurrentQty - 1);
                notifyItemChanged(holder.getAdapterPosition());
            }
        });

        holder.tvAddSpecialNote.setOnClickListener(v -> {
            if (instructionClickListener != null) {
                instructionClickListener.onInstructionClick(item, finalCurrentInstructions);
            }
        });
    }

    @Override
    public int getItemCount() {
        return menuItems.size();
    }

    static class MenuViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardMenuItem;
        TextView tvFoodName;
        TextView tvFoodDescription;
        TextView tvFoodPrice;
        TextView tvAvailabilityBadge;
        TextView tvAddSpecialNote;
        TextView tvSpecialInstructionsPreview;
        LinearLayout layoutQuantityControls;
        ImageButton btnMinus;
        TextView tvQuantity;
        ImageButton btnPlus;

        MenuViewHolder(@NonNull View itemView) {
            super(itemView);
            cardMenuItem = itemView.findViewById(R.id.cardMenuItem);
            tvFoodName = itemView.findViewById(R.id.tvFoodName);
            tvFoodDescription = itemView.findViewById(R.id.tvFoodDescription);
            tvFoodPrice = itemView.findViewById(R.id.tvFoodPrice);
            tvAvailabilityBadge = itemView.findViewById(R.id.tvAvailabilityBadge);
            tvAddSpecialNote = itemView.findViewById(R.id.tvAddSpecialNote);
            tvSpecialInstructionsPreview = itemView.findViewById(R.id.tvSpecialInstructionsPreview);
            layoutQuantityControls = itemView.findViewById(R.id.layoutQuantityControls);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnPlus = itemView.findViewById(R.id.btnPlus);
        }
    }
}
