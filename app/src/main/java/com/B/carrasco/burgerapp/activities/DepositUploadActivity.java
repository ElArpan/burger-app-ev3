package com.B.carrasco.burgerapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.B.carrasco.burgerapp.R;

public class DepositUploadActivity extends AppCompatActivity {
    private Button btnSelectPhoto, btnSubmit, btnChangePhoto;
    private ImageView ivDeposit;
    private TextView tvStatus, tvFileName;
    private LinearLayout llPhotoSelected;
    private static final int REQUEST_IMAGE_GALLERY = 100;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit_upload);

        initViews();
        setupClickListeners();
        updateUI();
    }

    private void initViews() {
        btnSelectPhoto = findViewById(R.id.btnSelectPhoto);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        ivDeposit = findViewById(R.id.ivDeposit);
        tvStatus = findViewById(R.id.tvStatus);
        tvFileName = findViewById(R.id.tvFileName);
        llPhotoSelected = findViewById(R.id.llPhotoSelected);
    }

    private void setupClickListeners() {
        btnSelectPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        btnChangePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadDeposit();
            }
        });
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

        tvStatus.setText("🔄 Subiendo comprobante...");
        tvStatus.setTextColor(getResources().getColor(R.color.info_blue));
        btnSubmit.setEnabled(false);

        // Simulación de subida a API (2 segundos)
        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        tvStatus.setText("✅ Comprobante subido exitosamente");
                        tvStatus.setTextColor(getResources().getColor(R.color.success_green));
                        Toast.makeText(DepositUploadActivity.this,
                                "Comprobante enviado para verificación", Toast.LENGTH_LONG).show();

                        // Volver después de 2 segundos
                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {
                                        finish();
                                    }
                                },
                                2000);
                    }
                },
                2000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_GALLERY && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                // Mostrar imagen
                ivDeposit.setImageURI(selectedImageUri);

                // Mostrar nombre del archivo (si es posible)
                String fileName = getFileNameFromUri(selectedImageUri);
                tvFileName.setText("Archivo: " + fileName);

                // Actualizar UI
                updateUI();

                tvStatus.setText("🖼️ Comprobante seleccionado - Listo para enviar");
                tvStatus.setTextColor(getResources().getColor(R.color.success_green));
            }
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try {
                android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result != null ? result : "comprobante.jpg";
    }

    private void updateUI() {
        boolean hasPhoto = selectedImageUri != null;

        if (hasPhoto) {
            // Foto seleccionada: mostrar panel de foto y botón cambiar
            llPhotoSelected.setVisibility(View.VISIBLE);
            btnSelectPhoto.setVisibility(View.GONE);
            btnSubmit.setEnabled(true);
            btnSubmit.setBackgroundTintList(getResources().getColorStateList(R.color.success_green));
        } else {
            // Sin foto: mostrar solo botón seleccionar
            llPhotoSelected.setVisibility(View.GONE);
            btnSelectPhoto.setVisibility(View.VISIBLE);
            btnSubmit.setEnabled(false);
            btnSubmit.setBackgroundTintList(getResources().getColorStateList(R.color.gray));
        }
    }
}