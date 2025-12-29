package com.B.carrasco.burgerapp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.B.carrasco.burgerapp.Config;
import com.B.carrasco.burgerapp.R;
import com.B.carrasco.burgerapp.adapters.CustomIngredientAdapter;
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
    private EditText etChefNotes; // <--- NUEVO CAMPO

    private List<Ingredient> allIngredients;
    private Map<String, Integer> selectedIngredientsQty = new HashMap<>();
    private FirebaseFirestore db;

    // USAMOS LOS PRECIOS DE LA NUBE (Config)
    private final double DOUBLE_PATTY_PRICE = 1200.0;
    private final double EXTRA_SINGLE_PRICE = 300.0;
    private final double EXTRA_PROMO_PRICE = 500.0;
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
    }

    private void initViews() {
        lvIngredients = findViewById(R.id.lvIngredients);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnProceedToSauces = findViewById(R.id.btnCreateBurger);
        cbDoublePatty = findViewById(R.id.cbDoublePatty);
        rgMeatFlavor = findViewById(R.id.rgMeatType);
        etChefNotes = findViewById(R.id.etChefNotes); // <--- VINCULAMOS EL EDIT TEXT
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

                        // --- MAGIA: SI ES UN RE-PEDIDO, CARGAMOS LOS DATOS ---
                        if (getIntent().hasExtra("REORDER_DESC")) {
                            String oldDesc = getIntent().getStringExtra("REORDER_DESC");
                            reconstructOrder(oldDesc);
                        }
                    } else {
                        loadFallbackIngredients();
                    }
                });
    }

    // --- EL RECONSTRUCTOR DE PEDIDOS ---
    private void reconstructOrder(String desc) {
        if (desc == null) return;
        String descLower = desc.toLowerCase();

        if (descLower.contains("picante")) {
            rgMeatFlavor.check(R.id.rbSpicy);
            meatFlavor = "Picante";
        } else if (descLower.contains("bbq")) {
            rgMeatFlavor.check(R.id.rbBarbecue);
            meatFlavor = "BBQ";
        } else {
            rgMeatFlavor.check(R.id.rbTraditional);
        }

        if (descLower.contains("doble")) cbDoublePatty.setChecked(true);

        selectedIngredientsQty.clear();
        for (Ingredient ing : allIngredients) {
            String name = ing.getName();
            if (desc.contains(name)) {
                int qty = 1;
                if (desc.contains(name + " (x2)")) qty = 2;
                if (desc.contains(name + " (x3)")) qty = 3;
                selectedIngredientsQty.put(name, qty);
            }
        }
        ((CustomIngredientAdapter)lvIngredients.getAdapter()).notifyDataSetChanged();
        calculatePrice();
        Toast.makeText(this, "✅ Ingredientes cargados del historial", Toast.LENGTH_LONG).show();
    }

    private void loadFallbackIngredients() {
        allIngredients = Arrays.asList(
                new Ingredient("Queso Mantecoso", true),
                new Ingredient("Tomate", true),
                new Ingredient("Lechuga", true)
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
        cbDoublePatty.setOnCheckedChangeListener((b, c) -> calculatePrice());

        lvIngredients.setOnItemClickListener((parent, view, position, id) -> {
            Ingredient ing = allIngredients.get(position);
            if (ing.getName().toLowerCase().contains("queso")) showCheeseQuantityDialog(ing);
            else toggleIngredient(ing.getName());
        });

        btnProceedToSauces.setOnClickListener(v -> showSaucesDialog());
    }

    private void toggleIngredient(String name) {
        if (selectedIngredientsQty.containsKey(name)) selectedIngredientsQty.remove(name);
        else {
            if (getTotalIngredientCount() >= MAX_INGREDIENTS) {
                Toast.makeText(this, "⚠️ Máximo " + MAX_INGREDIENTS + " ingredientes", Toast.LENGTH_SHORT).show();
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
                    if (which == 3) selectedIngredientsQty.remove(cheese.getName());
                    else selectedIngredientsQty.put(cheese.getName(), which + 1);
                    calculatePrice();
                    ((CustomIngredientAdapter)lvIngredients.getAdapter()).notifyDataSetChanged();
                }).show();
    }

    private int getTotalIngredientCount() {
        int total = 0;
        for (int qty : selectedIngredientsQty.values()) total += qty;
        return total;
    }

    private void calculatePrice() {
        double total = Config.PRECIO_BASE_HAMBURGUESA;

        boolean isDouble = cbDoublePatty.isChecked();
        if (isDouble) total += DOUBLE_PATTY_PRICE;

        int freeIngredientsLimit = isDouble ? 3 : 2;
        int payableIngredients = getTotalIngredientCount() - freeIngredientsLimit;

        if (payableIngredients > 0) {
            int pairs = payableIngredients / 2;
            int remainder = payableIngredients % 2;
            total += (pairs * EXTRA_PROMO_PRICE) + (remainder * EXTRA_SINGLE_PRICE);
        }
        currentBurgerPrice = total;
        tvTotalPrice.setText("Total: $" + (int)currentBurgerPrice);
    }

    private void showSaucesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_sauces, null);
        builder.setView(view);

        // Recuperar CheckBoxes del XML del dialogo
        CheckBox cbMayo = view.findViewById(R.id.cbDialogMayo);
        CheckBox cbKetchup = view.findViewById(R.id.cbDialogKetchup);
        CheckBox cbMustard = view.findViewById(R.id.cbDialogMustard);
        CheckBox cbSpicy = view.findViewById(R.id.cbDialogSpicy);
        Button btnConfirm = view.findViewById(R.id.btnConfirmSauces);

        AlertDialog dialog = builder.create();
        if(dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        btnConfirm.setOnClickListener(v -> {
            // Guardar salsas seleccionadas
            selectedSauces.clear();
            if (cbMayo != null && cbMayo.isChecked()) selectedSauces.add("Mayonesa");
            if (cbKetchup != null && cbKetchup.isChecked()) selectedSauces.add("Ketchup");
            if (cbMustard != null && cbMustard.isChecked()) selectedSauces.add("Mostaza");
            if (cbSpicy != null && cbSpicy.isChecked()) selectedSauces.add("Salsa Picante");

            dialog.dismiss();

            // IR AL PASO FINAL
            showFinalOptionsDialog();
        });

        dialog.show();
    }

    private void showFinalOptionsDialog() {
        List<Ingredient> finalIngs = new ArrayList<>();

        // 1. Ingredientes normales
        for (Map.Entry<String, Integer> entry : selectedIngredientsQty.entrySet()) {
            String name = entry.getKey();
            int qty = entry.getValue();
            String displayName = (qty > 1) ? name + " (x" + qty + ")" : name;
            finalIngs.add(new Ingredient(displayName, true));
        }

        // 2. Salsas
        if (!selectedSauces.isEmpty()) {
            StringBuilder saucesStr = new StringBuilder("Salsas: ");
            for (String sauce : selectedSauces) saucesStr.append(sauce).append(", ");
            String finalSauces = saucesStr.substring(0, saucesStr.length() - 2);
            finalIngs.add(new Ingredient(finalSauces, true));
        }

        // --- 3. NUEVO: CAPTURAR NOTA DEL CHEF ---
        String notasChef = etChefNotes.getText().toString().trim();
        if (!notasChef.isEmpty()) {
            // Se agrega como un ingrediente especial para que aparezca en todos lados
            finalIngs.add(new Ingredient("📝 NOTA: " + notasChef, true));
        }
        // ----------------------------------------

        Burger newBurger = new Burger();
        newBurger.setName("Burger " + meatFlavor + (cbDoublePatty.isChecked() ? " Doble" : " Simple"));
        newBurger.setIngredients(finalIngs);
        newBurger.setTotalPrice(currentBurgerPrice);

        CartManager.getInstance().addBurger(newBurger);

        new MaterialAlertDialogBuilder(this)
                .setTitle("🍔 ¡Hamburguesa Lista!")
                .setMessage("Tu creación ha sido guardada en el carrito.\n¿Qué hacemos ahora?")
                .setCancelable(false)
                .setPositiveButton("IR A PEDIR ➔", (dialog, which) -> {
                    // FLUJO NUEVO: IR A DIRECCIÓN/ENTREGA
                    Intent intent = new Intent(BuildBurgerActivity.this, DeliveryOptionsActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("➕ Armar Otra", (dialog, which) -> {
                    // Limpiar notas para la siguiente burger
                    etChefNotes.setText("");
                    Intent intent = new Intent(this, BuildBurgerActivity.class);
                    startActivity(intent);
                    finish();
                })
                .show();
    }
}