package com.example.projectnt118.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.projectnt118.R;
import com.example.projectnt118.network.Suggestion;

import java.util.List;

public class CustomAdapter extends ArrayAdapter<Suggestion> {

    public interface CustomAdapterListener {
        void onClickItem(Suggestion suggestion);
    }

    public CustomAdapter(@NonNull Context context, @NonNull List<Suggestion> objects) {
        super(context, 0, objects);
    }

    private CustomAdapterListener listener;

    public void setListener(CustomAdapterListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        Suggestion item = getItem(position);
        if (item != null) {
            TextView addressView = convertView.findViewById(R.id.address);
            TextView latitudeView = convertView.findViewById(R.id.latitude);
            TextView longitudeView = convertView.findViewById(R.id.longitude);

            addressView.setText(item.getPlaceName());
            latitudeView.setText("Latitude: " + item.getLatLng().getLatitude());
            longitudeView.setText("Longitude: " + item.getLatLng().getLongitude());

            convertView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClickItem(item);
                }
            });
        }

        return convertView;
    }
}