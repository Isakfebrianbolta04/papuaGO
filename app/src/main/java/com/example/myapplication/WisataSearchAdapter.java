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
import java.util.List;
import java.util.Locale;

public class WisataSearchAdapter extends RecyclerView.Adapter<WisataSearchAdapter.ViewHolder> {

    private List<Wisata> wisataList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Wisata wisata);
    }

    public WisataSearchAdapter(List<Wisata> wisataList, OnItemClickListener listener) {
        this.wisataList = wisataList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wisata_search, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Wisata wisata = wisataList.get(position);
        holder.bind(wisata, listener);
    }

    @Override
    public int getItemCount() {
        return wisataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgWisata, imgLocationIcon, btnDetail;
        ImageView star1, star2, star3, star4, star5;
        TextView txtNama, txtLokasi, txtHarga, txtKategori;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgWisata = itemView.findViewById(R.id.imgWisata);
            imgLocationIcon = itemView.findViewById(R.id.imgLocationIcon);
            btnDetail = itemView.findViewById(R.id.btnDetail);
            txtNama = itemView.findViewById(R.id.txtNama);
            txtLokasi = itemView.findViewById(R.id.txtLokasi);
            txtHarga = itemView.findViewById(R.id.txtHarga);
            txtKategori = itemView.findViewById(R.id.txtKategori);

            star1 = itemView.findViewById(R.id.star1);
            star2 = itemView.findViewById(R.id.star2);
            star3 = itemView.findViewById(R.id.star3);
            star4 = itemView.findViewById(R.id.star4);
            star5 = itemView.findViewById(R.id.star5);
        }

        public void bind(final Wisata wisata, final OnItemClickListener listener) {
            String kategori = wisata.getKategori();
            txtNama.setText(wisata.getNama());
            txtLokasi.setText(wisata.getLokasi());
            if (txtKategori != null) {
                txtKategori.setText(kategori);
            }

            // Set Rating Stars based on the rating field
            String ratingStr = wisata.getRating();
            float ratingValue = 4.5f;
            try {
                if (ratingStr != null)
                    ratingValue = Float.parseFloat(ratingStr);
            } catch (Exception e) {
            }

            star1.setVisibility(View.VISIBLE);
            star2.setVisibility(View.VISIBLE);
            star3.setVisibility(View.VISIBLE);
            star4.setVisibility(View.VISIBLE);
            star5.setVisibility(ratingValue >= 4.8f ? View.VISIBLE : View.GONE);

            // Set icon based on category
            if (kategori != null) {
                if (kategori.toLowerCase().contains("makanan")) {
                    imgLocationIcon.setImageResource(R.drawable.icon_makanan);
                } else if (kategori.toLowerCase().contains("penginapan")) {
                    imgLocationIcon.setImageResource(R.drawable.icon_hotel1);
                } else if (kategori.toLowerCase().contains("aksesoris")) {
                    imgLocationIcon.setImageResource(R.drawable.shoping_bag);
                } else {
                    imgLocationIcon.setImageResource(R.drawable.lokasi1);
                }
            }

            // Format Harga
            try {
                String cleanHarga = wisata.getHarga().replaceAll("[^\\d]", "");
                double hargaValue = Double.parseDouble(cleanHarga);
                Locale localeID = new Locale.Builder().setLanguage("id").setRegion("ID").build();
                NumberFormat formatRupiah = NumberFormat.getCurrencyInstance(localeID);

                String labelSuffix = " / orang";
                if (kategori != null) {
                    if (kategori.toLowerCase().contains("penginapan"))
                        labelSuffix = " / malam";
                    else if (kategori.toLowerCase().contains("makanan"))
                        labelSuffix = " / porsi";
                    else if (kategori.toLowerCase().contains("aksesoris"))
                        labelSuffix = "";
                }

                txtHarga.setText(formatRupiah.format(hargaValue) + labelSuffix);
            } catch (Exception e) {
                txtHarga.setText("Rp " + wisata.getHarga());
            }

            // Load Image with Glide
            Object imageSource = wisata.getImageUrl();
            int resourceId = itemView.getContext().getResources().getIdentifier(
                    wisata.getImageUrl(), "drawable", itemView.getContext().getPackageName());

            if (resourceId != 0) {
                imageSource = resourceId;
            }

            Glide.with(itemView.getContext())
                    .load(imageSource)
                    .placeholder(R.drawable.raja4)
                    .error(R.drawable.raja4)
                    .centerCrop()
                    .into(imgWisata);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(wisata);
                }
            });

            if (btnDetail != null) {
                btnDetail.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onItemClick(wisata);
                    }
                });
            }
        }
    }
}
