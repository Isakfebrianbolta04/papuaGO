package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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

public class FavoriteActivity extends AppCompatActivity {

        private RecyclerView rvFavorites;
        private LinearLayout layoutEmpty;
        private ProgressBar progressBar;
        private ImageView btnBack;

        private WisataSearchAdapter adapter;
        private List<Wisata> favoriteList;
        private DatabaseReference databaseReference;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_favorite);

                // Init Data
                favoriteList = new ArrayList<>();
                databaseReference = FirebaseDatabase.getInstance().getReference("wisata");

                // Init Views
                initViews();
                setupActions();

                // Load Data
                loadFavorites();
        }

        private void initViews() {
                rvFavorites = findViewById(R.id.rvFavorites);
                layoutEmpty = findViewById(R.id.layoutEmpty);
                progressBar = findViewById(R.id.progressBar);
                btnBack = findViewById(R.id.btnBack);

                rvFavorites.setLayoutManager(new LinearLayoutManager(this));
                adapter = new WisataSearchAdapter(favoriteList, this::openDetailWisata);
                rvFavorites.setAdapter(adapter);
        }

        private void setupActions() {
                btnBack.setOnClickListener(v -> finish());
        }

        private void loadFavorites() {
                progressBar.setVisibility(View.VISIBLE);
                layoutEmpty.setVisibility(View.GONE);
                rvFavorites.setVisibility(View.GONE);

                com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance()
                                .getCurrentUser();
                if (user == null) {
                        Toast.makeText(this, "Silakan login untuk melihat favorit", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        return;
                }

                // 1. Get Static Data
                List<Wisata> allData = new ArrayList<>(getLocalWisataList());

                // 2. Get Firebase Wisata Data (Async)
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot ds : snapshot.getChildren()) {
                                        Wisata w = ds.getValue(Wisata.class);
                                        if (w != null) {
                                                allData.add(w);
                                        }
                                }

                                // 3. Fetch User Favorites Keys
                                DatabaseReference userFavRef = FirebaseDatabase.getInstance().getReference("users")
                                                .child(user.getUid()).child("favorites");

                                userFavRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot favSnapshot) {
                                                favoriteList.clear();
                                                for (Wisata w : allData) {
                                                        // Check if this wisata ID exists in user favorites
                                                        // favSnapshot structure: { "id1": true, "id2": true }
                                                        if (favSnapshot.hasChild(w.getId())) {
                                                                favoriteList.add(w);
                                                        }
                                                }
                                                updateUI();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                                Toast.makeText(FavoriteActivity.this, "Gagal memuat favorit",
                                                                Toast.LENGTH_SHORT).show();
                                                updateUI();
                                        }
                                });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(FavoriteActivity.this, "Gagal memuat data wisata", Toast.LENGTH_SHORT)
                                                .show();
                                // Even if global wisata fails, we might still have static ones, so try to load
                                // favorites for them
                                DatabaseReference userFavRef = FirebaseDatabase.getInstance().getReference("users")
                                                .child(user.getUid()).child("favorites");
                                userFavRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot favSnapshot) {
                                                favoriteList.clear();
                                                for (Wisata w : allData) {
                                                        if (favSnapshot.hasChild(w.getId())) {
                                                                favoriteList.add(w);
                                                        }
                                                }
                                                updateUI();
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                                updateUI();
                                        }
                                });
                        }
                });
        }

        private void updateUI() {
                progressBar.setVisibility(View.GONE);
                if (favoriteList.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        rvFavorites.setVisibility(View.GONE);
                } else {
                        layoutEmpty.setVisibility(View.GONE);
                        rvFavorites.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                }
        }

        private void openDetailWisata(Wisata w) {
                Intent intent = new Intent(this, DetailWisataActivity.class);
                intent.putExtra("id", w.getId());
                intent.putExtra("nama", w.getNama());
                intent.putExtra("kategori", w.getKategori());
                intent.putExtra("deskripsi", w.getDeskripsi());
                intent.putExtra("harga", w.getHarga());
                intent.putExtra("lokasi", w.getLokasi());
                intent.putExtra("waktu", w.getWaktu());
                intent.putExtra("imageUrl", w.getImageUrl());
                intent.putExtra("transportasi", w.getTransportasi());
                intent.putExtra("rating", w.getRating());
                startActivity(intent);
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
                list.add(new Wisata("wisata_sorong", getString(R.string.title_sorong), getString(R.string.cat_wisata),
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
}
