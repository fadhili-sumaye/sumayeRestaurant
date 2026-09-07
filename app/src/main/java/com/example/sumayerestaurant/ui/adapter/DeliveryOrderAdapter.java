package com.example.sumayerestaurant.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sumayerestaurant.R;
import com.example.sumayerestaurant.data.model.Customer;
import com.example.sumayerestaurant.data.model.DeliveryOrder;
import com.example.sumayerestaurant.data.model.Order;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DeliveryOrderAdapter extends RecyclerView.Adapter<DeliveryOrderAdapter.ViewHolder> {

    public interface DeliveryActionListener {
        void onStatusChange(DeliveryOrder deliveryOrder, String newStatus);
        void onAssignToMe(DeliveryOrder deliveryOrder);
    }

    private final Context context;
    private final List<DeliveryOrder> items = new ArrayList<>();
    private final DeliveryActionListener listener;
    private boolean isMyDeliveriesView = true;

    public DeliveryOrderAdapter(Context context, DeliveryActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setItems(List<DeliveryOrder> newItems, boolean isMyView) {
        this.isMyDeliveriesView = isMyView;
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_delivery_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DeliveryOrder delivery = items.get(position);
        Order order = delivery.getOrder();
        Customer customer = delivery.getCustomer();

        // Order Number
        String orderNo = (order != null && order.getOrderNumber() != null)
                ? order.getOrderNumber()
                : "ID #" + delivery.getId();
        holder.tvOrderNumber.setText("Oda #" + orderNo);

        // Status Badge
        String status = delivery.getStatus() != null ? delivery.getStatus() : "PENDING";
        holder.tvStatusBadge.setText(getStatusLabel(status));

        // Customer Details
        String custName = customer != null && customer.getFullName() != null
                ? customer.getFullName()
                : "Mteja";
        String custPhone = customer != null && customer.getPhoneNumber() != null
                ? customer.getPhoneNumber()
                : "";
        holder.tvCustomerName.setText("Mteja: " + custName);
        holder.tvCustomerPhone.setText(custPhone.isEmpty() ? "Simu haijawekwa" : custPhone);

        // Call button
        if (!custPhone.isEmpty()) {
            holder.btnCallCustomer.setVisibility(View.VISIBLE);
            holder.btnCallCustomer.setOnClickListener(v -> {
                try {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + custPhone.trim()));
                    context.startActivity(callIntent);
                } catch (Exception e) {
                    Toast.makeText(context, "Imeshindikana kupiga simu", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            holder.btnCallCustomer.setVisibility(View.GONE);
        }

        // Delivery Address
        String addr = delivery.getDeliveryAddress() != null && !delivery.getDeliveryAddress().isEmpty()
                ? delivery.getDeliveryAddress()
                : (customer != null && customer.getAddress() != null ? customer.getAddress() : "Anwani haijabainishwa");
        holder.tvDeliveryAddress.setText(addr);

        // Fees
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        holder.tvDeliveryFee.setText("Ada: TZS " + nf.format(delivery.getDeliveryFee()));

        if (order != null && order.getTotalAmount() > 0) {
            holder.tvTotalAmount.setText("Jumla: TZS " + nf.format(order.getTotalAmount()));
            holder.tvTotalAmount.setVisibility(View.VISIBLE);
        } else {
            holder.tvTotalAmount.setVisibility(View.GONE);
        }

        // Action Button
        if (isMyDeliveriesView) {
            if ("ASSIGNED".equalsIgnoreCase(status) || "READY".equalsIgnoreCase(status)) {
                holder.btnDeliveryAction.setVisibility(View.VISIBLE);
                holder.btnDeliveryAction.setText("🚴 Chukua & Anza Safari");
                holder.btnDeliveryAction.setOnClickListener(v -> {
                    if (listener != null) listener.onStatusChange(delivery, "OUT_FOR_DELIVERY");
                });
            } else if ("OUT_FOR_DELIVERY".equalsIgnoreCase(status)) {
                holder.btnDeliveryAction.setVisibility(View.VISIBLE);
                holder.btnDeliveryAction.setText("✅ Imefikishwa kwa Mteja");
                holder.btnDeliveryAction.setOnClickListener(v -> {
                    if (listener != null) listener.onStatusChange(delivery, "DELIVERED");
                });
            } else {
                holder.btnDeliveryAction.setVisibility(View.GONE);
            }
        } else {
            // Branch View (Orders waiting for a rider)
            if (delivery.getRider() == null && ("READY".equalsIgnoreCase(status) || "CONFIRMED".equalsIgnoreCase(status) || "PENDING".equalsIgnoreCase(status))) {
                holder.btnDeliveryAction.setVisibility(View.VISIBLE);
                holder.btnDeliveryAction.setText("🤝 Jipangie Oda Hii");
                holder.btnDeliveryAction.setOnClickListener(v -> {
                    if (listener != null) listener.onAssignToMe(delivery);
                });
            } else {
                holder.btnDeliveryAction.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String getStatusLabel(String status) {
        if (status == null) return "Inasubiri";
        switch (status.toUpperCase()) {
            case "PENDING":
                return "Inasubiri";
            case "CONFIRMED":
                return "Imethibitishwa";
            case "PREPARING":
                return "Inapikwa";
            case "READY":
                return "Iko Tayari";
            case "ASSIGNED":
                return "Umepewa";
            case "OUT_FOR_DELIVERY":
                return "Safarini";
            case "DELIVERED":
            case "COMPLETED":
                return "Imefikishwa";
            case "CANCELLED":
                return "Imeghairiwa";
            default:
                return status;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNumber;
        TextView tvStatusBadge;
        TextView tvCustomerName;
        TextView tvCustomerPhone;
        MaterialButton btnCallCustomer;
        TextView tvDeliveryAddress;
        TextView tvDeliveryFee;
        TextView tvTotalAmount;
        MaterialButton btnDeliveryAction;

        ViewHolder(View itemView) {
            super(itemView);
            tvOrderNumber = itemView.findViewById(R.id.tvOrderNumber);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvCustomerPhone = itemView.findViewById(R.id.tvCustomerPhone);
            btnCallCustomer = itemView.findViewById(R.id.btnCallCustomer);
            tvDeliveryAddress = itemView.findViewById(R.id.tvDeliveryAddress);
            tvDeliveryFee = itemView.findViewById(R.id.tvDeliveryFee);
            tvTotalAmount = itemView.findViewById(R.id.tvTotalAmount);
            btnDeliveryAction = itemView.findViewById(R.id.btnDeliveryAction);
        }
    }
}
