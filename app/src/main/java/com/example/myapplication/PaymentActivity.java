package com.example.myapplication;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class PaymentActivity extends AppCompatActivity {

    private ImageView btnBack, imgQRCode;
    private TextView txtTotalAmount, txtOrderId, txtMethodTitle, txtVaNumber, btnCopyVa, txtTimer;
    private LinearLayout layoutVirtualAccount, layoutQRIS, layoutEwallet;
    private Button btnConfirmPayment;
    private TextView txtVaLabel, txtTransferInstrLabel, txtTransferInstr;

    private String orderId, method, amount, category, wisataNama, transactionId;
    private CountDownTimer paymentTimer;
    private Handler statusCheckHandler;
    private Runnable statusCheckRunnable;

    private static final long PAYMENT_TIMEOUT = 15 * 60 * 1000; // 15 minutes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        try {
            initViews();

            // Get data from intent
            orderId = getIntent().getStringExtra("orderId");
            method = getIntent().getStringExtra("method");
            amount = getIntent().getStringExtra("amount");
            category = getIntent().getStringExtra("category");
            wisataNama = getIntent().getStringExtra("wisataNama");

            // Validate required data
            if (orderId == null || method == null || amount == null) {
                Toast.makeText(this, "Data pembayaran tidak lengkap", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            setupData();
            setupListeners();
            createTransaction();
            startPaymentTimer();
            startStatusCheck();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        txtTotalAmount = findViewById(R.id.txtTotalAmount);
        txtOrderId = findViewById(R.id.txtOrderId);
        txtMethodTitle = findViewById(R.id.txtMethodTitle);
        txtTimer = findViewById(R.id.txtTimer);
        layoutVirtualAccount = findViewById(R.id.layoutVirtualAccount);
        layoutQRIS = findViewById(R.id.layoutQRIS);
        layoutEwallet = findViewById(R.id.layoutEwallet);
        txtVaNumber = findViewById(R.id.txtVaNumber);
        btnCopyVa = findViewById(R.id.btnCopyVa);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);
        imgQRCode = findViewById(R.id.imgQRCode);

        txtVaLabel = findViewById(R.id.txtVaLabel);
        txtTransferInstrLabel = findViewById(R.id.txtTransferInstrLabel);
        txtTransferInstr = findViewById(R.id.txtTransferInstr);
    }

    private void setupData() {
        // Display Order ID
        if (orderId != null) {
            String shortId = orderId.length() > 6 ? orderId.substring(orderId.length() - 6).toUpperCase() : orderId;
            txtOrderId.setText("Order ID: #ORD-" + shortId);
        }

        // Display Amount
        try {
            long hargaVal = Long.parseLong(amount);
            NumberFormat formatter = NumberFormat
                    .getCurrencyInstance(new Locale.Builder().setLanguage("id").setRegion("ID").build());
            txtTotalAmount.setText(formatter.format(hargaVal));
        } catch (Exception e) {
            txtTotalAmount.setText("Rp " + amount);
        }

        txtMethodTitle.setText(method);

        // Reset visibility
        layoutVirtualAccount.setVisibility(View.GONE);
        layoutQRIS.setVisibility(View.GONE);
        layoutEwallet.setVisibility(View.GONE);

        if (method.contains("QRIS")) {
            setupQRIS();
        } else {
            setupVirtualAccount();
        }
    }

    private void setupVirtualAccount() {
        layoutVirtualAccount.setVisibility(View.VISIBLE);
        txtVaNumber.setText("Memuat Rekening...");

        // Get Admin Bank Info from Firebase
        DatabaseReference bankRef = FirebaseDatabase.getInstance()
                .getReference("app_config").child("payment_info");

        bankRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String bank = snapshot.child("bank_name").getValue(String.class);
                    String number = snapshot.child("account_number").getValue(String.class);
                    String holder = snapshot.child("account_holder").getValue(String.class);

                    if (bank != null && number != null) {
                        String displayText = bank + "\n" + number + "\n(a.n " + holder + ")";
                        txtVaNumber.setText(displayText);
                        txtVaNumber.setTextColor(getResources().getColor(R.color.black));
                    } else {
                        txtVaNumber.setText("Belum ada rekening diatur");
                    }
                } else {
                    txtVaNumber.setText("Belum ada rekening diatur.\nHubungi Admin.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                txtVaNumber.setText("Gagal memuat info rekening");
            }
        });
    }

    private void setupQRIS() {
        layoutQRIS.setVisibility(View.VISIBLE);

        try {
            // Generate QR Code content (JSON)
            JSONObject qrData = new JSONObject();
            qrData.put("orderId", orderId != null ? orderId : "");
            qrData.put("amount", amount != null ? amount : "0");
            qrData.put("category", category != null ? category : "");
            qrData.put("wisataNama", wisataNama != null ? wisataNama : "");
            qrData.put("timestamp", System.currentTimeMillis());

            String qrContent = qrData.toString();

            // Generate QR Code bitmap
            Bitmap qrBitmap = QRCodeGenerator.generateQRCode(qrContent, 500, 500);
            if (qrBitmap != null) {
                imgQRCode.setImageBitmap(qrBitmap);
            } else {
                Toast.makeText(this, "Gagal generate QR Code", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error generate QR: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void createTransaction() {
        try {
            String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                    ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                    : "guest";

            transactionId = FirebaseDatabase.getInstance().getReference("transactions").push().getKey();

            if (transactionId == null) {
                Toast.makeText(this, "Gagal membuat ID transaksi", Toast.LENGTH_SHORT).show();
                return;
            }

            Transaction transaction = new Transaction();
            transaction.setTransactionId(transactionId);
            transaction.setOrderId(orderId);
            transaction.setUserId(userId);
            transaction.setName(wisataNama != null ? wisataNama : "Produk");
            transaction.setCategory(category != null ? category : "Unknown");
            transaction.setPaymentMethod(method != null && method.contains("QRIS") ? "QRIS" : "VA");

            try {
                transaction.setAmount(Long.parseLong(amount));
            } catch (NumberFormatException e) {
                transaction.setAmount(0);
            }

            transaction.setStatus("pending");
            transaction.setCreatedAt(System.currentTimeMillis());
            transaction.setExpiredAt(System.currentTimeMillis() + PAYMENT_TIMEOUT);

            // Save to Firebase
            FirebaseTransactionManager.createTransaction(transaction, task -> {
                if (task.isSuccessful()) {
                    // Update order with transactionId
                    updateOrderWithTransaction();
                } else {
                    // Silent fail - transaction creation is not critical for payment display
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            // Don't crash - just log error
            Toast.makeText(this, "Warning: Transaksi tidak tersimpan", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateOrderWithTransaction() {
        DatabaseReference orderRef = FirebaseDatabase.getInstance()
                .getReference("orders").child(orderId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("transactionId", transactionId);
        updates.put("status", "Menunggu Pembayaran");

        orderRef.updateChildren(updates);
    }

    private void startPaymentTimer() {
        paymentTimer = new CountDownTimer(PAYMENT_TIMEOUT, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int minutes = (int) (millisUntilFinished / 1000) / 60;
                int seconds = (int) (millisUntilFinished / 1000) % 60;
                txtTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                txtTimer.setText("00:00");
                handlePaymentExpired();
            }
        };
        paymentTimer.start();
    }

    private void startStatusCheck() {
        statusCheckHandler = new Handler();
        statusCheckRunnable = new Runnable() {
            @Override
            public void run() {
                checkPaymentStatus();
                statusCheckHandler.postDelayed(this, 10000); // Check every 10 seconds
            }
        };
        statusCheckHandler.postDelayed(statusCheckRunnable, 10000);
    }

    private void checkPaymentStatus() {
        if (transactionId == null)
            return;

        FirebaseTransactionManager.getTransaction(transactionId, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Transaction transaction = snapshot.getValue(Transaction.class);
                    if (transaction != null && "paid".equals(transaction.getStatus())) {
                        stopTimerAndStatusCheck();
                        handlePaymentSuccess();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
            }
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Batalkan Pembayaran?")
                    .setMessage("Apakah Anda yakin ingin membatalkan pembayaran?")
                    .setPositiveButton("Ya", (dialog, which) -> finish())
                    .setNegativeButton("Tidak", null)
                    .show();
        });

        btnCopyVa.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("VA Number", txtVaNumber.getText().toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Nomor berhasil disalin", Toast.LENGTH_SHORT).show();
        });

        btnConfirmPayment.setOnClickListener(v -> {
            btnConfirmPayment.setEnabled(false);
            btnConfirmPayment.setText("Memverifikasi Pembayaran...");

            new Handler().postDelayed(this::updateOrderStatus, 2000);
        });
    }

    private void updateOrderStatus() {
        if (transactionId == null)
            return;

        // Update transaction status to paid
        FirebaseTransactionManager.updateTransactionStatus(transactionId, "paid", task -> {
            if (task.isSuccessful()) {
                // Update order status
                DatabaseReference orderRef = FirebaseDatabase.getInstance()
                        .getReference("orders").child(orderId);

                Map<String, Object> updates = new HashMap<>();
                updates.put("status", "Dibayar");
                updates.put("paymentMethod", method);

                orderRef.updateChildren(updates).addOnSuccessListener(aVoid -> {
                    // Send notification to admin
                    FCMHelper.sendPaymentSuccessNotification(category, orderId, transactionId,
                            Long.parseLong(amount));

                    stopTimerAndStatusCheck();
                    handlePaymentSuccess();
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal update status: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    btnConfirmPayment.setEnabled(true);
                    btnConfirmPayment.setText("Saya Sudah Bayar");
                });
            } else {
                Toast.makeText(this, "Gagal memverifikasi pembayaran", Toast.LENGTH_SHORT).show();
                btnConfirmPayment.setEnabled(true);
                btnConfirmPayment.setText("Saya Sudah Bayar");
            }
        });
    }

    private void handlePaymentSuccess() {
        new AlertDialog.Builder(this)
                .setTitle("Pembayaran Berhasil! 🎉")
                .setMessage("Terima kasih, pembayaran Anda telah kami terima.")
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("Lanjutkan", (dialog, which) -> {
                    routeToSuccessPage();
                })
                .show();
    }

    private void routeToSuccessPage() {
        Intent intent;

        if ("Hotel".equalsIgnoreCase(category) || "Penginapan".equalsIgnoreCase(category)) {
            intent = new Intent(this, VoucherActivity.class);
        } else if ("Wisata".equalsIgnoreCase(category) || "Tempat Wisata".equalsIgnoreCase(category)) {
            intent = new Intent(this, QRTicketActivity.class);
        } else if ("Aksesoris".equalsIgnoreCase(category)) {
            intent = new Intent(this, TrackingActivity.class);
        } else if ("Kuliner".equalsIgnoreCase(category) || "Makanan".equalsIgnoreCase(category)) {
            intent = new Intent(this, OrderStatusActivity.class);
        } else {
            // Default: back to orders
            finish();
            return;
        }

        intent.putExtra("transactionId", transactionId);
        intent.putExtra("orderId", orderId);
        intent.putExtra("category", category);
        startActivity(intent);
        finish();
    }

    private void handlePaymentExpired() {
        // Update transaction status to expired
        if (transactionId != null) {
            FirebaseTransactionManager.updateTransactionStatus(transactionId, "expired", null);
        }

        new AlertDialog.Builder(this)
                .setTitle("Pembayaran Kadaluarsa")
                .setMessage("Waktu pembayaran telah habis. Silakan buat pesanan baru.")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> finish())
                .show();
    }

    private void stopTimerAndStatusCheck() {
        if (paymentTimer != null) {
            paymentTimer.cancel();
        }
        if (statusCheckHandler != null && statusCheckRunnable != null) {
            statusCheckHandler.removeCallbacks(statusCheckRunnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimerAndStatusCheck();
    }
}
