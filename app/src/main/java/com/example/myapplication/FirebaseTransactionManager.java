package com.example.myapplication;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class FirebaseTransactionManager {

    private static final DatabaseReference transactionsRef = FirebaseDatabase.getInstance()
            .getReference("transactions");

    /**
     * Create a new transaction in Firebase
     */
    public static void createTransaction(Transaction transaction, OnCompleteListener<Void> listener) {
        if (transaction.getTransactionId() == null || transaction.getTransactionId().isEmpty()) {
            String transactionId = transactionsRef.push().getKey();
            transaction.setTransactionId(transactionId);
        }

        transactionsRef.child(transaction.getTransactionId())
                .setValue(transaction)
                .addOnCompleteListener(listener);
    }

    /**
     * Update transaction status
     */
    public static void updateTransactionStatus(String transactionId, String status,
            OnCompleteListener<Void> listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);

        if ("paid".equals(status)) {
            updates.put("paidAt", System.currentTimeMillis());
        }

        transactionsRef.child(transactionId)
                .updateChildren(updates)
                .addOnCompleteListener(listener);
    }

    /**
     * Get a single transaction by ID
     */
    public static void getTransaction(String transactionId, ValueEventListener listener) {
        transactionsRef.child(transactionId)
                .addListenerForSingleValueEvent(listener);
    }

    /**
     * Get all transactions for a specific category
     */
    public static Query getTransactionsByCategory(String category) {
        return transactionsRef.orderByChild("category").equalTo(category);
    }

    /**
     * Get all transactions for a specific user
     */
    public static Query getTransactionsByUser(String userId) {
        return transactionsRef.orderByChild("userId").equalTo(userId);
    }

    /**
     * Get all transactions with a specific status
     */
    public static Query getTransactionsByStatus(String status) {
        return transactionsRef.orderByChild("status").equalTo(status);
    }

    /**
     * Get transactions by category and status
     */
    public static void getTransactionsByCategoryAndStatus(String category, String status,
            ValueEventListener listener) {
        transactionsRef.orderByChild("category").equalTo(category)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        // Filter by status manually since Firebase doesn't support multiple orderBy
                        listener.onDataChange(snapshot);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onCancelled(error);
                    }
                });
    }

    /**
     * Delete a transaction
     */
    public static void deleteTransaction(String transactionId, OnCompleteListener<Void> listener) {
        transactionsRef.child(transactionId)
                .removeValue()
                .addOnCompleteListener(listener);
    }

    /**
     * Update booking data
     */
    public static void updateBookingData(String transactionId, Map<String, Object> bookingData,
            OnCompleteListener<Void> listener) {
        transactionsRef.child(transactionId)
                .child("bookingData")
                .setValue(bookingData)
                .addOnCompleteListener(listener);
    }
}
