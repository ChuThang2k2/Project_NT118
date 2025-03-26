package com.example.projectnt118.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.projectnt118.fragment.DashboardFragment;
import com.example.projectnt118.fragment.MapFragment;
import com.example.projectnt118.fragment.ProfileFragment;
import com.example.projectnt118.fragment.SettingsFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    private MapFragment mapFragment;

    public void loadPotholes() {
        mapFragment.loadPotholes();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1:
                return new DashboardFragment();
            case 2:
                return new SettingsFragment();
            case 3:
                return new ProfileFragment();
            default:
                mapFragment = new MapFragment();
                return mapFragment;
        }

    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
