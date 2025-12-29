package com.B.carrasco.burgerapp;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.B.carrasco.burgerapp.activities.BuildBurgerActivity;
import com.B.carrasco.burgerapp.activities.LoginActivity;
import com.B.carrasco.burgerapp.activities.OrderHistoryActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private MediaPlayer bellSound;
    private TextView tvWelcomeName;

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

        // 1. Configurar Saludo
        configurarSaludo();

        // 2. Cargar Configuración y Sonidos
        cargarConfiguracion();
        inicializarSonido();

        // 3. Configurar Botones
        configurarBotonesDashboard();
    }

    private void configurarSaludo() {
        tvWelcomeName = findViewById(R.id.tvWelcomeName);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null && user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            String nombreCompleto = user.getDisplayName();
            String primerNombre = nombreCompleto.split(" ")[0];
            tvWelcomeName.setText("¡Hola, " + primerNombre + "!");
        } else {
            tvWelcomeName.setText("¡Hola, Vecino!");
        }
    }

    private void inicializarSonido() {
        try {
            // AHORA SÍ: Usamos tu archivo bell.mp3
            // Si "R" sale en rojo, asegúrate de NO tener "import android.R;" arriba.
            bellSound = MediaPlayer.create(this, R.raw.bell);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void configurarBotonesDashboard() {
        MaterialCardView cardPedir = findViewById(R.id.cardStartOrder);
        cardPedir.setOnClickListener(v -> performPremiumClickEffect(v, () -> {
            startActivity(new Intent(MainActivity.this, BuildBurgerActivity.class));
        }));

        MaterialCardView cardHistorial = findViewById(R.id.cardHistory);
        cardHistorial.setOnClickListener(v -> performPremiumClickEffect(v, () -> {
            startActivity(new Intent(MainActivity.this, OrderHistoryActivity.class));
        }));

        MaterialCardView cardPrecios = findViewById(R.id.cardPriceGuide);
        cardPrecios.setOnClickListener(v -> performPremiumClickEffect(v, () -> {
            Toast.makeText(this, "Próximamente: Guía interactiva de precios.", Toast.LENGTH_SHORT).show();
        }));

        MaterialCardView cardSoporte = findViewById(R.id.cardSupport);
        cardSoporte.setOnClickListener(v -> performPremiumClickEffect(v, () -> {
            abrirSoporte();
        }));

        MaterialCardView cardLogout = findViewById(R.id.cardLogout);
        cardLogout.setOnClickListener(v -> performPremiumClickEffect(v, () -> {
            cerrarSesion();
        }));
    }

    private void abrirSoporte() {
        String telefono = Config.TELEFONO_NEGOCIO;
        if (telefono != null && !telefono.isEmpty()) {
            try {
                String num = telefono.replace("+", "").replace(" ", "");
                String url = "https://api.whatsapp.com/send?phone=" + num;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            } catch (Exception e) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + telefono));
                startActivity(intent);
            }
        } else {
            Toast.makeText(this, "Número de contacto no disponible.", Toast.LENGTH_SHORT).show();
        }
    }

    private void cerrarSesion() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * EFECTO PREMIUM
     */
    private void performPremiumClickEffect(View view, Runnable onAnimationEnd) {
        // 1. Sonido
        if (bellSound != null) {
            if (bellSound.isPlaying()) bellSound.seekTo(0);
            bellSound.start();
        }

        // 2. Vibración (Compatible y corregida)
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(50L);
            }
        }

        // 3. Animación visual
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .withEndAction(onAnimationEnd)
                        .start())
                .start();
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
                    if (!Config.ESTA_ABIERTO) {
                        Toast.makeText(this, Config.MENSAJE_CIERRE, Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bellSound != null) {
            bellSound.release();
            bellSound = null;
        }
    }
}