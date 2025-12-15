package com.B.carrasco.burgerapp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.B.carrasco.burgerapp.R;
import com.B.carrasco.burgerapp.models.Burger;
import com.B.carrasco.burgerapp.models.Ingredient;
import com.B.carrasco.burgerapp.utils.CartManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildBurgerActivity extends AppCompatActivity {

    private ListView lvIngredients;
    private TextView tvTotalPrice;
    private Button btnProceedToSauces;
    private CheckBox cbDoublePatty;
    private RadioGroup rgMeatFlavor;

    private List<Ingredient> allIngredients;
    private Map<String, Integer> selectedIngredientsQty = new HashMap<>();
    private FirebaseFirestore db;

    // --- TABLA DE PRECIOS ---
    private final double BASE_PRICE = 2500.0;
    private final double DOUBLE_PATTY_PRICE = 1200.0;
    private final double EXTRA_SINGLE_PRICE = 300.0;
    private final double EXTRA_PROMO_PRICE = 500.0; // 2 x $500
    private final int MAX_INGREDIENTS = 8;

    private double currentBurgerPrice = 0.0;
    private String meatFlavor = "Tradicional";
    private final List<String> selectedSauces = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_burger);

        db = FirebaseFirestore.getInstance();
        initViews();
        fetchIngredientsFromFirebase();
        setupInteractions();
        calculatePrice();
    }

    private void initViews() {
        lvIngredients = findViewById(R.id.lvIngredients);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnProceedToSauces = findViewById(R.id.btnCreateBurger);
        cbDoublePatty = findViewById(R.id.cbDoublePatty);
        rgMeatFlavor = findViewById(R.id.rgMeatType);
    }

    private void fetchIngredientsFromFirebase() {
        db.collection("ingredients")
                .whereEqualTo("available", true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        allIngredients = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Ingredient ing = doc.toObject(Ingredient.class);
                            ing.setId(doc.getId());
                            allIngredients.add(ing);
                        }
                        updateAdapter();
                    } else {
                        loadFallbackIngredients();
                    }
                });
    }

    private void loadFallbackIngredients() {
        allIngredients = Arrays.asList(
                new Ingredient("Queso Mantecoso", true),
                new Ingredient("Tomate", true),
                new Ingredient("Lechuga", true),
                new Ingredient("Palta", true)
        );
        updateAdapter();
    }

    private void updateAdapter() {
        CustomIngredientAdapter adapter = new CustomIngredientAdapter(this, allIngredients, selectedIngredientsQty);
        lvIngredients.setAdapter(adapter);
    }

    private void setupInteractions() {
        rgMeatFlavor.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbTraditional) meatFlavor = "Tradicional";
            else if (checkedId == R.id.rbBarbecue) meatFlavor = "BBQ";
            else if (checkedId == R.id.rbSpicy) meatFlavor = "Picante";
        });

        cbDoublePatty.setOnCheckedChangeListener((buttonView, isChecked) -> calculatePrice());

        lvIngredients.setOnItemClickListener((parent, view, position, id) -> {
            Ingredient ing = allIngredients.get(position);
            if (ing.getName().toLowerCase().contains("queso")) {
                showCheeseQuantityDialog(ing);
            } else {
                toggleIngredient(ing.getName());
            }
        });

        btnProceedToSauces.setOnClickListener(v -> showSaucesDialog());
    }

    private void toggleIngredient(String name) {
        if (selectedIngredientsQty.containsKey(name)) {
            selectedIngredientsQty.remove(name);
        } else {
            if (getTotalIngredientCount() >= MAX_INGREDIENTS) {
                Toast.makeText(this, "⚠️ Máximo " + MAX_INGREDIENTS + " ingredientes permitidos", Toast.LENGTH_SHORT).show();
                return;
            }
            selectedIngredientsQty.put(name, 1);
        }
        calculatePrice();
        ((CustomIngredientAdapter)lvIngredients.getAdapter()).notifyDataSetChanged();
    }

    private void showCheeseQuantityDialog(Ingredient cheese) {
        final String[] options = {"Normal (x1)", "Extra (x2)", "Triple (x3)", "Quitar"};
        new MaterialAlertDialogBuilder(this)
                .setTitle("Cantidad de " + cheese.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 3) {
                        selectedIngredientsQty.remove(cheese.getName());
                    } else {
                        int newQty = which + 1;
                        int currentTotal = getTotalIngredientCount();
                        int currentCheeseQty = selectedIngredientsQty.getOrDefault(cheese.getName(), 0);
                        int netAddition = newQty - currentCheeseQty;

                        if (currentTotal + netAddition > MAX_INGREDIENTS) {
                            Toast.makeText(this, "⚠️ Excede el límite de 8 ingredientes", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        selectedIngredientsQty.put(cheese.getName(), newQty);
                    }
                    calculatePrice();
                    ((CustomIngredientAdapter)lvIngredients.getAdapter()).notifyDataSetChanged();
                })
                .show();
    }

    private int getTotalIngredientCount() {
        int total = 0;
        for (int qty : selectedIngredientsQty.values()) total += qty;
        return total;
    }

    private void calculatePrice() {
        double total = BASE_PRICE;
        boolean isDouble = cbDoublePatty.isChecked();
        if (isDouble) total += DOUBLE_PATTY_PRICE;

        int freeIngredientsLimit = isDouble ? 3 : 2;
        int totalSelected = getTotalIngredientCount();
        int payableIngredients = totalSelected - freeIngredientsLimit;

        if (payableIngredients > 0) {
            int pairs = payableIngredients / 2;
            int remainder = payableIngredients % 2;
            double extrasCost = (pairs * EXTRA_PROMO_PRICE) + (remainder * EXTRA_SINGLE_PRICE);
            total += extrasCost;
        }

        currentBurgerPrice = total;
        tvTotalPrice.setText("Total: $" + (int)currentBurgerPrice);
    }

    private void showSaucesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_sauces, null);
        builder.setView(dialogView);

        CheckBox cbMayo = dialogView.findViewById(R.id.cbDialogMayo);
        CheckBox cbKetchup = dialogView.findViewById(R.id.cbDialogKetchup);
        CheckBox cbMustard = dialogView.findViewById(R.id.cbDialogMustard);
        CheckBox cbSpicy = dialogView.findViewById(R.id.cbDialogSpicy);
        Button btnConfirmSauces = dialogView.findViewById(R.id.btnConfirmSauces);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        btnConfirmSauces.setOnClickListener(v -> {
            selectedSauces.clear();
            if (cbMayo.isChecked()) selectedSauces.add("Mayonesa Casera");
            if (cbKetchup.isChecked()) selectedSauces.add("Ketchup Premium");
            if (cbMustard.isChecked()) selectedSauces.add("Mostaza Selección");
            if (cbSpicy.isChecked()) selectedSauces.add("Salsa Picante");
            dialog.dismiss();
            showFinalOptionsDialog();
        });
        dialog.show();
    }

    private void showFinalOptionsDialog() {
        List<Ingredient> finalIngs = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : selectedIngredientsQty.entrySet()) {
            String name = entry.getKey();
            int qty = entry.getValue();
            String displayName = (qty > 1) ? name + " (x" + qty + ")" : name;
            finalIngs.add(new Ingredient(displayName, true));
        }

        if (!selectedSauces.isEmpty()) {
            StringBuilder saucesStr = new StringBuilder("Salsas: ");
            for (String sauce : selectedSauces) saucesStr.append(sauce).append(", ");
            String finalSauces = saucesStr.substring(0, saucesStr.length() - 2);
            finalIngs.add(new Ingredient(finalSauces, true));
        }

        Burger newBurger = new Burger();
        newBurger.setName("Burger " + meatFlavor + (cbDoublePatty.isChecked() ? " Doble" : " Simple"));
        newBurger.setIngredients(finalIngs);
        newBurger.setTotalPrice(currentBurgerPrice);

        CartManager.getInstance().addBurger(newBurger);

        new MaterialAlertDialogBuilder(this)
                .setTitle("🍔 ¡Lista para la plancha!")
                .setMessage("Tu creación está lista.\nTotal parcial: $" + (int)CartManager.getInstance().getTotalOrderPrice())
                .setCancelable(false)
                .setPositiveButton("CONTINUAR ➔", (dialog, which) -> {
                    // CAMBIO AQUÍ: Ahora vamos a Delivery Options
                    Intent intent = new Intent(BuildBurgerActivity.this, DeliveryOptionsActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("➕ Crear Otra", (dialog, which) -> {
                    Intent intent = new Intent(this, BuildBurgerActivity.class);
                    startActivity(intent);
                    finish();
                })
                .show();
    }
}