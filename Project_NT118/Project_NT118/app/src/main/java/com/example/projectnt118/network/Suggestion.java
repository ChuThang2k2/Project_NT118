package com.example.projectnt118.network;

import com.mapbox.mapboxsdk.geometry.LatLng;

public class Suggestion {
    private String placeName;
    private LatLng latLng;

    public Suggestion(String placeName, LatLng latLng) {
        this.placeName = placeName;
        this.latLng = latLng;
    }

    public String getPlaceName() {
        return placeName;
    }

    public LatLng getLatLng() {
        return latLng;
    }
}
