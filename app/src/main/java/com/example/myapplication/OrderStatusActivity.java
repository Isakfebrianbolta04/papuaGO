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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class OrderStatusActivity extends AppCompatActivity {

    private ImageView btnBack;
    private TextView txtFoodName, txtOrderId, txtCurrentStatus, txtReadyTime;
    private Button btnRefresh;

    private String transactionId, orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        transactionId = getIntent().getStringExtra("transactionId");
        orderId = getIntent().getStringExtra("orderId");

        initViews();
        loadOrderStatus();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtFoodName = findViewById(R.id.txtFoodName);
        txtOrderId = findViewById(R.id.txtOrderId);
        txtCurrentStatus = findViewById(R.id.txtCurrentStatus);
        txtReadyTime = findViewById(R.id.txtReadyTime);
        btnRefresh = findViewById(R.id.btnRefresh);

        btnBack.setOnClickListener(v -> finish());
        btnRefresh.setOnClickListener(v -> loadOrderStatus());
    }

    private void loadOrderStatus() {
        if (transactionId == null)
            return;

        FirebaseDatabase.getInstance().getReference("transactions")
                .child(transactionId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Transaction transaction = snapshot.getValue(Transaction.class);
                            if (transaction != null) {
                                displayStatusData(transaction);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(OrderStatusActivity.this, "Gagal memuat status", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayStatusData(Transaction transaction) {
        txtFoodName.setText(transaction.getName());
        txtOrderId.setText("Order ID: #" + orderId);

        String status = transaction.getStatus();
        if (status == null || status.isEmpty())
            status = "PENDING";

        txtCurrentStatus.setText(status.toUpperCase());

        if (transaction.getBookingData() != null) {
            String readyTime = BookingData.getString(transaction.getBookingData(), "readyTime");
            if (readyTime != null && !readyTime.isEmpty()) {
                txtReadyTime.setText("Perkiraan siap: " + readyTime);
            } else {
                txtReadyTime.setText("Sedang menunggu konfirmasi waktu...");
            }
        }
    }
}
