package com.B.carrasco.burgerapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.B.carrasco.burgerapp.R;
import com.B.carrasco.burgerapp.models.Ingredient;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.*;

public class BuildBurgerActivity extends AppCompatActivity {
    private ListView lvIngredients;
    private TextView tvSelectedCount, tvTotalPrice, tvBasePrice;
    private Button btnCreateBurger;
    private DatabaseHelper dbHelper;
    private List<Ingredient> allIngredients;
    private List<Ingredient> selectedIngredients;
    private double totalPrice = 0.0;
    private double basePrice = 2000.0;
    private String meatType = "Tradicional";
    private List<String> selectedSauces = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_build_burger);

        dbHelper = new DatabaseHelper(this);
        selectedIngredients = new ArrayList<>();
        initViews();
        loadIngredients();
        setupClickListeners();
        updateUI();
    }

    private void initViews() {
        lvIngredients = findViewById(R.id.lvIngredients);
        tvSelectedCount = findViewById(R.id.tvSelectedCount);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        tvBasePrice = findViewById(R.id.tvBasePrice);
        btnCreateBurger = findViewById(R.id.btnCreateBurger);
    }

    private void loadIngredients() {
        // Lista completa de ingredientes
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

        ArrayAdapter<Ingredient> adapter = new ArrayAdapter<Ingredient>(this,
                android.R.layout.simple_list_item_multiple_choice, allIngredients) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                Ingredient ingredient = getItem(position);
                ((TextView) view).setText(ingredient.getName() + " - $" + ingredient.getPrice());
                return view;
            }
        };

        lvIngredients.setAdapter(adapter);
        lvIngredients.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    private void setupClickListeners() {
        lvIngredients.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
            }
        });

        btnCreateBurger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedIngredients.size() >= 0) {
                    showSaucesDialog();
                }
            }
        });

        RadioGroup rgMeatType = findViewById(R.id.rgMeatType);
        rgMeatType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbTraditional) {
                    meatType = "Tradicional";
                } else if (checkedId == R.id.rbBarbecue) {
                    meatType = "Barbecue";
                }
                updateUI();
            }
        });
    }

    private void updateUI() {
        calculateTotalPrice();
        tvSelectedCount.setText("Ingredientes extra: " + selectedIngredients.size());
        tvBasePrice.setText("Base: $2000 (Pan + Doble Hamburguesa " + meatType + ")");
        tvTotalPrice.setText("Total: $" + totalPrice);
        btnCreateBurger.setEnabled(true);
    }

    private void calculateTotalPrice() {
        totalPrice = basePrice;

        for (int i = 0; i < selectedIngredients.size(); i++) {
            if (i < 3) {
                totalPrice += 300;
            } else {
                totalPrice += 500;
            }
        }
    }

    private void showSaucesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🍯 Selecciona tus Salsas");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_sauces, null);
        builder.setView(dialogView);

        CheckBox cbDialogMayo = dialogView.findViewById(R.id.cbDialogMayo);
        CheckBox cbDialogKetchup = dialogView.findViewById(R.id.cbDialogKetchup);
        CheckBox cbDialogMustard = dialogView.findViewById(R.id.cbDialogMustard);
        CheckBox cbDialogAji = dialogView.findViewById(R.id.cbDialogAji);

        builder.setPositiveButton("Continuar", (dialog, which) -> {
            // Guardar salsas seleccionadas
            selectedSauces.clear();
            if (cbDialogMayo.isChecked()) selectedSauces.add("Mayonesa");
            if (cbDialogKetchup.isChecked()) selectedSauces.add("Ketchup");
            if (cbDialogMustard.isChecked()) selectedSauces.add("Mostaza");
            if (cbDialogAji.isChecked()) selectedSauces.add("Ají");

            // Mostrar resumen antes de enviar
            showOrderSummary();
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void showOrderSummary() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("📋 Resumen de tu Pedido");

        // Construir mensaje del resumen
        StringBuilder summaryMessage = new StringBuilder();
        summaryMessage.append("¡Revisa tu hamburguesa antes de enviar el pedido!\n\n");

        summaryMessage.append("🍔 *Tu Hamburguesa Personalizada*\n\n");
        summaryMessage.append("• *Base:* Pan + Doble Hamburguesa ").append(meatType).append(" - $2000\n");

        if (!selectedIngredients.isEmpty()) {
            summaryMessage.append("\n• *Ingredientes Extra:*\n");
            for (int i = 0; i < selectedIngredients.size(); i++) {
                Ingredient ing = selectedIngredients.get(i);
                int price = (i < 3) ? 300 : 500;
                summaryMessage.append("   - ").append(ing.getName()).append(" - $").append(price).append("\n");
            }
        }

        if (!selectedSauces.isEmpty()) {
            summaryMessage.append("\n• *Salsas:* ").append(String.join(", ", selectedSauces)).append("\n");
        }

        summaryMessage.append("\n💰 *TOTAL A PAGAR: $").append(totalPrice).append("*\n\n");
        summaryMessage.append("¿Todo está correcto? El pedido se enviará al cocinero por WhatsApp.");

        builder.setMessage(summaryMessage.toString());

        builder.setPositiveButton("✅ Confirmar y Enviar", (dialog, which) -> {
            createOrderAndSendWhatsApp();
        });

        builder.setNegativeButton("✏️ Editar Pedido", (dialog, which) -> {
            // Volver a la pantalla de edición
            dialog.dismiss();
        });

        builder.setNeutralButton("❌ Cancelar", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void createOrderAndSendWhatsApp() {
        // Construir mensaje final para WhatsApp
        StringBuilder message = new StringBuilder();
        message.append("🍔 *NUEVO PEDIDO - BURGER APP* 🍔\n\n");
        message.append("*Base:* Pan + Doble Hamburguesa ").append(meatType).append(" - $2000\n");

        if (!selectedIngredients.isEmpty()) {
            message.append("\n*Ingredientes Extra:*\n");
            for (int i = 0; i < selectedIngredients.size(); i++) {
                Ingredient ing = selectedIngredients.get(i);
                int price = (i < 3) ? 300 : 500;
                message.append("• ").append(ing.getName()).append(" - $").append(price).append("\n");
            }
        }

        if (!selectedSauces.isEmpty()) {
            message.append("\n*Salsas:* ").append(String.join(", ", selectedSauces)).append("\n");
        }

        message.append("\n*TOTAL: $").append(totalPrice).append("*\n\n");
        message.append("⏰ *Hora:* ").append(java.text.SimpleDateFormat.getDateTimeInstance().format(new Date())).append("\n");
        message.append("📱 *Pedido desde App BurgerApp*");

        // Enviar por WhatsApp
        sendWhatsAppMessage(message.toString());

        Toast.makeText(this, "¡Pedido confirmado! Total: $" + totalPrice, Toast.LENGTH_LONG).show();
        finish();
    }

    private void sendWhatsAppMessage(String message) {
        try {
            String phoneNumber = "+56912345678"; // REEMPLAZA con número real del cocinero
            String url = "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" + Uri.encode(message);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);

        } catch (Exception e) {
            // Fallback: mostrar mensaje completo en la app
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("📞 Pedido Listo para Enviar")
                    .setMessage("No se pudo abrir WhatsApp automáticamente.\n\n" +
                            "Copia este mensaje y envíalo manualmente al cocinero:\n\n" + message)
                    .setPositiveButton("Copiar Mensaje", (dialog, which) -> {
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("Pedido Burger", message);
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(BuildBurgerActivity.this, "Mensaje copiado", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("OK", null)
                    .show();
        }
    }
}