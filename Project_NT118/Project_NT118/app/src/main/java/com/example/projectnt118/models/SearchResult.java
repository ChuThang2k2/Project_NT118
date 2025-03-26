package com.example.projectnt118.models;

public class SearchResult {
    private String address;
    private double latitude;
    private double longitude;

    public SearchResult(String address, double latitude, double longitude) {
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}