package com.example.projectnt118.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.projectnt118.R;

public class SettingsFragment extends Fragment {

    private static final String PREFS_NAME = "PotholeSettings";
    private static final String KEY_SMALL_POTHOLE = "bl_small_pothole";
    private static final String KEY_MEDIUM_POTHOLE = "bl_medium_pothole";
    private static final String KEY_LARGE_POTHOLE = "bl_large_pothole";
    private static final String KEY_WARNING_DISTANCE = "warning_distance";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        CheckBox checkBoxSmallPothole = view.findViewById(R.id.checkBoxSmallPothole);
        CheckBox checkBoxMediumPothole = view.findViewById(R.id.checkBoxMediumPothole);
        CheckBox checkBoxLargePothole = view.findViewById(R.id.checkBoxLargePothole);

        SeekBar seekBarWarningDistance = view.findViewById(R.id.seekBarWarningDistance);
        TextView textViewWarningDistanceValue = view.findViewById(R.id.textViewWarningDistanceValue);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        checkBoxSmallPothole.setChecked(sharedPreferences.getBoolean(KEY_SMALL_POTHOLE, false));
        checkBoxMediumPothole.setChecked(sharedPreferences.getBoolean(KEY_MEDIUM_POTHOLE, false));
        checkBoxLargePothole.setChecked(sharedPreferences.getBoolean(KEY_LARGE_POTHOLE, false));
        checkBoxSmallPothole.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_SMALL_POTHOLE, isChecked).apply();
        });
        checkBoxMediumPothole.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_MEDIUM_POTHOLE, isChecked).apply();
        });
        checkBoxLargePothole.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPreferences.edit().putBoolean(KEY_LARGE_POTHOLE, isChecked).apply();
        });
        int warningDistance = sharedPreferences.getInt(KEY_WARNING_DISTANCE, 10);
        seekBarWarningDistance.setProgress(warningDistance - 10);
        textViewWarningDistanceValue.setText(warningDistance + " meters");

        seekBarWarningDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int distance = progress + 10;
                textViewWarningDistanceValue.setText(distance + " meters");
                sharedPreferences.edit().putInt(KEY_WARNING_DISTANCE, distance).apply();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        return view;
    }
}