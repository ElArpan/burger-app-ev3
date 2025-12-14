package com.B.carrasco.burgerapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.B.carrasco.burgerapp.R;
import com.B.carrasco.burgerapp.utils.CartManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PaymentMethodActivity extends AppCompatActivity {

    private TextView tvTotalAmount, tvChange;
    private RadioButton rbCash, rbTransfer;
    private LinearLayout llCashInput;
    private EditText etCashAmount;
    private Button btnConfirmPayment;
    private CardView cardCash, cardTransfer;

    private double totalAmount = 0.0;
    private double changeAmount = 0.0;
    private boolean isCashSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);

        // Obtener total del carrito
        totalAmount = CartManager.getInstance().getTotalOrderPrice();

        initViews();
        setupInteractions();
    }

    private void initViews() {
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvTotalAmount.setText("Total a Pagar: $" + (int)totalAmount);

        rbCash = findViewById(R.id.rbCash);
        rbTransfer = findViewById(R.id.rbTransfer);
        llCashInput = findViewById(R.id.llCashInput);
        etCashAmount = findViewById(R.id.etCashAmount);
        tvChange = findViewById(R.id.tvChange);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);

        cardCash = findViewById(R.id.cardCash);
        cardTransfer = findViewById(R.id.cardTransfer);
    }

    private void setupInteractions() {
        // --- LÓGICA EFECTIVO ---
        View.OnClickListener cashListener = v -> {
            isCashSelected = true;
            rbCash.setChecked(true);
            rbTransfer.setChecked(false);
            llCashInput.setVisibility(View.VISIBLE);
            btnConfirmPayment.setText("FINALIZAR PEDIDO");
            validateCash(); // Verificamos si ya escribió algo
        };
        cardCash.setOnClickListener(cashListener);
        rbCash.setOnClickListener(cashListener);

        // --- LÓGICA TRANSFERENCIA ---
        View.OnClickListener transferListener = v -> {
            isCashSelected = false;
            rbCash.setChecked(false);
            rbTransfer.setChecked(true);
            llCashInput.setVisibility(View.GONE);

            btnConfirmPayment.setText("IR A SUBIR COMPROBANTE");
            btnConfirmPayment.setEnabled(true);
            btnConfirmPayment.setAlpha(1.0f);
        };
        cardTransfer.setOnClickListener(transferListener);
        rbTransfer.setOnClickListener(transferListener);

        // --- CÁLCULO DE VUELTO ---
        etCashAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateCash();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // --- BOTÓN CONFIRMAR ---
        btnConfirmPayment.setOnClickListener(v -> {
            if (isCashSelected) {
                submitCashOrder();
            } else {
                goToDepositUpload();
            }
        });
    }

    private void validateCash() {
        String input = etCashAmount.getText().toString().trim();
        if (input.isEmpty()) {
            tvChange.setText("Vuelto necesario: $0");
            btnConfirmPayment.setEnabled(false);
            btnConfirmPayment.setAlpha(0.5f);
            return;
        }

        try {
            double payAmount = Double.parseDouble(input);
            if (payAmount < totalAmount) {
                tvChange.setText("Falta dinero ($" + (int)(totalAmount - payAmount) + ")");
                tvChange.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                btnConfirmPayment.setEnabled(false);
                btnConfirmPayment.setAlpha(0.5f);
            } else {
                changeAmount = payAmount - totalAmount;
                tvChange.setText("Vuelto para el repartidor: $" + (int)changeAmount);
                tvChange.setTextColor(getResources().getColor(R.color.success_green));
                btnConfirmPayment.setEnabled(true);
                btnConfirmPayment.setAlpha(1.0f);
            }
        } catch (NumberFormatException e) {
            btnConfirmPayment.setEnabled(false);
        }
    }

    private void goToDepositUpload() {
        Intent intent = new Intent(this, DepositUploadActivity.class);
        intent.putExtra("TOTAL_AMOUNT", totalAmount);
        startActivity(intent);
    }

    private void submitCashOrder() {
        btnConfirmPayment.setEnabled(false);
        btnConfirmPayment.setText("Enviando...");

        // Guardar en Firebase (Firestore)
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        Map<String, Object> order = new HashMap<>();
        order.put("userId", userId);
        order.put("clientEmail", userEmail);
        order.put("totalPrice", totalAmount);
        order.put("paymentMethod", "Efectivo");
        order.put("status", "Pendiente de Envío");
        order.put("createdAt", new Date());

        // Datos del carrito y vuelto
        order.put("orderDescription", CartManager.getInstance().getOrderSummary());
        order.put("payWith", Double.parseDouble(etCashAmount.getText().toString()));
        order.put("changeDue", changeAmount);

        db.collection("orders").add(order)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "✅ ¡Pedido recibido!", Toast.LENGTH_LONG).show();
                    CartManager.getInstance().clearCart();

                    // Ir al inicio
                    Intent intent = new Intent(PaymentMethodActivity.this, ClientMainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnConfirmPayment.setEnabled(true);
                    btnConfirmPayment.setText("REINTENTAR");
                });
    }
}