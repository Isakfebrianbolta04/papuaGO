package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.graphics.Color;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.widget.RadioGroup;
import java.util.Collections;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

        private LinearLayout navHome, navExplore, navOrder, navProfile;

        // Search components
        private EditText searchInput;
        private ImageView iconSearch;
        private CardView searchWrapper;

        // Category buttons
        private LinearLayout btnTempatWisata, btnPenginapan, btnAksesoris, btnMakanan;
        private ImageView filterButton;
        private ScrollView mainScroll;

        // Data lists
        private List<Wisata> wisataList = new ArrayList<>();
        private List<Wisata> hotelList = new ArrayList<>();
        private List<Wisata> aksesorisList = new ArrayList<>();
        private List<Wisata> makananList = new ArrayList<>();

        // UI Cards
        private FrameLayout cardMaybratTour, cardSorongTour, cardSorselTour, cardRajaAmpatTour, cardTambrauwTour;
        private FrameLayout cardAksesoris1, cardAksesoris2, cardAksesoris3, cardAksesoris4, cardAksesoris5,
                        cardAksesoris6,
                        cardAksesoris7, cardAksesoris8, cardAksesoris9, cardAksesoris10;
        private FrameLayout cardHotel1, cardHotel2, cardHotel3, cardHotel4, cardHotel5;
        private FrameLayout cardMakanan1, cardMakanan2, cardMakanan3, cardMakanan4, cardMakanan5, cardMakanan6,
                        cardMakanan7, cardMakanan8;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);

                // Load Theme Preference logic
                android.content.SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                boolean isDarkMode = prefs.getBoolean("DarkMode", false);
                if (isDarkMode) {
                        androidx.appcompat.app.AppCompatDelegate
                                        .setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                        androidx.appcompat.app.AppCompatDelegate
                                        .setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
                }

                setContentView(R.layout.activity_search);

                initViews();
                setupNavigation();
                setupSearchFunctionality();
                setupCategoryButtons();

                // 1. Load basic local data immediately (Primary Source)
                populateLocalData();

                // 2. Load from Firebase IF needed (Commented out to ensure absolute consistency
                // with local assets)
                // loadAllCategoryData();

                selectNavItem(navExplore);
        }

        private void initViews() {
                // Bottom Navigation
                navHome = findViewById(R.id.navHome);
                navExplore = findViewById(R.id.navExplore);
                navOrder = findViewById(R.id.navOrder);
                navProfile = findViewById(R.id.navProfile);

                // Search components
                searchInput = findViewById(R.id.searchInput);
                iconSearch = findViewById(R.id.iconSearch);
                searchWrapper = findViewById(R.id.searchWrapper);

                // Category buttons
                btnTempatWisata = findViewById(R.id.btnTempatWisata);
                btnPenginapan = findViewById(R.id.btnPenginapan);
                btnAksesoris = findViewById(R.id.btnAksesoris);
                btnMakanan = findViewById(R.id.btnMakanan);
                filterButton = findViewById(R.id.filterButton);
                mainScroll = findViewById(R.id.mainScroll);

                // Dynamic cards from layout
                cardRajaAmpatTour = findViewById(R.id.cardRajaAmpatTour);
                cardMaybratTour = findViewById(R.id.cardMaybratTour);
                cardSorselTour = findViewById(R.id.cardSorselTour);
                cardSorongTour = findViewById(R.id.cardSorongTour);
                cardTambrauwTour = findViewById(R.id.cardTambrauwTour);

                cardAksesoris1 = findViewById(R.id.cardAksesoris1);
                cardAksesoris2 = findViewById(R.id.cardAksesoris2);
                cardAksesoris3 = findViewById(R.id.cardAksesoris3);
                cardAksesoris4 = findViewById(R.id.cardAksesoris4);
                cardAksesoris5 = findViewById(R.id.cardAksesoris5);
                cardAksesoris6 = findViewById(R.id.cardAksesoris6);
                cardAksesoris7 = findViewById(R.id.cardAksesoris7);
                cardAksesoris8 = findViewById(R.id.cardAksesoris8);
                cardAksesoris9 = findViewById(R.id.cardAksesoris9);
                cardAksesoris10 = findViewById(R.id.cardAksesoris10);

                cardHotel1 = findViewById(R.id.cardHotel1);
                cardHotel2 = findViewById(R.id.cardHotel2);
                cardHotel3 = findViewById(R.id.cardHotel3);
                cardHotel4 = findViewById(R.id.cardHotel4);
                cardHotel5 = findViewById(R.id.cardHotel5);

                cardMakanan1 = findViewById(R.id.cardMakanan1);
                cardMakanan2 = findViewById(R.id.cardMakanan2);
                cardMakanan3 = findViewById(R.id.cardMakanan3);
                cardMakanan4 = findViewById(R.id.cardMakanan4);
                cardMakanan5 = findViewById(R.id.cardMakanan5);
                cardMakanan6 = findViewById(R.id.cardMakanan6);
                cardMakanan7 = findViewById(R.id.cardMakanan7);
                cardMakanan8 = findViewById(R.id.cardMakanan8);
        }

        private void populateLocalData() {
                wisataList.clear();
                hotelList.clear();
                aksesorisList.clear();
                makananList.clear();

                // --- TEMPAT WISATA (RESTORED) ---
                wisataList.add(new Wisata("wisata_raja4", getString(R.string.title_raja_ampat),
                                getString(R.string.cat_wisata),
                                getString(R.string.desc_raja_ampat), "5000000", getString(R.string.waisai),
                                getString(R.string.dur_4d3n), "raja4", getString(R.string.trans_plane_boat), "4.9"));
                wisataList.add(new Wisata("wisata_maybrat", getString(R.string.title_ayamaru),
                                getString(R.string.cat_wisata),
                                getString(R.string.desc_ayamaru), "500000", getString(R.string.maybrat),
                                getString(R.string.dur_1day),
                                "maybrat", getString(R.string.trans_car_motor), "4.6"));
                wisataList.add(new Wisata("wisata_sorsel", getString(R.string.title_sorsel),
                                getString(R.string.cat_wisata),
                                getString(R.string.desc_sorsel), "1500000", getString(R.string.sorong_selatan),
                                getString(R.string.dur_2d1n), "sorsel", getString(R.string.trans_travel_car), "4.7"));
                wisataList.add(new Wisata("wisata_sorong", getString(R.string.title_sorong),
                                getString(R.string.cat_wisata),
                                getString(R.string.desc_sorong), "750000", getString(R.string.kota_sorong),
                                getString(R.string.dur_1day), "sorong", getString(R.string.trans_public), "4.6"));
                wisataList.add(new Wisata("wisata_um", getString(R.string.title_um), getString(R.string.cat_wisata),
                                getString(R.string.desc_um), "100000", getString(R.string.kota_sorong),
                                getString(R.string.dur_1day),
                                "um", getString(R.string.trans_boat), "4.8"));
                wisataList.add(new Wisata("wisata_uter", getString(R.string.title_uter), getString(R.string.cat_wisata),
                                getString(R.string.desc_uter), "250000", getString(R.string.maybrat),
                                getString(R.string.dur_1day), "uter", getString(R.string.trans_car_motor), "4.9"));
                wisataList.add(new Wisata("wisata_yerusel", getString(R.string.title_yerusel),
                                getString(R.string.cat_wisata),
                                getString(R.string.desc_yerusel), "150000", getString(R.string.kota_sorong),
                                getString(R.string.dur_1day), "yerusel", getString(R.string.trans_boat), "4.7"));
                wisataList.add(new Wisata("wisata_sembra", getString(R.string.title_sembra),
                                getString(R.string.cat_wisata),
                                getString(R.string.desc_sembra), "75000", getString(R.string.maybrat),
                                getString(R.string.dur_1day), "sembra", getString(R.string.trans_car_motor), "4.5"));
                wisataList.add(new Wisata("wisata_tambrauw", getString(R.string.title_tambrauw),
                                getString(R.string.cat_wisata),
                                getString(R.string.desc_tambrauw), "3500000", getString(R.string.tambrauw),
                                getString(R.string.dur_3d2n), "tambrauw", getString(R.string.trans_4x4), "4.7"));

                // --- PENGINAPAN (RESTORED ALFIRA & AIMAS) ---
                hotelList.add(new Wisata("wisata_nusa", getString(R.string.title_nusa), "Penginapan",
                                getString(R.string.desc_nusa), "450000", getString(R.string.sorong_selatan), "Malam",
                                "nusa", "Travel", "4.5"));
                hotelList.add(new Wisata("wisata_maratuwa", getString(R.string.title_maratuwa), "Penginapan",
                                "Akomodasi tenang di Sorsel.", "500000", getString(R.string.sorong_selatan), "Malam",
                                "maratuwa", "Travel", "4.6"));
                hotelList.add(new Wisata("wisata_piaynemo", "Piaynemo Homestay", "Penginapan",
                                "View karst yang ikonik.", "1500000", "Raja Ampat", "Malam", "piaynemo_homestay",
                                "Speedboat", "4.8"));
                hotelList.add(new Wisata("wisata_alfira", "Hotel Alfira", "Penginapan", "Hotel nyaman di Sorong.",
                                "400000", "Sorong", "Malam", "alfira", "Taksi", "4.4"));
                hotelList.add(new Wisata("wisata_aimas", "Hotel Aimas", "Penginapan", "Fasilitas lengkap di Aimas.",
                                "550000", "Aimas", "Malam", "aimas", "Taksi", "4.5"));

                // --- AKSESORIS (REFINED) ---
                aksesorisList.add(new Wisata("wisata_noken", getString(R.string.title_noken), "Aksesoris",
                                getString(R.string.desc_noken), "250000", "Pusat Seni", "Buah", "noken", "-", "4.8"));
                aksesorisList.add(new Wisata("wisata_noken_kayu", getString(R.string.title_noken_kayu), "Aksesoris",
                                getString(R.string.desc_noken_kayu), "450000", "Pasar Budaya", "Buah", "tas_kulit_kayu",
                                "-", "4.9"));
                aksesorisList.add(new Wisata("wisata_noken_ra", "Noken Raja Ampat", "Aksesoris",
                                "Tas rajut autentik RA.", "150000", "Raja Ampat", "Buah", "noken_ra", "-", "4.8"));
                aksesorisList.add(new Wisata("wisata_akar_bahar", "Gelang Akar Bahar", "Aksesoris",
                                "Gelang laut mistis.", "350000", "Pusat Kerajinan", "Buah", "akar_bahar", "-", "4.9"));
                aksesorisList.add(new Wisata("wisata_kalung_kerang", "Kalung Kerang", "Aksesoris",
                                "Hiasan leher kerang asli.", "75000", "Pantai", "Buah", "kalung_kerang", "-", "4.7"));
                aksesorisList.add(new Wisata("wisata_pakaian", "Pakaian Adat", "Aksesoris", "Busana tradisional Papua.",
                                "1200000", "Sorong", "Set", "pakian_adat", "-", "4.9"));
                aksesorisList.add(new Wisata("wisata_mahkota", "Mahkota Cendrawasih", "Aksesoris",
                                "Hiasan kepala mewah.", "750000", "Sorong", "Buah", "mahkota_baru", "-", "5.0"));
                aksesorisList.add(new Wisata("wisata_tifa", "Tifa", "Aksesoris", "Alat musik tradisional.", "500000",
                                "Sentra Seni", "Buah", "tifa", "-", "4.8"));
                aksesorisList.add(new Wisata("wisata_batik", getString(R.string.title_batik), "Aksesoris",
                                getString(R.string.desc_batik), "350000", "Sorong", "Meter", "batik_sorong", "-",
                                "4.7"));
                aksesorisList.add(new Wisata("wisata_anyaman", getString(R.string.title_anyaman), "Aksesoris",
                                getString(R.string.desc_anyaman), "250000", "Sorong Selatan", "Buah", "anyaman_pbd",
                                "-", "4.6"));

                // --- MAKANAN (RESTORED & FIXED IMAGES) ---
                makananList.add(new Wisata("wisata_papeda", "Papeda", "Makanan", getString(R.string.desc_papeda),
                                "50000", "Rumah Makan", "Porsi", "papeda_baru", "-", "4.9"));
                makananList.add(new Wisata("wisata_keladi", "Keladi Bakar", "Makanan", "Keladi bakar gurih.", "25000",
                                "Pasar", "Porsi", "keladi_asli", "-", "4.8"));
                makananList.add(new Wisata("wisata_nasgor", "Nasi Goreng Papua", "Makanan", "Nasgor rempah lokal.",
                                "30000", "Warung", "Porsi", "nasgor_baru", "-", "4.5"));
                makananList.add(new Wisata("wisata_chips", "Keripik Keladi", "Makanan", "Snack renyah khas Sorong.",
                                "35000", "Toko Oleh-oleh", "Bungkus", "keripik_keladi", "-", "4.7"));
                makananList.add(new Wisata("wisata_sagu", "Sagu Lempeng", "Makanan", "Kue sagu tradisional.", "15000",
                                "Pasar", "Buah", "sagu_lempeng", "-", "4.6"));
                makananList.add(new Wisata("wisata_ikan", "Ikan Bakar Papua", "Makanan", "Ikan segar bumbu kuning.",
                                "85000", "Pantai", "Porsi", "ikan_bakar", "-", "4.7"));
                makananList.add(new Wisata("wisata_udang", "Udang Selingkuh", "Makanan", "Udang khas Wamena/Papua.",
                                "120000", "Resto", "Porsi", "udang_selingkuh", "-", "4.8"));
                makananList.add(new Wisata("wisata_ikan_bungkus", getString(R.string.title_ikan_bungkus), "Makanan",
                                getString(R.string.desc_ikan_bungkus), "45000", "Pasar", "Bungkus", "ikan_bungkus", "-",
                                "4.7"));

                updateWisataUI();
                updateHotelUI();
                updateAksesorisUI();
                updateMakananUI();
        }

        private void updateWisataUI() {
                updateStaticCard(cardRajaAmpatTour, wisataList.size() > 0 ? wisataList.get(0) : null);
                updateStaticCard(cardMaybratTour, wisataList.size() > 1 ? wisataList.get(1) : null);
                updateStaticCard(cardSorselTour, wisataList.size() > 2 ? wisataList.get(2) : null);
                updateStaticCard(cardSorongTour, wisataList.size() > 3 ? wisataList.get(3) : null);
                updateStaticCard(cardTambrauwTour, wisataList.size() > 5 ? wisataList.get(5) : null); // Skip 4 if it's
                                                                                                      // redundant Pulau
                                                                                                      // Um
        }

        private void updateHotelUI() {
                updateStaticCard(cardHotel1, hotelList.size() > 0 ? hotelList.get(0) : null);
                updateStaticCard(cardHotel2, hotelList.size() > 1 ? hotelList.get(1) : null);
                updateStaticCard(cardHotel3, hotelList.size() > 2 ? hotelList.get(2) : null);
                updateStaticCard(cardHotel4, hotelList.size() > 3 ? hotelList.get(3) : null);
                updateStaticCard(cardHotel5, hotelList.size() > 4 ? hotelList.get(4) : null);
        }

        private void updateAksesorisUI() {
                updateStaticCard(cardAksesoris1, aksesorisList.size() > 0 ? aksesorisList.get(0) : null);
                updateStaticCard(cardAksesoris2, aksesorisList.size() > 1 ? aksesorisList.get(1) : null);
                updateStaticCard(cardAksesoris3, aksesorisList.size() > 2 ? aksesorisList.get(2) : null);
                updateStaticCard(cardAksesoris4, aksesorisList.size() > 3 ? aksesorisList.get(3) : null);
                updateStaticCard(cardAksesoris5, aksesorisList.size() > 4 ? aksesorisList.get(4) : null);
                updateStaticCard(cardAksesoris6, aksesorisList.size() > 5 ? aksesorisList.get(5) : null);
                updateStaticCard(cardAksesoris7, aksesorisList.size() > 6 ? aksesorisList.get(6) : null);
                updateStaticCard(cardAksesoris8, aksesorisList.size() > 7 ? aksesorisList.get(7) : null);
                updateStaticCard(cardAksesoris9, aksesorisList.size() > 8 ? aksesorisList.get(8) : null);
                updateStaticCard(cardAksesoris10, aksesorisList.size() > 9 ? aksesorisList.get(9) : null);
        }

        private void updateMakananUI() {
                updateStaticCard(cardMakanan1, makananList.size() > 0 ? makananList.get(0) : null);
                updateStaticCard(cardMakanan2, makananList.size() > 1 ? makananList.get(1) : null);
                updateStaticCard(cardMakanan3, makananList.size() > 2 ? makananList.get(2) : null);
                updateStaticCard(cardMakanan4, makananList.size() > 3 ? makananList.get(3) : null);
                updateStaticCard(cardMakanan5, makananList.size() > 4 ? makananList.get(4) : null);
                updateStaticCard(cardMakanan6, makananList.size() > 5 ? makananList.get(5) : null);
                updateStaticCard(cardMakanan7, makananList.size() > 6 ? makananList.get(6) : null);
                updateStaticCard(cardMakanan8, makananList.size() > 7 ? makananList.get(7) : null);
        }

        private void updateStaticCard(FrameLayout card, Wisata wisata) {
                if (card == null)
                        return;

                if (wisata == null) {
                        card.setVisibility(View.GONE);
                        return;
                }

                card.setVisibility(View.VISIBLE);
                ImageView img = card.findViewById(R.id.menuImage);
                TextView title = card.findViewById(R.id.menuTitle);
                TextView subtitle = card.findViewById(R.id.menuSubtitle);
                TextView priceView = card.findViewById(R.id.menuPrice);

                if (title != null)
                        title.setText(wisata.getNama());
                if (subtitle != null)
                        subtitle.setText(wisata.getLokasi());
                if (priceView != null) {
                        // Format Harga dengan NumberFormat Indonesia
                        try {
                                String cleanHarga = wisata.getHarga().replaceAll("[^\\d]", "");
                                double hargaValue = Double.parseDouble(cleanHarga);
                                java.util.Locale localeID = new java.util.Locale.Builder().setLanguage("id")
                                                .setRegion("ID").build();
                                java.text.NumberFormat formatRupiah = java.text.NumberFormat
                                                .getCurrencyInstance(localeID);

                                String kategori = wisata.getKategori();
                                String labelSuffix = " / orang";
                                if (kategori != null) {
                                        if (kategori.toLowerCase().contains("penginapan"))
                                                labelSuffix = " / malam";
                                        else if (kategori.toLowerCase().contains("makanan"))
                                                labelSuffix = " / porsi";
                                        else if (kategori.toLowerCase().contains("aksesoris"))
                                                labelSuffix = "";
                                }

                                priceView.setText(formatRupiah.format(hargaValue) + labelSuffix);
                        } catch (Exception e) {
                                priceView.setText("Rp " + wisata.getHarga());
                        }
                }

                if (img != null) {
                        int resId = getResources().getIdentifier(wisata.getImageUrl(), "drawable", getPackageName());
                        if (resId != 0) {
                                Glide.with(this).load(resId).centerCrop().into(img);
                        } else {
                                img.setImageResource(R.drawable.raja4);
                        }
                }
                card.setOnClickListener(v -> openDetailWisata(wisata));
        }

        private void openDetailWisata(Wisata wisata) {
                Intent intent = new Intent(this, DetailWisataActivity.class);
                intent.putExtra("id", wisata.getId());
                intent.putExtra("nama", wisata.getNama());
                intent.putExtra("imageUrl", wisata.getImageUrl());
                intent.putExtra("harga", wisata.getHarga());
                intent.putExtra("lokasi", wisata.getLokasi());
                intent.putExtra("deskripsi", wisata.getDeskripsi());
                intent.putExtra("transportasi", wisata.getTransportasi());
                intent.putExtra("kategori", wisata.getKategori());
                intent.putExtra("waktu", wisata.getWaktu());
                intent.putExtra("rating", wisata.getRating());
                startActivity(intent);
        }

        private void setupNavigation() {
                navHome.setOnClickListener(v -> {
                        startActivity(new Intent(this, BerandaActivity.class));
                        finish();
                });
                navOrder.setOnClickListener(v -> startActivity(new Intent(this, OrderActivity.class)));
                navProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        }

        private void setupSearchFunctionality() {
                if (searchInput != null) {
                        searchInput.setOnEditorActionListener((v, actionId, event) -> {
                                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                                        performSearch();
                                        return true;
                                }
                                return false;
                        });
                }
        }

        private void performSearch() {
                String query = searchInput.getText().toString();
                Intent intent = new Intent(this, ResultSearchActivity.class);
                intent.putExtra("category", query);
                startActivity(intent);
        }

        private void setupCategoryButtons() {
                if (btnTempatWisata != null)
                        btnTempatWisata.setOnClickListener(v -> {
                                Intent intent = new Intent(this, ResultSearchActivity.class);
                                intent.putExtra("category", "Tempat Wisata");
                                startActivity(intent);
                        });

                if (btnPenginapan != null)
                        btnPenginapan.setOnClickListener(v -> {
                                Intent intent = new Intent(this, ResultSearchActivity.class);
                                intent.putExtra("category", "Penginapan");
                                startActivity(intent);
                        });

                if (btnAksesoris != null)
                        btnAksesoris.setOnClickListener(v -> {
                                Intent intent = new Intent(this, ResultSearchActivity.class);
                                intent.putExtra("category", "Aksesoris");
                                startActivity(intent);
                        });

                if (btnMakanan != null)
                        btnMakanan.setOnClickListener(v -> {
                                Intent intent = new Intent(this, ResultSearchActivity.class);
                                intent.putExtra("category", "Makanan");
                                startActivity(intent);
                        });
        }

        private void selectNavItem(LinearLayout nav) {
                // Reset colors logic if needed
        }
}