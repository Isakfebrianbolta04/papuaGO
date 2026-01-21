package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.Locale;

public class DetailWisataActivity extends AppCompatActivity {

    // UI Components
    private ImageView headerImage, btnBack;
    private TextView txtNama, txtKategori, txtHarga, txtWaktu;
    private TextView txtLokasi, txtTransportasi, txtDeskripsi;
    private Button btnBook;
    private FloatingActionButton btnFavorite;
    private boolean isFavorite = false;

    // Data Wisata
    private Wisata wisata;

    // Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_wisata);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Initialize Views
        initViews();

        // Get data from Intent
        getWisataData();

        // Display data
        displayWisataData();

        // Setup buttons
        setupButtons();
    }

    private void initViews() {
        headerImage = findViewById(R.id.headerImage);
        btnBack = findViewById(R.id.btnBack);
        btnFavorite = findViewById(R.id.btnFavorite); // New
        txtNama = findViewById(R.id.txtNama);
        txtKategori = findViewById(R.id.txtKategori);
        txtHarga = findViewById(R.id.txtHarga);
        txtWaktu = findViewById(R.id.txtWaktu);
        txtLokasi = findViewById(R.id.txtLokasi);
        txtTransportasi = findViewById(R.id.txtTransportasi);
        txtDeskripsi = findViewById(R.id.txtDeskripsi);
        btnBook = findViewById(R.id.btnBook);
    }

    private void getWisataData() {
        // Get data dari Intent
        String id = getIntent().getStringExtra("id");
        String nama = getIntent().getStringExtra("nama");
        String kategori = getIntent().getStringExtra("kategori");
        String deskripsi = getIntent().getStringExtra("deskripsi");
        String harga = getIntent().getStringExtra("harga");
        String lokasi = getIntent().getStringExtra("lokasi");
        String waktu = getIntent().getStringExtra("waktu");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String transportasi = getIntent().getStringExtra("transportasi");

        // Create Wisata object
        wisata = new Wisata(id, nama, kategori, deskripsi, harga, lokasi, waktu, imageUrl, transportasi);
    }

    private void displayWisataData() {
        if (wisata == null) {
            Toast.makeText(this, R.string.error_data_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set text data
        txtNama.setText(wisata.getNama());
        txtKategori.setText(wisata.getKategori());
        txtWaktu.setText(wisata.getWaktu());
        txtLokasi.setText(wisata.getLokasi());
        txtTransportasi.setText(wisata.getTransportasi());
        txtDeskripsi.setText(wisata.getDeskripsi());

        // Format harga
        txtHarga.setText(formatRupiah(wisata.getHarga()));

        // Set dynamic button text
        String kategori = wisata.getKategori();
        if (kategori != null) {
            if (kategori.contains("Wisata"))
                btnBook.setText(R.string.action_book_ticket);
            else if (kategori.contains("Penginapan"))
                btnBook.setText(R.string.action_book_room);
            else if (kategori.contains("Makanan"))
                btnBook.setText(R.string.action_order_menu);
            else if (kategori.contains("Aksesoris"))
                btnBook.setText(R.string.action_buy_now);
            else
                btnBook.setText(R.string.btn_order_now);
        }

        // Load image dengan Glide (Handle URL vs Resource Name)
        String imageUrl = wisata.getImageUrl();
        Object imageSource = imageUrl;

        // Jika imageUrl bukan URL internet (tidak diawali http), coba cari di drawable
        // lokal
        if (imageUrl != null && !imageUrl.startsWith("http")) {
            int resId = getResources().getIdentifier(imageUrl, "drawable", getPackageName());
            if (resId != 0) {
                imageSource = resId; // Gunakan resource ID lokal
            }
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this)
                    .load(imageSource)
                    .placeholder(R.drawable.raja4)
                    .error(R.drawable.raja4)
                    .centerCrop()
                    .into(headerImage);
        }
    }

    private String formatRupiah(String price) {
        if (price == null || price.isEmpty())
            return "Rp. 0";
        try {
            String cleanPrice = price.replaceAll("[^0-9]", "");
            if (cleanPrice.isEmpty())
                return "Rp. " + price;

            long value = Long.parseLong(cleanPrice);
            java.text.DecimalFormat formatter = (java.text.DecimalFormat) java.text.NumberFormat
                    .getInstance(new java.util.Locale("id", "ID"));
            return "Rp. " + formatter.format(value);
        } catch (Exception e) {
            return "Rp. " + price;
        }
    }

    private void setupButtons() {
        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Favorite Button
        checkFavoriteStatus();
        btnFavorite.setOnClickListener(v -> toggleFavorite());

        // Book button
        btnBook.setOnClickListener(v -> {
            // Tampilkan Dialog Konfirmasi
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_confirm_order_title)
                    .setMessage(
                            getString(R.string.dialog_confirm_order_message,
                                    btnBook.getText().toString().toLowerCase()))
                    .setPositiveButton(R.string.action_yes_order, (dialog, which) -> performBooking())
                    .setNegativeButton(R.string.action_cancel, null)
                    .show();
        });
    }

    private void checkFavoriteStatus() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null || wisata == null)
            return;

        DatabaseReference favRef = mDatabase.child("users").child(user.getUid())
                .child("favorites").child(wisata.getId());

        favRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    isFavorite = true;
                    btnFavorite.setColorFilter(android.graphics.Color.parseColor("#E91E63")); // Red
                } else {
                    isFavorite = false;
                    btnFavorite.setColorFilter(android.graphics.Color.parseColor("#D9D9D9")); // Grey
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }

    private void toggleFavorite() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, R.string.toast_login_required, Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference favRef = mDatabase.child("users").child(user.getUid())
                .child("favorites").child(wisata.getId());

        if (isFavorite) {
            favRef.removeValue()
                    .addOnSuccessListener(
                            aVoid -> Toast.makeText(this, R.string.toast_fav_removed, Toast.LENGTH_SHORT).show());
        } else {
            favRef.setValue(true)
                    .addOnSuccessListener(
                            aVoid -> Toast.makeText(this, R.string.toast_fav_added, Toast.LENGTH_SHORT).show());
        }
    }

    private void performBooking() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.toast_login_required, Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String orderId = mDatabase.child("orders").push().getKey();
        long timestamp = System.currentTimeMillis();

        Order order = new Order(
                orderId,
                userId,
                wisata.getId(),
                wisata.getNama(),
                wisata.getHarga(),
                wisata.getImageUrl(),
                wisata.getKategori(),
                timestamp,
                getString(R.string.status_pending), // Fixed Status
                null, // Payment method initially null
                null); // Transaction ID initially null

        if (orderId != null) {
            btnBook.setEnabled(false);
            btnBook.setText(R.string.status_processing);

            mDatabase.child("orders").child(orderId).setValue(order)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(DetailWisataActivity.this, R.string.toast_order_success, Toast.LENGTH_LONG)
                                .show();
                        btnBook.setText(R.string.status_already_ordered);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(DetailWisataActivity.this,
                                getString(R.string.error_order_failed, e.getMessage()),
                                Toast.LENGTH_SHORT).show();
                        btnBook.setEnabled(true);
                        btnBook.setText(R.string.btn_order_now);
                    });
        }
    }
}
