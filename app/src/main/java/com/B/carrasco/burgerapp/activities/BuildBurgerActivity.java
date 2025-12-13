package com.B.carrasco.burgerapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.B.carrasco.burgerapp.R;
import com.B.carrasco.burgerapp.database.DatabaseHelper;
import com.B.carrasco.burgerapp.models.Ingredient;

import java.text.SimpleDateFormat;
import java.util.*;

public class BuildBurgerActivity extends AppCompatActivity {
    private ListView lvIngredients;
    private TextView tvSelectedCount, tvTotalPrice;
    private Button btnCreateBurger;
    private CheckBox cbDoublePatty;
    private DatabaseHelper dbHelper;
    private List<Ingredient> allIngredients;
    private final List<Ingredient> selectedIngredients = new ArrayList<>();
    private double totalPrice = 0.0;
    private final double BUN_PRICE = 800.0;
    private final double SINGLE_PATTY_PRICE = 600.0;
    private final double DOUBLE_PATTY_PRICE = 1200.0;
    private double currentPattyPrice = SINGLE_PATTY_PRICE;
    private final List<String> selectedSauces = new ArrayList<>();
    private String meatType = "Tradicional";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_burger);

        dbHelper = new DatabaseHelper(this);
        initViews();
        loadIngredients();
        setupClickListeners();
        updateUI();
    }

    private void initViews() {
        lvIngredients = findViewById(R.id.lvIngredients);
        tvSelectedCount = findViewById(R.id.tvSelectedCount);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        btnCreateBurger = findViewById(R.id.btnCreateBurger);
        cbDoublePatty = findViewById(R.id.cbDoublePatty);
    }

    private void loadIngredients() {
        allIngredients = dbHelper.getAllIngredients();
        if (allIngredients == null || allIngredients.isEmpty()) {
            // fallback
            allIngredients = Arrays.asList(
                    new Ingredient("Queso mantecoso", 300, "queso"),
                    new Ingredient("Queso cheddar", 300, "queso"),
                    new Ingredient("Papas hilo", 300, "extras"),
                    new Ingredient("Palta", 300, "vegetales"),
                    new Ingredient("Jamón pierna", 300, "carnes"),
                    new Ingredient("Cebolla frita", 300, "vegetales"),
                    new Ingredient("Tomate", 300, "vegetales"),
                    new Ingredient("Lechuga", 300, "vegetales"),
                    new Ingredient("Tocino", 300, "carnes"),
                    new Ingredient("Champiñón", 300, "vegetales")
            );
        }

        ArrayAdapter<Ingredient> adapter = new ArrayAdapter<Ingredient>(this,
                android.R.layout.simple_list_item_multiple_choice, allIngredients) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                Ingredient ingredient = getItem(position);
                ((TextView) view).setText(ingredient.getName() + " - $" + (int) ingredient.getPrice());
                return view;
            }
        };

        lvIngredients.setAdapter(adapter);
        lvIngredients.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    private void setupClickListeners() {
        lvIngredients.setOnItemClickListener((parent, view, position, id) -> {
            Ingredient ingredient = allIngredients.get(position);
            CheckedTextView checkedTextView = (CheckedTextView) view;

            if (checkedTextView.isChecked()) {
                if (selectedIngredients.size() < 10) {
                    selectedIngredients.add(ingredient);
                } else {
                    checkedTextView.setChecked(false);
                    Toast.makeText(BuildBurgerActivity.this,
                            "Máximo 10 ingredientes extra", Toast.LENGTH_SHORT).show();
                }
            } else {
                selectedIngredients.remove(ingredient);
            }
            updateUI();
        });

        btnCreateBurger.setOnClickListener(v -> showSaucesDialog());

        RadioGroup rgMeatType = findViewById(R.id.rgMeatType);
        rgMeatType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbTraditional) {
                meatType = "Tradicional";
            } else if (checkedId == R.id.rbBarbecue) {
                meatType = "Barbecue";
            }
            updateUI();
        });

        cbDoublePatty.setOnCheckedChangeListener((buttonView, isChecked) -> {
            currentPattyPrice = isChecked ? DOUBLE_PATTY_PRICE : SINGLE_PATTY_PRICE;
            updateUI();
        });
    }

    private void updateUI() {
        calculateTotalPrice();
        tvSelectedCount.setText("Ingredientes: " + selectedIngredients.size());
        tvTotalPrice.setText("Total: $" + (int) totalPrice);
    }

    private void calculateTotalPrice() {
        totalPrice = BUN_PRICE + currentPattyPrice;
        for (int i = 0; i < selectedIngredients.size(); i++) {
            totalPrice += (i < 3) ? 300 : 500;
        }
    }

    private void showSaucesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona tus salsas");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_sauces, null);
        builder.setView(dialogView);

        CheckBox cbDialogMayo = dialogView.findViewById(R.id.cbDialogMayo);
        CheckBox cbDialogKetchup = dialogView.findViewById(R.id.cbDialogKetchup);
        CheckBox cbDialogMustard = dialogView.findViewById(R.id.cbDialogMustard);
        CheckBox cbDialogAji = dialogView.findViewById(R.id.cbDialogAji);

        builder.setPositiveButton("Continuar", (dialog, which) -> {
            selectedSauces.clear();
            if (cbDialogMayo.isChecked()) selectedSauces.add("Mayonesa");
            if (cbDialogKetchup.isChecked()) selectedSauces.add("Ketchup");
            if (cbDialogMustard.isChecked()) selectedSauces.add("Mostaza");
            if (cbDialogAji.isChecked()) selectedSauces.add("Ají");

            showOrderSummary();
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void showOrderSummary() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Resumen del pedido");

        StringBuilder summary = new StringBuilder();
        summary.append("Base: Pan - $").append((int)BUN_PRICE).append("\n");
        summary.append("Carnes: ").append(cbDoublePatty.isChecked() ? "Doble - $" + (int)DOUBLE_PATTY_PRICE : "Simple - $" + (int)SINGLE_PATTY_PRICE).append("\n\n");

        if (!selectedIngredients.isEmpty()) {
            summary.append("Ingredientes:\n");
            for (int i = 0; i < selectedIngredients.size(); i++) {
                Ingredient ing = selectedIngredients.get(i);
                int price = (i < 3) ? 300 : 500;
                summary.append("• ").append(ing.getName()).append(" - $").append(price).append("\n");
            }
            summary.append("\n");
        }

        if (!selectedSauces.isEmpty()) {
            summary.append("Salsas: ").append(String.join(", ", selectedSauces)).append("\n\n");
        }

        summary.append("TOTAL: $").append((int) totalPrice).append("\n");
        summary.append("Horario: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));

        builder.setMessage(summary.toString());

        builder.setPositiveButton("Confirmar y enviar", (dialog, which) -> {
            createOrderAndSendWhatsApp(summary.toString());
        });

        builder.setNegativeButton("Editar", null);
        builder.show();
    }

    private void createOrderAndSendWhatsApp(String message) {
        try {
            String phoneNumber = "+56977193187"; // reemplaza con número real
            String url = "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" + Uri.encode(message);
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            Toast.makeText(this, "Pedido enviado", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Pedido listo")
                    .setMessage("No se pudo abrir WhatsApp. Copia el mensaje:\n\n" + message)
                    .setPositiveButton("Copiar", (d, w) -> {
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("Pedido", message);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(this, "Copiado", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("OK", null)
                    .show();
        }
    }
}