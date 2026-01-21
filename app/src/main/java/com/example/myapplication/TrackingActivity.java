package com.example.myapplication;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TrackingActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView txtProductName, txtOrderId, txtAddress, txtCourier, txtTrackingNumber;
    private ImageView dotProcessed, dotShipped;
    private View lineProcessed;
    private TextView txtStatusProcessed, txtStatusShipped;
    private Button btnRefresh;

    private String transactionId, orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking);

        transactionId = getIntent().getStringExtra("transactionId");
        orderId = getIntent().getStringExtra("orderId");

        initViews();
        loadTrackingData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtProductName = findViewById(R.id.txtProductName);
        txtOrderId = findViewById(R.id.txtOrderId);
        txtAddress = findViewById(R.id.txtAddress);
        txtCourier = findViewById(R.id.txtCourier);
        txtTrackingNumber = findViewById(R.id.txtTrackingNumber);

        dotProcessed = findViewById(R.id.dotProcessed);
        dotShipped = findViewById(R.id.dotShipped);
        lineProcessed = findViewById(R.id.lineProcessed);

        txtStatusProcessed = findViewById(R.id.txtStatusProcessed);
        txtStatusShipped = findViewById(R.id.txtStatusShipped);

        btnRefresh = findViewById(R.id.btnRefresh);

        btnBack.setOnClickListener(v -> finish());
        btnRefresh.setOnClickListener(v -> loadTrackingData());
    }

    private void loadTrackingData() {
        if (transactionId == null)
            return;

        FirebaseDatabase.getInstance().getReference("transactions")
                .child(transactionId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Transaction transaction = snapshot.getValue(Transaction.class);
                            if (transaction != null) {
                                displayTrackingData(transaction);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TrackingActivity.this, "Gagal memuat status", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayTrackingData(Transaction transaction) {
        txtProductName.setText(transaction.getName());
        txtOrderId.setText("Order ID: #" + orderId);

        if (transaction.getBookingData() != null) {
            String address = BookingData.getString(transaction.getBookingData(), "shippingAddress");
            String courier = BookingData.getString(transaction.getBookingData(), "courier");
            String trackingNum = BookingData.getString(transaction.getBookingData(), "trackingNumber");

            txtAddress.setText(address.isEmpty() ? "-" : address);

            if (courier != null && !courier.isEmpty()) {
                txtCourier.setText(courier);
                txtTrackingNumber.setText(trackingNum);

                // Update Timeline to Shipped
                updateTimeline(2); // Shipped status
            } else if ("Completed".equals(transaction.getStatus()) || "Paid".equals(transaction.getStatus())) {
                // Update Timeline to Processed
                updateTimeline(1); // Processed status
            }
        }
    }

    private void updateTimeline(int level) {
        int colorSuccess = Color.parseColor("#4CAF50");
        int colorGray = Color.parseColor("#CCCCCC");

        if (level >= 1) { // Processed
            dotProcessed.setImageTintList(ColorStateList.valueOf(colorSuccess));
            txtStatusProcessed.setTextColor(Color.BLACK);
            txtStatusProcessed.setText("Pesanan Sedang Diproses");
        }

        if (level >= 2) { // Shipped
            lineProcessed.setBackgroundColor(colorSuccess);
            dotShipped.setImageTintList(ColorStateList.valueOf(colorSuccess));
            txtStatusShipped.setTextColor(Color.BLACK);
            txtStatusShipped.setText("Pesanan Telah Dikirim");
        }
    }
}
