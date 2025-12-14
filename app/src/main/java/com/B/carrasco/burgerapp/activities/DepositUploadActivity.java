package com.B.carrasco.burgerapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.B.carrasco.burgerapp.R;
import com.B.carrasco.burgerapp.utils.CartManager; // Importamos el carrito

public class DepositUploadActivity extends AppCompatActivity {

    private Button btnSelectPhoto, btnSubmit, btnChangePhoto, btnCancel;
    private ImageView ivDeposit;
    private TextView tvStatus, tvFileName;
    private LinearLayout llPhotoSelected;

    private static final int REQUEST_IMAGE_GALLERY = 100;
    private Uri selectedImageUri;
    private double totalAmount = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit_upload); // Asegúrate que sea el XML correcto

        // Recibir el monto total si viene del intent
        totalAmount = getIntent().getDoubleExtra("TOTAL_AMOUNT", 0.0);
        if (totalAmount == 0) {
            totalAmount = CartManager.getInstance().getTotalOrderPrice();
        }

        initViews();
        setupClickListeners();
        updateUI();
    }

    private void initViews() {
        btnSelectPhoto = findViewById(R.id.btnSelectPhoto);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        btnCancel = findViewById(R.id.btnCancel); // Agregué el botón cancelar del XML nuevo
        ivDeposit = findViewById(R.id.ivDeposit);
        // Nota: tvStatus y tvFileName no están en el XML nuevo "Premium" que te pasé antes (el que tenía Lottie),
        // pero si usas el XML viejo, déjalos. Si usas el nuevo, puedes borrarlos o agregarlos al XML.
        // Para que compile, asumiré que los agregaste o usas el XML mixto.
        // Si no los encuentras, comenta estas líneas:
        // tvStatus = findViewById(R.id.tvStatus);
        // tvFileName = findViewById(R.id.tvFileName);

        llPhotoSelected = findViewById(R.id.llPhotoSelected);
    }

    private void setupClickListeners() {
        btnSelectPhoto.setOnClickListener(v -> openGallery());
        btnChangePhoto.setOnClickListener(v -> openGallery());

        btnCancel.setOnClickListener(v -> finish()); // Volver atrás

        btnSubmit.setOnClickListener(v -> uploadDeposit());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Selecciona el comprobante"), REQUEST_IMAGE_GALLERY);
    }

    private void uploadDeposit() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "Primero selecciona una foto", Toast.LENGTH_SHORT).show();
            return;
        }

        // Aquí iría el código real de Firebase Storage
        // Por ahora simulamos la carga
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Enviando...");

        new android.os.Handler().postDelayed(() -> {
            Toast.makeText(DepositUploadActivity.this, "✅ ¡Pedido Enviado con éxito!", Toast.LENGTH_LONG).show();

            // 1. Limpiar el carrito porque ya se compró
            CartManager.getInstance().clearCart();

            // 2. Redirigir al inicio (ClientMain) limpiando historial de pantallas
            Intent intent = new Intent(DepositUploadActivity.this, ClientMainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        }, 2000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                ivDeposit.setImageURI(selectedImageUri);
                updateUI();
            }
        }
    }

    private void updateUI() {
        boolean hasPhoto = selectedImageUri != null;

        if (hasPhoto) {
            llPhotoSelected.setVisibility(View.VISIBLE);
            btnSelectPhoto.setVisibility(View.GONE);
            btnSubmit.setEnabled(true);
            btnSubmit.setAlpha(1.0f);
        } else {
            llPhotoSelected.setVisibility(View.GONE);
            btnSelectPhoto.setVisibility(View.VISIBLE);
            btnSubmit.setEnabled(false);
            btnSubmit.setAlpha(0.5f);
        }
    }
}