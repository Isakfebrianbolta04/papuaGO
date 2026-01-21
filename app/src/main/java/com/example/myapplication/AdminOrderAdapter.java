package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.ViewHolder> {

    private List<Transaction> transactionList;
    private Context context;

    public AdminOrderAdapter(List<Transaction> transactionList, Context context) {
        this.transactionList = transactionList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        holder.bind(transaction, context);
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtOrderId, txtCategory, txtAmount, txtStatus, txtDate;
        CardView cardOrder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtOrderId = itemView.findViewById(R.id.txtOrderId);
            txtCategory = itemView.findViewById(R.id.txtCategory);
            txtAmount = itemView.findViewById(R.id.txtAmount);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtDate = itemView.findViewById(R.id.txtDate);
            cardOrder = itemView.findViewById(R.id.cardOrder);
        }

        public void bind(Transaction transaction, Context context) {
            // Order ID (short format)
            String orderId = transaction.getOrderId();
            if (orderId != null && orderId.length() > 6) {
                txtOrderId.setText("#" + orderId.substring(orderId.length() - 6).toUpperCase());
            } else {
                txtOrderId.setText("#" + (orderId != null ? orderId : "UNKNOWN"));
            }

            // Category
            txtCategory.setText(transaction.getCategory() != null ? transaction.getCategory() : "Unknown");

            // Amount
            try {
                NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                txtAmount.setText(formatter.format(transaction.getAmount()));
            } catch (Exception e) {
                txtAmount.setText("Rp " + transaction.getAmount());
            }

            // Status
            String status = transaction.getStatus();
            txtStatus.setText(status != null ? status : "pending");

            // Status color
            if ("success".equalsIgnoreCase(status) || "paid".equalsIgnoreCase(status)) {
                txtStatus.setBackgroundResource(R.drawable.rounded_white_bg);
                txtStatus.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
            } else if ("pending".equalsIgnoreCase(status)) {
                txtStatus.setBackgroundResource(R.drawable.rounded_gray);
                txtStatus.setTextColor(android.graphics.Color.WHITE);
            } else {
                txtStatus.setBackgroundResource(R.drawable.rounded_gray);
                txtStatus.setTextColor(android.graphics.Color.WHITE);
            }

            // Date
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm", new Locale("id", "ID"));
            txtDate.setText(sdf.format(new Date(transaction.getCreatedAt())));

            // Click listener - open detail based on category
            cardOrder.setOnClickListener(v -> {
                String category = transaction.getCategory();
                Intent intent = null;

                if ("Wisata".equalsIgnoreCase(category) || "Tempat Wisata".equalsIgnoreCase(category)) {
                    intent = new Intent(context, AdminWisataScanActivity.class);
                } else if ("Penginapan".equalsIgnoreCase(category)) {
                    intent = new Intent(context, AdminHotelDetailActivity.class);
                } else if ("Makanan".equalsIgnoreCase(category)) {
                    intent = new Intent(context, AdminKulinerDetailActivity.class);
                } else if ("Aksesoris".equalsIgnoreCase(category)) {
                    intent = new Intent(context, AdminAksesorisDetailActivity.class);
                }

                if (intent != null) {
                    intent.putExtra("transactionId", transaction.getTransactionId());
                    intent.putExtra("orderId", transaction.getOrderId());
                    context.startActivity(intent);
                }
            });
        }
    }
}
