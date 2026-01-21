package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Button btnMasuk = findViewById(R.id.btnMasuk);
        Button btnDaftar = findViewById(R.id.btnDaftar);

        btnMasuk.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
        });

        btnDaftar.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, RegisterActivity.class));
        });
    }
}
