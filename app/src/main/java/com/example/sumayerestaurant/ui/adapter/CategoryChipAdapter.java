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
import com.example.sumayerestaurant.data.model.MenuCategory;

import java.util.ArrayList;
import java.util.List;

public class CategoryChipAdapter extends RecyclerView.Adapter<CategoryChipAdapter.ChipViewHolder> {

    public interface OnCategorySelectedListener {
        void onCategorySelected(Long categoryId);
    }

    private final Context context;
    private final List<MenuCategory> categories = new ArrayList<>();
    private final OnCategorySelectedListener listener;
    private Long selectedCategoryId = null; // null represents "Vyote" (All)

    public CategoryChipAdapter(Context context, OnCategorySelectedListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setCategories(List<MenuCategory> categoryList) {
        this.categories.clear();
        // Add "Vyote" (All) category as first element
        this.categories.add(new MenuCategory(null, "Vyote"));
        if (categoryList != null) {
            this.categories.addAll(categoryList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_chip, parent, false);
        return new ChipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChipViewHolder holder, int position) {
        MenuCategory cat = categories.get(position);
        holder.tvChip.setText(cat.getName());

        boolean isSelected = (selectedCategoryId == null && cat.getId() == null) ||
                (selectedCategoryId != null && selectedCategoryId.equals(cat.getId()));

        if (isSelected) {
            holder.tvChip.setBackgroundResource(R.drawable.bg_chip_selected);
            holder.tvChip.setTextColor(ContextCompat.getColor(context, R.color.white));
        } else {
            holder.tvChip.setBackgroundResource(R.drawable.bg_chip_unselected);
            holder.tvChip.setTextColor(ContextCompat.getColor(context, R.color.text_color));
        }

        holder.itemView.setOnClickListener(v -> {
            selectedCategoryId = cat.getId();
            notifyDataSetChanged();
            if (listener != null) {
                listener.onCategorySelected(selectedCategoryId);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ChipViewHolder extends RecyclerView.ViewHolder {
        TextView tvChip;

        ChipViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChip = itemView.findViewById(R.id.tvCategoryChip);
        }
    }
}
