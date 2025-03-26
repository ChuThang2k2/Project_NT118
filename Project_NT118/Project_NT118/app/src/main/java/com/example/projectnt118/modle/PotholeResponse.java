package com.example.projectnt118.modle;

public class PotholeResponse {

    private double lat;
    private double lang;
    private int severity;

    public PotholeResponse(double lat, double lang, int severity) {
        this.lat = lat;
        this.lang = lang;
        this.severity = severity;
    }

    public PotholeResponse() {
    }

    public int getSeverity() {
        return severity;
    }

    public void setSeverity(int severity) {
        this.severity = severity;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLang() {
        return lang;
    }

    public void setLang(double lang) {
        this.lang = lang;
    }
}
