package com.example.myapplication;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DataSeeder {

        public static void seedAll() {
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("wisata");

                // --- TEMPAT WISATA ---
                save(ref, new Wisata("wisata_raja4", "Raja Ampat", "Tempat Wisata",
                                "Kepulauan dengan pemandangan bawah laut yang menakjubkan.", "5000000", "Raja Ampat",
                                "4 Hari 3 Malam", "raja4", "Pesawat & Kapal Cepat", "4.9"));

                save(ref, new Wisata("wisata_maybrat", "Danau Ayamaru", "Tempat Wisata",
                                "Danau tenang dengan air jernih di Kabupaten Maybrat.", "500000", "Maybrat", "1 Hari",
                                "maybrat", "Mobil / Motor", "4.6"));

                save(ref, new Wisata("wisata_yerusel", "Pulau Yerusel", "Tempat Wisata",
                                "Pulau eksotis dengan pasir putih dan mangrove.", "300000", "Sorong Selatan", "1 Hari",
                                "yerusel", "Perahu", "4.7"));

                save(ref, new Wisata("wisata_um", "Pulau Um", "Tempat Wisata",
                                "Pulau habitat kelelawar dan burung camar.", "100000", "Makbon", "1 Hari", "um",
                                "Perahu", "4.8"));

                save(ref, new Wisata("wisata_uter", "Danau Uter", "Tempat Wisata",
                                "Danau bening bak kristal di tengah hutan Maybrat.", "250000", "Maybrat", "1 Hari",
                                "uter", "Mobil", "4.9"));

                save(ref, new Wisata("wisata_yerusel", "Pulau Yerusel", "Tempat Wisata",
                                "Pulau eksotis dengan pasir putih dan mangrove.", "300000", "Sorong Selatan", "1 Hari",
                                "yerusel", "Perahu", "4.7"));

                save(ref, new Wisata("wisata_sembra", "Kali Sembra", "Tempat Wisata",
                                "Sungai biru toska yang tenang.", "75000", "Maybrat", "1 Hari", "sembra",
                                "Mobil", "4.5"));

                save(ref, new Wisata("wisata_sorsel", "Sorong Selatan", "Tempat Wisata",
                                "Keindahan sungai jernih di Teminabuan.", "1500000", "Sorong Selatan", "2 Hari",
                                "sorsel", "Mobil", "4.7"));

                save(ref, new Wisata("wisata_tambrauw", "Tambrauw", "Tempat Wisata",
                                "Kabupaten Konservasi dengan alam yang sangat asri.", "3500000", "Tambrauw", "3 Hari",
                                "tambrauw", "Mobil 4x4", "4.7"));

                // --- PENGINAPAN ---
                save(ref, new Wisata("wisata_nusa", "Hotel Nusa Indah", "Penginapan",
                                "Hotel nyaman di tengah kota.", "450000", "Sorong Selatan", "Malam", "nusa", "Mobil",
                                "4.5"));

                save(ref, new Wisata("wisata_piaynemo", "Piaynemo Homestay", "Penginapan",
                                "Penginapan di atas air dengan view ikonik.", "1500000", "Raja Ampat", "Malam",
                                "piaynemo_homestay", "Speedboat", "4.8"));

                // --- MAKANAN ---
                save(ref, new Wisata("wisata_papeda", "Papeda Kuah Kuning", "Makanan",
                                "Sagu dengan ikan kuah kuning segar.", "50000", "Rumah Makan", "Porsi", "papeda_baru",
                                "-", "4.9"));

                save(ref, new Wisata("wisata_keladi", "Keladi Bakar", "Makanan",
                                "Keladi bakar bumbu tradisional.", "25000", "Pasar", "Porsi", "keladi_asli", "-",
                                "4.8"));

                save(ref, new Wisata("wisata_nasgor", "Nasi Goreng Papua", "Makanan",
                                "Nasi goreng spesial rempah lokal.", "30000", "Warung", "Porsi", "nasgor_baru", "-",
                                "4.5"));

                save(ref, new Wisata("wisata_chips", "Keripik Keladi", "Makanan",
                                "Snack renyah khas Sorong.", "35000", "Toko Oleh-oleh", "Bungkus", "keripik_keladi",
                                "-",
                                "4.7"));

                save(ref, new Wisata("wisata_sagu", "Sagu Lempeng", "Makanan",
                                "Kue sagu tradisional.", "15000", "Pasar", "Buah", "sagu_lempeng", "-",
                                "4.6"));

                // --- AKSESORIS ---
                save(ref, new Wisata("wisata_noken", "Noken", "Aksesoris",
                                "Tas tradisional dari serat kulit kayu.", "150000", "Toko", "Buah", "noken", "-",
                                "4.8"));

                save(ref, new Wisata("wisata_tifa", "Tifa", "Aksesoris",
                                "Alat musik pukul tradisional Papua.", "500000", "Toko Seni", "Buah", "tifa", "-",
                                "4.7"));

                save(ref, new Wisata("wisata_pakaian", "Pakaian Adat", "Aksesoris",
                                "Busana tradisional bahan alami.", "1200000", "Butik", "Set", "pakian_adat", "-",
                                "4.9"));
        }

        private static void save(DatabaseReference ref, Wisata wisata) {
                if (wisata.getId() != null) {
                        ref.child(wisata.getId()).setValue(wisata);
                }
        }
}
