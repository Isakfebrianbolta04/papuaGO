package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText etNama, etEmail, etPassword;
    private Button btnUploadFoto, btnDaftarAkun;

    private Uri selectedImageUri = null;

    // Inisialisasi Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    // Ambil foto dari galeri
    ActivityResultLauncher<Intent> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    Toast.makeText(this, R.string.toast_photo_selected, Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inisialisasi Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        etNama = findViewById(R.id.etNama);
        etEmail = findViewById(R.id.etEmailDaftar);
        etPassword = findViewById(R.id.etPasswordDaftar);
        btnUploadFoto = findViewById(R.id.btnUploadFoto);
        btnDaftarAkun = findViewById(R.id.btnDaftarAkun);

        // Upload Foto
        btnUploadFoto.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            pickImageLauncher.launch(intent);
        });

        // Tombol Daftar
        btnDaftarAkun.setOnClickListener(view -> {
            String nama = etNama.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();

            // Default semua pendaftar baru sebagai 'user'
            String role = "user";

            if (nama.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, R.string.error_all_fields_required, Toast.LENGTH_SHORT).show();
                return;
            }

            // Daftarkan pengguna menggunakan Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Jika registrasi auth berhasil, simpan metadata ke database
                            String uid = mAuth.getCurrentUser().getUid();
                            UserAccount account = new UserAccount(uid, nama, email, role);

                            databaseReference.child(uid).setValue(account)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, R.string.toast_register_success,
                                                Toast.LENGTH_LONG).show();
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, getString(R.string.error_save_data_failed, e.getMessage()),
                                                Toast.LENGTH_SHORT)
                                                .show();
                                    });
                        } else {
                            String errorMsg = task.getException() != null ? task.getException().getMessage()
                                    : getString(R.string.unknown_error);
                            Toast.makeText(this, getString(R.string.error_register_failed, errorMsg),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
