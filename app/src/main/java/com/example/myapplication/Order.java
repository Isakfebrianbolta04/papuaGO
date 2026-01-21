package com.example.myapplication;

public class Order {
    private String orderId;
    private String userId;
    private String wisataId;
    private String wisataNama;
    private String wisataHarga;
    private String wisataImageUrl;
    private String wisataKategori;
    private long tanggal;
    private String status;
    private String paymentMethod;
    private String transactionId; // Link to Transaction

    // Empty constructor for Firebase
    public Order() {
    }

    public Order(String orderId, String userId, String wisataId, String wisataNama,
            String wisataHarga, String wisataImageUrl, String wisataKategori, long tanggal, String status,
            String paymentMethod, String transactionId) {
        this.orderId = orderId;
        this.userId = userId;
        this.wisataId = wisataId;
        this.wisataNama = wisataNama;
        this.wisataHarga = wisataHarga;
        this.wisataImageUrl = wisataImageUrl;
        this.wisataKategori = wisataKategori;
        this.tanggal = tanggal;
        this.status = status;
        this.paymentMethod = paymentMethod;
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

    public String getWisataId() {
        return wisataId;
    }

    public void setWisataId(String wisataId) {
        this.wisataId = wisataId;
    }

    public String getWisataNama() {
        return wisataNama;
    }

    public void setWisataNama(String wisataNama) {
        this.wisataNama = wisataNama;
    }

    public String getWisataHarga() {
        return wisataHarga;
    }

    public void setWisataHarga(String wisataHarga) {
        this.wisataHarga = wisataHarga;
    }

    public String getWisataImageUrl() {
        return wisataImageUrl;
    }

    public void setWisataImageUrl(String wisataImageUrl) {
        this.wisataImageUrl = wisataImageUrl;
    }

    public long getTanggal() {
        return tanggal;
    }

    public void setTanggal(long tanggal) {
        this.tanggal = tanggal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWisataKategori() {
        return wisataKategori;
    }

    public void setWisataKategori(String wisataKategori) {
        this.wisataKategori = wisataKategori;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
