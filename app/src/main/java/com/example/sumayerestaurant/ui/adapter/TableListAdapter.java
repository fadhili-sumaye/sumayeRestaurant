package com.example.sumayerestaurant.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sumayerestaurant.R;
import com.example.sumayerestaurant.data.model.RestaurantTable;
import com.example.sumayerestaurant.util.Constants;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;

public class TableListAdapter extends RecyclerView.Adapter<TableListAdapter.TableViewHolder> {

    public interface OnTableClickListener {
        void onTableClick(RestaurantTable table);
    }

    private final Context context;
    private final List<RestaurantTable> tableList = new ArrayList<>();
    private final OnTableClickListener listener;

    public TableListAdapter(Context context, OnTableClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setTables(List<RestaurantTable> tables) {
        this.tableList.clear();
        if (tables != null) {
            this.tableList.addAll(tables);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_table, parent, false);
        return new TableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TableViewHolder holder, int position) {
        RestaurantTable table = tableList.get(position);
        holder.tvTableNumber.setText("Meza " + table.getTableNumber());
        holder.tvCapacity.setText(String.format(context.getString(R.string.capacity_format), table.getCapacity()));
        holder.tvStatus.setText(table.getStatusLabelSwahili());

        String status = table.getStatus() != null ? table.getStatus().toUpperCase() : "AVAILABLE";
        switch (status) {
            case "AVAILABLE":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_available);
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_available));
                holder.cardTable.setStrokeColor(ContextCompat.getColor(context, R.color.brand_green));
                holder.cardTable.setStrokeWidth(2);
                break;
            case "OCCUPIED":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_occupied);
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_occupied));
                holder.cardTable.setStrokeColor(ContextCompat.getColor(context, R.color.divider));
                holder.cardTable.setStrokeWidth(1);
                break;
            case "RESERVED":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_reserved);
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_reserved));
                holder.cardTable.setStrokeColor(ContextCompat.getColor(context, R.color.divider));
                holder.cardTable.setStrokeWidth(1);
                break;
            case "CLEANING":
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_cleaning);
                holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.status_cleaning));
                holder.cardTable.setStrokeColor(ContextCompat.getColor(context, R.color.divider));
                holder.cardTable.setStrokeWidth(1);
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            if (table.isAvailable()) {
                if (listener != null) {
                    listener.onTableClick(table);
                }
            } else {
                Toast.makeText(context, "Meza hii " + table.getStatusLabelSwahili().toLowerCase() + ". Tafadhali chagua meza iliyo wazi.", Toast.LENGTH_SHORT).show();
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            String token = table.getQrToken();
            if (token == null || token.length() < 32) {
                Toast.makeText(context, "Msimbo wa QR wa meza hii bado haujatengenezwa.", Toast.LENGTH_LONG).show();
                return true;
            }
            String qrDeepLink = "sumaye://order/" + token;
            String httpLink = Constants.BASE_URL + "api/public/qr/" + token;

            new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Kiungo cha QR — Meza " + table.getTableNumber())
                .setMessage("Kiungo cha kuagiza kwa wateja (Self-Service QR):\n\n" + qrDeepLink + "\n\nKiungo cha API ya wavuti:\n" + httpLink)
                .setPositiveButton("Fungua Agizo la QR", (dialog, which) -> {
                    try {
                        android.content.Intent qrIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(qrDeepLink));
                        context.startActivity(qrIntent);
                    } catch (Exception e) {
                        Toast.makeText(context, "Imeshindikana kufungua: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Nakili Kiungo", (dialog, which) -> {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText("QR Link", qrDeepLink);
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(context, "Kiungo kimenakiliwa kwenye clipboard!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Funga", null)
                .show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return tableList.size();
    }

    static class TableViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardTable;
        TextView tvTableNumber;
        TextView tvCapacity;
        TextView tvStatus;

        TableViewHolder(@NonNull View itemView) {
            super(itemView);
            cardTable = itemView.findViewById(R.id.cardTable);
            tvTableNumber = itemView.findViewById(R.id.tvTableNumber);
            tvCapacity = itemView.findViewById(R.id.tvCapacity);
            tvStatus = itemView.findViewById(R.id.tvTableStatus);
        }
    }
}
