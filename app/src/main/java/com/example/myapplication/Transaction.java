package com.example.myapplication;

import java.util.Map;

public class Transaction {
    private String transactionId;
    private String orderId;
    private String userId;
    private String category; // "Wisata", "Hotel", "Kuliner", "Aksesoris"
    private String paymentMethod; // "VA", "QRIS"
    private String vaNumber; // Jika VA
    private String qrisUrl; // Jika QRIS
    private long amount;
    private String status; // "pending", "paid", "expired", "used"
    private String name; // Name of the product/item
    private Map<String, Object> bookingData; // Flexible data per category
    private long createdAt;
    private long expiredAt; // createdAt + 15 minutes
    private long paidAt;

    // Empty constructor for Firebase
    public Transaction() {
    }

    public Transaction(String transactionId, String orderId, String userId, String category,
            String paymentMethod, String vaNumber, String qrisUrl, long amount,
            String status, Map<String, Object> bookingData, long createdAt,
            long expiredAt, long paidAt) {
        this.transactionId = transactionId;
        this.orderId = orderId;
        this.userId = userId;
        this.category = category;
        this.paymentMethod = paymentMethod;
        this.vaNumber = vaNumber;
        this.qrisUrl = qrisUrl;
        this.amount = amount;
        this.status = status;
        this.bookingData = bookingData;
        this.createdAt = createdAt;
        this.expiredAt = expiredAt;
        this.paidAt = paidAt;
    }

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getVaNumber() {
        return vaNumber;
    }

    public void setVaNumber(String vaNumber) {
        this.vaNumber = vaNumber;
    }

    public String getQrisUrl() {
        return qrisUrl;
    }

    public void setQrisUrl(String qrisUrl) {
        this.qrisUrl = qrisUrl;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Object> getBookingData() {
        return bookingData;
    }

    public void setBookingData(Map<String, Object> bookingData) {
        this.bookingData = bookingData;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(long expiredAt) {
        this.expiredAt = expiredAt;
    }

    public long getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(long paidAt) {
        this.paidAt = paidAt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
