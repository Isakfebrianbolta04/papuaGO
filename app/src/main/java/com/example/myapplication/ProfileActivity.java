package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.bumptech.glide.Glide;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ProfileActivity extends AppCompatActivity {

    private LinearLayout navHome, navExplore, navOrder, navProfile;
    private DatabaseReference userRef;
    private ActivityResultLauncher<String> getContent;
    private ImageView imgProfile, imgAccountSmall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load Theme Preference before setContentView
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("DarkMode", false);
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_profile);

        initImagePicker();
        initViews();
        setupNavigation();

        // Set Profile active
        selectNavItem(navProfile);
    }

    private void initImagePicker() {
        getContent = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                saveProfileImage(uri);
            }
        });
    }

    private void saveProfileImage(Uri uri) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
            return;

        Toast.makeText(this, "Menyimpan foto...", Toast.LENGTH_SHORT).show();

        try {
            // 1. Convert Uri to Bitmap
            Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

            // 2. Resize Bitmap (Max 500px) to save space
            Bitmap resizedBitmap = resizeBitmap(bitmap, 500);

            // 3. Convert to Base64 String
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            String base64Image = Base64.encodeToString(byteArray, Base64.DEFAULT);

            // 4. Save to Realtime Database
            FirebaseDatabase.getInstance().getReference("users")
                    .child(user.getUid())
                    .child("profileImage")
                    .setValue(base64Image)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Foto profil berhasil disimpan!", Toast.LENGTH_SHORT).show();
                        loadProfileImage(); // Refresh UI immediate
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Gagal menyimpan: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap resizeBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private void updateUserProfile(Uri photoUri) {
        // Not used in Base64 method
    }

    private void loadProfileImage() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
            return;

        // Priority 1: Check for Gmail profile photo from Firebase Auth
        Uri photoUrl = user.getPhotoUrl();
        if (photoUrl != null) {
            // Load Gmail profile photo using Glide
            if (imgProfile != null) {
                Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.profile_placeholder)
                        .error(R.drawable.profile_placeholder)
                        .circleCrop()
                        .into(imgProfile);
            }
            if (imgAccountSmall != null) {
                Glide.with(this)
                        .load(photoUrl)
                        .placeholder(R.drawable.profile_placeholder)
                        .error(R.drawable.profile_placeholder)
                        .circleCrop()
                        .into(imgAccountSmall);
            }
            return; // Gmail photo loaded, no need to check database
        }

        // Priority 2: Check for custom uploaded Base64 image from database
        DatabaseReference profileRef = FirebaseDatabase.getInstance().getReference("users")
                .child(user.getUid())
                .child("profileImage");

        profileRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String base64Image = snapshot.getValue(String.class);
                    try {
                        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                        if (imgProfile != null)
                            imgProfile.setImageBitmap(decodedByte);
                        if (imgAccountSmall != null)
                            imgAccountSmall.setImageBitmap(decodedByte);

                    } catch (Exception e) {
                        e.printStackTrace();
                        loadPlaceholder();
                    }
                } else {
                    // Priority 3: Load default placeholder if no image exists
                    loadPlaceholder();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadPlaceholder();
            }
        });
    }

    private void loadPlaceholder() {
        if (imgProfile != null)
            imgProfile.setImageResource(R.drawable.profile_placeholder);
        if (imgAccountSmall != null)
            imgAccountSmall.setImageResource(R.drawable.profile_placeholder);
    }

    private void initViews() {
        navHome = findViewById(R.id.navHome);
        navExplore = findViewById(R.id.navExplore);
        navOrder = findViewById(R.id.navOrder);
        navProfile = findViewById(R.id.navProfile);
        imgProfile = findViewById(R.id.imgProfile);
        imgAccountSmall = findViewById(R.id.imgAccountSmall);

        loadProfileImage();
    }

    private void setupNavigation() {
        TextView txtName = findViewById(R.id.txtName);
        TextView txtEmail = findViewById(R.id.txtEmail);

        // Account List items
        TextView txtNameSmall = findViewById(R.id.txtNameSmall);
        TextView txtEmailSmall = findViewById(R.id.txtEmailSmall);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Load custom name from Firebase (same logic as BerandaActivity)
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .child("name");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String displayName = null;

                    // Priority 1: Custom name from Firebase
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
                                // Capitalize first letter, lowercase rest
                                displayName = emailName.substring(0, 1).toUpperCase()
                                        + emailName.substring(1).toLowerCase();
                            }
                        }
                    }

                    // Final fallback
                    if (displayName == null || displayName.isEmpty()) {
                        displayName = "Pengguna";
                    }

                    // Update UI
                    if (txtName != null)
                        txtName.setText(displayName);
                    if (txtNameSmall != null)
                        txtNameSmall.setText(displayName);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Fallback
                    String name = user.getEmail();
                    if (name != null && name.contains("@")) {
                        name = name.split("@")[0];
                    }
                    if (txtName != null)
                        txtName.setText(name);
                    if (txtNameSmall != null)
                        txtNameSmall.setText(name);
                }
            });

            // Set email
            if (txtEmail != null)
                txtEmail.setText(user.getEmail());
            if (txtEmailSmall != null)
                txtEmailSmall.setText(user.getEmail());
        }

        // Edit Profile
        View btnEditProfile = findViewById(R.id.btnEditProfile);
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> showEditOptions());
        }

        // Favorites
        LinearLayout menuFavorites = findViewById(R.id.menuFavorites);
        if (menuFavorites != null) {
            menuFavorites.setOnClickListener(v -> {
                Intent intent = new Intent(this, FavoriteActivity.class);
                startActivity(intent);
            });
        }

        // Theme Settings with SharedPreferences
        LinearLayout menuTheme = findViewById(R.id.menuTheme);
        android.widget.Switch switchTheme = findViewById(R.id.switchTheme);
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("DarkMode", false);

        if (switchTheme != null) {
            switchTheme.setChecked(isDarkMode);
            // Disable switch click to let the row handle it, or handle both
            switchTheme.setClickable(false);
        }

        if (menuTheme != null) {
            menuTheme.setOnClickListener(v -> {
                boolean newState = !prefs.getBoolean("DarkMode", false);
                if (switchTheme != null) {
                    switchTheme.setChecked(newState);
                }

                // Save Preference
                prefs.edit().putBoolean("DarkMode", newState).apply();

                // Apply Effect
                if (newState) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    Toast.makeText(this, R.string.theme_dark_active, Toast.LENGTH_SHORT).show();
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    Toast.makeText(this, R.string.theme_light_active, Toast.LENGTH_SHORT).show();
                }
            });
        }

        // General Settings Dialog
        LinearLayout menuSettings = findViewById(R.id.menuSettings);
        if (menuSettings != null) {
            menuSettings.setOnClickListener(v -> {
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
            });
        }

        // Language Settings Dialog
        LinearLayout menuLanguage = findViewById(R.id.menuLanguage);
        if (menuLanguage != null) {
            menuLanguage.setOnClickListener(v -> {
                String[] languages = { "Bahasa Indonesia", "English" };
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle(R.string.choose_language)
                        .setItems(languages, (dialog, which) -> {
                            if (which == 0) {
                                // Indonesia
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("id"));
                            } else if (which == 1) {
                                // English
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"));
                            }

                            // Restart app to ensure all activities (especially singleTop) pick up the
                            // change
                            Intent intent = new Intent(this, BerandaActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .show();
            });
        }

        // Help & Support
        LinearLayout menuHelp = findViewById(R.id.menuHelp);
        if (menuHelp != null) {
            menuHelp.setOnClickListener(v -> {
                Intent intent = new Intent(this, HelpActivity.class);
                startActivity(intent);
            });
        }

        // Account Management
        LinearLayout menuAddAccount = findViewById(R.id.btnAddAccount);
        if (menuAddAccount != null) {
            menuAddAccount.setOnClickListener(v -> {
                // Navigate to Register (Add Account)
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
            });
        }

        LinearLayout layoutCurrentAccount = findViewById(R.id.layoutCurrentAccount);
        if (layoutCurrentAccount != null)
            layoutCurrentAccount
                    .setOnClickListener(v -> Toast.makeText(this, R.string.account_active, Toast.LENGTH_SHORT).show());

        if (navHome != null) {
            navHome.setOnClickListener(v -> {
                Intent intent = new Intent(this, BerandaActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        if (navExplore != null) {
            navExplore.setOnClickListener(v -> {
                Intent intent = new Intent(this, SearchActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        if (navOrder != null) {
            navOrder.setOnClickListener(v -> {
                Intent intent = new Intent(this, OrderActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                overridePendingTransition(0, 0);
            });
        }

        if (navProfile != null) {
            navProfile.setOnClickListener(v -> {
                selectNavItem(navProfile);
            });
        }

        // Logout
        LinearLayout menuLogout = findViewById(R.id.menuLogout);
        if (menuLogout != null) {
            menuLogout.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, R.string.logout_success, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        // Back Button
        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void showEditOptions() {
        String[] options = { "Ubah Nama", "Ganti Foto Profil" };
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Edit Profil")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        showEditProfileDialog();
                    } else {
                        getContent.launch("image/*");
                    }
                })
                .show();
    }

    private void showEditProfileDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint(R.string.edit_profile_hint);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Load current name from Firebase
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .child("name");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        input.setText(snapshot.getValue(String.class));
                    } else if (user.getDisplayName() != null) {
                        input.setText(user.getDisplayName());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.edit_profile_title)
                .setView(input)
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty() && user != null) {
                        // Save to Firebase Realtime Database
                        FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(user.getUid())
                                .child("name")
                                .setValue(newName)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, R.string.profile_name_updated, Toast.LENGTH_SHORT).show();

                                    // Update UI immediately
                                    TextView txtName = findViewById(R.id.txtName);
                                    TextView txtNameSmall = findViewById(R.id.txtNameSmall);
                                    if (txtName != null)
                                        txtName.setText(newName);
                                    if (txtNameSmall != null)
                                        txtNameSmall.setText(newName);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Gagal menyimpan: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });

                        // Also update FirebaseAuth profile for compatibility
                        com.google.firebase.auth.UserProfileChangeRequest profileUpdates = new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                .setDisplayName(newName)
                                .build();
                        user.updateProfile(profileUpdates);
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void selectNavItem(LinearLayout nav) {
        if (nav == null)
            return;
        try {
            android.widget.FrameLayout iconFrame = (android.widget.FrameLayout) nav.getChildAt(0);
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
            e.printStackTrace();
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
            android.widget.FrameLayout iconFrame = (android.widget.FrameLayout) nav.getChildAt(0);
            ImageView icon = (ImageView) iconFrame.getChildAt(0);
            TextView text = (TextView) nav.getChildAt(1);

            // Inactive state: Brand Brown
            int inactiveColor = getResources().getColor(R.color.brand_brown, getTheme());

            icon.animate().scaleX(1f).scaleY(1f).alpha(0.8f).setDuration(200).start();
            icon.setColorFilter(inactiveColor);

            text.animate().alpha(0.8f).scaleX(1f).scaleY(1f).setDuration(200).start();
            text.setTextColor(inactiveColor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        selectNavItem(navProfile);
        loadProfileImage();
    }
}