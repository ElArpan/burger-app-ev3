package com.B.carrasco.burgerapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.B.carrasco.burgerapp.R;
import android.widget.Toast;

public class ClientMainActivity extends AppCompatActivity {
    private TextView tvWelcome;
    private Button btnBuildBurger, btnOrderHistory, btnLogout;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_main);

        username = getIntent().getStringExtra("USERNAME");
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        btnBuildBurger = findViewById(R.id.btnBuildBurger);
        btnOrderHistory = findViewById(R.id.btnOrderHistory);
        btnLogout = findViewById(R.id.btnLogout);

        tvWelcome.setText("Bienvenido: " + username);
    }

    private void setupClickListeners() {
        btnBuildBurger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ClientMainActivity.this, BuildBurgerActivity.class));
            }
        });

        btnOrderHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Próximamente: Historial de pedidos
                Toast.makeText(ClientMainActivity.this, "Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}