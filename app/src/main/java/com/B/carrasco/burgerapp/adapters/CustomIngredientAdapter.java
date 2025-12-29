package com.B.carrasco.burgerapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.B.carrasco.burgerapp.R;
import com.B.carrasco.burgerapp.models.Ingredient;

import java.util.List;
import java.util.Map;

public class CustomIngredientAdapter extends ArrayAdapter<Ingredient> {

    private Context context;
    private List<Ingredient> ingredients;
    private Map<String, Integer> selectedQty;

    public CustomIngredientAdapter(Context context, List<Ingredient> ingredients, Map<String, Integer> selectedQty) {
        super(context, R.layout.item_ingredient_client, ingredients);
        this.context = context;
        this.ingredients = ingredients;
        this.selectedQty = selectedQty;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_ingredient_client, parent, false);
        }

        Ingredient ing = ingredients.get(position);

        TextView tvName = convertView.findViewById(R.id.tvIngredientName);
        TextView tvStatus = convertView.findViewById(R.id.tvIngredientStatus);
        TextView tvBadge = convertView.findViewById(R.id.tvQuantityBadge);
        ImageView ivIcon = convertView.findViewById(R.id.ivSelectionState);
        LinearLayout container = convertView.findViewById(R.id.containerIngredient); // Opcional si queremos cambiar fondo

        tvName.setText(ing.getName());

        // --- LÓGICA VISUAL LIMPIA ---

        if (selectedQty.containsKey(ing.getName())) {
            // == SELECCIONADO ==
            int qty = selectedQty.get(ing.getName());

            // Usamos nuestro vector VERDE SÓLIDO (Sin filtros de color)
            ivIcon.setImageResource(R.drawable.ic_check_selected);
            ivIcon.clearColorFilter(); // Limpiamos cualquier filtro anterior

            tvStatus.setText("Agregado");
            tvStatus.setTextColor(ContextCompat.getColor(context, R.color.success_green));

            tvBadge.setVisibility(View.VISIBLE);
            tvBadge.setText("x" + qty);

            // Un fondo verde MUY sutil (casi blanco) para resaltar
            convertView.setBackgroundColor(Color.parseColor("#FAFDFB"));

        } else {
            // == NO SELECCIONADO ==

            // Usamos nuestro círculo GRIS (Outline)
            ivIcon.setImageResource(R.drawable.ic_check_empty);
            ivIcon.clearColorFilter();

            tvStatus.setText("Disponible");
            tvStatus.setTextColor(Color.parseColor("#BDBDBD"));

            tvBadge.setVisibility(View.GONE);
            convertView.setBackgroundColor(Color.WHITE);
        }

        // Lógica de Stock (Bloqueo visual)
        if (!ing.isAvailable()) {
            tvName.setTextColor(Color.LTGRAY);
            tvStatus.setText("AGOTADO");
            tvStatus.setTextColor(Color.RED);
            ivIcon.setVisibility(View.INVISIBLE); // Ocultamos el check si no hay stock
            convertView.setEnabled(false);
            convertView.setAlpha(0.5f);
        } else {
            tvName.setTextColor(Color.parseColor("#3E2723"));
            ivIcon.setVisibility(View.VISIBLE);
            convertView.setEnabled(true);
            convertView.setAlpha(1.0f);
        }

        return convertView;
    }
}