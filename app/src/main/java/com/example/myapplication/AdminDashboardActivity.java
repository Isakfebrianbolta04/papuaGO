package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    private LinearLayout btnAdminWisata, btnAdminHotel, btnAdminMakanan, btnAdminAksesoris;
    private LinearLayout btnResetData, btnLogout, btnPaymentSettings;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnAdminWisata = findViewById(R.id.btnAdminWisata);
        btnAdminHotel = findViewById(R.id.btnAdminHotel);
        btnAdminMakanan = findViewById(R.id.btnAdminMakanan);
        btnAdminAksesoris = findViewById(R.id.btnAdminAksesoris);
        btnResetData = findViewById(R.id.btnResetData);
        btnLogout = findViewById(R.id.btnLogout);
        btnPaymentSettings = findViewById(R.id.btnPaymentSettings);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupListeners() {
        // Fix Back Button Crash: Redirect to Login instead of just finish()
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> logout());
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> logout());
        }

        if (btnPaymentSettings != null) {
            btnPaymentSettings.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminPaymentSettingsActivity.class);
                startActivity(intent);
                startActivity(intent);
            });
        }

        LinearLayout btnContactSettings = findViewById(R.id.btnContactSettings);
        if (btnContactSettings != null) {
            btnContactSettings.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminContactSettingsActivity.class);
                startActivity(intent);
            });
        }

        // Category buttons now open Order Management
        if (btnAdminWisata != null) {
            btnAdminWisata.setOnClickListener(v -> openOrderManagement("Wisata", 0));
        }

        if (btnAdminHotel != null) {
            btnAdminHotel.setOnClickListener(v -> openOrderManagement("Hotel", 1));
        }

        if (btnAdminMakanan != null) {
            btnAdminMakanan.setOnClickListener(v -> openOrderManagement("Kuliner", 2));
        }

        if (btnAdminAksesoris != null) {
            btnAdminAksesoris.setOnClickListener(v -> openOrderManagement("Aksesoris", 3));
        }

        // Add New Data button (will be added to layout)
        LinearLayout btnAddNewData = findViewById(R.id.btnAddNewData);
        if (btnAddNewData != null) {
            btnAddNewData.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminInputDataActivity.class);
                startActivity(intent);
            });
        }

        // Remove "Lihat/Hapus Data" - commented out
        // if (btnViewData != null) { ... }

        if (btnResetData != null) {
            btnResetData.setOnClickListener(v -> {
                DataSeeder.seedAll();
                Toast.makeText(this, "Data Default Telah Diisi!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void logout() {
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
        Toast.makeText(this, "Berhasil Keluar", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void openOrderManagement(String category, int tabIndex) {
        Intent intent = new Intent(this, AdminOrdersActivity.class);
        intent.putExtra("category", category);
        intent.putExtra("tabIndex", tabIndex);
        startActivity(intent);
    }
}
