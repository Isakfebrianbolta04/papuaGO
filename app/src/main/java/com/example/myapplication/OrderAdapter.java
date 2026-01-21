package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private List<Order> orderList;

    public OrderAdapter(List<Order> orderList) {
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgWisata, btnDelete; // Added btnDelete
        TextView txtOrderId, txtNamaWisata, txtTanggal, txtStatus, txtHarga;

        // New views
        TextView txtPaymentMethod;
        android.widget.Button btnPay;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgWisata = itemView.findViewById(R.id.imgWisata);
            txtOrderId = itemView.findViewById(R.id.txtOrderId);
            txtNamaWisata = itemView.findViewById(R.id.txtNamaWisata);
            txtTanggal = itemView.findViewById(R.id.txtTanggal);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtHarga = itemView.findViewById(R.id.txtHarga);

            // Init new views
            txtPaymentMethod = itemView.findViewById(R.id.txtPaymentMethod);
            btnPay = itemView.findViewById(R.id.btnPay);
            btnDelete = itemView.findViewById(R.id.btnDelete); // Init btnDelete
        }

        public void bind(Order order) {
            String orderId = order.getOrderId();
            String displayOrderId = (orderId != null && orderId.length() >= 6)
                    ? orderId.substring(orderId.length() - 6).toUpperCase()
                    : "UNKNOW";
            txtOrderId.setText("#ORD-" + displayOrderId);
            String kategoriTag = order.getWisataKategori() != null ? "[" + order.getWisataKategori() + "] " : "";
            txtNamaWisata.setText(kategoriTag + order.getWisataNama());
            txtStatus.setText(order.getStatus());

            // Format Tanggal
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm",
                    new Locale.Builder().setLanguage("id").setRegion("ID").build());
            txtTanggal.setText(sdf.format(new Date(order.getTanggal())));

            // Format Harga
            try {
                long hargaValue = Long.parseLong(order.getWisataHarga());
                NumberFormat formatter = NumberFormat
                        .getCurrencyInstance(new Locale.Builder().setLanguage("id").setRegion("ID").build());
                txtHarga.setText(formatter.format(hargaValue));
            } catch (Exception e) {
                txtHarga.setText("Rp " + order.getWisataHarga());
            }

            // Load Image
            Object imageSource = order.getWisataImageUrl();
            if (order.getWisataImageUrl() != null && !order.getWisataImageUrl().startsWith("http")) {
                int resId = itemView.getResources().getIdentifier(order.getWisataImageUrl(), "drawable",
                        itemView.getContext().getPackageName());
                if (resId != 0) {
                    imageSource = resId;
                }
            }

            Glide.with(itemView.getContext())
                    .load(imageSource)
                    .placeholder(R.drawable.raja4)
                    .centerCrop()
                    .into(imgWisata);

            // Logic Status & Pembayaran
            boolean isPaid = "Selesai".equalsIgnoreCase(order.getStatus())
                    || "Dibayar".equalsIgnoreCase(order.getStatus());

            if (isPaid) {
                btnPay.setVisibility(View.GONE);
                txtPaymentMethod.setVisibility(View.VISIBLE);
                txtPaymentMethod.setText(
                        itemView.getContext().getString(R.string.label_payment_method) + " "
                                + (order.getPaymentMethod() != null ? order.getPaymentMethod() : "-"));
                txtStatus.setBackgroundResource(R.drawable.rounded_white_bg); // Green/Success
                txtStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
            } else {
                // Pending Payment
                btnPay.setVisibility(View.GONE); // Hide individual pay button to enforce Bulk Checkout
                txtPaymentMethod.setVisibility(View.GONE);
                txtStatus.setText(itemView.getResources().getString(R.string.status_pending));
                txtStatus.setBackgroundResource(R.drawable.rounded_gray);
                txtStatus.setTextColor(android.graphics.Color.WHITE);

                // btnPay.setOnClickListener(v -> showPaymentDialog(v.getContext(), order));
            }

            // Delete Action
            btnDelete.setOnClickListener(v -> confirmDelete(v.getContext(), order));

            // View Details / Ticket Action for Paid Orders
            itemView.setOnClickListener(v -> {
                if (isPaid) {
                    openSuccessActivity(v.getContext(), order);
                } else {
                    // Maybe show a message or do nothing for pending orders
                    android.widget.Toast.makeText(v.getContext(),
                            "Pesanan dalam proses / menunggu pembayaran", android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void openSuccessActivity(android.content.Context context, Order order) {
            String category = order.getWisataKategori();
            String transId = order.getTransactionId();

            if (transId == null || transId.isEmpty()) {
                android.widget.Toast
                        .makeText(context, "ID Transaksi tidak ditemukan", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            android.content.Intent intent;
            if ("Hotel".equalsIgnoreCase(category) || "Penginapan".equalsIgnoreCase(category)) {
                intent = new android.content.Intent(context, VoucherActivity.class);
            } else if ("Wisata".equalsIgnoreCase(category) || "Tempat Wisata".equalsIgnoreCase(category)) {
                intent = new android.content.Intent(context, QRTicketActivity.class);
            } else if ("Aksesoris".equalsIgnoreCase(category)) {
                intent = new android.content.Intent(context, TrackingActivity.class);
            } else if ("Kuliner".equalsIgnoreCase(category) || "Makanan".equalsIgnoreCase(category)) {
                intent = new android.content.Intent(context, OrderStatusActivity.class);
            } else {
                return;
            }

            intent.putExtra("transactionId", transId);
            intent.putExtra("orderId", order.getOrderId());
            intent.putExtra("category", category);
            context.startActivity(intent);
        }

        private void showPaymentDialog(android.content.Context context, Order order) {
            String[] methods = { "Transfer Bank (BCA, Mandiri, BRI)", "E-Wallet (OVO, GoPay, Dana)",
                    "Kartu Kredit / Debit", "Bayar di Tempat (COD)" };

            new android.app.AlertDialog.Builder(context)
                    .setTitle("Pilih Metode Pembayaran")
                    .setItems(methods, (dialog, which) -> {
                        String selectedMethod = methods[which];
                        confirmPayment(context, order, selectedMethod);
                    })
                    .show();
        }

        private void confirmPayment(android.content.Context context, Order order, String method) {
            // Launch Payment Activity
            android.content.Intent intent = new android.content.Intent(context, PaymentActivity.class);
            intent.putExtra("orderId", order.getOrderId());
            intent.putExtra("method", method);
            intent.putExtra("amount", order.getWisataHarga());
            intent.putExtra("category", order.getWisataKategori()); // Added
            intent.putExtra("wisataNama", order.getWisataNama()); // Added
            context.startActivity(intent);
        }

        private void confirmDelete(android.content.Context context, Order order) {
            new android.app.AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.dialog_delete_title))
                    .setMessage(context.getString(R.string.dialog_delete_message))
                    .setPositiveButton(context.getString(R.string.action_delete), (dialog, which) -> {
                        deleteOrder(context, order);
                    })
                    .setNegativeButton(context.getString(R.string.action_cancel), null)
                    .show();
        }

        private void deleteOrder(android.content.Context context, Order order) {
            if (order.getOrderId() != null) {
                com.google.firebase.database.DatabaseReference ref = com.google.firebase.database.FirebaseDatabase
                        .getInstance().getReference("orders").child(order.getOrderId());

                ref.removeValue().addOnSuccessListener(aVoid -> {
                    android.widget.Toast
                            .makeText(context, context.getString(R.string.toast_order_deleted),
                                    android.widget.Toast.LENGTH_SHORT)
                            .show();
                }).addOnFailureListener(e -> {
                    android.widget.Toast
                            .makeText(context, context.getString(R.string.error_delete_failed, e.getMessage()),
                                    android.widget.Toast.LENGTH_SHORT)
                            .show();
                });
            }
        }
    }
}
