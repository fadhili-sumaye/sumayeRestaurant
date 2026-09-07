package com.example.sumayerestaurant.ui.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sumayerestaurant.R;
import com.example.sumayerestaurant.data.model.KitchenOrderItem;

import java.util.ArrayList;
import java.util.List;

public class KitchenOrderItemAdapter extends RecyclerView.Adapter<KitchenOrderItemAdapter.KotItemViewHolder> {

    private final Context context;
    private final List<KitchenOrderItem> items = new ArrayList<>();

    public KitchenOrderItemAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<KitchenOrderItem> list) {
        this.items.clear();
        if (list != null) {
            this.items.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public KotItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_kitchen_order_item, parent, false);
        return new KotItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KotItemViewHolder holder, int position) {
        KitchenOrderItem item = items.get(position);
        holder.tvNameQty.setText(item.getQuantity() + " × " + item.getItemName());

        if (!TextUtils.isEmpty(item.getSpecialInstructions())) {
            holder.tvInstructions.setText("⚠️ Maelekezo: " + item.getSpecialInstructions());
            holder.tvInstructions.setVisibility(View.VISIBLE);
        } else {
            holder.tvInstructions.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class KotItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvNameQty;
        TextView tvInstructions;
        TextView tvStatus;

        KotItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNameQty = itemView.findViewById(R.id.tvKotItemNameQty);
            tvInstructions = itemView.findViewById(R.id.tvKotItemInstructions);
            tvStatus = itemView.findViewById(R.id.tvKotItemStatus);
        }
    }
}
