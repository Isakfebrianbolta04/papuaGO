package com.example.myapplication;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

public class QRTicketActivity extends AppCompatActivity {

    private ImageView btnBack, imgTicketQR;
    private TextView txtWisataNama, txtVisitorName, txtVisitDate, txtTicketQuantity, txtOrderId;

    private String transactionId, orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_ticket);

        transactionId = getIntent().getStringExtra("transactionId");
        orderId = getIntent().getStringExtra("orderId");

        initViews();
        loadTicketData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgTicketQR = findViewById(R.id.imgTicketQR);
        txtWisataNama = findViewById(R.id.txtWisataNama);
        txtVisitorName = findViewById(R.id.txtVisitorName);
        txtVisitDate = findViewById(R.id.txtVisitDate);
        txtTicketQuantity = findViewById(R.id.txtTicketQuantity);
        txtOrderId = findViewById(R.id.txtOrderId);

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadTicketData() {
        if (transactionId == null) {
            Toast.makeText(this, "Data tiket tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase.getInstance().getReference("transactions")
                .child(transactionId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Transaction transaction = snapshot.getValue(Transaction.class);
                            if (transaction != null) {
                                displayTicketData(transaction);
                                generateTicketQR(transaction);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(QRTicketActivity.this,
                                "Gagal memuat data tiket", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayTicketData(Transaction transaction) {
        txtOrderId.setText("#" + orderId);
        txtWisataNama.setText(transaction.getName());

        // Get booking data
        if (transaction.getBookingData() != null) {
            String visitorName = BookingData.getString(transaction.getBookingData(), "visitorName");
            String visitDate = BookingData.getString(transaction.getBookingData(), "visitDate");
            int quantity = BookingData.getInt(transaction.getBookingData(), "ticketQuantity");

            txtVisitorName.setText(visitorName.isEmpty() ? "-" : visitorName);
            txtVisitDate.setText(visitDate.isEmpty() ? "-" : visitDate);
            txtTicketQuantity.setText(String.valueOf(quantity) + " Tiket");
        }
    }

    private void generateTicketQR(Transaction transaction) {
        try {
            JSONObject qrData = new JSONObject();
            qrData.put("orderId", orderId);
            qrData.put("transactionId", transactionId);
            qrData.put("category", transaction.getCategory());
            qrData.put("visitorName", BookingData.getString(transaction.getBookingData(), "visitorName"));
            qrData.put("visitDate", BookingData.getString(transaction.getBookingData(), "visitDate"));
            qrData.put("ticketQuantity", BookingData.getInt(transaction.getBookingData(), "ticketQuantity"));
            qrData.put("qrGeneratedAt", System.currentTimeMillis());

            String qrContent = qrData.toString();

            Bitmap qrBitmap = QRCodeGenerator.generateQRCode(qrContent, 500, 500);
            if (qrBitmap != null) {
                imgTicketQR.setImageBitmap(qrBitmap);
            } else {
                Toast.makeText(this, "Gagal generate QR Code tiket", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
