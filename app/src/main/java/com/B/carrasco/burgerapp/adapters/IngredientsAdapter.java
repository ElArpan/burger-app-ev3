package com.B.carrasco.burgerapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.B.carrasco.burgerapp.R;
import com.B.carrasco.burgerapp.models.Ingredient;

import java.util.List;

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.ViewHolder> {

    public interface OnIngredientActionListener {
        void onEditClicked(Ingredient ingredient);
        void onDeleteClicked(Ingredient ingredient);
    }

    private List<Ingredient> items;
    private OnIngredientActionListener listener;

    public IngredientsAdapter(List<Ingredient> items, OnIngredientActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public IngredientsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ingredient, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientsAdapter.ViewHolder holder, int position) {
        Ingredient ing = items.get(position);
        holder.tvName.setText(ing.getName());
        holder.tvCategory.setText(ing.getCategory());
        holder.tvPrice.setText("$" + (int) ing.getPrice());
        holder.tvAvailable.setText(ing.isAvailable() ? "Disponible" : "No disponible");
        holder.tvAvailable.setAlpha(ing.isAvailable() ? 1f : 0.6f);

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClicked(ing);
        });
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClicked(ing);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvPrice, tvAvailable;
        ImageButton btnEdit, btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvIngredientName);
            tvCategory = itemView.findViewById(R.id.tvIngredientCategory);
            tvPrice = itemView.findViewById(R.id.tvIngredientPrice);
            tvAvailable = itemView.findViewById(R.id.tvIngredientAvailable);
            btnEdit = itemView.findViewById(R.id.btnEditIngredient);
            btnDelete = itemView.findViewById(R.id.btnDeleteIngredient);
        }
    }
}
