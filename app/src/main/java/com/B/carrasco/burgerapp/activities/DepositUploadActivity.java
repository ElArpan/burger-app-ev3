package com.B.carrasco.burgerapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.B.carrasco.burgerapp.R;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DepositUploadActivity extends AppCompatActivity {
    private Button btnTakePhoto, btnSubmit;
    private ImageView ivDeposit;
    private TextView tvStatus;
    private String currentPhotoPath;
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit_upload);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnSubmit = findViewById(R.id.btnSubmit);
        ivDeposit = findViewById(R.id.ivDeposit);
        tvStatus = findViewById(R.id.tvStatus);

        btnSubmit.setEnabled(false);
    }

    private void setupClickListeners() {
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Simular subida a API
                tvStatus.setText("🔄 Subiendo comprobante...");

                // Simulación de petición HTTP
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                tvStatus.setText("✅ Comprobante subido exitosamente");
                                Toast.makeText(DepositUploadActivity.this,
                                        "Comprobante enviado para verificación", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        },
                        2000);
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error creando archivo", Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.B.carrasco.burgerapp.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "DEPOSITO_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(null);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Foto tomada exitosamente
            ivDeposit.setImageURI(Uri.parse(currentPhotoPath));
            btnSubmit.setEnabled(true);
            tvStatus.setText("📸 Foto tomada - Lista para enviar");
        }
    }
}