package com.example.projectnt118;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.projectnt118.network.GraphHopperService;
import com.example.projectnt118.network.RouteResponse;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.LineManager;
import com.mapbox.mapboxsdk.plugins.annotation.LineOptions;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NavigationActivity extends AppCompatActivity {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    String apiKey = "2290ccec-c3da-4649-ab99-6844a908759d";
    String key = "OKRTVHPBV2gz7AUYTtTT";
    String mapId = "streets-v2";
    String styleUrl = "https://api.maptiler.com/maps/" + mapId + "/style.json?key=" + key;
    String mapboxKey = "sk.eyJ1IjoibWFpdGhpaGlldSIsImEiOiJjbTMweWpkajgwbzh1MmpzNzFudGg0aHRtIn0.gf_3XXj87bvRYpnvzV5qiQ";
    List<LatLng> potholes = new ArrayList<>();
    private MapView mapView;
    private MapboxMap mapboxMap;
    private LatLng originLatLng, destinationLatLng;
    private TextView tvDistance;
    private TextView tvNextStop;
    private TextView tvTimeToNext;
    private ImageView ivDirection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        // Nhận thông tin từ Intent
        String origin = getIntent().getStringExtra("origin");
        String destination = getIntent().getStringExtra("destination");

        ivDirection = findViewById(R.id.ivDirection);

        // Chuyển đổi từ chuỗi tọa độ (nếu cần)
        String[] originCoords = origin.split(",");
        String[] destCoords = destination.split(",");

        originLatLng = new LatLng(Double.parseDouble(originCoords[0]), Double.parseDouble(originCoords[1]));
        destinationLatLng = new LatLng(Double.parseDouble(destCoords[0]), Double.parseDouble(destCoords[1]));

        // Khởi tạo MapView
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        Button btnEnd = findViewById(R.id.btnEnd);
        btnEnd.setOnClickListener(v -> onBackPressed());

        mapView.getMapAsync(map -> {
            mapboxMap = map;

            // Tải style từ MapTiler
            mapboxMap.setStyle(new Style.Builder().fromUri(styleUrl), style -> {
                // Sau khi tải xong style, thực hiện hiển thị tuyến đường
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                } else {
                    enableLocationComponent(style);
                }
                drawRoute();
            });
        });


        tvDistance = findViewById(R.id.tvDistance);
        tvNextStop = findViewById(R.id.tvNextStop);
        tvTimeToNext = findViewById(R.id.tvTimeToNext);

        Random random = new Random();
        int numberOfPotholes = 5 + random.nextInt(16);
        generateRandomPotholes(originLatLng, numberOfPotholes, 1000);
    }

    private List<LatLng> generateRandomPotholes(LatLng currentLocation, int numberOfPotholes, double radiusInMeters) {
        Random random = new Random();

        for (int i = 0; i < numberOfPotholes; i++) {
            double randomDistance = radiusInMeters * random.nextDouble();
            double randomAngle = 2 * Math.PI * random.nextDouble();

            double deltaLat = randomDistance * Math.cos(randomAngle) / 111000; // Convert meters to degrees
            double deltaLng = randomDistance * Math.sin(randomAngle) / (111000 * Math.cos(Math.toRadians(currentLocation.getLatitude())));

            double newLat = currentLocation.getLatitude() + deltaLat;
            double newLng = currentLocation.getLongitude() + deltaLng;

            potholes.add(new LatLng(newLat, newLng));
        }

        TextView tvHoles = findViewById(R.id.tvHoles);
        tvHoles.setText(potholes.size() + "");

        return potholes;
    }

    private void enableLocationComponent(Style style) {
        LocationComponent locationComponent = mapboxMap.getLocationComponent();
        LocationComponentActivationOptions locationComponentActivationOptions = LocationComponentActivationOptions.builder(this, style).build();
        locationComponent.activateLocationComponent(locationComponentActivationOptions);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setCameraMode(CameraMode.TRACKING);
        locationComponent.setRenderMode(RenderMode.COMPASS);

        Location lastKnownLocation = locationComponent.getLastKnownLocation();
        if (lastKnownLocation != null) {
            mapboxMap.setCameraPosition(new CameraPosition.Builder().target(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude())).zoom(15.0).build());
        }

        // Draw potholes on the map
        SymbolManager symbolManager = new SymbolManager(mapView, mapboxMap, style);
        symbolManager.setIconAllowOverlap(true);
        symbolManager.setTextAllowOverlap(true);

        // Add pothole symbols
        for (LatLng pothole : potholes) {
            symbolManager.create(new SymbolOptions().withLatLng(pothole).withIconImage("pothole-icon").withIconSize(1.0f));
        }

        // Add pothole icon to the style
        style.addImage("pothole-icon", BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.flag)));
    }

    private void drawRoute() {
        // Gọi API GraphHopper hoặc Mapbox để lấy tuyến đường giữa origin và destination
        findDirections(originLatLng, destinationLatLng);
    }

    private void findDirections(LatLng origin, LatLng destination) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://graphhopper.com/api/1/")  // Hoặc URL cho Mapbox Directions API
                .addConverterFactory(GsonConverterFactory.create()).build();

        GraphHopperService graphHopperService = retrofit.create(GraphHopperService.class);
        String originStr = origin.getLatitude() + "," + origin.getLongitude();
        String destinationStr = destination.getLatitude() + "," + destination.getLongitude();

        Call<RouteResponse> call = graphHopperService.getRoute(originStr, destinationStr, "car", "en", apiKey);
        call.enqueue(new Callback<RouteResponse>() {
            @Override
            public void onResponse(Call<RouteResponse> call, Response<RouteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RouteResponse routeResponse = response.body();
                    if (routeResponse != null) {
                        displayRoute(routeResponse);
                    }
                } else {
                    Toast.makeText(NavigationActivity.this, "No route found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RouteResponse> call, Throwable t) {
                Toast.makeText(NavigationActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayRoute(RouteResponse routeResponse) {
        List<LatLng> routePoints = new ArrayList<>();
        // Duyệt qua các `Path` và giải mã `points`
        for (RouteResponse.Path path : routeResponse.getPaths()) {
            routePoints.addAll(path.decodePolyline());
        }

        // Vẽ tuyến đường trên bản đồ
        mapboxMap.getStyle(style -> {
            // Vẽ tuyến đường
            LineManager lineManager = new LineManager(mapView, mapboxMap, style);
            LineOptions lineOptions = new LineOptions().withLatLngs(routePoints).withLineWidth(5.0f)  // Độ dày của tuyến đường
                    .withLineColor("#3b9ddd");  // Màu sắc của tuyến đường
            lineManager.create(lineOptions);

            // Tạo SymbolManager để thêm các biểu tượng
            SymbolManager symbolManager = new SymbolManager(mapView, mapboxMap, style);
            symbolManager.setIconAllowOverlap(true);
            symbolManager.setTextAllowOverlap(true);

            // Thêm điểm xuất phát với biểu tượng xe máy
            symbolManager.create(new SymbolOptions().withLatLng(routePoints.get(0)) // Điểm đầu của tuyến đường
                    .withIconImage("motorcycle-icon") // Tên biểu tượng xe máy
                    .withIconSize(1.5f));

            // Thêm điểm đến với biểu tượng hình tròn
            symbolManager.create(new SymbolOptions().withLatLng(routePoints.get(routePoints.size() - 1)) // Điểm cuối của tuyến đường
                    .withIconImage("circle-icon") // Tên biểu tượng hình tròn
                    .withIconSize(1.2f));

            // Thêm các biểu tượng vào style
//            style.addImage("motorcycle-icon", BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.motorcycle)));
//            style.addImage("circle-icon", BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.target)));


            // Di chuyển camera để hiển thị tuyến đường
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(routePoints.get(0));  // Điểm đầu
            builder.include(routePoints.get(routePoints.size() - 1));  // Điểm cuối

            // Tính toán vùng bao quanh và giới hạn camera để hiển thị tất cả
            LatLngBounds bounds = builder.build();
            mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100)); // 100 là khoảng cách margin (paddings)

            // Di chuyển camera đến điểm xuất phát (origin)
            mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(routePoints.get(0), 14)); // Di chuyển đến điểm xuất phát với zoom level 14

            updateRouteDetails(routeResponse, routePoints.get(0));
        });
    }

    private void updateRouteDetails(RouteResponse routeResponse, LatLng currentLocation) {
        // Lấy thông tin từ đường đi đầu tiên
        RouteResponse.Path path = routeResponse.getPaths().get(0);

        // Lấy danh sách các điểm trên tuyến đường
        List<LatLng> routePoints = path.decodePolyline();

        // Lấy điểm dừng tiếp theo (sau điểm xuất phát)
        LatLng nextStop = routePoints.get(1); // Điểm tiếp theo trong tuyến đường (sau điểm xuất phát)


        List<RouteResponse.Instruction> instructions = path.getInstructions();
        if (!instructions.isEmpty()) {
            RouteResponse.Instruction currentInstruction = instructions.get(0);
            tvDistance.setText(String.format("%.2f", currentInstruction.getDistance() / 1000));  // Convert to km and display
            if (currentInstruction.getSign() == 0) {
                ivDirection.setImageResource(R.drawable.go_straight);  // Display direction sign
            } else if (currentInstruction.getSign() == 1) {
                ivDirection.setImageResource(R.drawable.turn_right);  // Display direction sign
            } else if (currentInstruction.getSign() == 2) {
                ivDirection.setImageResource(R.drawable.turn_left);  // Display direction sign
            }
            tvNextStop.setText(currentInstruction.getText());  // Display instruction text

            double timeInSeconds = currentInstruction.getTime();
            tvTimeToNext.setText(String.format("%.0f", timeInSeconds / 60.0));  // Convert to minutes and display
        }
    }
}