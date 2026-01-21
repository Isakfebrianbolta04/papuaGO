package com.example.myapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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

public class AdminHotelDetailActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView txtGuestName, txtRoomType, txtNights, txtTotalPrice;
    private EditText edtBookingCode;
    private Button btnConfirm;
    private String transactionId, orderId;
    private DatabaseReference transactionsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_hotel_detail);

        // Get data from intent
        transactionId = getIntent().getStringExtra("transactionId");
        orderId = getIntent().getStringExtra("orderId");

        initViews();
        setupListeners();
        fetchData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtGuestName = findViewById(R.id.txtGuestName);
        txtRoomType = findViewById(R.id.txtRoomType);
        txtNights = findViewById(R.id.txtNights);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        edtBookingCode = findViewById(R.id.edtBookingCode);
        btnConfirm = findViewById(R.id.btnConfirm);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        btnConfirm.setOnClickListener(v -> confirmBooking());
    }

    private void fetchData() {
        transactionsRef = FirebaseDatabase.getInstance().getReference("transactions").child(transactionId);
        transactionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String guestName = snapshot.child("owner").getValue(String.class);
                    String roomType = snapshot.child("name").getValue(String.class);
                    String nights = snapshot.child("info").getValue(String.class); // Usually contains something like "2
                                                                                   // Malam"
                    Integer price = snapshot.child("amount").getValue(Integer.class);
                    String status = snapshot.child("status").getValue(String.class);

                    txtGuestName.setText(guestName != null ? guestName : "-");
                    txtRoomType.setText(roomType != null ? roomType : "-");
                    txtNights.setText(nights != null ? nights : "-");

                    if (price != null) {
                        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                        txtTotalPrice.setText(formatter.format(price));
                    }

                    if ("Completed".equalsIgnoreCase(status) || "Selesai".equalsIgnoreCase(status)) {
                        btnConfirm.setEnabled(false);
                        btnConfirm.setText("Pesanan Sudah Dikonfirmasi");
                        String savedCode = snapshot.child("bookingCode").getValue(String.class);
                        if (savedCode != null)
                            edtBookingCode.setText(savedCode);
                        edtBookingCode.setEnabled(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminHotelDetailActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    private void confirmBooking() {
        String code = edtBookingCode.getText().toString().trim();
        if (TextUtils.isEmpty(code)) {
            edtBookingCode.setError("Kode booking harus diisi!");
            return;
        }

        btnConfirm.setEnabled(false);
        btnConfirm.setText("Memproses...");

        transactionsRef.child("status").setValue("Completed");
        transactionsRef.child("bookingCode").setValue(code)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Booking Berhasil Dikonfirmasi!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnConfirm.setEnabled(true);
                    btnConfirm.setText("Konfirmasi Pesanan");
                    Toast.makeText(this, "Gagal: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
