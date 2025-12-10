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
    private Button btnBuildBurger, btnOrderHistory, btnDeposit, btnLogout;
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
        btnDeposit = findViewById(R.id.btnDeposit);
        btnLogout = findViewById(R.id.btnLogout);

        if (username != null && !username.isEmpty()) {
            tvWelcome.setText("Bienvenido, " + username);
        } else {
            tvWelcome.setText("Bienvenido, Cliente");
        }
    }

    private void setupClickListeners() {
        btnBuildBurger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClientMainActivity.this, BuildBurgerActivity.class);
                startActivity(intent);
            }
        });

        btnOrderHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClientMainActivity.this, OrderHistoryActivity.class);
                startActivity(intent);
            }
        });

        btnDeposit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClientMainActivity.this, DepositUploadActivity.class);
                startActivity(intent);
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