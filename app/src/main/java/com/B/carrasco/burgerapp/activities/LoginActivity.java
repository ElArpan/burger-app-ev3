package com.B.carrasco.burgerapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.B.carrasco.burgerapp.MainActivity;
import com.B.carrasco.burgerapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    // Vistas
    private EditText etEmail, etPassword; // CAMBIO: Usamos etEmail para ser consistentes
    private Button btnLogin, btnRegister;
    private TextView tvForgotPassword, tvSkipForNow;
    private ProgressBar progressBar;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // 2. Auto-Login: Verificar si ya hay una sesión activa
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Mostramos carga mientras verificamos el rol
            // Nota: initViews aún no se ejecuta, así que lo hacemos manual o iniciamos vistas antes
            setContentView(R.layout.activity_login);
            initViews();
            showLoading(true);
            checkRoleAndRedirect(currentUser.getUid());
        } else {
            initViews();
            setupClickListeners();
        }
    }

    private void initViews() {
        // Vinculamos con los IDs exactos del XML corregido
        etEmail = findViewById(R.id.etEmail); // CAMBIO IMPORTANTE: ID correcto
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSkipForNow = findViewById(R.id.tvSkipForNow);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        // LOGIN
        btnLogin.setOnClickListener(v -> {
            if (validateInputs()) {
                performLogin();
            }
        });

        // REGISTRO
        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // RECUPERAR CONTRASEÑA
        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());

        // MODO INVITADO
        tvSkipForNow.setOnClickListener(v -> startAsGuest());
    }

    // Valida campos vacíos y formato de correo
    private boolean validateInputs() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        boolean isValid = true;

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Ingresa tu correo");
            shakeView(etEmail);
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Correo no válido");
            shakeView(etEmail);
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Ingresa tu contraseña");
            shakeView(etPassword);
            isValid = false;
        }

        return isValid;
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        showLoading(true);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Login correcto en Auth, verificamos rol en Firestore
                        if (mAuth.getCurrentUser() != null) {
                            checkRoleAndRedirect(mAuth.getCurrentUser().getUid());
                        }
                    } else {
                        showLoading(false);
                        String error = task.getException() != null ? task.getException().getMessage() : "Error desconocido";

                        // Mensajes de error más amigables
                        if (error != null && (error.contains("password") || error.contains("credential") || error.contains("user-not-found"))) {
                            Toast.makeText(LoginActivity.this, "Correo o contraseña incorrectos", Toast.LENGTH_LONG).show();
                            shakeView(etPassword);
                        } else if (error != null && error.contains("network")) {
                            Toast.makeText(LoginActivity.this, "Revisa tu conexión a internet", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void checkRoleAndRedirect(String userId) {
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        String username = documentSnapshot.getString("username");

                        // Valores por defecto para evitar crashes
                        if (role == null) role = "client";
                        if (username == null) username = "Vecino";

                        Intent intent;
                        if ("admin".equals(role)) {
                            intent = new Intent(LoginActivity.this, AdminMainActivity.class);
                            Toast.makeText(this, "Modo Admin activado", Toast.LENGTH_SHORT).show();
                        } else {
                            intent = new Intent(LoginActivity.this, MainActivity.class);
                            Toast.makeText(this, "¡Hola, " + username + "!", Toast.LENGTH_SHORT).show();
                        }

                        intent.putExtra("USERNAME", username);
                        // Limpiamos el historial para que no puedan volver al login con "atrás"
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        // CASO CRÍTICO: Usuario en Auth pero no en Firestore
                        showLoading(false);
                        Toast.makeText(this, "Error: Usuario no encontrado en base de datos.", Toast.LENGTH_LONG).show();
                        mAuth.signOut(); // Cerramos la sesión corrupta
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(this, "Error de conexión: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // No cerramos sesión aquí por si es solo un fallo de internet temporal
                });
    }

    private void showForgotPasswordDialog() {
        EditText resetMail = new EditText(this);
        resetMail.setHint("ejemplo@correo.com");
        // Pre-llenar si ya escribieron algo
        resetMail.setText(etEmail.getText().toString());

        // Un poco de padding para que se vea bien
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        resetMail.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(this)
                .setTitle("Recuperar Contraseña")
                .setMessage("Ingresa tu correo y te enviaremos un enlace para restablecerla.")
                .setView(resetMail)
                .setPositiveButton("Enviar", (dialog, which) -> {
                    String mail = resetMail.getText().toString().trim();
                    if (!TextUtils.isEmpty(mail) && Patterns.EMAIL_ADDRESS.matcher(mail).matches()) {
                        mAuth.sendPasswordResetEmail(mail)
                                .addOnSuccessListener(aVoid -> Toast.makeText(this, "¡Listo! Revisa tu correo.", Toast.LENGTH_LONG).show())
                                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Ingresa un correo válido", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void startAsGuest() {
        new AlertDialog.Builder(this)
                .setTitle("Modo Invitado")
                .setMessage("Podrás ver el menú, pero necesitarás registrarte para pedir. ¿Continuar?")
                .setPositiveButton("Entrar", (dialog, which) -> {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("USERNAME", "Invitado");
                    intent.putExtra("IS_GUEST", true);
                    startActivity(intent);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Animación de "Temblor" para feedback visual
    private void shakeView(View view) {
        TranslateAnimation shake = new TranslateAnimation(0, 10, 0, 0);
        shake.setDuration(500);
        shake.setInterpolator(new CycleInterpolator(7));
        view.startAnimation(shake);
    }

    private void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }

        // Deshabilitar inputs para evitar doble clic
        btnLogin.setEnabled(!isLoading);
        if (isLoading) {
            btnLogin.setText(""); // Ocultar texto para mostrar solo spinner
        } else {
            btnLogin.setText("INICIAR SESIÓN");
        }

        etEmail.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
        btnRegister.setEnabled(!isLoading);
        tvSkipForNow.setEnabled(!isLoading);
    }
}