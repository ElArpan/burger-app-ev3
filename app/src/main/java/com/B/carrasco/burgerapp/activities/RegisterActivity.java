package com.B.carrasco.burgerapp.activities;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.B.carrasco.burgerapp.R;
import com.B.carrasco.burgerapp.database.DatabaseHelper;
import com.B.carrasco.burgerapp.models.User;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    private EditText etUsername, etPassword, etEmail, etPhone, etAddress;
    private Button btnRegister;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Cambiar al layout mejorado
        setContentView(R.layout.activity_register_improved);

        dbHelper = new DatabaseHelper(this);
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        btnRegister = findViewById(R.id.btnRegister);
    }

    private void setupClickListeners() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        // Validaciones
        if (username.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.length() < 3) {
            Toast.makeText(this, "Usuario debe tener al menos 3 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidPhone(phone)) {
            Toast.makeText(this, "Teléfono inválido. Use formato: +56912345678", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verificar si usuario ya existe
        if (dbHelper.userExists(username)) {
            Toast.makeText(this, "Usuario ya existe", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear usuario
        User user = new User(username, password, email, "client");
        user.setPhone(phone);
        user.setAddress(address);

        long result = dbHelper.insertUser(user);

        if (result != -1) {
            Toast.makeText(this, "¡Registro exitoso! Ahora puedes iniciar sesión", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error en el registro", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidEmail(String email) {
        Pattern pattern = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
        return pattern.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        // Formato chileno: +56912345678
        return phone.matches("^\\+569[0-9]{8}$");
    }

    // Método para el onClick del enlace "Iniciar sesión"
    public void goToLogin(View view) {
        finish();
    }
}