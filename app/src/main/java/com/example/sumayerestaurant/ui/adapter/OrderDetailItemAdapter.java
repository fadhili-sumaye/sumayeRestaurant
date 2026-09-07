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
import com.example.sumayerestaurant.data.model.OrderItem;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderDetailItemAdapter extends RecyclerView.Adapter<OrderDetailItemAdapter.DetailItemViewHolder> {

    private final Context context;
    private final List<OrderItem> items = new ArrayList<>();
    private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.US);

    public OrderDetailItemAdapter(Context context) {
        this.context = context;
    }

    public void setItems(List<OrderItem> itemList) {
        this.items.clear();
        if (itemList != null) {
            this.items.addAll(itemList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DetailItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_detail_item, parent, false);
        return new DetailItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailItemViewHolder holder, int position) {
        OrderItem item = items.get(position);

        holder.tvName.setText(item.getItemName());
        holder.tvQtyPrice.setText(String.format("Idadi: %d × TZS %s",
                item.getQuantity(),
                currencyFormat.format(item.getUnitPrice())));
        holder.tvSubtotal.setText(String.format("TZS %s", currencyFormat.format(item.getSubtotal())));

        if (!TextUtils.isEmpty(item.getSpecialInstructions())) {
            holder.tvInstructions.setText("Maelezo: " + item.getSpecialInstructions());
            holder.tvInstructions.setVisibility(View.VISIBLE);
        } else {
            holder.tvInstructions.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class DetailItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvQtyPrice;
        TextView tvSubtotal;
        TextView tvInstructions;

        DetailItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvDetailItemName);
            tvQtyPrice = itemView.findViewById(R.id.tvDetailItemQtyPrice);
            tvSubtotal = itemView.findViewById(R.id.tvDetailItemSubtotal);
            tvInstructions = itemView.findViewById(R.id.tvDetailItemInstructions);
        }
    }
}
