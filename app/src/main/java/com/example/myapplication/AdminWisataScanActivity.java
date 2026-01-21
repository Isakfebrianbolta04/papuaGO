package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.util.List;

public class AdminWisataScanActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView txtInfo, txtStatus;
    private DecoratedBarcodeView barcodeScanner;
    private String transactionId, orderId;
    private DatabaseReference transactionsRef;
    private boolean isVerifying = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_wisata_scan);

        // Get data from intent
        transactionId = getIntent().getStringExtra("transactionId");
        orderId = getIntent().getStringExtra("orderId");

        initViews();
        setupListeners();
        setupScanner();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtInfo = findViewById(R.id.txtInfo);
        txtStatus = findViewById(R.id.txtStatus);
        barcodeScanner = findViewById(R.id.barcodeScanner);

        if (txtInfo != null) {
            txtInfo.setText("Scan QR Code untuk verifikasi tiket\n\nOrder ID: " + orderId);
        }
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupScanner() {
        barcodeScanner.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result.getText() != null && !isVerifying) {
                    verifyScannerResult(result.getText());
                }
            }

            @Override
            public void possibleResultPoints(List<com.google.zxing.ResultPoint> resultPoints) {
            }
        });
    }

    private void verifyScannerResult(String data) {
        // Data format from QRCodeGenerator: transactionId|orderId|itemName|userName
        if (data.contains("|")) {
            String[] parts = data.split("\\|");
            String scannedTransactionId = parts[0];

            if (scannedTransactionId.equals(transactionId)) {
                isVerifying = true;
                txtStatus.setText("Memverifikasi...");
                verifyInFirebase();
            } else {
                Toast.makeText(this, "QR Code tidak cocok dengan pesanan ini!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "QR Code format tidak valid!", Toast.LENGTH_SHORT).show();
        }
    }

    private void verifyInFirebase() {
        transactionsRef = FirebaseDatabase.getInstance().getReference("transactions").child(transactionId);
        transactionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String currentStatus = snapshot.child("status").getValue(String.class);
                    if ("Paid".equalsIgnoreCase(currentStatus) || "Sudah Bayar".equalsIgnoreCase(currentStatus)) {
                        updateStatusToUsed();
                    } else if ("Completed".equalsIgnoreCase(currentStatus)
                            || "Selesai".equalsIgnoreCase(currentStatus)) {
                        txtStatus.setText("Tiket sudah pernah digunakan!");
                        Toast.makeText(AdminWisataScanActivity.this, "Tiket sudah digunakan!", Toast.LENGTH_LONG)
                                .show();
                        isVerifying = false;
                    } else {
                        txtStatus.setText("Status pesanan: " + currentStatus);
                        Toast.makeText(AdminWisataScanActivity.this, "Pesanan belum dibayar!", Toast.LENGTH_LONG)
                                .show();
                        isVerifying = false;
                    }
                } else {
                    txtStatus.setText("Pesanan tidak ditemukan di database");
                    isVerifying = false;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                isVerifying = false;
                Toast.makeText(AdminWisataScanActivity.this, "Database Error: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStatusToUsed() {
        transactionsRef.child("status").setValue("Completed")
                .addOnSuccessListener(aVoid -> {
                    txtStatus.setText("VERIFIKASI BERHASIL!");
                    Toast.makeText(this, "Tiket Berhasil Diverifikasi!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    isVerifying = false;
                    txtStatus.setText("Gagal memperbarui status");
                    Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeScanner.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScanner.pause();
    }
}
