package com.example.sumayerestaurant.ui.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sumayerestaurant.R;
import com.example.sumayerestaurant.data.model.KitchenOrder;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class KitchenOrderAdapter extends RecyclerView.Adapter<KitchenOrderAdapter.KitchenOrderViewHolder> {

    public interface OnKitchenActionListener {
        void onAccept(KitchenOrder order);
        void onStartPreparing(KitchenOrder order);
        void onMarkReady(KitchenOrder order);
        void onCancel(KitchenOrder order);
        void onOrderClick(KitchenOrder order);
    }

    private final Context context;
    private final List<KitchenOrder> orders = new ArrayList<>();
    private final OnKitchenActionListener listener;

    public KitchenOrderAdapter(Context context, OnKitchenActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setOrders(List<KitchenOrder> list) {
        this.orders.clear();
        if (list != null) {
            this.orders.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public KitchenOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_kitchen_order, parent, false);
        return new KitchenOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KitchenOrderViewHolder holder, int position) {
        KitchenOrder order = orders.get(position);

        holder.tvOrderNumber.setText("ODA #" + order.getOrderNumber());

        long elapsedMinutes = order.getElapsedMinutes();
        holder.tvElapsedTime.setText("⏱️ Dakika " + elapsedMinutes);
        if (elapsedMinutes >= 15) {
            holder.tvElapsedTime.setBackgroundColor(ContextCompat.getColor(context, R.color.status_occupied));
            holder.tvElapsedTime.setTextColor(ContextCompat.getColor(context, R.color.white));
            holder.card.setStrokeColor(ContextCompat.getColor(context, R.color.error_color));
            holder.card.setStrokeWidth(2);
        } else {
            holder.tvElapsedTime.setBackgroundColor(ContextCompat.getColor(context, R.color.surface_raised));
            holder.tvElapsedTime.setTextColor(ContextCompat.getColor(context, R.color.text_primary));
            holder.card.setStrokeColor(ContextCompat.getColor(context, R.color.divider));
            holder.card.setStrokeWidth(1);
        }

        if (order.getTableNumber() != null) {
            holder.tvTable.setText("🍽️ Meza: " + order.getTableNumber() + " (" + order.getOrderType() + ")");
        } else {
            holder.tvTable.setText("📦 " + order.getOrderType());
        }

        if (order.getWaiterName() != null) {
            holder.tvWaiter.setText("👤 Mhudumu: " + order.getWaiterName());
            holder.tvWaiter.setVisibility(View.VISIBLE);
        } else {
            holder.tvWaiter.setVisibility(View.GONE);
        }

        holder.tvStatusBadge.setText(order.getStatusLabelSwahili());

        String status = order.getStatus() != null ? order.getStatus().toUpperCase() : "NEW";
        switch (status) {
            case "NEW":
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_reserved);
                holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.primary));
                holder.btnAction.setText("Pokea Oda");
                holder.btnAction.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary)));
                holder.btnAction.setEnabled(true);
                holder.btnAction.setVisibility(View.VISIBLE);
                holder.btnCancel.setVisibility(View.VISIBLE);
                break;
            case "ACCEPTED":
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_cleaning);
                holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.warning_color));
                holder.btnAction.setText("Anza Kuandaa");
                holder.btnAction.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.warning_color)));
                holder.btnAction.setEnabled(true);
                holder.btnAction.setVisibility(View.VISIBLE);
                holder.btnCancel.setVisibility(View.VISIBLE);
                break;
            case "PREPARING":
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_cleaning);
                holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_cleaning));
                holder.btnAction.setText("Weka Tayari");
                holder.btnAction.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.brand_green)));
                holder.btnAction.setEnabled(true);
                holder.btnAction.setVisibility(View.VISIBLE);
                holder.btnCancel.setVisibility(View.VISIBLE);
                break;
            case "READY":
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_available);
                holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_available));
                holder.btnAction.setText("Tayari Kupelekwa");
                holder.btnAction.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.status_available)));
                holder.btnAction.setEnabled(false);
                holder.btnAction.setVisibility(View.VISIBLE);
                holder.btnCancel.setVisibility(View.GONE);
                break;
            case "CANCELLED":
                holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_occupied);
                holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_occupied));
                holder.btnAction.setVisibility(View.GONE);
                holder.btnCancel.setVisibility(View.GONE);
                break;
        }

        if (!TextUtils.isEmpty(order.getNotes())) {
            holder.tvNotes.setText("Dokezo: " + order.getNotes());
            holder.tvNotes.setVisibility(View.VISIBLE);
        } else {
            holder.tvNotes.setVisibility(View.GONE);
        }

        // Setup items RecyclerView
        KitchenOrderItemAdapter itemAdapter = new KitchenOrderItemAdapter(context);
        holder.recyclerItems.setLayoutManager(new LinearLayoutManager(context));
        holder.recyclerItems.setAdapter(itemAdapter);
        itemAdapter.setItems(order.getItems());

        holder.btnAction.setOnClickListener(v -> {
            if (listener == null) return;
            switch (status) {
                case "NEW":
                    listener.onAccept(order);
                    break;
                case "ACCEPTED":
                    listener.onStartPreparing(order);
                    break;
                case "PREPARING":
                    listener.onMarkReady(order);
                    break;
            }
        });

        holder.btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancel(order);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class KitchenOrderViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView tvOrderNumber;
        TextView tvElapsedTime;
        TextView tvTable;
        TextView tvWaiter;
        TextView tvStatusBadge;
        RecyclerView recyclerItems;
        TextView tvNotes;
        MaterialButton btnCancel;
        MaterialButton btnAction;

        KitchenOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardKitchenOrder);
            tvOrderNumber = itemView.findViewById(R.id.tvKotOrderNumber);
            tvElapsedTime = itemView.findViewById(R.id.tvKotElapsedTime);
            tvTable = itemView.findViewById(R.id.tvKotTable);
            tvWaiter = itemView.findViewById(R.id.tvKotWaiter);
            tvStatusBadge = itemView.findViewById(R.id.tvKotStatusBadge);
            recyclerItems = itemView.findViewById(R.id.recyclerViewKotItems);
            tvNotes = itemView.findViewById(R.id.tvKotNotes);
            btnCancel = itemView.findViewById(R.id.btnCancelKot);
            btnAction = itemView.findViewById(R.id.btnKotAction);
        }
    }
}
