package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DetailHotelNusaIndahActivity extends AppCompatActivity {

    private TextView hotelName, hotelPrice, roomInfo, roomNumber, roomType, roomPrice, totalPrice, userName, userEmail, userPhone;
    private Button btnPesanSekarang;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_hotel_nusaindah);

        // Inisialisasi views
        hotelName = findViewById(R.id.hotelName);
        hotelPrice = findViewById(R.id.hotelPrice);
        roomInfo = findViewById(R.id.roomInfo);
        roomNumber = findViewById(R.id.roomNumber);
        roomType = findViewById(R.id.roomType);
        roomPrice = findViewById(R.id.roomPrice);
        totalPrice = findViewById(R.id.totalPrice);
        userName = findViewById(R.id.userName);
        userEmail = findViewById(R.id.userEmail);
        userPhone = findViewById(R.id.userPhone);
        btnPesanSekarang = findViewById(R.id.btnPesanSekarang);

        // Set Text
        hotelName.setText("Hotel Nusa Indah");
        hotelPrice.setText("200.000rb / Hari");
        roomInfo.setText("Informasi Kamar");
        roomNumber.setText("Nomor kamar: A01");
        roomType.setText("Tipe Kamar: Regular");
        roomPrice.setText("Harga: 200rb (1x 24 jam)");
        totalPrice.setText("Total: 200rb");
        userName.setText("Nama: Febrian Boltai");
        userEmail.setText("Email: febrian@gmail.com");
        userPhone.setText("Nomor HP: 08228988990");

        // Tombol Pesan sekarang
        btnPesanSekarang.setOnClickListener(v -> {
            // Logic untuk melakukan pemesanan (misalnya redirect ke halaman pembayaran atau konfirmasi)
            // Contoh: Redirect ke activity berikutnya
        });
    }
}
