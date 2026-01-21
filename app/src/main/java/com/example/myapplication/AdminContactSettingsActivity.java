package com.example.myapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AdminContactSettingsActivity extends AppCompatActivity {

    private EditText etWhatsApp, etEmail;
    private Button btnSave;
    private ImageView btnBack;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_contact_settings);

        mDatabase = FirebaseDatabase.getInstance().getReference("app_config").child("contact_info");

        etWhatsApp = findViewById(R.id.etWhatsApp);
        etEmail = findViewById(R.id.etEmail);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> saveSettings());
        }

        loadSettings();
    }

    private void loadSettings() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String whatsapp = snapshot.child("whatsapp").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    if (whatsapp != null)
                        etWhatsApp.setText(whatsapp);
                    if (email != null)
                        etEmail.setText(email);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminContactSettingsActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveSettings() {
        String whatsapp = etWhatsApp.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(whatsapp)) {
            etWhatsApp.setError("Nomor WhatsApp wajib diisi");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("whatsapp", whatsapp);
        updates.put("email", email);

        mDatabase.updateChildren(updates, (error, ref) -> {
            if (error == null) {
                Toast.makeText(AdminContactSettingsActivity.this, "Kontak berhasil disimpan", Toast.LENGTH_SHORT)
                        .show();
                finish();
            } else {
                Toast.makeText(AdminContactSettingsActivity.this, "Gagal menyimpan: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
