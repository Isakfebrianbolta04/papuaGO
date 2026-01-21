package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class AdminInputDataActivity extends AppCompatActivity {

    private LinearLayout btnInputWisata, btnInputHotel, btnInputKuliner, btnInputAksesoris;
    private ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_input_data);

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnInputWisata = findViewById(R.id.btnInputWisata);
        btnInputHotel = findViewById(R.id.btnInputHotel);
        btnInputKuliner = findViewById(R.id.btnInputKuliner);
        btnInputAksesoris = findViewById(R.id.btnInputAksesoris);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnInputWisata != null) {
            btnInputWisata.setOnClickListener(v -> openInputForm("Tempat Wisata"));
        }

        if (btnInputHotel != null) {
            btnInputHotel.setOnClickListener(v -> openInputForm("Penginapan"));
        }

        if (btnInputKuliner != null) {
            btnInputKuliner.setOnClickListener(v -> openInputForm("Makanan"));
        }

        if (btnInputAksesoris != null) {
            btnInputAksesoris.setOnClickListener(v -> openInputForm("Aksesoris"));
        }
    }

    private void openInputForm(String category) {
        Intent intent = new Intent(this, InputWisataActivity.class);
        intent.putExtra("targetCategory", category);
        startActivity(intent);
    }
}
