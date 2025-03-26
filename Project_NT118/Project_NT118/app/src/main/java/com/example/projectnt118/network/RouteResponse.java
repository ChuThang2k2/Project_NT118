package com.example.projectnt118.network;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

public class RouteResponse {
    private List<Path> paths;

    public List<Path> getPaths() {
        return paths;
    }

    public static class Path {
        private double distance;
        private double time;
        private String points; // Encoded polyline of the route
        private List<Instruction> instructions; // List of instructions

        public double getDistance() {
            return distance;
        }

        public double getTime() {
            return time;
        }

        public String getPoints() {
            return points;
        }

        public List<Instruction> getInstructions() {
            return instructions;
        }

        // Method to decode polyline
        public List<LatLng> decodePolyline() {
            List<LatLng> poly = new ArrayList<>();
            int index = 0, len = points.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = points.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = points.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((lat / 1E5), (lng / 1E5));
                poly.add(p);
            }
            return poly;
        }
    }

    public static class Instruction {
        private String text;
        private double distance;
        private double time;
        private int sign;

        private String street_name;

        public String getText() {
            return text;
        }

        public double getDistance() {
            return distance;
        }

        public double getTime() {
            return time;
        }

        public int getSign() {
            return sign;
        }

        public String generateInstruction() {
            // Check if street_name and distance are available
            if (street_name != null && distance > 0) {
                // Return instruction text
                return "Đi tiếp trên " + street_name + " trong " + distance + " km.";
            }
            // In case of missing information
            return "Thông tin không đầy đủ để tạo hướng dẫn.";
        }
    }
}