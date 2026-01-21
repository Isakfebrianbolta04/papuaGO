package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
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

public class AdminKulinerDetailActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView txtMenuName, txtQuantity, txtCurrentStatus, txtTotalPrice;
    private Button btnProcess, btnReady, btnComplete;
    private String transactionId, orderId;
    private DatabaseReference transactionsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_kuliner_detail);

        // Get data from intent
        transactionId = getIntent().getStringExtra("transactionId");
        orderId = getIntent().getStringExtra("orderId");

        initViews();
        setupListeners();
        fetchData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtMenuName = findViewById(R.id.txtMenuName);
        txtQuantity = findViewById(R.id.txtQuantity);
        txtCurrentStatus = findViewById(R.id.txtCurrentStatus);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        btnProcess = findViewById(R.id.btnProcess);
        btnReady = findViewById(R.id.btnReady);
        btnComplete = findViewById(R.id.btnComplete);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        btnProcess.setOnClickListener(v -> updateStatus("Sedang Disiapkan"));
        btnReady.setOnClickListener(v -> updateStatus("Siap Diambil"));
        btnComplete.setOnClickListener(v -> updateStatus("Completed"));
    }

    private void fetchData() {
        transactionsRef = FirebaseDatabase.getInstance().getReference("transactions").child(transactionId);
        transactionsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String menuName = snapshot.child("name").getValue(String.class);
                    String quantity = snapshot.child("info").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);
                    Integer price = snapshot.child("amount").getValue(Integer.class);

                    txtMenuName.setText(menuName != null ? menuName : "-");
                    txtQuantity.setText(quantity != null ? quantity : "-");
                    txtCurrentStatus.setText(status != null ? status : "Pending");

                    if (price != null) {
                        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                        txtTotalPrice.setText(formatter.format(price));
                    }

                    // Adjust button states based on status
                    if ("Completed".equalsIgnoreCase(status) || "Selesai".equalsIgnoreCase(status)) {
                        btnProcess.setEnabled(false);
                        btnReady.setEnabled(false);
                        btnComplete.setEnabled(false);
                        btnComplete.setText("Pesanan Selesai");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminKulinerDetailActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void updateStatus(String newStatus) {
        transactionsRef.child("status").setValue(newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Status Diperbarui: " + newStatus, Toast.LENGTH_SHORT).show();
                    if ("Completed".equals(newStatus)) {
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
