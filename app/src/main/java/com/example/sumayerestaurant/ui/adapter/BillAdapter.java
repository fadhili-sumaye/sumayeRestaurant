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
import com.example.sumayerestaurant.data.model.Bill;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.BillViewHolder> {

    public interface OnBillActionListener {
        void onProcessPayment(Bill bill);
        void onViewReceipt(Bill bill);
        void onBillClick(Bill bill);
    }

    private final Context context;
    private final List<Bill> bills = new ArrayList<>();
    private final OnBillActionListener listener;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("sw", "TZ"));

    public BillAdapter(Context context, OnBillActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setBills(List<Bill> list) {
        this.bills.clear();
        if (list != null) {
            this.bills.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bill, parent, false);
        return new BillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        Bill bill = bills.get(position);

        holder.tvBillNumber.setText("ANKARA #" + bill.getBillNumber());

        String tableText = (bill.getTableNumber() != null)
                ? "🍽️ Meza " + bill.getTableNumber() + " • Oda #" + bill.getOrderNumber()
                : "📦 " + bill.getOrderType() + " • Oda #" + bill.getOrderNumber();
        holder.tvOrderTable.setText(tableText);

        if (bill.getWaiterName() != null) {
            holder.tvWaiter.setText("👤 " + bill.getWaiterName());
            holder.tvWaiter.setVisibility(View.VISIBLE);
        } else {
            holder.tvWaiter.setVisibility(View.GONE);
        }

        // Financials
        holder.tvTotalAmount.setText(formatCurrency(bill.getTotalAmount()));
        holder.tvAmountPaid.setText(formatCurrency(bill.getAmountPaid()));
        holder.tvBalanceDue.setText(formatCurrency(bill.getBalanceDue()));

        // Status badge
        holder.tvStatusBadge.setText(bill.getPaymentStatusLabelSwahili());
        String status = bill.getPaymentStatus() != null ? bill.getPaymentStatus().toUpperCase() : "UNPAID";

        if ("PAID".equals(status)) {
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_available);
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_available));
            holder.btnProcessPayment.setVisibility(View.GONE);
            holder.card.setStrokeColor(ContextCompat.getColor(context, R.color.divider));
        } else if ("PARTIALLY_PAID".equals(status)) {
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_cleaning);
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.warning_color));
            holder.btnProcessPayment.setText("Kamilisha Malipo");
            holder.btnProcessPayment.setVisibility(View.VISIBLE);
            holder.card.setStrokeColor(ContextCompat.getColor(context, R.color.warning_color));
        } else {
            holder.tvStatusBadge.setBackgroundResource(R.drawable.bg_badge_occupied);
            holder.tvStatusBadge.setTextColor(ContextCompat.getColor(context, R.color.status_occupied));
            holder.btnProcessPayment.setText("Lipa Malipo");
            holder.btnProcessPayment.setVisibility(View.VISIBLE);
            holder.card.setStrokeColor(ContextCompat.getColor(context, R.color.status_occupied));
        }

        // Bill requested banner
        if (bill.isBillRequested() && !"PAID".equals(status)) {
            holder.tvBillRequestedBanner.setVisibility(View.VISIBLE);
        } else {
            holder.tvBillRequestedBanner.setVisibility(View.GONE);
        }

        holder.btnProcessPayment.setOnClickListener(v -> {
            if (listener != null) listener.onProcessPayment(bill);
        });

        holder.btnViewReceipt.setOnClickListener(v -> {
            if (listener != null) listener.onViewReceipt(bill);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onBillClick(bill);
        });
    }

    @Override
    public int getItemCount() {
        return bills.size();
    }

    private String formatCurrency(java.math.BigDecimal amount) {
        if (amount == null) return "TZS 0";
        return "TZS " + String.format(Locale.getDefault(), "%,.0f", amount.doubleValue());
    }

    static class BillViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView tvBillNumber;
        TextView tvStatusBadge;
        TextView tvBillRequestedBanner;
        TextView tvOrderTable;
        TextView tvWaiter;
        TextView tvTotalAmount;
        TextView tvAmountPaid;
        TextView tvBalanceDue;
        MaterialButton btnViewReceipt;
        MaterialButton btnProcessPayment;

        BillViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardBill);
            tvBillNumber = itemView.findViewById(R.id.tvBillNumber);
            tvStatusBadge = itemView.findViewById(R.id.tvBillStatusBadge);
            tvBillRequestedBanner = itemView.findViewById(R.id.tvBillRequestedBanner);
            tvOrderTable = itemView.findViewById(R.id.tvBillOrderTable);
            tvWaiter = itemView.findViewById(R.id.tvBillWaiter);
            tvTotalAmount = itemView.findViewById(R.id.tvBillTotalAmount);
            tvAmountPaid = itemView.findViewById(R.id.tvBillAmountPaid);
            tvBalanceDue = itemView.findViewById(R.id.tvBillBalanceDue);
            btnViewReceipt = itemView.findViewById(R.id.btnViewReceipt);
            btnProcessPayment = itemView.findViewById(R.id.btnProcessPayment);
        }
    }
}
