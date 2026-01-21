package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.util.ArrayList;
import java.util.List;

public class BerandaActivity extends AppCompatActivity {

    // CARD
    private FrameLayout cardTopUm, cardTopYerusel, cardTopUter, cardTopAyamaru, cardTopSembra;
    private FrameLayout cardMidSorsel, cardMidRaja41, cardMidSorong;
    private FrameLayout cardMidMaybrat, cardMidTambrauw;
    private LinearLayout btnCircle1, btnCircle2, btnCircle3, btnCircle4, btnCircle5;

    // HEADER
    private TextView txtWelcome, txtUserName, txtAjak;
    private CardView cardProfileImage, btnSettings;

    // BOTTOM NAV
    private LinearLayout navHome, navExplore, navOrder, navProfile;
    private LinearLayout currentSelectedNav;
    private ImageView imgProfile;

    // Firebase
    private DatabaseReference databaseReference;
    private List<Wisata> wisataListData; // Destinasi
    private List<Wisata> hotelListData; // Penginapan
    private List<Wisata> otherListData; // Kuliner/Aksesoris

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load Theme Preference before setContentView
        android.content.SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("DarkMode", false);
        int targetMode = isDarkMode ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
        if (androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode() != targetMode) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(targetMode);
        }

        setContentView(R.layout.activity_beranda);

        initViews();
        setHeaderText();
        setupNavigation();

        // Initialize Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("wisata");
        wisataListData = new ArrayList<>();
        hotelListData = new ArrayList<>();
        otherListData = new ArrayList<>();

        // Setup default click listeners and static data immediately
        setupDefaultClicks();

        // Load data multi-kategori (listeners will only update if methods are
        // implemented)
        loadMultiCategoryData();

        // Set Home active
        selectNavItem(navHome);
    }

    private void loadMultiCategoryData() {
        // 1. Load Destinasi (Wisata) untuk Horizontal Scroll 1
        databaseReference.orderByChild("kategori").equalTo("Tempat Wisata").limitToFirst(3)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (isDestroyed() || isFinishing())
                            return;
                        wisataListData.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Wisata w = ds.getValue(Wisata.class);
                            if (w != null)
                                wisataListData.add(w);
                        }
                        updateTopCards();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        // 2. Load Penginapan untuk Horizontal Scroll 2
        databaseReference.orderByChild("kategori").equalTo("Penginapan").limitToFirst(3)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (isDestroyed() || isFinishing())
                            return;
                        hotelListData.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Wisata w = ds.getValue(Wisata.class);
                            if (w != null)
                                hotelListData.add(w);
                        }
                        updateMiddleCards();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        // 3. Load Makanan/Aksesoris untuk Circular List
        databaseReference.orderByChild("kategori").startAt("Aksesoris").limitToFirst(5)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (isDestroyed() || isFinishing())
                            return;
                        otherListData.clear();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Wisata w = ds.getValue(Wisata.class);
                            if (w != null && !w.getKategori().equals("Tempat Wisata")
                                    && !w.getKategori().equals("Penginapan")) {
                                otherListData.add(w);
                            }
                        }
                        // We don't overwrite the first 3 anymore as they are fixed destinations
                        // updateCircleItems();

                        // If you want to load more items beyond the first 3, we would need more IDs
                        // For now we follow user's fixed destination request for the circles.
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    private void updateTopCards() {
        // This method is now handled by static card population in setupDefaultClicks
        // Or can be updated to handle dynamic cards (cardTopUm, etc.) if needed.
    }

    private void updateMiddleCards() {
        // This method is now handled by static card population in setupDefaultClicks
        // Or can be updated to handle dynamic cards if needed.
    }

    private void openDetailWisata(Wisata w) {
        Intent intent = new Intent(this, DetailWisataActivity.class);
        intent.putExtra("id", w.getId());
        intent.putExtra("nama", w.getNama());
        intent.putExtra("kategori", w.getKategori());
        intent.putExtra("harga", w.getHarga());
        intent.putExtra("lokasi", w.getLokasi());
        intent.putExtra("waktu", w.getWaktu());
        intent.putExtra("deskripsi", w.getDeskripsi());
        intent.putExtra("imageUrl", w.getImageUrl());
        intent.putExtra("transportasi", w.getTransportasi());
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reset navigation to home when returning to this activity
        selectNavItem(navHome);
        loadProfileImage();
    }

    private void loadProfileImage() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            DatabaseReference profileRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(user.getUid())
                    .child("profileImage");

            profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (isDestroyed() || isFinishing())
                        return;

                    if (snapshot.exists()) {
                        String base64Image = snapshot.getValue(String.class);
                        try {
                            byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            if (imgProfile != null) {
                                imgProfile.setImageBitmap(decodedByte);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        if (imgProfile != null)
                            imgProfile.setImageResource(R.drawable.profile_placeholder);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    private void initViews() {
        // Header
        txtWelcome = findViewById(R.id.txtWelcome);
        txtUserName = findViewById(R.id.txtUserName);
        txtAjak = findViewById(R.id.txtAjak);
        cardProfileImage = findViewById(R.id.cardProfileImage);
        btnSettings = findViewById(R.id.btnSettings);

        // Cards Top (Natural Destinations)
        cardTopUm = findViewById(R.id.cardTopUm);
        cardTopYerusel = findViewById(R.id.cardTopYerusel);
        cardTopUter = findViewById(R.id.cardTopUter);
        cardTopAyamaru = findViewById(R.id.cardTopAyamaru);
        cardTopSembra = findViewById(R.id.cardTopSembra);

        // Cards Mid (Popular Destinations)
        cardMidSorsel = findViewById(R.id.cardMidSorsel);
        cardMidRaja41 = findViewById(R.id.cardMidRaja41);
        cardMidSorong = findViewById(R.id.cardMidSorong);
        cardMidMaybrat = findViewById(R.id.cardMidMaybrat);
        cardMidTambrauw = findViewById(R.id.cardMidTambrauw);

        // Circular items
        btnCircle1 = findViewById(R.id.btnCircle1);
        btnCircle2 = findViewById(R.id.btnCircle2);
        btnCircle3 = findViewById(R.id.btnCircle3);
        btnCircle4 = findViewById(R.id.btnCircle4);
        btnCircle5 = findViewById(R.id.btnCircle5);

        // Bottom Nav
        navHome = findViewById(R.id.navHome);
        navExplore = findViewById(R.id.navExplore);
        navOrder = findViewById(R.id.navOrder);
        navProfile = findViewById(R.id.navProfile);
        imgProfile = findViewById(R.id.imgProfile);
    }

    private void setHeaderText() {
        txtWelcome.setText(R.string.welcome_back);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Load custom name from Firebase profile
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .child("name");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (isDestroyed() || isFinishing())
                        return;

                    String displayName = null;

                    // Priority 1: Custom name from profile
                    if (snapshot.exists() && snapshot.getValue() != null) {
                        String customName = snapshot.getValue(String.class);
                        if (customName != null && !customName.trim().isEmpty()) {
                            displayName = customName.trim();
                        }
                    }

                    // Priority 2: Firebase display name
                    if (displayName == null || displayName.isEmpty()) {
                        String authName = user.getDisplayName();
                        if (authName != null && !authName.trim().isEmpty()) {
                            displayName = authName.trim();
                        }
                    }

                    // Priority 3: Extract from email
                    if (displayName == null || displayName.isEmpty()) {
                        String email = user.getEmail();
                        if (email != null && email.contains("@")) {
                            String emailName = email.split("@")[0];
                            if (emailName != null && !emailName.isEmpty()) {
                                // Capitalize first letter
                                displayName = emailName.substring(0, 1).toUpperCase()
                                        + emailName.substring(1).toLowerCase();
                            }
                        }
                    }

                    // Final fallback
                    if (displayName == null || displayName.isEmpty()) {
                        displayName = getString(R.string.default_user);
                    }

                    txtUserName.setText(displayName);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Fallback to email if Firebase fails
                    String name = user.getEmail();
                    if (name != null && name.contains("@")) {
                        name = name.split("@")[0];
                    }
                    txtUserName.setText(name != null ? name : getString(R.string.default_user));
                }
            });

            // Load Profile Image
            loadProfileImage();

        } else {
            String userName = getIntent().getStringExtra("username");
            if (userName == null || userName.isEmpty()) {
                userName = getString(R.string.default_user);
            }
            txtUserName.setText(userName);
        }
        txtAjak.setText(R.string.where_to_go);
    }

    private void setupDefaultClicks() {
        // 1. TOP CARDS (Standardized IDs & Scenic Images)
        Wisata umData = new Wisata("wisata_um", getString(R.string.title_um), getString(R.string.cat_wisata),
                getString(R.string.desc_um), "100000", getString(R.string.kota_sorong), getString(R.string.dur_1day),
                "um",
                getString(R.string.trans_boat), "4.8");

        Wisata yeruselData = new Wisata("wisata_yerusel", getString(R.string.title_yerusel),
                getString(R.string.cat_wisata),
                getString(R.string.desc_yerusel), "150000", getString(R.string.kota_sorong),
                getString(R.string.dur_1day), "yerusel",
                getString(R.string.trans_boat), "4.7");

        Wisata uterData = new Wisata("wisata_uter", getString(R.string.title_uter), getString(R.string.cat_wisata),
                getString(R.string.desc_uter), "250000", getString(R.string.maybrat), getString(R.string.dur_1day),
                "uter",
                getString(R.string.trans_car_motor), "4.9");

        Wisata ayamaruData = new Wisata("wisata_maybrat", getString(R.string.title_ayamaru),
                getString(R.string.cat_wisata),
                getString(R.string.desc_ayamaru), "500000", getString(R.string.maybrat), getString(R.string.dur_1day),
                "maybrat",
                getString(R.string.trans_car_motor), "4.6");

        Wisata sembraData = new Wisata("wisata_sembra", getString(R.string.title_sembra),
                getString(R.string.cat_wisata),
                getString(R.string.desc_sembra), "75000", getString(R.string.maybrat), getString(R.string.dur_1day),
                "sembra",
                getString(R.string.trans_car_motor), "4.5");

        // Apply Top Cards
        if (cardTopUm != null)
            updateCard(cardTopUm, umData);
        if (cardTopYerusel != null)
            updateCard(cardTopYerusel, yeruselData);
        if (cardTopUter != null)
            updateCard(cardTopUter, uterData);
        if (cardTopAyamaru != null)
            updateCard(cardTopAyamaru, ayamaruData);
        if (cardTopSembra != null)
            updateCard(cardTopSembra, sembraData);

        // 2. MIDDLE CARDS
        Wisata sorselData = new Wisata("wisata_sorsel", getString(R.string.title_sorsel),
                getString(R.string.cat_wisata),
                getString(R.string.desc_sorsel), "1500000", getString(R.string.sorong_selatan),
                getString(R.string.dur_2d1n), "sorsel",
                getString(R.string.trans_travel_car), "4.7");

        Wisata rajaAmpatData = new Wisata("wisata_raja4", getString(R.string.raja_ampat),
                getString(R.string.cat_wisata),
                getString(R.string.desc_raja_ampat), "4500000", getString(R.string.waisai),
                getString(R.string.dur_3d2n), "raja4",
                getString(R.string.trans_ferry_speedboat), "4.9");

        Wisata sorongData = new Wisata("wisata_sorong", getString(R.string.kota_sorong), getString(R.string.cat_wisata),
                getString(R.string.desc_sorong), "750000", getString(R.string.kota_sorong),
                getString(R.string.dur_1day), "sorong",
                getString(R.string.trans_public), "4.6");

        Wisata tambrauwData = new Wisata("wisata_tambrauw", getString(R.string.title_tambrauw),
                getString(R.string.cat_wisata),
                getString(R.string.desc_tambrauw), "3500000", getString(R.string.tambrauw),
                getString(R.string.dur_3d2n), "tambrauw",
                getString(R.string.trans_4x4), "4.7");

        // Apply Middle Cards
        if (cardMidSorsel != null)
            updateCard(cardMidSorsel, sorselData);
        if (cardMidRaja41 != null)
            updateCard(cardMidRaja41, rajaAmpatData);
        if (cardMidSorong != null)
            updateCard(cardMidSorong, sorongData);
        if (cardMidMaybrat != null)
            updateCard(cardMidMaybrat, ayamaruData);
        if (cardMidTambrauw != null)
            updateCard(cardMidTambrauw, tambrauwData);

        // 3. POPULAR DESTINATION CIRCLES (Unique Scenery for each city)
        updateCircleItemBound(R.id.imgCircle1, R.id.txtCircle1, sorselData);
        updateCircleItemBound(R.id.imgCircle2, R.id.txtCircle2, sorongData);
        updateCircleItemBound(R.id.imgCircle3, R.id.txtCircle3, rajaAmpatData);
        updateCircleItemBound(R.id.imgCircle4, R.id.txtCircle4, ayamaruData);
        updateCircleItemBound(R.id.imgCircle5, R.id.txtCircle5, tambrauwData);

        // Click listeners for circles
        if (findViewById(R.id.btnCircle1) != null)
            findViewById(R.id.btnCircle1).setOnClickListener(v -> openDetailWisata(sorselData));
        if (findViewById(R.id.btnCircle2) != null)
            findViewById(R.id.btnCircle2).setOnClickListener(v -> openDetailWisata(sorongData));
        if (findViewById(R.id.btnCircle3) != null)
            findViewById(R.id.btnCircle3).setOnClickListener(v -> openDetailWisata(rajaAmpatData));
        if (findViewById(R.id.btnCircle4) != null)
            findViewById(R.id.btnCircle4).setOnClickListener(v -> openDetailWisata(ayamaruData));
        if (findViewById(R.id.btnCircle5) != null)
            findViewById(R.id.btnCircle5).setOnClickListener(v -> openDetailWisata(tambrauwData));
    }

    private void updateCircleItemBound(int imgId, int txtId, Wisata wisata) {
        ImageView img = findViewById(imgId);
        TextView txt = findViewById(txtId);
        if (img != null && txt != null) {
            txt.setText(wisata.getNama());

            String imageUrl = wisata.getImageUrl();

            // DEBUG: Log circle image loading
            android.util.Log.d("BerandaActivity",
                    "Circle - Loading: " + wisata.getNama() + " with imageUrl: " + imageUrl);

            int resId = getResources().getIdentifier(imageUrl, "drawable", getPackageName());
            if (resId != 0) {
                android.util.Log.d("BerandaActivity", "Circle - Found resource ID: " + resId + " for: " + imageUrl);
                Glide.with(this)
                        .load(resId)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                        .centerCrop()
                        .into(img);
            } else {
                android.util.Log.e("BerandaActivity", "Circle - FAILED to find drawable for: " + imageUrl);
            }

            // Bind click to the parent LinearLayout
            // Check if parent is CardView (for consistency with XML structure)
            View parent = (View) img.getParent();
            if (parent instanceof androidx.cardview.widget.CardView) {
                parent = (View) parent.getParent();
            }
            // If parent was not CardView, it's likely the LinearLayout itself (e.g. for
            // Tambrauw)

            parent.setOnClickListener(v -> openDetailWisata(wisata));
        }
    }

    private void openDetailDirectly(String nama, String kat, String harga, String lok, String waktu, String img,
            String desc, String trans) {
        Intent intent = new Intent(this, DetailWisataActivity.class);
        intent.putExtra("id", "static_" + nama.toLowerCase().replace(" ", "_"));
        intent.putExtra("nama", nama);
        intent.putExtra("kategori", kat);
        intent.putExtra("harga", harga);
        intent.putExtra("lokasi", lok);
        intent.putExtra("waktu", waktu);
        intent.putExtra("imageUrl", img); // Ini akan memicu placeholder karena bukan URL, tapi cukup untuk demo
        intent.putExtra("deskripsi", desc);
        intent.putExtra("transportasi", trans);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void setupNavigation() {
        if (navHome != null) {
            navHome.setOnClickListener(v -> selectNavItem(navHome));
        }

        if (navExplore != null) {
            navExplore.setOnClickListener(v -> {
                selectNavItem(navExplore);
                Intent intent = new Intent(this, SearchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        if (navOrder != null) {
            navOrder.setOnClickListener(v -> {
                selectNavItem(navOrder);
                Intent intent = new Intent(this, OrderActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                selectNavItem(navProfile);
                Intent intent = new Intent(this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        if (cardProfileImage != null) {
            cardProfileImage.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }

        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            });
        }
    }

    private void selectNavItem(LinearLayout nav) {
        if (nav == null)
            return;
        currentSelectedNav = nav;
        try {
            FrameLayout iconFrame = (FrameLayout) nav.getChildAt(0);
            ImageView icon = (ImageView) iconFrame.getChildAt(0);
            TextView text = (TextView) nav.getChildAt(1);

            // Active state: Bright color
            int activeColor = getResources().getColor(R.color.nav_active_bright, getTheme());

            icon.animate().scaleX(1.15f).scaleY(1.15f).alpha(1f).setDuration(300).start();
            icon.setColorFilter(activeColor);

            text.animate().alpha(1f).scaleX(1.05f).scaleY(1.05f).setDuration(200).start();
            text.setTextColor(activeColor);

            deselectOtherNavItems(nav);
        } catch (Exception e) {
        }
    }

    private void deselectOtherNavItems(LinearLayout selectedNav) {
        LinearLayout[] allNavs = { navHome, navExplore, navOrder, navProfile };
        for (LinearLayout nav : allNavs) {
            if (nav != null && nav != selectedNav) {
                deselectNavItem(nav);
            }
        }
    }

    private void deselectNavItem(LinearLayout nav) {
        try {
            FrameLayout iconFrame = (FrameLayout) nav.getChildAt(0);
            ImageView icon = (ImageView) iconFrame.getChildAt(0);
            TextView text = (TextView) nav.getChildAt(1);

            int inactiveColor = getResources().getColor(R.color.brand_brown, getTheme());

            icon.animate().scaleX(1f).scaleY(1f).alpha(0.8f).setDuration(200).start();
            icon.setColorFilter(inactiveColor);

            text.animate().alpha(0.8f).scaleX(1f).scaleY(1f).setDuration(200).start();
            text.setTextColor(inactiveColor);
        } catch (Exception e) {
        }
    }

    private void updateCircleItems() {
        if (otherListData.size() > 0) {
            updateCircleItem(R.id.imgCircle1, R.id.txtCircle1, otherListData.get(0));
        }
        if (otherListData.size() > 1) {
            updateCircleItem(R.id.imgCircle2, R.id.txtCircle2, otherListData.get(1));
        }
        if (otherListData.size() > 2) {
            updateCircleItem(R.id.imgCircle3, R.id.txtCircle3, otherListData.get(2));
        }
    }

    private void updateCircleItem(int imgId, int txtId, Wisata wisata) {
        ImageView img = findViewById(imgId);
        TextView txt = findViewById(txtId);
        if (img != null && txt != null) {
            String imageUrl = wisata.getImageUrl();
            Object imageSource = imageUrl;

            if (imageUrl != null && !imageUrl.startsWith("http")) {
                int resId = getResources().getIdentifier(imageUrl, "drawable", getPackageName());
                if (resId != 0) {
                    imageSource = resId;
                }
            }

            txt.setText(wisata.getNama());
            Glide.with(this).load(imageSource).placeholder(R.drawable.raja4).centerCrop().into(img);

            // Re-bind parent (LinearLayout) click to detail
            View parent = (View) img.getParent().getParent();
            parent.setOnClickListener(v -> openDetailWisata(wisata));
        }
    }

    private String formatRupiah(String price) {
        if (price == null || price.isEmpty())
            return "Rp. 0";
        try {
            // Remove any non-numeric characters first if they exist
            String cleanPrice = price.replaceAll("[^0-9]", "");
            if (cleanPrice.isEmpty())
                return "Rp. " + price;

            long value = Long.parseLong(cleanPrice);
            java.text.DecimalFormat formatter = (java.text.DecimalFormat) java.text.NumberFormat
                    .getInstance(new java.util.Locale("id", "ID"));
            return "Rp. " + formatter.format(value);
        } catch (Exception e) {
            return "Rp. " + price;
        }
    }

    private void updateCard(FrameLayout card, Wisata wisata) {
        // Find views in card using IDs from item_static_card.xml
        ImageView imageView = card.findViewById(R.id.menuImage);
        TextView titleView = card.findViewById(R.id.menuTitle);
        TextView subtitleView = card.findViewById(R.id.menuSubtitle);
        TextView durationView = card.findViewById(R.id.menuDuration);
        TextView transportView = card.findViewById(R.id.menuTransport);
        TextView priceView = card.findViewById(R.id.menuPrice);
        TextView ratingView = card.findViewById(R.id.menuRating);
        ImageView btnLike = card.findViewById(R.id.btnLike);

        // Set data
        if (titleView != null) {
            titleView.setText(wisata.getNama());
        }

        if (ratingView != null) {
            ratingView.setText(wisata.getRating());
        }

        if (subtitleView != null) {
            subtitleView.setText(wisata.getLokasi()); // Di search model, subtitle adalah lokasi
        }

        if (durationView != null) {
            durationView.setText(wisata.getWaktu());
        }

        if (transportView != null) {
            transportView.setText(wisata.getTransportasi());
        }

        if (priceView != null) {
            // Format Harga dengan NumberFormat Indonesia
            try {
                String cleanHarga = wisata.getHarga().replaceAll("[^\\d]", "");
                double hargaValue = Double.parseDouble(cleanHarga);
                java.util.Locale localeID = new java.util.Locale.Builder().setLanguage("id").setRegion("ID").build();
                java.text.NumberFormat formatRupiah = java.text.NumberFormat.getCurrencyInstance(localeID);

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

        // Handle URL vs Resource Name
        String imageUrl = wisata.getImageUrl();
        Object imageSource = imageUrl;

        // DEBUG: Log the image URL being loaded
        android.util.Log.d("BerandaActivity", "Loading image for: " + wisata.getNama() + " with imageUrl: " + imageUrl);

        if (imageUrl != null && !imageUrl.startsWith("http")) {
            int resId = card.getContext().getResources().getIdentifier(imageUrl, "drawable",
                    card.getContext().getPackageName());
            if (resId != 0) {
                imageSource = resId;
                android.util.Log.d("BerandaActivity", "Found drawable resource ID: " + resId + " for: " + imageUrl);
            } else {
                android.util.Log.e("BerandaActivity", "FAILED to find drawable for: " + imageUrl);
            }
        }

        // Load image dengan Glide - SKIP CACHE untuk debugging
        if (imageView != null && imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(card.getContext())
                    .load(imageSource)
                    .skipMemoryCache(true) // Force refresh
                    .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE) // Skip disk cache
                    .placeholder(R.drawable.raja4)
                    .error(R.drawable.raja4)
                    .centerCrop()
                    .into(imageView);
        }

        // Handle Favorite Button
        // Handle Favorite Button (Firebase)
        if (btnLike != null) {
            com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance()
                    .getCurrentUser();
            if (user != null) {
                com.google.firebase.database.DatabaseReference favRef = com.google.firebase.database.FirebaseDatabase
                        .getInstance()
                        .getReference("users").child(user.getUid()).child("favorites").child(wisata.getId());

                // Check initial state
                favRef.addValueEventListener(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                        try {
                            // Needed check if activity is destroyed to avoid crash?
                            // Using card context is safe enough for basic impl
                            boolean isFav = snapshot.exists();
                            if (isFav) {
                                btnLike.setColorFilter(android.graphics.Color.parseColor("#E91E63")); // Red
                            } else {
                                btnLike.setColorFilter(android.graphics.Color.parseColor("#D9D9D9")); // Grey
                            }
                        } catch (Exception e) {
                        }
                    }

                    @Override
                    public void onCancelled(com.google.firebase.database.DatabaseError error) {
                    }
                });

                // Handle Click
                btnLike.setOnClickListener(v -> {
                    favRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                        @Override
                        public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                favRef.removeValue()
                                        .addOnSuccessListener(aVoid -> Toast
                                                .makeText(card.getContext(), "Dihapus dari Favorit", Toast.LENGTH_SHORT)
                                                .show());
                            } else {
                                favRef.setValue(true)
                                        .addOnSuccessListener(aVoid -> Toast.makeText(card.getContext(),
                                                "Ditambahkan ke Favorit", Toast.LENGTH_SHORT).show());
                            }
                        }

                        @Override
                        public void onCancelled(com.google.firebase.database.DatabaseError error) {
                        }
                    });
                });
            } else {
                btnLike.setOnClickListener(v -> Toast
                        .makeText(card.getContext(), "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show());
            }
        }

        // Set click listener untuk navigate ke detail
        card.setOnClickListener(v ->

        openDetailWisata(wisata));
    }

}