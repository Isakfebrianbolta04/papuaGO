package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class PaymentMethodActivity extends AppCompatActivity {

    private ImageView btnBack;
    private CardView cardVA, cardQRIS;

    private String orderId, category, amount, wisataNama;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);

        // Get data from intent
        orderId = getIntent().getStringExtra("orderId");
        category = getIntent().getStringExtra("category");
        amount = getIntent().getStringExtra("amount");
        wisataNama = getIntent().getStringExtra("wisataNama");

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        cardVA = findViewById(R.id.cardVA);
        cardQRIS = findViewById(R.id.cardQRIS);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        cardVA.setOnClickListener(v -> {
            openPaymentActivity("Transfer Bank / Virtual Account");
        });

        cardQRIS.setOnClickListener(v -> {
            openPaymentActivity("QRIS");
        });
    }

    private void openPaymentActivity(String method) {
        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("orderId", orderId);
        intent.putExtra("method", method);
        intent.putExtra("amount", amount);
        intent.putExtra("category", category);
        intent.putExtra("wisataNama", wisataNama);
        startActivity(intent);
        finish(); // Close this activity
    }
}
