package com.B.carrasco.burgerapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.B.carrasco.burgerapp.R;

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

        tvWelcome.setText("Panel Admin: " + (username != null ? username : ""));
    }

    private void setupClickListeners() {
        btnManageOrders.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí podrías abrir ManageOrdersActivity cuando la implementes
                // por ahora abrimos la pantalla de historial de pedidos como placeholder
                startActivity(new Intent(AdminMainActivity.this, OrderHistoryActivity.class));
            }
        });

        btnManageIngredients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abrir gestión de ingredientes
                startActivity(new Intent(AdminMainActivity.this, ManageIngredientsActivity.class));
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
