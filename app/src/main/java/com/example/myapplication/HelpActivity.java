package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class HelpActivity extends AppCompatActivity {

    private String adminWhatsApp = "+6281234567890"; // Default
    private String adminEmail = "support@papuago.com"; // Default

    // UI Components
    private ImageView btnBack;
    private CardView btnEmail;
    private CardView btnWhatsApp;
    private android.widget.TextView tvEmail, tvWhatsApp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        btnBack = findViewById(R.id.btnBack);
        btnEmail = findViewById(R.id.btnEmail);
        btnWhatsApp = findViewById(R.id.btnWhatsApp);
        tvEmail = findViewById(R.id.tvEmail);
        tvWhatsApp = findViewById(R.id.tvWhatsApp);

        fetchAdminContacts();

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (btnEmail != null) {
            btnEmail.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + adminEmail));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Bantuan Aplikasi PapuaGO");
                try {
                    startActivity(Intent.createChooser(intent, "Kirim Email via..."));
                } catch (Exception e) {
                    Toast.makeText(this, "Tidak ada aplikasi email terpasang", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnWhatsApp != null) {
            btnWhatsApp.setOnClickListener(v -> {
                String message = "Halo, saya butuh bantuan di aplikasi PapuaGO.";
                String url = "https://api.whatsapp.com/send?phone=" + adminWhatsApp + "&text=" + Uri.encode(message);
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            });
        }
    }

    private void fetchAdminContacts() {
        com.google.firebase.database.FirebaseDatabase.getInstance().getReference("app_config")
                .child("contact_info")
                .addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(
                            @androidx.annotation.NonNull com.google.firebase.database.DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            String wa = snapshot.child("whatsapp").getValue(String.class);
                            String mail = snapshot.child("email").getValue(String.class);

                            if (wa != null && !wa.isEmpty()) {
                                adminWhatsApp = wa;
                                if (tvWhatsApp != null)
                                    tvWhatsApp.setText(wa);
                            }

                            if (mail != null && !mail.isEmpty()) {
                                adminEmail = mail;
                                if (tvEmail != null)
                                    tvEmail.setText(mail);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(
                            @androidx.annotation.NonNull com.google.firebase.database.DatabaseError error) {
                        // Keep default
                    }
                });
    }
}
