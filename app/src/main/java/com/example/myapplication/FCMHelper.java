package com.example.myapplication;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class FCMHelper {

    private static final DatabaseReference notificationsRef = FirebaseDatabase.getInstance()
            .getReference("admin_notifications");

    /**
     * Send payment success notification to admin
     * In production, this should trigger a Cloud Function to send actual FCM
     * For now, we save to Firebase and admin app will listen to this node
     */
    public static void sendPaymentSuccessNotification(String category, String orderId,
            String transactionId, long amount) {
        DatabaseReference notifRef = notificationsRef.push();

        Map<String, Object> notifData = new HashMap<>();
        notifData.put("title", "Pembayaran Baru! 💰");
        notifData.put("body", "Pesanan " + category + " #" + orderId + " telah dibayar (Rp " + amount + ")");
        notifData.put("category", category);
        notifData.put("orderId", orderId);
        notifData.put("transactionId", transactionId);
        notifData.put("timestamp", System.currentTimeMillis());
        notifData.put("read", false);

        notifRef.setValue(notifData);
    }

    /**
     * Send order status update notification
     */
    public static void sendOrderStatusNotification(String userId, String orderId,
            String status, String message) {
        DatabaseReference userNotifRef = FirebaseDatabase.getInstance()
                .getReference("user_notifications")
                .child(userId)
                .push();

        Map<String, Object> notifData = new HashMap<>();
        notifData.put("title", "Update Pesanan");
        notifData.put("body", message);
        notifData.put("orderId", orderId);
        notifData.put("status", status);
        notifData.put("timestamp", System.currentTimeMillis());
        notifData.put("read", false);

        userNotifRef.setValue(notifData);
    }
}
