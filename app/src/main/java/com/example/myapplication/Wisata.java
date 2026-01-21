package com.example.myapplication;

public class Wisata {
    private String id;
    private String nama;
    private String kategori;
    private String deskripsi;
    private String harga;
    private String lokasi;
    private String waktu;
    private String imageUrl;
    private String transportasi;
    private String rating;

    // Constructor kosong diperlukan untuk Firebase
    public Wisata() {
    }

    // Constructor dengan parameter lengkap
    public Wisata(String id, String nama, String kategori, String deskripsi,
            String harga, String lokasi, String waktu, String imageUrl, String transportasi, String rating) {
        this.id = id;
        this.nama = nama;
        this.kategori = kategori;
        this.deskripsi = deskripsi;
        this.harga = harga;
        this.lokasi = lokasi;
        this.waktu = waktu;
        this.imageUrl = imageUrl;
        this.transportasi = transportasi;
        this.rating = rating;
    }

    // Constructor lama tanpa rating (untuk kompatibilitas)
    public Wisata(String id, String nama, String kategori, String deskripsi,
            String harga, String lokasi, String waktu, String imageUrl, String transportasi) {
        this(id, nama, kategori, deskripsi, harga, lokasi, waktu, imageUrl, transportasi, "4.5");
    }

    // Getters dan Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getKategori() {
        return kategori;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getHarga() {
        return harga;
    }

    public void setHarga(String harga) {
        this.harga = harga;
    }

    public String getLokasi() {
        return lokasi;
    }

    public void setLokasi(String lokasi) {
        this.lokasi = lokasi;
    }

    public String getWaktu() {
        return waktu;
    }

    public void setWaktu(String waktu) {
        this.waktu = waktu;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTransportasi() {
        return transportasi;
    }

    public void setTransportasi(String transportasi) {
        this.transportasi = transportasi;
    }

    public String getRating() {
        return rating != null ? rating : "4.5"; // Nilai default
    }

    public void setRating(String rating) {
        this.rating = rating;
    }
}
