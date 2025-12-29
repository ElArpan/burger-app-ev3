package com.B.carrasco.burgerapp.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.B.carrasco.burgerapp.MainActivity;
import com.B.carrasco.burgerapp.R;
import com.B.carrasco.burgerapp.utils.CartManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PaymentMethodActivity extends AppCompatActivity {

    // --- DATOS BANCARIOS ---
    private final String MY_BANK_NAME = "Banco Estado";
    private final String MY_ACCOUNT_TYPE = "Cuenta RUT";
    private final String MY_RUT = "17.807.475-k";
    private final String MY_ACCOUNT_NUM = "0017807475";
    private final String MY_HOLDER_NAME = "Byron Carrasco";
    private final String CHEF_PHONE_NUMBER = "56977193187"; // ¡PON TU NÚMERO AQUÍ!

    private TextView tvTotalAmount, tvChange;
    private RadioButton rbCash, rbTransfer;
    private LinearLayout llCashInput, llTransferDetails;
    private LinearLayout layoutCopyRut, layoutCopyAccount;
    private EditText etCashAmount;
    private Button btnConfirmPayment;
    private CardView cardCash, cardTransfer;

    private TextView tvBankName, tvAccountType, tvRutData, tvAccountData, tvHolderName;

    private double totalAmount = 0.0;
    private double changeAmount = 0.0;
    private boolean isCashSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);

        // CALCULAR TOTAL (Comida + Propina)
        double foodTotal = CartManager.getInstance().getTotalOrderPrice();
        double tip = CartManager.getInstance().getTipAmount();
        totalAmount = foodTotal + tip;

        initViews();

        // Mostrar desglose en el botón
        tvTotalAmount.setText("Total a Pagar: $" + (int)totalAmount);
        if (tip > 0) {
            tvTotalAmount.append("\n(Incluye $" + (int)tip + " propina)");
        }

        setupBankData();
        setupInteractions();
    }

    private void initViews() {
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        rbCash = findViewById(R.id.rbCash);
        rbTransfer = findViewById(R.id.rbTransfer);
        llCashInput = findViewById(R.id.llCashInput);
        llTransferDetails = findViewById(R.id.llTransferDetails);
        etCashAmount = findViewById(R.id.etCashAmount);
        tvChange = findViewById(R.id.tvChange);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);
        cardCash = findViewById(R.id.cardCash);
        cardTransfer = findViewById(R.id.cardTransfer);
        tvHolderName = findViewById(R.id.tvHolderName);
        tvBankName = findViewById(R.id.tvBankName);
        tvAccountType = findViewById(R.id.tvAccountType);
        tvRutData = findViewById(R.id.tvRutData);
        tvAccountData = findViewById(R.id.tvAccountData);
        layoutCopyRut = findViewById(R.id.layoutCopyRut);
        layoutCopyAccount = findViewById(R.id.layoutCopyAccount);
    }

    private void setupBankData() {
        tvHolderName.setText("Titular: " + MY_HOLDER_NAME);
        tvBankName.setText("Banco: " + MY_BANK_NAME);
        tvAccountType.setText("Tipo: " + MY_ACCOUNT_TYPE);
        tvRutData.setText("RUT: " + MY_RUT);
        tvAccountData.setText("N° Cuenta: " + MY_ACCOUNT_NUM);
    }

    private void setupInteractions() {
        View.OnClickListener cashListener = v -> {
            isCashSelected = true;
            rbCash.setChecked(true);
            rbTransfer.setChecked(false);
            llCashInput.setVisibility(View.VISIBLE);
            llTransferDetails.setVisibility(View.GONE);
            btnConfirmPayment.setText("FINALIZAR PEDIDO");
            validateCash();
        };
        cardCash.setOnClickListener(cashListener);
        rbCash.setOnClickListener(cashListener);

        View.OnClickListener transferListener = v -> {
            isCashSelected = false;
            rbCash.setChecked(false);
            rbTransfer.setChecked(true);
            llCashInput.setVisibility(View.GONE);
            llTransferDetails.setVisibility(View.VISIBLE);
            btnConfirmPayment.setText("YA TRANSFERÍ, SUBIR FOTO");
            btnConfirmPayment.setEnabled(true);
            btnConfirmPayment.setAlpha(1.0f);
        };
        cardTransfer.setOnClickListener(transferListener);
        rbTransfer.setOnClickListener(transferListener);

        layoutCopyRut.setOnClickListener(v -> copyToClipboard("RUT", MY_RUT));
        layoutCopyAccount.setOnClickListener(v -> copyToClipboard("Cuenta", MY_ACCOUNT_NUM));

        etCashAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { validateCash(); }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnConfirmPayment.setOnClickListener(v -> {
            if (isCashSelected) {
                submitCashOrder();
            } else {
                goToDepositUpload();
            }
        });
    }

    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, label + " copiado ✅", Toast.LENGTH_SHORT).show();
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
                tvChange.setText("Vuelto para repartidor: $" + (int)changeAmount);
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
        order.put("orderDescription", CartManager.getInstance().getOrderSummary());

        // --- GUARDADO DE DATOS DE ENTREGA ---
        order.put("deliveryMode", CartManager.getInstance().getDeliveryMode());
        order.put("deliveryAddress", CartManager.getInstance().getDeliveryAddress());
        order.put("tipAmount", CartManager.getInstance().getTipAmount());

        order.put("payWith", Double.parseDouble(etCashAmount.getText().toString()));
        order.put("changeDue", changeAmount);

        db.collection("orders").add(order)
                .addOnSuccessListener(documentReference -> {
                    // LIMPIAR Y ENVIAR WHATSAPP
                    String desc = CartManager.getInstance().getOrderSummary();
                    CartManager.getInstance().clearCart();

                    sendWhatsAppToChef("Efectivo", (int)changeAmount, desc);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnConfirmPayment.setEnabled(true);
                    btnConfirmPayment.setText("REINTENTAR");
                });
    }

    private void sendWhatsAppToChef(String method, int change, String desc) {
        try {
            String message = "🔔 *NUEVO PEDIDO BURGER APP*\n\n" +
                    "👤 Cliente: " + FirebaseAuth.getInstance().getCurrentUser().getEmail() + "\n" +
                    "🍔 Detalle: " + desc + "\n" +
                    "💰 Total: $" + (int)totalAmount + "\n" +
                    "💳 Pago: " + method + "\n";

            if (change > 0) message += "⚠️ LLEVAR VUELTO: $" + change + "\n";

            // Datos de Entrega en el mensaje
            message += "\n📍 Entrega: " + CartManager.getInstance().getDeliveryMode() + "\n" +
                    "🏠 Dirección: " + CartManager.getInstance().getDeliveryAddress();

            String url = "https://api.whatsapp.com/send?phone=" + CHEF_PHONE_NUMBER + "&text=" + java.net.URLEncoder.encode(message, "UTF-8");

            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
            finish();
        } catch (Exception e) {
            // Si falla, volver al inicio igual
            Intent intent = new Intent(PaymentMethodActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }
}