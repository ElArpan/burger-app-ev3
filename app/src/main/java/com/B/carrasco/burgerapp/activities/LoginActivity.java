package com.B.carrasco.burgerapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.B.carrasco.burgerapp.R;
import com.B.carrasco.burgerapp.database.DatabaseHelper;
import com.B.carrasco.burgerapp.utils.SessionManager;
import com.B.carrasco.burgerapp.utils.ValidationUtils;

public class LoginActivity extends AppCompatActivity {

    // Views
    private EditText etUsername, etPassword;
    private Button btnLogin, btnRegister;
    private ProgressBar progressBar;
    private TextView tvForgotPassword, tvSkipForNow;

    // Helpers
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    // Flags
    private boolean isLoggingIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Usar el layout con diseño completo
        setContentView(R.layout.activity_login);

        // Inicializar helpers
        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        // Verificar si ya está logueado
        if (sessionManager.isLoggedIn()) {
            redirectToAppropriateScreen();
            return;
        }

        initViews();
        setupClickListeners();

        // Auto-llenar si hay datos guardados
        autoFillCredentials();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSkipForNow = findViewById(R.id.tvSkipForNow);

        // Verificar que las vistas críticas no sean null
        if (etUsername == null) {
            Toast.makeText(this, "Error: Campo usuario no encontrado", Toast.LENGTH_LONG).show();
        }
        if (etPassword == null) {
            Toast.makeText(this, "Error: Campo contraseña no encontrado", Toast.LENGTH_LONG).show();
        }
        if (btnLogin == null) {
            Toast.makeText(this, "Error: Botón login no encontrado", Toast.LENGTH_LONG).show();
        }
    }

    private void setupClickListeners() {
        // Botón de login
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isLoggingIn) {
                    loginUser();
                }
            }
        });

        // Botón de registro
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        // Texto para recuperar contraseña (futura funcionalidad)
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showForgotPasswordDialog();
                }
            });
        }

        // Texto para saltar (modo invitado)
        if (tvSkipForNow != null) {
            tvSkipForNow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startAsGuest();
                }
            });
        }

        // Enter key listener para el campo de contraseña
        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (!isLoggingIn) {
                loginUser();
                return true;
            }
            return false;
        });
    }

    private void loginUser() {
        // Validar campos
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!ValidationUtils.validateLoginFields(this, username, password)) {
            return;
        }

        // Mostrar progreso
        setLoadingState(true);

        // Usar arrays finales para solucionar el problema de variables en lambda
        final boolean[] loginSuccess = {false};
        final String[] userRole = {null};

        // Simular delay para UX (en producción sería llamada a API)
        new android.os.Handler().postDelayed(() -> {
            try {
                // Verificar credenciales en la base de datos
                if (dbHelper.checkUser(username, password)) {
                    userRole[0] = dbHelper.getUserRole(username);
                    loginSuccess[0] = true;

                    // Guardar sesión
                    sessionManager.createLoginSession(username, userRole[0]);

                    // Guardar credenciales para auto-login (opcional)
                    if (shouldRememberCredentials()) {
                        saveCredentials(username, password);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            runOnUiThread(() -> {
                setLoadingState(false);

                if (loginSuccess[0]) {
                    // Redirigir según rol
                    redirectUser(userRole[0], username);
                } else {
                    showLoginError();
                }
            });
        }, 1500); // Simular 1.5 segundos de delay
    }

    private void redirectUser(String role, String username) {
        Intent intent;

        if ("admin".equalsIgnoreCase(role)) {
            intent = new Intent(LoginActivity.this, AdminMainActivity.class);
            Toast.makeText(this, "Bienvenido Administrador", Toast.LENGTH_SHORT).show();
        } else {
            intent = new Intent(LoginActivity.this, ClientMainActivity.class);
            Toast.makeText(this, "¡Bienvenido " + username + "!", Toast.LENGTH_SHORT).show();
        }

        intent.putExtra("USERNAME", username);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void redirectToAppropriateScreen() {
        String username = sessionManager.getUsername();
        String role = sessionManager.getUserRole();

        Intent intent;
        if ("admin".equalsIgnoreCase(role)) {
            intent = new Intent(this, AdminMainActivity.class);
        } else {
            intent = new Intent(this, ClientMainActivity.class);
        }

        intent.putExtra("USERNAME", username);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoadingState(boolean isLoading) {
        isLoggingIn = isLoading;

        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }

        btnLogin.setEnabled(!isLoading);
        btnLogin.setText(isLoading ? "CARGANDO..." : "INICIAR SESIÓN");
        btnLogin.setAlpha(isLoading ? 0.7f : 1f);

        if (btnRegister != null) {
            btnRegister.setEnabled(!isLoading);
        }

        etUsername.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
    }

    private void showLoginError() {
        // Animación de error en los campos
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            etUsername.animate().translationXBy(10f).setDuration(50).withEndAction(() -> {
                etUsername.animate().translationXBy(-20f).setDuration(50).withEndAction(() -> {
                    etUsername.animate().translationXBy(10f).setDuration(50).start();
                }).start();
            }).start();

            etPassword.animate().translationXBy(10f).setDuration(50).withEndAction(() -> {
                etPassword.animate().translationXBy(-20f).setDuration(50).withEndAction(() -> {
                    etPassword.animate().translationXBy(10f).setDuration(50).start();
                }).start();
            }).start();
        }

        Toast.makeText(this, "Credenciales incorrectas. Por favor verifica.", Toast.LENGTH_LONG).show();

        // Limpiar solo la contraseña
        etPassword.setText("");
        etPassword.requestFocus();
    }

    private void showForgotPasswordDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("¿Olvidaste tu contraseña?");
        builder.setMessage("Por seguridad, contacta al administrador del condominio para restablecer tu contraseña.");
        builder.setPositiveButton("Entendido", null);
        builder.setNegativeButton("Contactar", (dialog, which) -> {
            // Abrir WhatsApp o llamada
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(android.net.Uri.parse("tel:+56912345678"));
            startActivity(intent);
        });
        builder.show();
    }

    private void startAsGuest() {
        // Modo invitado - acceso limitado
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Modo Invitado");
        builder.setMessage("Como invitado podrás ver el menú pero necesitarás registrarte para hacer pedidos.");
        builder.setPositiveButton("Continuar", (dialog, which) -> {
            sessionManager.setGuestMode(true);
            Intent intent = new Intent(LoginActivity.this, ClientMainActivity.class);
            intent.putExtra("GUEST_MODE", true);
            startActivity(intent);
            finish();
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void autoFillCredentials() {
        // Cargar credenciales guardadas si existen
        String savedUsername = getSharedPreferences("BurgerAppPrefs", MODE_PRIVATE)
                .getString("saved_username", "");
        String savedPassword = getSharedPreferences("BurgerAppPrefs", MODE_PRIVATE)
                .getString("saved_password", "");

        if (!savedUsername.isEmpty()) {
            etUsername.setText(savedUsername);
            if (!savedPassword.isEmpty()) {
                etPassword.setText(savedPassword);
                etPassword.requestFocus();
            }
        }
    }

    private boolean shouldRememberCredentials() {
        // Aquí podrías agregar un CheckBox "Recordarme"
        return false; // Por defecto no recordar por seguridad
    }

    private void saveCredentials(String username, String password) {
        android.content.SharedPreferences.Editor editor =
                getSharedPreferences("BurgerAppPrefs", MODE_PRIVATE).edit();
        editor.putString("saved_username", username);
        editor.putString("saved_password", password);
        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}