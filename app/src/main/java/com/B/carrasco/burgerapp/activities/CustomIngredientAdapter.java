package com.B.carrasco.burgerapp.activities;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.B.carrasco.burgerapp.models.Ingredient;
import java.util.List;
import java.util.Map;

public class CustomIngredientAdapter extends ArrayAdapter<Ingredient> {
    private Map<String, Integer> selections;

    public CustomIngredientAdapter(Context context, List<Ingredient> ingredients, Map<String, Integer> selections) {
        super(context, android.R.layout.simple_list_item_1, ingredients);
        this.selections = selections;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView tv = view.findViewById(android.R.id.text1);

        Ingredient ing = getItem(position);

        if (ing != null && selections.containsKey(ing.getName())) {
            int qty = selections.get(ing.getName());
            String text = ing.getName();
            if (qty > 1) text += " (x" + qty + ")";
            text += " ✅";

            tv.setText(text);
            tv.setTextColor(Color.parseColor("#4CAF50")); // Verde cuando está seleccionado
            tv.setTypeface(null, Typeface.BOLD);
        } else {
            if (ing != null) tv.setText(ing.getName());
            tv.setTextColor(Color.BLACK);
            tv.setTypeface(null, Typeface.NORMAL);
        }
        return view;
    }
}