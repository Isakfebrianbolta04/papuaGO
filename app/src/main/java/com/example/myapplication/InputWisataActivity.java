package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class InputWisataActivity extends AppCompatActivity {

    // UI Components
    private TextInputEditText namaInput, deskripsiInput, hargaInput, lokasiInput;
    private TextInputEditText waktuInput, transportasiInput, imageUrlInput;
    private Spinner kategoriSpinner;
    private Button btnSave, btnCancel, btnSelectImage;
    private ImageView btnBack;
    private ProgressBar progressBar;

    // Firebase
    private DatabaseReference databaseReference;

    // Selected values
    private String selectedKategori = "";

    // Gallery Launcher
    private final androidx.activity.result.ActivityResultLauncher<String> selectImageLauncher = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        String path = copyImageToInternalStorage(uri);
                        if (path != null) {
                            imageUrlInput.setText(path);
                            Toast.makeText(this, "Foto berhasil dipilih!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Gagal memproses foto", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_wisata);

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("wisata");

        // Initialize Views
        initViews();

        // Setup Kategori Spinner
        setupKategoriSpinner();

        // Setup Buttons
        setupButtons();

        // Check for pre-selected category from Admin Dashboard
        String target = getIntent().getStringExtra("targetCategory");
        if (target != null && !target.isEmpty()) {
            selectedKategori = target;
            setSpinnerToCategory(target);
        }
    }

    private void setSpinnerToCategory(String category) {
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) kategoriSpinner.getAdapter();
        if (adapter != null) {
            int pos = adapter.getPosition(category);
            if (pos >= 0) {
                kategoriSpinner.setSelection(pos);
            }
        }
    }

    private void initViews() {
        namaInput = findViewById(R.id.namaInput);
        deskripsiInput = findViewById(R.id.deskripsiInput);
        hargaInput = findViewById(R.id.hargaInput);
        lokasiInput = findViewById(R.id.lokasiInput);
        waktuInput = findViewById(R.id.waktuInput);
        imageUrlInput = findViewById(R.id.imageUrlInput); // Changed
        transportasiInput = findViewById(R.id.transportasiInput);
        kategoriSpinner = findViewById(R.id.kategoriSpinner);

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        btnBack = findViewById(R.id.btnBack);
        btnSelectImage = findViewById(R.id.btnSelectImage); // New Button

        progressBar = findViewById(R.id.progressBar);
    }

    private void setupKategoriSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.kategori_wisata,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        kategoriSpinner.setAdapter(adapter);

        kategoriSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectedKategori = parent.getItemAtPosition(position).toString();
                } else {
                    selectedKategori = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedKategori = "";
            }
        });
    }

    private void setupButtons() {
        // Back button
        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());
        btnSave.setOnClickListener(v -> saveWisataToFirebase());

        // Gallery Button
        btnSelectImage.setOnClickListener(v -> selectImageLauncher.launch("image/*"));
    }

    private String copyImageToInternalStorage(android.net.Uri uri) {
        try {
            java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
            java.io.File directory = new java.io.File(getFilesDir(), "wisata_images");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Generate unique filename
            String filename = "img_" + System.currentTimeMillis() + ".jpg";
            java.io.File file = new java.io.File(directory, filename);
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();

            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveWisataToFirebase() {
        // Get input values
        String nama = namaInput.getText().toString().trim();
        String deskripsi = deskripsiInput.getText().toString().trim();
        String harga = hargaInput.getText().toString().trim();
        String lokasi = lokasiInput.getText().toString().trim();
        String waktu = waktuInput.getText().toString().trim();
        String transportasi = transportasiInput.getText().toString().trim();

        // Use text from EditText
        String imageUrl = imageUrlInput.getText().toString().trim();

        // Validasi input
        if (!validateInputs(nama, deskripsi, harga, lokasi, waktu, transportasi, imageUrl)) {
            return;
        }

        // Show progress
        showProgress(true);

        // Generate unique ID menggunakan Firebase push()
        String wisataId = databaseReference.push().getKey();

        if (wisataId == null) {
            showProgress(false);
            Toast.makeText(this, "Error: Tidak dapat membuat ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create Wisata object
        Wisata wisata = new Wisata(
                wisataId,
                nama,
                selectedKategori,
                deskripsi,
                harga,
                lokasi,
                waktu,
                imageUrl,
                transportasi);

        // Save to Firebase
        databaseReference.child(wisataId).setValue(wisata)
                .addOnSuccessListener(aVoid -> {
                    showProgress(false);
                    Toast.makeText(InputWisataActivity.this,
                            "Data berhasil disimpan!", Toast.LENGTH_SHORT).show();
                    clearInputs();
                    finish(); // Kembali ke halaman sebelumnya
                })
                .addOnFailureListener(e -> {
                    showProgress(false);
                    Toast.makeText(InputWisataActivity.this,
                            "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateInputs(String nama, String deskripsi, String harga,
            String lokasi, String waktu, String transportasi, String imageUrl) {
        if (nama.isEmpty()) {
            namaInput.setError("Nama tempat harus diisi");
            namaInput.requestFocus();
            return false;
        }

        if (selectedKategori.isEmpty() || selectedKategori.equals("Pilih Kategori")) {
            Toast.makeText(this, "Silakan pilih kategori", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (deskripsi.isEmpty()) {
            deskripsiInput.setError("Deskripsi harus diisi");
            deskripsiInput.requestFocus();
            return false;
        }

        if (harga.isEmpty()) {
            hargaInput.setError("Harga harus diisi");
            hargaInput.requestFocus();
            return false;
        }

        if (lokasi.isEmpty()) {
            lokasiInput.setError("Lokasi harus diisi");
            lokasiInput.requestFocus();
            return false;
        }

        if (waktu.isEmpty()) {
            waktuInput.setError("Waktu liburan harus diisi");
            waktuInput.requestFocus();
            return false;
        }

        if (imageUrl.isEmpty()) {
            imageUrlInput.setError("Silakan pilih gambar atau masukkan URL");
            imageUrlInput.requestFocus();
            // Can show Toast
            return false;
        }

        if (transportasi.isEmpty()) {
            transportasiInput.setError("Transportasi harus diisi");
            transportasiInput.requestFocus();
            return false;
        }

        return true;
    }

    private void showProgress(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            btnSave.setEnabled(false);
            btnCancel.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnSave.setEnabled(true);
            btnCancel.setEnabled(true);
        }
    }

    private void clearInputs() {
        namaInput.setText("");
        deskripsiInput.setText("");
        hargaInput.setText("");
        lokasiInput.setText("");
        waktuInput.setText("");
        imageUrlInput.setText("");
        transportasiInput.setText("");
        kategoriSpinner.setSelection(0);
    }
}
