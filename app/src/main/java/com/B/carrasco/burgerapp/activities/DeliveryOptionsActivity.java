package com.B.carrasco.burgerapp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.B.carrasco.burgerapp.R;
import com.B.carrasco.burgerapp.utils.CartManager;

public class DeliveryOptionsActivity extends AppCompatActivity {

    private RadioGroup rgDeliveryType;
    private LinearLayout llAddressInput;
    private EditText etBlock, etDept, etCustomTip;
    private Button btnTip500, btnTip1000, btnTip0, btnContinue;

    private double selectedTip = 0.0;
    private boolean isDelivery = true; // Por defecto

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_options); // Asegúrate de tener el XML que te di antes

        initViews();
        setupInteractions();
    }

    private void initViews() {
        rgDeliveryType = findViewById(R.id.rgDeliveryType);
        llAddressInput = findViewById(R.id.llAddressInput);
        etBlock = findViewById(R.id.etBlock);
        etDept = findViewById(R.id.etDept);
        etCustomTip = findViewById(R.id.etCustomTip);

        btnTip500 = findViewById(R.id.btnTip500);
        btnTip1000 = findViewById(R.id.btnTip1000);
        btnTip0 = findViewById(R.id.btnTip0);
        btnContinue = findViewById(R.id.btnContinueToPay);
    }

    private void setupInteractions() {
        // 1. Alternar Domicilio / Retiro
        rgDeliveryType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbDelivery) {
                isDelivery = true;
                llAddressInput.setVisibility(View.VISIBLE);
            } else {
                isDelivery = false;
                llAddressInput.setVisibility(View.GONE);
            }
        });

        // 2. Botones de Propina Rápida
        btnTip500.setOnClickListener(v -> setTip(500, v));
        btnTip1000.setOnClickListener(v -> setTip(1000, v));
        btnTip0.setOnClickListener(v -> setTip(0, v));

        // 3. Propina Manual
        etCustomTip.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                resetTipButtons();
                try {
                    String clean = s.toString().trim();
                    if (!clean.isEmpty()) {
                        selectedTip = Double.parseDouble(clean);
                    } else {
                        selectedTip = 0.0;
                    }
                } catch (NumberFormatException e) {
                    selectedTip = 0.0;
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // 4. Validar y Continuar
        btnContinue.setOnClickListener(v -> {
            String addressFinal = "";

            if (isDelivery) {
                String block = etBlock.getText().toString().trim();
                String dept = etDept.getText().toString().trim();

                if (block.isEmpty() || dept.isEmpty()) {
                    Toast.makeText(this, "Por favor indica Block y Depto", Toast.LENGTH_SHORT).show();
                    return;
                }
                addressFinal = "Block " + block + ", Depto " + dept;
                CartManager.getInstance().setDeliveryMode("Domicilio");
            } else {
                addressFinal = "Retiro en Portería"; // Puedes poner el nombre del condominio si lo sabes
                CartManager.getInstance().setDeliveryMode("Retiro");
            }

            // Guardar en CartManager
            CartManager.getInstance().setDeliveryAddress(addressFinal);
            CartManager.getInstance().setTipAmount(selectedTip);

            // Ir al Pago
            Intent intent = new Intent(this, PaymentMethodActivity.class);
            startActivity(intent);
        });

        // Seleccionar Domicilio por defecto
        rgDeliveryType.check(R.id.rbDelivery);
    }

    private void setTip(double amount, View selectedBtn) {
        selectedTip = amount;
        etCustomTip.setText(""); // Limpiar manual
        resetTipButtons();

        // Resaltar seleccionado (Dorado)
        selectedBtn.setBackgroundColor(Color.parseColor("#FFC107"));
        ((Button)selectedBtn).setTextColor(Color.WHITE);
    }

    private void resetTipButtons() {
        int defaultColor = Color.parseColor("#EEEEEE");
        int defaultText = Color.parseColor("#333333");

        btnTip500.setBackgroundColor(defaultColor);
        btnTip500.setTextColor(defaultText);

        btnTip1000.setBackgroundColor(defaultColor);
        btnTip1000.setTextColor(defaultText);

        btnTip0.setBackgroundColor(Color.parseColor("#FFEBEE"));
        btnTip0.setTextColor(Color.RED);
    }
}