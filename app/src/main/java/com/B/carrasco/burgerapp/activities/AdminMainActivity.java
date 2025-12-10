package com.B.carrasco.burgerapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.B.carrasco.burgerapp.R;
import android.widget.Toast;

public class AdminMainActivity extends AppCompatActivity {
    private TextView tvWelcome;
    private Button btnManageOrders, btnManageIngredients, btnLogout;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        username = getIntent().getStringExtra("USERNAME");
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        btnManageOrders = findViewById(R.id.btnManageOrders);
        btnManageIngredients = findViewById(R.id.btnManageIngredients);
        btnLogout = findViewById(R.id.btnLogout);

        tvWelcome.setText("Panel Admin: " + username);
    }

    private void setupClickListeners() {
        btnManageOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Próximamente: Gestión de pedidos
                Toast.makeText(AdminMainActivity.this, "Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show();
            }
        });

        btnManageIngredients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Próximamente: Gestión de ingredientes
                Toast.makeText(AdminMainActivity.this, "Funcionalidad en desarrollo", Toast.LENGTH_SHORT).show();
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