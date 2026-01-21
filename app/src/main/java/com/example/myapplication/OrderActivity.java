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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OrderActivity extends AppCompatActivity {

    private ImageView btnBack;
    private RecyclerView recyclerOrders;
    private LinearLayout emptyState;
    private ProgressBar progressBar;
    private LinearLayout navHome, navExplore, navOrder, navProfile;
    private LinearLayout currentSelectedNav;

    private OrderAdapter adapter;
    private List<Order> orderList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Checkout Fields
    private androidx.cardview.widget.CardView layoutCheckout;
    private TextView txtTotalBayar;
    private android.widget.Button btnCheckoutAll;
    private long currentTotal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load Theme Preference before setContentView
        android.content.SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("DarkMode", false);
        if (isDarkMode) {
            androidx.appcompat.app.AppCompatDelegate
                    .setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate
                    .setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_order);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("orders");

        initViews();
        setupBottomNav();

        // Set Order active
        selectNavItem(navOrder);

        loadOrders();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        recyclerOrders = findViewById(R.id.recyclerOrders);
        emptyState = findViewById(R.id.emptyState);
        progressBar = findViewById(R.id.progressBar);

        navHome = findViewById(R.id.navHome);
        navExplore = findViewById(R.id.navExplore);
        navOrder = findViewById(R.id.navOrder);
        navProfile = findViewById(R.id.navProfile);

        // Checkout UI
        layoutCheckout = findViewById(R.id.layout_checkout);
        txtTotalBayar = findViewById(R.id.txtTotalBayar);
        btnCheckoutAll = findViewById(R.id.btnCheckoutAll);

        orderList = new ArrayList<>();
        recyclerOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(orderList);
        recyclerOrders.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());

        btnCheckoutAll.setOnClickListener(v -> {
            if (currentTotal > 0) {
                showBulkPaymentDialog();
            } else {
                Toast.makeText(this, "Tidak ada tagihan yang harus dibayar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTotalUI(long total, int count) {
        currentTotal = total;
        java.text.NumberFormat formatter = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale("id", "ID"));
        txtTotalBayar.setText(formatter.format(total));
        btnCheckoutAll.setText(getString(R.string.action_pay_count, count));
    }

    private void showBulkPaymentDialog() {
        String[] methods = { "Transfer Bank (BCA, Mandiri, BRI)", "E-Wallet (OVO, GoPay, Dana)", "Kartu Kredit / Debit",
                "Bayar di Tempat (COD)" };
        new android.app.AlertDialog.Builder(this)
                .setTitle("Pilih Metode Pembayaran")
                .setItems(methods, (dialog, which) -> {
                    String selectedMethod = methods[which];

                    // Get first pending order for basic info
                    String orderId = "BULK";
                    String category = "Multiple";
                    String wisataNama = "Pembayaran Bulk";

                    for (Order order : orderList) {
                        if ("Menunggu Pembayaran".equalsIgnoreCase(order.getStatus())) {
                            orderId = order.getOrderId();
                            category = order.getWisataKategori();
                            wisataNama = "Pembayaran " + orderList.size() + " pesanan";
                            break;
                        }
                    }

                    Intent intent = new Intent(OrderActivity.this, PaymentActivity.class);
                    intent.putExtra("isBulk", true);
                    intent.putExtra("orderId", orderId);
                    intent.putExtra("amount", String.valueOf(currentTotal));
                    intent.putExtra("method", selectedMethod);
                    intent.putExtra("category", category);
                    intent.putExtra("wisataNama", wisataNama);
                    startActivity(intent);
                })
                .show();
    }

    private void loadOrders() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            emptyState.setVisibility(View.VISIBLE);
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mDatabase.orderByChild("userId").equalTo(user.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        orderList.clear();
                        long totalPrice = 0;
                        int pendingCount = 0;

                        for (DataSnapshot ds : snapshot.getChildren()) {
                            Order order = ds.getValue(Order.class);
                            if (order != null) {
                                orderList.add(0, order); // Newest first

                                // Calculate total for pending orders
                                if ("Menunggu Pembayaran".equalsIgnoreCase(order.getStatus())) {
                                    try {
                                        totalPrice += Long.parseLong(order.getWisataHarga());
                                        pendingCount++;
                                    } catch (Exception e) {
                                        // Ignore parse error
                                    }
                                }
                            }
                        }

                        // Update Total UI
                        updateTotalUI(totalPrice, pendingCount);

                        progressBar.setVisibility(View.GONE);
                        if (orderList.isEmpty()) {
                            emptyState.setVisibility(View.VISIBLE);
                            recyclerOrders.setVisibility(View.GONE);
                            layoutCheckout.setVisibility(View.GONE);
                        } else {
                            emptyState.setVisibility(View.GONE);
                            recyclerOrders.setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();

                            // Only show checkout bar if there are pending items
                            if (pendingCount > 0) {
                                layoutCheckout.setVisibility(View.VISIBLE);
                            } else {
                                layoutCheckout.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(OrderActivity.this, "Gagal memuat: " + error.getMessage(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void setupBottomNav() {
        navHome.setOnClickListener(v -> navigateTo(BerandaActivity.class));
        navExplore.setOnClickListener(v -> navigateTo(SearchActivity.class));
        navProfile.setOnClickListener(v -> navigateTo(ProfileActivity.class));
        // navOrder is current
    }

    private void navigateTo(Class<?> target) {
        Intent intent = new Intent(this, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void selectNavItem(LinearLayout nav) {
        if (nav == null)
            return;
        currentSelectedNav = nav;
        try {
            android.widget.FrameLayout iconFrame = (android.widget.FrameLayout) nav.getChildAt(0);
            ImageView icon = (ImageView) iconFrame.getChildAt(0);
            android.widget.TextView text = (android.widget.TextView) nav.getChildAt(1);

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
            android.widget.TextView text = (android.widget.TextView) nav.getChildAt(1);

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
        selectNavItem(navOrder);
    }
}
