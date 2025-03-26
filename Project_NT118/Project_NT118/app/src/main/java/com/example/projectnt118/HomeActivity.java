package com.example.projectnt118;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.projectnt118.adapter.ViewPagerAdapter;
import com.example.projectnt118.fragment.MapFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.mapbox.mapboxsdk.Mapbox;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Init Mapbox
        Mapbox.getInstance(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        viewPager = findViewById(R.id.view_pager);
        viewPager.setUserInputEnabled(false);
        setupViewPager();
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.action_map) {
                    viewPager.setCurrentItem(0);
                    ((ViewPagerAdapter) Objects.requireNonNull(viewPager.getAdapter())).loadPotholes();
                    return true;
                } else if (item.getItemId() == R.id.action_dashboard) {
                    viewPager.setCurrentItem(1);
                    return true;
                } else if (item.getItemId() == R.id.action_setting) {
                    viewPager.setCurrentItem(2);
                    return true;
                } else if (item.getItemId() == R.id.action_profile) {
                    viewPager.setCurrentItem(3);
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(adapter);

    }
}