package com.example.myapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
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

import java.text.NumberFormat;
import java.util.Locale;

public class AdminAksesorisDetailActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView txtProductName, txtQuantity, txtAddress, txtTotalPrice;
    private EditText edtCourier, edtTrackingNumber;
    private Button btnSubmit;
    private String transactionId, orderId;
    private DatabaseReference transactionsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_aksesoris_detail);

        // Get data from intent
        transactionId = getIntent().getStringExtra("transactionId");
        orderId = getIntent().getStringExtra("orderId");

        initViews();
        setupListeners();
        fetchData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtProductName = findViewById(R.id.txtProductName);
        txtQuantity = findViewById(R.id.txtQuantity);
        txtAddress = findViewById(R.id.txtAddress);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        edtCourier = findViewById(R.id.edtCourier);
        edtTrackingNumber = findViewById(R.id.edtTrackingNumber);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        btnSubmit.setOnClickListener(v -> updateTracking());
    }

    private void fetchData() {
        transactionsRef = FirebaseDatabase.getInstance().getReference("transactions").child(transactionId);
        transactionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String productName = snapshot.child("name").getValue(String.class);
                    String info = snapshot.child("info").getValue(String.class);
                    String address = snapshot.child("shippingAddress").getValue(String.class);
                    Integer price = snapshot.child("amount").getValue(Integer.class);
                    String status = snapshot.child("status").getValue(String.class);

                    txtProductName.setText(productName != null ? productName : "-");
                    txtQuantity.setText(info != null ? info : "-");
                    txtAddress.setText(address != null ? address : "Alamat tidak tersedia");

                    if (price != null) {
                        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                        txtTotalPrice.setText(formatter.format(price));
                    }

                    if ("Completed".equalsIgnoreCase(status) || "Selesai".equalsIgnoreCase(status)) {
                        btnSubmit.setEnabled(false);
                        btnSubmit.setText("Resi Sudah Diinput");
                        String savedCourier = snapshot.child("courier").getValue(String.class);
                        String savedResi = snapshot.child("trackingNumber").getValue(String.class);
                        if (savedCourier != null)
                            edtCourier.setText(savedCourier);
                        if (savedResi != null)
                            edtTrackingNumber.setText(savedResi);
                        edtCourier.setEnabled(false);
                        edtTrackingNumber.setEnabled(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminAksesorisDetailActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void updateTracking() {
        String courier = edtCourier.getText().toString().trim();
        String tracking = edtTrackingNumber.getText().toString().trim();

        if (TextUtils.isEmpty(courier)) {
            edtCourier.setError("Nama kurir harus diisi!");
            return;
        }
        if (TextUtils.isEmpty(tracking)) {
            edtTrackingNumber.setError("Nomor resi harus diisi!");
            return;
        }

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Memproses...");

        transactionsRef.child("status").setValue("Completed");
        transactionsRef.child("courier").setValue(courier);
        transactionsRef.child("trackingNumber").setValue(tracking)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Tracking Berhasil Diperbarui!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSubmit.setEnabled(true);
                    btnSubmit.setText("Simpan & Update Status");
                    Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
