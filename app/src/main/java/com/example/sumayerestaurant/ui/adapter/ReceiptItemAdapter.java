package com.example.sumayerestaurant.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sumayerestaurant.R;
import com.example.sumayerestaurant.data.model.ReceiptItem;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReceiptItemAdapter extends RecyclerView.Adapter<ReceiptItemAdapter.ReceiptItemViewHolder> {

    private final Context context;
    private final List<ReceiptItem> items = new ArrayList<>();

    public ReceiptItemAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<ReceiptItem> list) {
        this.items.clear();
        if (list != null) {
            this.items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReceiptItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_receipt_food, parent, false);
        return new ReceiptItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReceiptItemViewHolder holder, int position) {
        ReceiptItem item = items.get(position);
        holder.tvName.setText(item.getItemName());
        holder.tvQty.setText(String.valueOf(item.getQuantity()));
        holder.tvPrice.setText(formatNumber(item.getUnitPrice()));
        holder.tvSubtotal.setText(formatNumber(item.getSubtotal()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String formatNumber(BigDecimal amount) {
        if (amount == null) return "0";
        return String.format(Locale.getDefault(), "%,.0f", amount.doubleValue());
    }

    static class ReceiptItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvQty;
        TextView tvPrice;
        TextView tvSubtotal;

        ReceiptItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvReceiptFoodName);
            tvQty = itemView.findViewById(R.id.tvReceiptFoodQty);
            tvPrice = itemView.findViewById(R.id.tvReceiptFoodPrice);
            tvSubtotal = itemView.findViewById(R.id.tvReceiptFoodSubtotal);
        }
    }
}
