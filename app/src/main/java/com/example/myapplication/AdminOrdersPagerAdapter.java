package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AdminOrdersPagerAdapter extends FragmentStateAdapter {

    public AdminOrdersPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new WisataOrdersFragment();
            case 1:
                return new HotelOrdersFragment();
            case 2:
                return new KulinerOrdersFragment();
            case 3:
                return new AksesorisOrdersFragment();
            default:
                return new WisataOrdersFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4; // Wisata, Hotel, Kuliner, Aksesoris
    }
}
