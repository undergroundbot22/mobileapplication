package com.richfield.smartpantry;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantry.R;

import java.util.List;

@SuppressWarnings("unused")
public class PantryAdapter extends RecyclerView.Adapter<PantryAdapter.ViewHolder> {

    // 1. Interface definition for click callbacks
    public interface OnItemClickListener {
        void onEdit(PantryItem item);
        void onDelete(int id);
    }

    private final List<PantryItem> itemList;
    private final OnItemClickListener listener;

    public PantryAdapter(List<PantryItem> itemList, OnItemClickListener listener) {
        this.itemList = itemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pantry_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (itemList == null) return;
        PantryItem item = itemList.get(position);
        holder.tvName.setText(item.getName());
        Context context = holder.itemView.getContext();

        // Format quantity (omit unnecessary decimals like 2.0 -> 2)
        double qty = item.getQuantity();
        String qtyStr = (qty == (long) qty) ? String.format("%d", (long) qty) : String.valueOf(qty);
        String unit = item.getUnit() != null ? item.getUnit() : "";
        holder.tvQuantity.setText(context.getString(R.string.pantry_quantity_format, qtyStr, unit));

        // Format expiry date if present
        if (item.getExpiryDate() != null && !item.getExpiryDate().trim().isEmpty()) {
            holder.tvExpiry.setText(context.getString(R.string.pantry_expiry_format, item.getExpiryDate().trim()));
            holder.tvExpiry.setVisibility(View.VISIBLE);
        } else {
            holder.tvExpiry.setVisibility(View.GONE);
        }

        // Bind button click listeners to interface callbacks
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEdit(item);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(item.getId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList != null ? itemList.size() : 0;
    }

    // 2. ViewHolder class holding references to layout views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity, tvExpiry;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_ingredient_name);
            tvQuantity = itemView.findViewById(R.id.tv_ingredient_qty);
            tvExpiry = itemView.findViewById(R.id.tv_ingredient_expiry);
            btnEdit = itemView.findViewById(R.id.btn_edit_item);
            btnDelete = itemView.findViewById(R.id.btn_delete_item);
        }
    }
}
