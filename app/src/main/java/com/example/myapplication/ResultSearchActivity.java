package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ResultSearchActivity extends AppCompatActivity {

        private TextView txtSearchResult;
        private ImageView btnBack;
        private RecyclerView recyclerResults;
        private LinearLayout emptyState;
        private ProgressBar progressBar;

        private WisataSearchAdapter adapter;
        private List<Wisata> wisataList;
        private DatabaseReference databaseReference;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_result_search);

                // 1. Initialize data list immediately
                wisataList = new ArrayList<>();
                databaseReference = FirebaseDatabase.getInstance().getReference("wisata");

                // 2. Initialize Views
                initViews();
                setupBackButton();

                // 3. Get Intent data
                String query = getIntent().getStringExtra("category");

                if (query != null && !query.isEmpty()) {
                        Toast.makeText(this, "Mencari: " + query, Toast.LENGTH_SHORT).show();
                        if (txtSearchResult != null) {
                                txtSearchResult.setText("Hasil: " + query);
                        }
                        searchWisata(query);
                } else {
                        if (txtSearchResult != null) {
                                txtSearchResult.setText("Pencarian Kosong");
                        }
                        showEmptyState();
                }
        }

        private void initViews() {
                txtSearchResult = findViewById(R.id.txtSearchResult);
                btnBack = findViewById(R.id.btnBack);
                recyclerResults = findViewById(R.id.recyclerResults);
                emptyState = findViewById(R.id.emptyState);
                progressBar = findViewById(R.id.progressBar);

                if (recyclerResults != null) {
                        recyclerResults.setLayoutManager(new LinearLayoutManager(this));
                        adapter = new WisataSearchAdapter(wisataList, wisata -> openDetailWisata(wisata));
                        recyclerResults.setAdapter(adapter);
                }
        }

        private void setupBackButton() {
                if (btnBack != null) {
                        btnBack.setOnClickListener(v -> {
                                finish();
                                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        });
                }
        }

        private void searchWisata(String query) {
                if (progressBar != null)
                        progressBar.setVisibility(View.VISIBLE);
                if (emptyState != null)
                        emptyState.setVisibility(View.GONE);
                if (recyclerResults != null)
                        recyclerResults.setVisibility(View.GONE);

                // Simulate network delay using Main Looper for stability
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                        if (isFinishing())
                                return;
                        wisataList.clear();
                        List<Wisata> allData = getLocalWisataList();
                        String queryLower = query.toLowerCase();

                        for (Wisata wisata : allData) {
                                boolean matchCategory = wisata.getKategori() != null &&
                                                wisata.getKategori().toLowerCase().contains(queryLower);
                                boolean matchName = wisata.getNama() != null &&
                                                wisata.getNama().toLowerCase().contains(queryLower);
                                boolean matchLocation = wisata.getLokasi() != null &&
                                                wisata.getLokasi().toLowerCase().contains(queryLower);
                                boolean matchDesc = wisata.getDeskripsi() != null &&
                                                wisata.getDeskripsi().toLowerCase().contains(queryLower);

                                if (matchCategory || matchName || matchLocation || matchDesc) {
                                        wisataList.add(wisata);
                                }
                        }

                        if (progressBar != null)
                                progressBar.setVisibility(View.GONE);

                        if (wisataList == null || wisataList.isEmpty()) {
                                showEmptyState();
                        } else {
                                showResults();
                        }
                }, 500);
        }

        private List<Wisata> getLocalWisataList() {
                List<Wisata> list = new ArrayList<>();
                // --- TEMPAT WISATA ---
                list.add(new Wisata("wisata_raja4", getString(R.string.title_raja_ampat),
                                getString(R.string.cat_wisata), getString(R.string.desc_raja_ampat), "5000000",
                                getString(R.string.waisai), getString(R.string.dur_4d3n), "raja4",
                                getString(R.string.trans_plane_boat), "4.9"));
                list.add(new Wisata("wisata_maybrat", getString(R.string.title_ayamaru), getString(R.string.cat_wisata),
                                getString(R.string.desc_ayamaru), "500000", getString(R.string.maybrat),
                                getString(R.string.dur_1day), "maybrat", getString(R.string.trans_car_motor), "4.6"));
                list.add(new Wisata("wisata_sorsel", getString(R.string.title_sorsel), getString(R.string.cat_wisata),
                                getString(R.string.desc_sorsel), "1500000", getString(R.string.sorong_selatan),
                                getString(R.string.dur_2d1n), "sorsel", getString(R.string.trans_travel_car), "4.7"));
                list.add(new Wisata("wisata_sorong", getString(R.string.title_sorong),
                                getString(R.string.cat_wisata),
                                getString(R.string.desc_sorong), "750000", getString(R.string.kota_sorong),
                                getString(R.string.dur_1day), "sorong", getString(R.string.trans_public), "4.6"));
                list.add(new Wisata("wisata_um", getString(R.string.title_um), getString(R.string.cat_wisata),
                                getString(R.string.desc_um), "100000", getString(R.string.kota_sorong),
                                getString(R.string.dur_1day), "um", getString(R.string.trans_boat), "4.8"));
                list.add(new Wisata("wisata_uter", getString(R.string.title_uter), getString(R.string.cat_wisata),
                                getString(R.string.desc_uter), "250000", getString(R.string.maybrat),
                                getString(R.string.dur_1day), "uter", getString(R.string.trans_car_motor), "4.9"));
                list.add(new Wisata("wisata_yerusel", getString(R.string.title_yerusel),
                                getString(R.string.cat_wisata),
                                getString(R.string.desc_yerusel), "150000", getString(R.string.kota_sorong),
                                getString(R.string.dur_1day), "yerusel", getString(R.string.trans_boat), "4.7"));
                list.add(new Wisata("wisata_sembra", getString(R.string.title_sembra),
                                getString(R.string.cat_wisata),
                                getString(R.string.desc_sembra), "75000", getString(R.string.maybrat),
                                getString(R.string.dur_1day), "sembra", getString(R.string.trans_car_motor), "4.5"));
                list.add(new Wisata("wisata_tambrauw", getString(R.string.title_tambrauw),
                                getString(R.string.cat_wisata), getString(R.string.desc_tambrauw), "3500000",
                                getString(R.string.tambrauw), getString(R.string.dur_3d2n), "tambrauw",
                                getString(R.string.trans_4x4), "4.7"));

                // --- PENGINAPAN (RESTORED ALFIRA & AIMAS) ---
                list.add(new Wisata("wisata_nusa", getString(R.string.title_nusa), "Penginapan",
                                getString(R.string.desc_nusa), "450000", getString(R.string.sorong_selatan), "Malam",
                                "nusa", "Travel", "4.5"));
                list.add(new Wisata("wisata_maratuwa", getString(R.string.title_maratuwa), "Penginapan",
                                "Akomodasi tenang di Sorsel.", "500000", getString(R.string.sorong_selatan), "Malam",
                                "maratuwa", "Travel", "4.6"));
                list.add(new Wisata("wisata_piaynemo", "Piaynemo Homestay", "Penginapan", "View karst yang ikonik.",
                                "1500000", "Raja Ampat", "Malam", "piaynemo_homestay", "Speedboat", "4.8"));
                list.add(new Wisata("wisata_alfira", "Hotel Alfira", "Penginapan", "Hotel nyaman di Sorong.", "400000",
                                "Sorong", "Malam", "alfira", "Taksi", "4.4"));
                list.add(new Wisata("wisata_aimas", "Hotel Aimas", "Penginapan", "Fasilitas lengkap di Aimas.",
                                "550000", "Aimas", "Malam", "aimas", "Taksi", "4.5"));

                // --- AKSESORIS (REFINED) ---
                list.add(new Wisata("wisata_noken", getString(R.string.title_noken), "Aksesoris",
                                getString(R.string.desc_noken), "250000", "Pusat Seni", "Buah", "noken", "-", "4.8"));
                list.add(new Wisata("wisata_noken_kayu", getString(R.string.title_noken_kayu), "Aksesoris",
                                getString(R.string.desc_noken_kayu), "450000", "Pasar Budaya", "Buah", "tas_kulit_kayu",
                                "-", "4.9"));
                list.add(new Wisata("wisata_noken_ra", "Noken Raja Ampat", "Aksesoris", "Tas rajut autentik RA.",
                                "150000", "Raja Ampat", "Buah", "noken_ra", "-", "4.8"));
                list.add(new Wisata("wisata_akar_bahar", "Gelang Akar Bahar", "Aksesoris", "Gelang laut mistis.",
                                "350000", "Pusat Kerajinan", "Buah", "akar_bahar", "-", "4.9"));
                list.add(new Wisata("wisata_kalung_kerang", "Kalung Kerang", "Aksesoris", "Hiasan leher kerang asli.",
                                "75000", "Pantai", "Buah", "kalung_kerang", "-", "4.7"));
                list.add(new Wisata("wisata_pakaian", "Pakaian Adat", "Aksesoris", "Busana tradisional Papua.",
                                "1200000", "Sorong", "Set", "pakian_adat", "-", "4.9"));
                list.add(new Wisata("wisata_mahkota", "Mahkota Cendrawasih", "Aksesoris", "Hiasan kepala mewah.",
                                "750000", "Sorong", "Buah", "mahkota_baru", "-", "5.0"));
                list.add(new Wisata("wisata_tifa", "Tifa", "Aksesoris", "Alat musik tradisional.", "500000",
                                "Sentra Seni", "Buah", "tifa", "-", "4.8"));
                list.add(new Wisata("wisata_batik", getString(R.string.title_batik), "Aksesoris",
                                getString(R.string.desc_batik), "350000", "Sorong", "Meter", "batik_sorong", "-",
                                "4.7"));
                list.add(new Wisata("wisata_anyaman", getString(R.string.title_anyaman), "Aksesoris",
                                getString(R.string.desc_anyaman), "250000", "Sorong Selatan", "Buah", "anyaman_pbd",
                                "-", "4.6"));

                // --- MAKANAN (RESTORED & FIXED IMAGES) ---
                list.add(new Wisata("wisata_papeda", "Papeda", "Makanan", getString(R.string.desc_papeda), "50000",
                                "Rumah Makan", "Porsi", "papeda_baru", "-", "4.9"));
                list.add(new Wisata("wisata_keladi", "Keladi Bakar", "Makanan", "Keladi bakar gurih.", "25000", "Pasar",
                                "Porsi", "keladi_asli", "-", "4.8"));
                list.add(new Wisata("wisata_nasgor", "Nasi Goreng Papua", "Makanan", "Nasgor rempah lokal.", "30000",
                                "Warung", "Porsi", "nasgor_baru", "-", "4.5"));
                list.add(new Wisata("wisata_chips", "Keripik Keladi", "Makanan", "Snack renyah khas Sorong.", "35000",
                                "Toko Oleh-oleh", "Bungkus", "keripik_keladi", "-", "4.7"));
                list.add(new Wisata("wisata_sagu", "Sagu Lempeng", "Makanan", "Kue sagu tradisional.", "15000", "Pasar",
                                "Buah", "sagu_lempeng", "-", "4.6"));
                list.add(new Wisata("wisata_ikan", "Ikan Bakar Papua", "Makanan", "Ikan segar bumbu kuning.", "85000",
                                "Pantai", "Porsi", "ikan_bakar", "-", "4.7"));
                list.add(new Wisata("wisata_udang", "Udang Selingkuh", "Makanan", "Udang khas Wamena/Papua.", "120000",
                                "Resto", "Porsi", "udang_selingkuh", "-", "4.8"));
                list.add(new Wisata("wisata_ikan_bungkus", getString(R.string.title_ikan_bungkus), "Makanan",
                                getString(R.string.desc_ikan_bungkus), "45000", "Pasar", "Bungkus", "ikan_bungkus", "-",
                                "4.7"));

                return list;
        }

        private void showEmptyState() {
                if (emptyState != null)
                        emptyState.setVisibility(View.VISIBLE);
                if (recyclerResults != null)
                        recyclerResults.setVisibility(View.GONE);
        }

        private void showResults() {
                if (emptyState != null)
                        emptyState.setVisibility(View.GONE);
                if (recyclerResults != null)
                        recyclerResults.setVisibility(View.VISIBLE);
                if (adapter != null)
                        adapter.notifyDataSetChanged();
        }

        private void openDetailWisata(Wisata wisata) {
                Intent intent = new Intent(this, DetailWisataActivity.class);
                intent.putExtra("id", wisata.getId());
                intent.putExtra("nama", wisata.getNama());
                intent.putExtra("kategori", wisata.getKategori());
                intent.putExtra("deskripsi", wisata.getDeskripsi());
                intent.putExtra("harga", wisata.getHarga());
                intent.putExtra("lokasi", wisata.getLokasi());
                intent.putExtra("waktu", wisata.getWaktu());
                intent.putExtra("imageUrl", wisata.getImageUrl());
                intent.putExtra("transportasi", wisata.getTransportasi());
                intent.putExtra("rating", wisata.getRating());
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }

        @Override
        public void onBackPressed() {
                super.onBackPressed();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
}
