package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DetailPulauYeruselActivity extends AppCompatActivity {

    private TextView txtTitle, txtPrice, txtRingkasan, txtDurasi, txtRating, txtDeskripsi;
    private Button btnOrder;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_yerusel);  // Sesuaikan dengan nama file XML Anda

        // Inisialisasi views
        txtTitle = findViewById(R.id.txtTitle);
        txtPrice = findViewById(R.id.txtPrice);
        txtRingkasan = findViewById(R.id.txtRingkasan);
        txtDurasi = findViewById(R.id.txtDurasi);
        txtRating = findViewById(R.id.txtRating);
        txtDeskripsi = findViewById(R.id.txtDeskripsi);
        btnOrder = findViewById(R.id.btnOrder);

        // Set Text
        txtTitle.setText("Pulau Yerusel");
        txtPrice.setText("100.000/ orang");
        txtRingkasan.setText("Ringkasan");
        txtDurasi.setText("Durasi 30 menit");
        txtRating.setText("Rating 70.5%");
        txtDeskripsi.setText("Pulau Yerusel di kab. Sorong, Papua Barat Daya, adalah mutiara tersembunyi yang menawarkan Keindahan sempurna ke alam dengan hamparan pasir putih, air laut jernih sebening kristal, dan taman bawah laut yang memukau untuk Berlibur. Hanya butuh 30 menit naik perahu dari daratan, ke pulau ini.");

        // Tombol Pesan sekarang
        btnOrder.setOnClickListener(v -> {
            // Logic untuk melakukan pemesanan (misalnya redirect ke halaman pembayaran atau konfirmasi)
            // Contoh: Redirect ke activity berikutnya

            finish();  // Memastikan aktivitas ini ditutup saat pemesanan dilakukan dan kembali ke halaman sebelumnya
        });
    }
}
