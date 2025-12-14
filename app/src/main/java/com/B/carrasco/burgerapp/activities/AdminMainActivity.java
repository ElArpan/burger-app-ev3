package com.B.carrasco.burgerapp.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat; // Asegúrate de usar este import
import androidx.cardview.widget.CardView;

import com.B.carrasco.burgerapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminMainActivity extends AppCompatActivity {

    private TextView tvAdminHeader, tvStatusIndicator;
    private SwitchCompat switchStoreStatus;
    private CardView cardManageOrders, cardManageIngredients, cardStoreControl;
    private Button btnLogout;
    private String username;

    // Firebase
    private FirebaseFirestore db;
    private DocumentReference storeConfigRef;
    private boolean isUpdatingSwitch = false; // Para evitar bucles infinitos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        db = FirebaseFirestore.getInstance();
        storeConfigRef = db.collection("config").document("store");

        username = getIntent().getStringExtra("USERNAME");

        initViews();
        setupClickListeners();

        // Escuchar el estado actual de la tienda en tiempo real
        setupStoreListener();
    }

    private void initViews() {
        tvAdminHeader = findViewById(R.id.tvAdminHeader);
        if (username != null) {
            tvAdminHeader.setText("Panel de " + username);
        }

        tvStatusIndicator = findViewById(R.id.tvStatusIndicator);
        switchStoreStatus = findViewById(R.id.switchStoreStatus);

        cardManageOrders = findViewById(R.id.cardManageOrders);
        cardManageIngredients = findViewById(R.id.cardManageIngredients);
        cardStoreControl = findViewById(R.id.cardStoreControl);

        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setupStoreListener() {
        // Escuchar cambios en la nube
        storeConfigRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) return;

            if (snapshot != null && snapshot.exists()) {
                Boolean isOpen = snapshot.getBoolean("isOpen");
                boolean state = (isOpen != null) && isOpen;

                // Actualizar UI
                updateUIState(state);

                // Actualizar switch sin disparar el listener del switch
                isUpdatingSwitch = true;
                switchStoreStatus.setChecked(state);
                isUpdatingSwitch = false;
            } else {
                // Si no existe el documento, lo creamos cerrado por defecto
                createDefaultConfig();
            }
        });
    }

    private void createDefaultConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("isOpen", false);
        storeConfigRef.set(config);
    }

    private void updateUIState(boolean isOpen) {
        if (isOpen) {
            tvStatusIndicator.setText("🟢 ABIERTO");
            tvStatusIndicator.setTextColor(Color.parseColor("#4CAF50")); // Verde
            cardStoreControl.setCardBackgroundColor(Color.parseColor("#1B5E20")); // Fondo verde oscuro
            switchStoreStatus.setText("El local está recibiendo pedidos");
        } else {
            tvStatusIndicator.setText("🔴 CERRADO");
            tvStatusIndicator.setTextColor(Color.parseColor("#FF5252")); // Rojo claro
            cardStoreControl.setCardBackgroundColor(Color.parseColor("#263238")); // Fondo gris oscuro
            switchStoreStatus.setText("El local está cerrado");
        }
    }

    private void setupClickListeners() {
        // Lógica del Switch (Cambiar estado en la Nube)
        switchStoreStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingSwitch) return; // Si el cambio vino de la nube, no hacemos nada

            // Enviar cambio a Firebase
            storeConfigRef.update("isOpen", isChecked)
                    .addOnFailureListener(e -> {
                        Toast.makeText(AdminMainActivity.this, "Error al actualizar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        // Revertir visualmente si falló
                        isUpdatingSwitch = true;
                        switchStoreStatus.setChecked(!isChecked);
                        isUpdatingSwitch = false;
                    });
        });

        // Ir a Pedidos
        cardManageOrders.setOnClickListener(v -> {
            startActivity(new Intent(AdminMainActivity.this, AdminOrdersActivity.class));
        });

        // Ir a Ingredientes
        cardManageIngredients.setOnClickListener(v -> {
            startActivity(new Intent(AdminMainActivity.this, ManageIngredientsActivity.class));
        });

        // Cerrar Sesión
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Salir del modo administrador?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(AdminMainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}