package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword, etAdminSecretLogin;
    CheckBox cbAdminMode;
    Button btnLogin;
    private FirebaseAuth mAuth;
    private View progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inisialisasi FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etAdminSecretLogin = findViewById(R.id.etAdminSecretLogin);
        cbAdminMode = findViewById(R.id.cbAdminMode);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        // Toggle visibility kode rahasia admin
        cbAdminMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etAdminSecretLogin.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        btnLogin.setOnClickListener(v -> {

            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, R.string.email_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.isEmpty()) {
                Toast.makeText(this, R.string.password_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            // Validasi Kode Admin jika mode admin aktif
            if (cbAdminMode.isChecked()) {
                String secretInput = etAdminSecretLogin.getText().toString().trim();
                if (!"PAPUAGO2026".equals(secretInput)) {
                    Toast.makeText(this, R.string.error_admin_secret, Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Login dengan Firebase Authentication
            // Show loading
            progressBar.setVisibility(View.VISIBLE);
            btnLogin.setEnabled(false);

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                if (cbAdminMode.isChecked()) {
                                    // MODE UPGRADE ADMIN
                                    DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users")
                                            .child(user.getUid());
                                    java.util.HashMap<String, Object> updates = new java.util.HashMap<>();
                                    updates.put("role", "admin");

                                    userRef.updateChildren(updates).addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, R.string.toast_admin_upgrade,
                                                Toast.LENGTH_SHORT).show();
                                        navigateToAdminDashboard();
                                    }).addOnFailureListener(e -> {
                                        Toast.makeText(this, getString(R.string.error_save_data_failed, e.getMessage()),
                                                Toast.LENGTH_LONG)
                                                .show();
                                        checkRoleAndNavigate(user.getUid());
                                    });
                                } else {
                                    // LOGIN NORMAL - Cek Role di Database
                                    // Save Gmail photo URL if available
                                    saveGmailPhotoUrl(user);
                                    checkRoleAndNavigate(user.getUid());
                                }
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            btnLogin.setEnabled(true);
                            String errorMsg = task.getException() != null ? task.getException().getMessage()
                                    : getString(R.string.unknown_error);
                            Toast.makeText(this, getString(R.string.login_failed, errorMsg), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Forgot Password Logic
        android.widget.TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvForgotPassword.setOnClickListener(v -> {
            android.widget.EditText resetMail = new android.widget.EditText(v.getContext());
            resetMail.setHint(R.string.hint_email_reset);
            if (etEmail.getText().toString().length() > 0) {
                resetMail.setText(etEmail.getText().toString());
            }

            new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                    .setTitle(R.string.dialog_reset_pwd_title)
                    .setMessage(R.string.dialog_reset_pwd_message)
                    .setView(resetMail)
                    .setPositiveButton(R.string.action_send, (dialog, which) -> {
                        String mail = resetMail.getText().toString().trim();
                        if (!mail.isEmpty()) {
                            mAuth.sendPasswordResetEmail(mail)
                                    .addOnSuccessListener(aVoid -> {
                                        new androidx.appcompat.app.AlertDialog.Builder(LoginActivity.this)
                                                .setTitle(R.string.dialog_email_sent_title)
                                                .setMessage(getString(R.string.dialog_email_sent_message, mail))
                                                .setPositiveButton("OK", null)
                                                .show();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(LoginActivity.this,
                                            getString(R.string.error_send_email_failed, e.getMessage()),
                                            Toast.LENGTH_LONG).show());
                        } else {
                            Toast.makeText(LoginActivity.this, R.string.error_email_required, Toast.LENGTH_SHORT)
                                    .show();
                        }
                    })
                    .setNegativeButton(R.string.action_cancel, null)
                    .show();
        });
    }

    private void saveGmailPhotoUrl(FirebaseUser user) {
        if (user == null)
            return;

        android.net.Uri photoUrl = user.getPhotoUrl();
        if (photoUrl != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid());

            // Check if user already has a custom profile image
            userRef.child("profileImage").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    // Only save Gmail photo URL if no custom image exists
                    if (!snapshot.exists()) {
                        // Save the Gmail photo URL for future use
                        userRef.child("gmailPhotoUrl").setValue(photoUrl.toString())
                                .addOnFailureListener(e -> {
                                    // Silent fail - not critical
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Silent fail - not critical
                }
            });
        }
    }

    private void checkRoleAndNavigate(String uid) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isFinishing()) {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);

                    String role = "user"; // default
                    String username = "Pengguna"; // default name

                    if (snapshot.exists()) {
                        if (snapshot.hasChild("role")) {
                            Object roleObj = snapshot.child("role").getValue();
                            if (roleObj != null)
                                role = String.valueOf(roleObj).trim();
                        }
                        if (snapshot.hasChild("username")) {
                            Object nameObj = snapshot.child("username").getValue();
                            if (nameObj != null)
                                username = String.valueOf(nameObj).trim();
                        } else if (snapshot.hasChild("name")) {
                            Object nameObj = snapshot.child("name").getValue();
                            if (nameObj != null)
                                username = String.valueOf(nameObj).trim();
                        }
                    }

                    if ("admin".equalsIgnoreCase(role)) {
                        // User request: Even if admin, go to Beranda unless Admin Mode was checked
                        Toast.makeText(LoginActivity.this, getString(R.string.toast_welcome, username),
                                Toast.LENGTH_SHORT)
                                .show();
                        navigateToBeranda();
                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.toast_welcome, username),
                                Toast.LENGTH_SHORT)
                                .show();
                        navigateToBeranda();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isFinishing()) {
                    progressBar.setVisibility(View.GONE);
                    btnLogin.setEnabled(true);
                    Toast.makeText(LoginActivity.this, "Error Database: " + error.getMessage(), Toast.LENGTH_SHORT)
                            .show();
                    navigateToBeranda(); // Fallback
                }
            }
        });
    }

    private void navigateToBeranda() {
        Intent intent = new Intent(LoginActivity.this, BerandaActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToAdminDashboard() {
        Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
