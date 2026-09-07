package com.example.sumayerestaurant.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sumayerestaurant.R;
import com.example.sumayerestaurant.data.model.Order;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    private final Context context;
    private final List<Order> orderList = new ArrayList<>();
    private final OnOrderClickListener listener;
    private final NumberFormat currencyFormat = NumberFormat.getNumberInstance(Locale.US);

    public OrderHistoryAdapter(Context context, OnOrderClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setOrders(List<Order> orders) {
        this.orderList.clear();
        if (orders != null) {
            this.orderList.addAll(orders);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvOrderNumber.setText(order.getOrderNumber());
        holder.tvStatus.setText(order.getStatusLabelSwahili());

        if (order.getTableNumber() != null) {
            holder.tvTable.setText("🍽️ Meza " + order.getTableNumber());
        } else {
            holder.tvTable.setText("📦 " + order.getOrderType());
        }

        String createdAt = order.getCreatedAt() != null ? order.getCreatedAt().replace("T", " ") : "";
        holder.tvDate.setText(createdAt);
        holder.tvTotal.setText(String.format("TZS %s", currencyFormat.format(order.getTotalAmount())));

        String status = order.getStatus() != null ? order.getStatus().toUpperCase() : "";
        if ("CANCELLED".equals(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_occupied);
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_occupied));
        } else if ("COMPLETED".equals(status) || "SERVED".equals(status)) {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_available);
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_available));
        } else {
            holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_reserved);
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.primary));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNumber;
        TextView tvStatus;
        TextView tvTable;
        TextView tvDate;
        TextView tvTotal;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderNumber = itemView.findViewById(R.id.tvOrderNumber);
            tvStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvTable = itemView.findViewById(R.id.tvOrderTable);
            tvDate = itemView.findViewById(R.id.tvOrderDate);
            tvTotal = itemView.findViewById(R.id.tvOrderTotal);
        }
    }
}
