package com.example.myapplication;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AdminPaymentSettingsActivity extends AppCompatActivity {

    private EditText etBankName, etAccountNumber, etAccountHolder;
    private Button btnSave;
    private ImageView btnBack;
    private DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_payment_settings);

        mRef = FirebaseDatabase.getInstance().getReference("app_config").child("payment_info");

        initViews();
        loadCurrentData();
        setupListeners();
    }

    private void initViews() {
        etBankName = findViewById(R.id.etBankName);
        etAccountNumber = findViewById(R.id.etAccountNumber);
        etAccountHolder = findViewById(R.id.etAccountHolder);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);
    }

    private void loadCurrentData() {
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String bank = snapshot.child("bank_name").getValue(String.class);
                    String number = snapshot.child("account_number").getValue(String.class);
                    String holder = snapshot.child("account_holder").getValue(String.class);

                    if (bank != null)
                        etBankName.setText(bank);
                    if (number != null)
                        etAccountNumber.setText(number);
                    if (holder != null)
                        etAccountHolder.setText(holder);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AdminPaymentSettingsActivity.this, "Gagal memuat data: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            String bank = etBankName.getText().toString().trim();
            String number = etAccountNumber.getText().toString().trim();
            String holder = etAccountHolder.getText().toString().trim();

            if (bank.isEmpty() || number.isEmpty() || holder.isEmpty()) {
                Toast.makeText(this, "Semua kolom harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            saveData(bank, number, holder);
        });
    }

    private void saveData(String bank, String number, String holder) {
        btnSave.setEnabled(false);
        btnSave.setText("Menyimpan...");

        Map<String, Object> data = new HashMap<>();
        data.put("bank_name", bank);
        data.put("account_number", number);
        data.put("account_holder", holder);

        mRef.setValue(data).addOnCompleteListener(task -> {
            btnSave.setEnabled(true);
            btnSave.setText("Simpan Perubahan");

            if (task.isSuccessful()) {
                Toast.makeText(this, "Data Rekening Berhasil Disimpan!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Toast.makeText(this, "Gagal menyimpan: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
