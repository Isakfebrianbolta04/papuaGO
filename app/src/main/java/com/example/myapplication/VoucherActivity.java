package com.example.myapplication;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
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

import java.text.NumberFormat;
import java.util.Locale;

public class VoucherActivity extends AppCompatActivity {

    private ImageView btnBack, imgVoucherQR;
    private TextView txtHotelNama, txtRoomType, txtGuestName, txtNights, txtOrderId, txtStatus, txtBookingCode;
    private String transactionId, orderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voucher);

        transactionId = getIntent().getStringExtra("transactionId");
        orderId = getIntent().getStringExtra("orderId");

        initViews();
        loadVoucherData();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        imgVoucherQR = findViewById(R.id.imgVoucherQR);
        txtHotelNama = findViewById(R.id.txtHotelNama);
        txtRoomType = findViewById(R.id.txtRoomType);
        txtGuestName = findViewById(R.id.txtGuestName);
        txtNights = findViewById(R.id.txtNights);
        txtOrderId = findViewById(R.id.txtOrderId);
        txtStatus = findViewById(R.id.txtStatus);
        txtBookingCode = findViewById(R.id.txtBookingCode);

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadVoucherData() {
        if (transactionId == null) {
            Toast.makeText(this, "Data voucher tidak ditemukan", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase.getInstance().getReference("transactions")
                .child(transactionId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Transaction transaction = snapshot.getValue(Transaction.class);
                            if (transaction != null) {
                                displayVoucherData(transaction);
                                if ("Completed".equals(transaction.getStatus())
                                        || "Selesai".equals(transaction.getStatus())) {
                                    generateVoucherQR(transaction);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(VoucherActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayVoucherData(Transaction transaction) {
        txtOrderId.setText("#" + orderId);
        txtStatus.setText(transaction.getStatus());

        if (transaction.getBookingData() != null) {
            String guestName = BookingData.getString(transaction.getBookingData(), "guestName");
            if (guestName.isEmpty())
                guestName = BookingData.getString(transaction.getBookingData(), "visitorName");

            String roomType = BookingData.getString(transaction.getBookingData(), "roomType");
            int nights = BookingData.getInt(transaction.getBookingData(), "nights");
            String bookingCode = BookingData.getString(transaction.getBookingData(), "bookingCode");

            txtGuestName.setText(guestName.isEmpty() ? "-" : guestName);
            txtRoomType.setText(roomType.isEmpty() ? "Standard Room" : roomType);
            txtNights.setText(nights + " Malam");

            if (bookingCode != null && !bookingCode.isEmpty()) {
                txtBookingCode.setText(bookingCode);
                txtBookingCode.setTextColor(getResources().getColor(R.color.brand_brown));
            } else {
                txtBookingCode.setText("MENUNGGU KONFIRMASI");
                txtBookingCode.setTextColor(getResources().getColor(R.color.text_secondary));
            }
        }

        txtHotelNama.setText(transaction.getName());
    }

    private void generateVoucherQR(Transaction transaction) {
        try {
            JSONObject qrData = new JSONObject();
            qrData.put("type", "HOTEL_VOUCHER");
            qrData.put("orderId", orderId);
            qrData.put("transactionId", transactionId);
            qrData.put("bookingCode", BookingData.getString(transaction.getBookingData(), "bookingCode"));

            Bitmap bitmap = QRCodeGenerator.generateQRCode(qrData.toString(), 500, 500);
            if (bitmap != null) {
                imgVoucherQR.setImageBitmap(bitmap);
                imgVoucherQR.setAlpha(1.0f);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
