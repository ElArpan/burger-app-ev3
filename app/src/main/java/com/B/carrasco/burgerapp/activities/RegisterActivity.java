package com.B.carrasco.burgerapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.B.carrasco.burgerapp.MainActivity;
import com.B.carrasco.burgerapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    // Agregamos los nuevos campos
    private EditText etUsername, etPassword, etEmail, etPhone, etAddress;
    private Button btnRegister;
    private TextView tvGoToLogin;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);       // Nuevo
        etAddress = findViewById(R.id.etAddress);   // Nuevo
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(v -> registerUserInCloud());

        // Volver al login de forma segura
        tvGoToLogin.setOnClickListener(v -> finish());
    }

    private void registerUserInCloud() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validaciones completas
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Nombre requerido");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email requerido");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Teléfono requerido"); // Validación nueva
            return;
        }
        if (TextUtils.isEmpty(address)) {
            etAddress.setError("Dirección requerida"); // Validación nueva
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Contraseña requerida");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Mínimo 6 caracteres");
            return;
        }

        Toast.makeText(this, "Creando cuenta...", Toast.LENGTH_SHORT).show();
        btnRegister.setEnabled(false);

        // 1. Crear usuario en Auth (Solo usa email y pass)
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // 2. Guardar TODOS los datos (incluyendo dirección y teléfono) en Firestore
                        FirebaseUser user = mAuth.getCurrentUser();
                        saveUserExtras(user, username, email, phone, address);
                    } else {
                        btnRegister.setEnabled(true);
                        String error = task.getException() != null ? task.getException().getMessage() : "Error desconocido";
                        Toast.makeText(RegisterActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserExtras(FirebaseUser user, String username, String email, String phone, String address) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("username", username);
        userMap.put("email", email);
        userMap.put("phone", phone);        // Guardamos teléfono
        userMap.put("address", address);    // Guardamos dirección
        userMap.put("role", "client");
        userMap.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(user.getUid())
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterActivity.this, "¡Bienvenido vecino!", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.putExtra("USERNAME", username);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Si falla la base de datos, limpiamos el Auth para evitar inconsistencias
                    user.delete();
                    btnRegister.setEnabled(true);
                    Toast.makeText(RegisterActivity.this, "Error guardando datos. Intenta de nuevo.", Toast.LENGTH_SHORT).show();
                });
    }
}