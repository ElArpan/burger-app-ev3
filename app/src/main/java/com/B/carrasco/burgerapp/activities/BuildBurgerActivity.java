package com.B.carrasco.burgerapp.activities;

import android.content.Intent;
import android.os.Bundle;
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

    // Datos
    private List<Ingredient> allIngredients;
    // Usamos un Mapa para guardar CANTIDAD de cada ingrediente (Ej: Queso -> 3)
    private Map<String, Integer> selectedIngredientsQty = new HashMap<>();

    // Precios y Reglas
    private final double BASE_PRICE = 2000.0;
    private final double EXTRA_PATTY_PRICE = 1200.0;
    private final double EXTRA_INGREDIENT_PRICE = 500.0;

    private double currentBurgerPrice = 0.0;
    private String meatFlavor = "Tradicional"; // Default
    private final List<String> selectedSauces = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_burger);

        initViews();
        loadIngredientsData();
        setupInteractions();
        calculatePrice(); // Cálculo inicial
    }

    private void initViews() {
        lvIngredients = findViewById(R.id.lvIngredients);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnProceedToSauces = findViewById(R.id.btnCreateBurger);
        cbDoublePatty = findViewById(R.id.cbDoublePatty);
        rgMeatFlavor = findViewById(R.id.rgMeatType);
    }

    private void loadIngredientsData() {
        // Lista de ingredientes
        allIngredients = Arrays.asList(
                new Ingredient("Queso mantecoso", 0, "queso"),
                new Ingredient("Queso cheddar", 0, "queso"),
                new Ingredient("Papas hilo", 0, "extras"),
                new Ingredient("Palta", 0, "vegetales"),
                new Ingredient("Jamón pierna", 0, "carnes"),
                new Ingredient("Cebolla frita", 0, "vegetales"),
                new Ingredient("Tomate", 0, "vegetales"),
                new Ingredient("Lechuga", 0, "vegetales"),
                new Ingredient("Tocino", 0, "carnes"),
                new Ingredient("Champiñón", 0, "vegetales"),
                new Ingredient("Aros de Cebolla", 0, "extras"),
                new Ingredient("Huevo frito", 0, "extras")
        );

        // Configuramos el adaptador personalizado
        CustomIngredientAdapter adapter = new CustomIngredientAdapter(this, allIngredients, selectedIngredientsQty);
        lvIngredients.setAdapter(adapter);
    }

    private void setupInteractions() {
        // 1. Selección de Sabor de Carne
        rgMeatFlavor.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbTraditional) meatFlavor = "Tradicional";
            else if (checkedId == R.id.rbBarbecue) meatFlavor = "Barbecue";
            else if (checkedId == R.id.rbSpicy) meatFlavor = "Picante";
        });

        // 2. Doble Hamburguesa
        cbDoublePatty.setOnCheckedChangeListener((buttonView, isChecked) -> calculatePrice());

        // 3. Selección de Ingredientes
        lvIngredients.setOnItemClickListener((parent, view, position, id) -> {
            Ingredient ing = allIngredients.get(position);

            // Lógica especial para Quesos (Multiplicadores)
            if (ing.getName().toLowerCase().contains("queso")) {
                showCheeseQuantityDialog(ing);
            } else {
                // Ingredientes normales (On/Off)
                if (selectedIngredientsQty.containsKey(ing.getName())) {
                    selectedIngredientsQty.remove(ing.getName());
                } else {
                    selectedIngredientsQty.put(ing.getName(), 1);
                }
                calculatePrice();
                // Actualizar vista visualmente
                ((CustomIngredientAdapter)lvIngredients.getAdapter()).notifyDataSetChanged();
            }
        });

        // 4. Botón "Agregar Salsas"
        btnProceedToSauces.setOnClickListener(v -> showSaucesDialog());
    }

    private void showCheeseQuantityDialog(Ingredient cheese) {
        final String[] options = {"Normal (x1)", "Extra (x2)", "Triple (x3)", "Cuádruple (x4)", "Quíntuple (x5)", "Quitar"};

        new MaterialAlertDialogBuilder(this)
                .setTitle("Cantidad de " + cheese.getName())
                .setItems(options, (dialog, which) -> {
                    if (which == 5) { // Opción Quitar
                        selectedIngredientsQty.remove(cheese.getName());
                    } else {
                        // which es 0 para x1, 1 para x2, etc.
                        selectedIngredientsQty.put(cheese.getName(), which + 1);
                    }
                    calculatePrice();
                    ((CustomIngredientAdapter)lvIngredients.getAdapter()).notifyDataSetChanged();
                })
                .show();
    }

    // --- LÓGICA DE PRECIOS ---
    private void calculatePrice() {
        double total = BASE_PRICE;

        // 1. Costo Carne Extra
        boolean isDouble = cbDoublePatty.isChecked();
        if (isDouble) {
            total += EXTRA_PATTY_PRICE;
        }

        // 2. Lógica de Ingredientes Gratis y Extras
        int freeIngredientsLimit = isDouble ? 3 : 2;

        // Contar total de unidades de ingredientes seleccionados
        int totalIngredientsCount = 0;
        for (int qty : selectedIngredientsQty.values()) {
            totalIngredientsCount += qty;
        }

        // Calcular costo extra si supera el límite gratuito
        if (totalIngredientsCount > freeIngredientsLimit) {
            int extraCount = totalIngredientsCount - freeIngredientsLimit;
            total += (extraCount * EXTRA_INGREDIENT_PRICE);
        }

        currentBurgerPrice = total;
        tvTotalPrice.setText("Total Burger: $" + (int)currentBurgerPrice);
    }

    private void showSaucesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_sauces, null);
        builder.setView(dialogView);

        CheckBox cbMayo = dialogView.findViewById(R.id.cbDialogMayo);
        CheckBox cbKetchup = dialogView.findViewById(R.id.cbDialogKetchup);
        CheckBox cbMustard = dialogView.findViewById(R.id.cbDialogMustard);
        CheckBox cbAji = dialogView.findViewById(R.id.cbDialogAji);
        CheckBox cbGarlic = dialogView.findViewById(R.id.cbDialogGarlic);

        Button btnConfirmSauces = dialogView.findViewById(R.id.btnConfirmSauces);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnConfirmSauces.setOnClickListener(v -> {
            selectedSauces.clear();
            if (cbMayo.isChecked()) selectedSauces.add("Mayonesa");
            if (cbKetchup.isChecked()) selectedSauces.add("Ketchup");
            if (cbMustard.isChecked()) selectedSauces.add("Mostaza");
            if (cbAji.isChecked()) selectedSauces.add("Ají");
            if (cbGarlic.isChecked()) selectedSauces.add("Salsa Ajo");

            dialog.dismiss();
            showFinalOptionsDialog(); // Ir al paso final
        });

        dialog.show();
    }

    private void showFinalOptionsDialog() {
        // Crear lista visual de ingredientes para guardar
        List<Ingredient> finalIngs = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : selectedIngredientsQty.entrySet()) {
            String name = entry.getKey();
            int qty = entry.getValue();
            String displayName = (qty > 1) ? name + " (x" + qty + ")" : name;
            finalIngs.add(new Ingredient(displayName, 0, "extra"));
        }

        // Crear objeto Burger
        Burger newBurger = new Burger();
        newBurger.setName("Hamburguesa " + meatFlavor + (cbDoublePatty.isChecked() ? " Doble" : " Simple"));
        newBurger.setIngredients(finalIngs);
        newBurger.setTotalPrice(currentBurgerPrice);

        // Guardar en Carrito
        CartManager.getInstance().addBurger(newBurger);

        // Preguntar Siguiente Acción
        new MaterialAlertDialogBuilder(this)
                .setTitle("🍔 ¡Hamburguesa lista!")
                .setMessage("Llevas " + CartManager.getInstance().getOrder().size() + " hamburguesa(s).\nTotal parcial: $" + (int)CartManager.getInstance().getTotalOrderPrice())
                .setCancelable(false)
                .setPositiveButton("PAGAR AHORA ➔", (dialog, which) -> {
                    // CAMBIO: Ahora vamos a PaymentMethodActivity para elegir medio de pago
                    Intent intent = new Intent(BuildBurgerActivity.this, PaymentMethodActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("➕ Agregar otra Hamburguesa", (dialog, which) -> {
                    // Reiniciamos la actividad para armar otra
                    recreate();
                    Toast.makeText(this, "¡Arma la siguiente!", Toast.LENGTH_SHORT).show();
                })
                .show();
    }
}