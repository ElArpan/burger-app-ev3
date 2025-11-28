package com.B.carrasco.burgerapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.B.carrasco.burgerapp.R;

public class ClientMainActivity extends AppCompatActivity {
    private TextView tvWelcome;
    private Button btnBuildBurger, btnUploadDeposit, btnOrderHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_main);

        initViews();
        setupClickListeners();

        String username = getIntent().getStringExtra("USERNAME");
        tvWelcome.setText("Bienvenido: " + username);
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        btnBuildBurger = findViewById(R.id.btnBuildBurger);
        btnUploadDeposit = findViewById(R.id.btnUploadDeposit);
        btnOrderHistory = findViewById(R.id.btnOrderHistory);
    }

    private void setupClickListeners() {
        btnBuildBurger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ClientMainActivity.this, BuildBurgerActivity.class));
            }
        });

        btnUploadDeposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ClientMainActivity.this, DepositUploadActivity.class));
            }
        });

        btnOrderHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ClientMainActivity.this,
                        "Historial de pedidos - Próximamente", Toast.LENGTH_SHORT).show();
            }
        });
    }
}