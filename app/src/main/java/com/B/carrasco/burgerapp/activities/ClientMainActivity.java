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
import androidx.cardview.widget.CardView; // Importante para las tarjetas

import com.B.carrasco.burgerapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class ClientMainActivity extends AppCompatActivity {

    private TextView tvWelcome, tvStoreStatus, tvStatusEmoji;
    private CardView cardBuildBurger, cardHistory, cardDeposit, cardStoreStatus;
    private Button btnLogout;
    private String username;

    // Control de Tienda
    private boolean isStoreOpen = false; // Por defecto cerrado hasta que cargue
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_main);

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        // Recuperar nombre
        username = getIntent().getStringExtra("USERNAME");
        if (username == null) username = "Vecino";

        initViews();
        setupClickListeners();
        setDynamicGreeting();

        // Iniciar el listener que revisa si la tienda está abierta
        setupStoreStatusListener();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvStoreStatus = findViewById(R.id.tvStoreStatus);
        tvStatusEmoji = findViewById(R.id.tvStatusEmoji);

        // Ahora usamos Cards en lugar de botones simples
        cardBuildBurger = findViewById(R.id.cardBuildBurger);
        cardHistory = findViewById(R.id.cardHistory);
        cardDeposit = findViewById(R.id.cardDeposit);
        cardStoreStatus = findViewById(R.id.cardStoreStatus);

        btnLogout = findViewById(R.id.btnLogout);
    }

    private void setDynamicGreeting() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        String greeting;

        if (timeOfDay >= 0 && timeOfDay < 12) {
            greeting = "Buenos días";
        } else if (timeOfDay >= 12 && timeOfDay < 20) {
            greeting = "Buenas tardes";
        } else {
            greeting = "Buenas noches";
        }

        tvWelcome.setText(greeting + ", " + username);
    }

    // --- MAGIA: Listener en Tiempo Real del Estado de la Tienda ---
    private void setupStoreStatusListener() {
        // Escuchamos el documento "store" en la colección "config"
        db.collection("config").document("store")
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        return; // Error silencioso
                    }

                    if (snapshot != null && snapshot.exists()) {
                        Boolean open = snapshot.getBoolean("isOpen");
                        isStoreOpen = (open != null) && open;
                        updateStoreStatusUI(isStoreOpen);
                    } else {
                        // Si no existe la configuración, asumimos cerrado por seguridad
                        updateStoreStatusUI(false);
                    }
                });
    }

    private void updateStoreStatusUI(boolean isOpen) {
        if (isOpen) {
            tvStoreStatus.setText("ABIERTO");
            tvStoreStatus.setTextColor(Color.parseColor("#4CAF50")); // Verde
            tvStatusEmoji.setText("🟢");
            // Habilitar visualmente la tarjeta de pedir
            cardBuildBurger.setAlpha(1.0f);
        } else {
            tvStoreStatus.setText("CERRADO");
            tvStoreStatus.setTextColor(Color.parseColor("#D32F2F")); // Rojo
            tvStatusEmoji.setText("🔴");
            // Deshabilitar visualmente la tarjeta de pedir (se ve opaca)
            cardBuildBurger.setAlpha(0.6f);
        }
    }

    private void setupClickListeners() {
        // Acción: Construir Hamburguesa
        cardBuildBurger.setOnClickListener(v -> {
            if (isStoreOpen) {
                Intent intent = new Intent(ClientMainActivity.this, BuildBurgerActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            } else {
                // Si está cerrado, mostramos alerta y NO dejamos pasar
                new AlertDialog.Builder(this)
                        .setTitle("Local Cerrado 😔")
                        .setMessage("Lo sentimos, en este momento no estamos recibiendo pedidos. Revisa el indicador de estado.")
                        .setPositiveButton("Entendido", null)
                        .show();
            }
        });

        // Acción: Historial
        cardHistory.setOnClickListener(v -> {
            startActivity(new Intent(ClientMainActivity.this, OrderHistoryActivity.class));
        });

        // Acción: Depósito
        cardDeposit.setOnClickListener(v -> {
            startActivity(new Intent(ClientMainActivity.this, DepositUploadActivity.class));
        });

        // Acción: Cerrar Sesión
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar Sesión")
                .setMessage("¿Estás seguro que quieres salir?")
                .setPositiveButton("Sí, salir", (dialog, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(ClientMainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}