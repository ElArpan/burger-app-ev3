package com.B.carrasco.burgerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.B.carrasco.burgerapp.activities.BuildBurgerActivity;
import com.B.carrasco.burgerapp.activities.OrderHistoryActivity; // Asegúrate de tener este import
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. CARGAR PRECIOS
        cargarConfiguracion();

        // 2. BOTONES DEL MENÚ (Asegúrate que en tu XML tengan estos IDs)
        Button btnPedir = findViewById(R.id.btnStartOrder); // Tu botón "Comencemos"
        Button btnHistorial = findViewById(R.id.btnHistory); // Tu botón "Mis Pedidos"

        if (btnPedir != null) {
            btnPedir.setOnClickListener(v -> {
                // FLUJO NUEVO: Directo a la Cocina
                Intent intent = new Intent(MainActivity.this, BuildBurgerActivity.class);
                startActivity(intent);
            });
        }

        if (btnHistorial != null) {
            btnHistorial.setOnClickListener(v -> {
                startActivity(new Intent(MainActivity.this, OrderHistoryActivity.class));
            });
        }
    }

    private void cargarConfiguracion() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("configuracion_app").document("valores_generales")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long precio = documentSnapshot.getLong("precio_base_hamburguesa");
                        if (precio != null) Config.PRECIO_BASE_HAMBURGUESA = precio;

                        Long envio = documentSnapshot.getLong("costo_envio");
                        if (envio != null) Config.COSTO_ENVIO = envio;

                        String fono = documentSnapshot.getString("telefono_negocio");
                        if (fono != null) Config.TELEFONO_NEGOCIO = fono;

                        Boolean abierto = documentSnapshot.getBoolean("esta_abierto");
                        if (abierto != null) Config.ESTA_ABIERTO = abierto;

                        String mensaje = documentSnapshot.getString("mensaje_cierre");
                        if (mensaje != null) Config.MENSAJE_CIERRE = mensaje;
                    }
                    verificarSiEstamosAbiertos();
                });
    }

    private void verificarSiEstamosAbiertos() {
        if (!Config.ESTA_ABIERTO) {
            Toast.makeText(this, Config.MENSAJE_CIERRE, Toast.LENGTH_LONG).show();
            // finish(); // Descomentar si quieres cerrar la app
        }
    }
}