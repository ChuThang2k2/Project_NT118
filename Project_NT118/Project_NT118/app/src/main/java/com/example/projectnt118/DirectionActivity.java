package com.example.projectnt118;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.projectnt118.api.ApiService;
import com.example.projectnt118.api.RetrofitClient;
import com.example.projectnt118.databinding.ActivityDirectionBinding;
import com.example.projectnt118.fragment.CustomAdapter;
import com.example.projectnt118.modle.PotholeResponse;
import com.example.projectnt118.network.Feature;
import com.example.projectnt118.network.GeocodingResponse;
import com.example.projectnt118.network.GraphHopperService;
import com.example.projectnt118.network.MapboxGeocodingService;
import com.example.projectnt118.network.RouteResponse;
import com.example.projectnt118.network.Suggestion;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
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
import com.mapbox.turf.TurfMeasurement;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DirectionActivity extends AppCompatActivity implements SensorEventListener {

    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private static final String CHANNEL_ID = "drop_notification_channel";
    private static final int PERMISSION_REQUEST_CODE = 1;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    findDirections();
                } else {
                    Toast.makeText(this, "You need to grant location permission to use this feature", Toast.LENGTH_SHORT).show();

                    Intent settingsIntent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName())
                            .putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL_ID);
                    startActivity(settingsIntent);
                }
            });

    protected boolean isShowingSuggestions = true;
    // Find other maps in https://cloud.maptiler.com/maps/
    String mapId = "streets-v2";
    // Get the API Key by app's BuildConfig
    String key = "OKRTVHPBV2gz7AUYTtTT";
    String apiKey = "2290ccec-c3da-4649-ab99-6844a908759d";
    String mapboxKey = "sk.eyJ1IjoibWFpdGhpaGlldSIsImEiOiJjbTMweWpkajgwbzh1MmpzNzFudGg0aHRtIn0.gf_3XXj87bvRYpnvzV5qiQ";
    String styleUrl = "https://api.maptiler.com/maps/" + mapId + "/style.json?key=" + key;
    ListView lvSuggestions;
    CustomAdapter suggestionsAdapter;
    List<Suggestion> suggestionsList = new ArrayList<Suggestion>();
    Button btnFindDirections;
    private MapView mapView;
    private EditText etDestination;
    private MapboxMap mapboxMap;
    private @NonNull ActivityDirectionBinding binding;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private LatLng originLatLng;
    private LatLng destinationLatLng;
    private GraphHopperService graphHopperService;
    private EditText targetInput;
    private LinearLayout llButtons;
    private FusedLocationProviderClient fusedLocationClient;
    private EditText etOrigin;
    private Timer timeSchedule;
    private int warningDistance;
    private FloatingActionButton zoomInButton, zoomOutButton;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("PotholeSettings", Context.MODE_PRIVATE);
        warningDistance = sharedPreferences.getInt("warning_distance", 100); // default 100m canh bao

        try {
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager != null) {
                accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            }

            createNotificationChannel();

            // Init Mapbox
            Mapbox.getInstance(this);

            // Init layout view
            binding = ActivityDirectionBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            lvSuggestions = findViewById(R.id.lvSuggestions);
            suggestionsAdapter = new CustomAdapter(this, suggestionsList);
            suggestionsAdapter.setListener(suggestion -> {
                lvSuggestions.setVisibility(View.GONE);
                targetInput.setText(suggestion.getPlaceName());
                isShowingSuggestions = false;
                destinationLatLng = suggestion.getLatLng();
            });
            lvSuggestions.setAdapter(suggestionsAdapter);

            llButtons = findViewById(R.id.llButtons);

            // Init the MapView
            mapView = findViewById(R.id.mapView);
            mapView.onCreate(savedInstanceState);

            mapView.getMapAsync(map -> {
                // Gán đối tượng MapboxMap vào biến toàn cục
                mapboxMap = map;

                // Thiết lập Style cho MapboxMap
                mapboxMap.setStyle(new Style.Builder().fromUri(styleUrl), style -> {
                    // Thực hiện các thao tác khác sau khi tải Style thành công
                });
            });

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            } else {
                enableLocationComponent();
            }

            // Set up the button to find directions
            btnFindDirections = findViewById(R.id.btnFindDirections);
            etOrigin = findViewById(R.id.etOrigin);
            etDestination = findViewById(R.id.etDestination);
            zoomInButton = findViewById(R.id.zoom_in);
            zoomOutButton = findViewById(R.id.zoom_out);
            ListView lvSuggestions = findViewById(R.id.lvSuggestions);

            etOrigin = findViewById(R.id.etOrigin);
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            zoomInButton.setOnClickListener(v -> {
                double zoomCurrent = mapboxMap.getCameraPosition().zoom;
                mapboxMap.setCameraPosition(
                        new CameraPosition.Builder()
                                .zoom(zoomCurrent + 1)
                                .build()
                );
            });
            zoomOutButton.setOnClickListener(v -> {
                double zoomCurrent = mapboxMap.getCameraPosition().zoom;
                mapboxMap.setCameraPosition(
                        new CameraPosition.Builder()
                                .zoom(zoomCurrent - 1)
                                .build()
                );
            });
            etOrigin.setOnClickListener(v -> {
                if (etOrigin.getText().toString() == "Your location") {
                    etOrigin.setText("");
                }
                targetInput = etOrigin;
            });

            etDestination.setOnClickListener(v -> {
                targetInput = etDestination;
            });

            // Kiểm tra quyền truy cập vị trí
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            } else {
                getLastLocation();
            }

            setupClearButton(etDestination);
            setupClearButton(etOrigin);

            etOrigin.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    suggestionsList.clear();
                    // Cập nhật lại ListView sau khi xóa
                    suggestionsAdapter.notifyDataSetChanged();
                    lvSuggestions.setVisibility(View.VISIBLE);
                    isShowingSuggestions = true;
                    targetInput = etOrigin;
                    fetchSuggestions(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            etDestination.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    suggestionsList.clear();
                    // Cập nhật lại ListView sau khi xóa
                    suggestionsAdapter.notifyDataSetChanged();
                    lvSuggestions.setVisibility(View.VISIBLE);
                    isShowingSuggestions = true;
                    targetInput = etDestination;
                    fetchSuggestions(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            lvSuggestions.setOnItemClickListener((parent, view, position, id) -> {
//                SearchResult selectedSuggestion = suggestionsList.get(position);
//                String selectedAddress = selectedSuggestion.getPlaceName();
//                targetInput.setText(selectedAddress);
//                targetInput.setEnabled(false);
//
//                isShowingSuggestions = false;
//                if (targetInput == etDestination) {
//                    destinationLatLng = selectedSuggestion.getLatLng();
//                } else {
//                    originLatLng = selectedSuggestion.getLatLng();
//                }
//
//                lvSuggestions.setVisibility(View.GONE);
//                // Xóa danh sách gợi ý
//                suggestionsList.clear();
//                fetchSuggestions("");
//                isShowingSuggestions = false;

                // Cập nhật lại ListView sau khi xóa
                suggestionsAdapter.notifyDataSetChanged();
            });

            btnFindDirections.setOnClickListener(v -> {
                if (originLatLng == null || destinationLatLng == null) {
                    Toast.makeText(this, "Please enter both origin and destination", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Build.VERSION.SDK_INT >= 33) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
                        return;
                    }
                    findDirections();
                }
            });
        } catch (Exception e) {
            Log.e("MainActivity", "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void findDirections() {
        loadPotholes();
        etOrigin.setVisibility(View.GONE);
        etDestination.setVisibility(View.GONE);
        lvSuggestions.setVisibility(View.GONE);
        LinearLayout llSearchBox = findViewById(R.id.llSearchBox);
        llSearchBox.setVisibility(View.GONE);

        LocationComponent locationComponent = mapboxMap.getLocationComponent();
        Location lastKnownLocation = locationComponent.getLastKnownLocation();
        if (lastKnownLocation != null) {
            LatLng origin;

            if (originLatLng != null) {
                // Nếu trường `etOrigin` rỗng, sử dụng `originLatLng`
                origin = originLatLng;
            } else {
                // Nếu trường `etOrigin` không rỗng, lấy tọa độ từ `lastKnownLocation`
                origin = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            }
            LatLng destination = destinationLatLng; // Lấy tọa độ điểm đích từ input của người dùng (có thể là tọa độ hoặc địa chỉ)

            findDirections(origin, destination);
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Kiểm tra xem có vị trí hay không
                if (location != null) {
                    originLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    // Điền vào EditText
                    etOrigin.setText("Your location");
                } else {
                    Toast.makeText(DirectionActivity.this, "Không thể lấy vị trí hiện tại", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void findDirections(LatLng origin, LatLng destination) {
        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://graphhopper.com/api/1/").addConverterFactory(GsonConverterFactory.create()).build();

        graphHopperService = retrofit.create(GraphHopperService.class);

        // Chuyển đổi LatLng sang chuỗi "lat,lng"
        String originStr = origin.getLatitude() + "," + origin.getLongitude();
        String destinationStr = destination.getLatitude() + "," + destination.getLongitude();

        // Gọi API với chuỗi tọa độ đã chuyển đổi
        Call<RouteResponse> call = graphHopperService.getRoute(originStr, destinationStr, "car", "en", apiKey);
        call.enqueue(new Callback<RouteResponse>() {
            @Override
            public void onResponse(Call<RouteResponse> call, Response<RouteResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RouteResponse routeResponse = response.body();
                    if (routeResponse != null) {
                        Gson gson = new Gson();
                        String jsonResponse = gson.toJson(routeResponse);
                        Log.d("RouteResponse", "Route: " + jsonResponse);
                    }
                    // Xử lý hiển thị tuyến đường
                    displayRoute(routeResponse);
                } else {
//                    Toast.makeText(MainActivity.this, "No route found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RouteResponse> call, Throwable t) {
                Toast.makeText(DirectionActivity.this, "Error:: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private AlertDialog alertDialog;

    private void displayRoute(RouteResponse routeResponse) {
        List<LatLng> routePoints = new ArrayList<>();

        // Duyệt qua tất cả các `Path` và giải mã `points`
        for (RouteResponse.Path path : routeResponse.getPaths()) {
            routePoints.addAll(path.decodePolyline()); // Giải mã Polyline và thêm vào danh sách
        }

        // Lấy điểm đầu và điểm cuối của tuyến đường
        LatLng origin = routePoints.get(0);
        LatLng destination = routePoints.get(routePoints.size() - 1);

        // Tạo một LatLngBounds bao quanh cả 2 điểm
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(origin);  // Thêm điểm đầu vào vùng bao quanh
        builder.include(destination);  // Thêm điểm cuối vào vùng bao quanh

        // Tính toán vùng bao quanh và giới hạn bản đồ để hiển thị tất cả
        LatLngBounds bounds = builder.build();

        // Di chuyển camera để bao quanh cả 2 điểm
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100)); // 100 là khoảng cách margin (paddings) xung quanh vùng hiển thị

        btnFindDirections.setVisibility(View.GONE);
        suggestionsList.clear();
        suggestionsAdapter.notifyDataSetChanged();
        lvSuggestions.setVisibility(View.GONE);
        llButtons.setVisibility(View.VISIBLE);
        Button btnCancel = findViewById(R.id.btnCancel);

        btnCancel.setOnClickListener(v -> {
            // Tạo lại MainActivity bằng cách sử dụng Intent
            if (timeSchedule != null) {
                timeSchedule.cancel();
            }

            Intent intent = new Intent(DirectionActivity.this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Đảm bảo Activity cũ được đóng lại
        });

        Button btnGo = findViewById(R.id.btnGo);

        btnGo.setOnClickListener(v -> {
//            String originStr = originLatLng.getLatitude() + "," + originLatLng.getLongitude();
//            String destinationStr = destinationLatLng.getLatitude() + "," + destinationLatLng.getLongitude();
            // Chuyển tới DetailActivity và truyền thông tin
//            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
//            intent.putExtra("origin", originStr); // Truyền thông tin về điểm bắt đầu
//            intent.putExtra("destination", destinationStr); // Truyền thông tin về điểm kết thúc
//            startActivity(intent);
            checkPotholes();
            scheduleCheckPotholes();
        });

        // Vẽ Polyline trên bản đồ MapTiler
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
            symbolManager.create(new SymbolOptions().withLatLng(origin) // Điểm đầu của tuyến đường
                    .withIconImage("motorcycle-icon") // Tên biểu tượng xe máy
                    .withIconSize(1.5f));

            // Thêm điểm đến với biểu tượng hình tròn
            symbolManager.create(new SymbolOptions().withLatLng(destination) // Điểm cuối của tuyến đường
                    .withIconImage("circle-icon") // Tên biểu tượng hình tròn
                    .withIconSize(1.2f));

            // Đảm bảo bạn đã thêm biểu tượng xe máy và hình tròn vào style
//            style.addImage("motorcycle-icon", BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.motorcycle)));
//            style.addImage("circle-icon", BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.target)));

            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });
    }

    private void scheduleCheckPotholes() {
        timeSchedule = new java.util.Timer();
        timeSchedule.schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        checkPotholes();
                        timeSchedule.cancel();
                        scheduleCheckPotholes();
                    }
                },
                10000
        );
    }

    private void checkPotholes() {
        Log.d("GT56_x", "time start = " + System.currentTimeMillis() / 1000);
        LocationComponent locationComponent = mapboxMap.getLocationComponent();
        Location lastKnownLocation = locationComponent.getLastKnownLocation();
        if (lastKnownLocation != null) {
            for (PotholeResponse po : potholeList) {
                double distance = TurfMeasurement.distance(
                        Point.fromLngLat(po.getLat(), po.getLang()),
                        Point.fromLngLat(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude())
                );
                if (distance * 1000 <= warningDistance) {
                    // show notification warning
                    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.icon_warning)
                            .setContentTitle("Cảnh báo phía trước có ổ gà")
                            .setContentText("Phát hiện có ổ gà! Chú ý di chuyển.")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    NotificationManagerCompat.from(this).notify(1, builder.build());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (alertDialog != null && alertDialog.isShowing()) {
                                return;
                            }
                            alertDialog = new AlertDialog.Builder(DirectionActivity.this)
                                    .setTitle("Cảnh báo")
                                    .setMessage("Phát hiện ổ gà\nVị trí: " + po.getLat() + ", " + po.getLang() + "\nMức độ: " + po.getSeverity())
                                    .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                    .setCancelable(false)
                                    .create();
                            alertDialog.show();
                        }
                    });
                }
                Log.d("GT56_x", "distance = " + distance * 1000);
            }
        }
    }

    private final ArrayList<PotholeResponse> potholeList = new ArrayList<>();

    private void loadPotholes() {
        potholeList.clear();

        Retrofit retrofit = RetrofitClient.getLocalClient();
        ApiService apiService = retrofit.create(ApiService.class);

        apiService.getPotholes().enqueue(new retrofit2.Callback<List<PotholeResponse>>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<List<PotholeResponse>> call, @NonNull retrofit2.Response<List<PotholeResponse>> response) {
                if (response.body() != null) {
                    boolean showSmall = sharedPreferences.getBoolean("bl_small_pothole", false);
                    boolean showMedium = sharedPreferences.getBoolean("bl_medium_pothole", false);
                    boolean showLarge = sharedPreferences.getBoolean("bl_large_pothole", false);

                    for (PotholeResponse res : response.body()) {
                        if (res.getSeverity() == 1 && showSmall || res.getSeverity() == 2 && showMedium || res.getSeverity() == 3 && showLarge) {
                            potholeList.add(res);
                        }
                    }

                    Log.d("GT63_x", "potholeList = " + potholeList.size());
                    for (PotholeResponse pothole : potholeList) {
                        addMaker(pothole.getLat(), pothole.getLang());
                    }
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<List<PotholeResponse>> call, @NonNull Throwable t) {
                Log.d("GT63_x", "onFailure: " + t.getMessage());
            }
        });
    }

    private void addMaker(double lat, double lng) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.setTitle("Cảnh báo ổ gà");
        markerOptions.setSnippet("Vị trí ổ gà");
        markerOptions.setPosition(new LatLng(lat, lng));
        mapboxMap.addMarker(markerOptions);
    }

    // Hàm tính toán tọa độ trung điểm giữa hai điểm
    private LatLng getMidpoint(LatLng origin, LatLng destination) {
        double lat1 = Math.toRadians(origin.getLatitude());
        double lon1 = Math.toRadians(origin.getLongitude());
        double lat2 = Math.toRadians(destination.getLatitude());
        double lon2 = Math.toRadians(destination.getLongitude());

        double dLon = lon2 - lon1;

        // Tính toán điểm giữa
        double bx = Math.cos(lat2) * Math.cos(dLon);
        double by = Math.cos(lat2) * Math.sin(dLon);
        double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + bx) * (Math.cos(lat1) + bx) + by * by));
        double lon3 = lon1 + Math.atan2(by, Math.cos(lat1) + bx);

        // Chuyển đổi từ radian sang độ
        return new LatLng(Math.toDegrees(lat3), Math.toDegrees(lon3));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        Log.d("SensorEvent", "x: " + x + ", y: " + y + ", z: " + z);

        double acceleration = Math.sqrt(x * x + y * y + z * z);
        if (acceleration > 20) { // Threshold for detecting a drop
            Toast.makeText(this, "Phát hiện ổ gà. Chờ API lưu lat long ổ gà", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used
    }

    private void createNotificationChannel() {
        CharSequence name = "Drop Notification Channel";
        String description = "Channel for drop notifications";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        channel.setDescription(description);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupClearButton(final EditText editText) {
        final Drawable clearButton = ContextCompat.getDrawable(this, R.drawable.ic_clear);
        clearButton.setBounds(0, 0, clearButton.getIntrinsicWidth(), clearButton.getIntrinsicHeight());

        editText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getX() >= (editText.getRight() - clearButton.getBounds().width())) {
                    editText.setText("");
                    isShowingSuggestions = true;
                    return true;
                }
            }
            return false;
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    editText.setCompoundDrawables(null, null, clearButton, null);
                } else {
                    editText.setCompoundDrawables(null, null, null, null);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }


    private void fetchSuggestions(String query) {
        if (!isShowingSuggestions) {
            return;
        }
        if (query == null || query.trim().isEmpty()) {
            Toast.makeText(this, "Query cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Retrofit retrofit = new Retrofit.Builder().baseUrl("https://api.mapbox.com/").addConverterFactory(GsonConverterFactory.create()).build();

        MapboxGeocodingService service = retrofit.create(MapboxGeocodingService.class);
        Call<GeocodingResponse> call = service.getSuggestions(query, mapboxKey, true, "VN", 5);
        Log.d("MainActivity", "JSON Response: ");
        call.enqueue(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Feature> results = response.body().getFeatures();

                    // Kiểm tra xem 'results' có dữ liệu trước khi vòng lặp
                    if (results != null && !results.isEmpty()) {
                        // Xóa các gợi ý cũ khỏi danh sách
                        suggestionsList.clear();

                        // Thêm các địa điểm gợi ý mới vào 'suggestionsList'
                        for (Feature feature : results) {
                            LatLng latLng = new LatLng(feature.getGeometry().getCoordinates().get(1), feature.getGeometry().getCoordinates().get(0));
                            suggestionsList.add(new Suggestion(feature.getPlaceName(), latLng));
                        }

                        // Cập nhật adapter và hiển thị ListView
                        suggestionsAdapter.notifyDataSetChanged();
                        lvSuggestions.setVisibility(View.VISIBLE);
                    } else {
                        // Trường hợp không có kết quả
                        lvSuggestions.setVisibility(View.GONE);
//                        Toast.makeText(MainActivity.this, "No results found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    lvSuggestions.setVisibility(View.GONE);
//                    Toast.makeText(MainActivity.this, "No results found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable t) {
                lvSuggestions.setVisibility(View.GONE);
                Toast.makeText(DirectionActivity.this, "Error: : " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocationComponent();
            } else {
                Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void enableLocationComponent() {
        mapView.getMapAsync(map -> {
            map.setStyle(new Style.Builder().fromUri(styleUrl), style -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    LocationComponent locationComponent = map.getLocationComponent();
                    Log.d("GT45_x", "locationComponent: " + locationComponent);
                    LocationComponentActivationOptions locationComponentActivationOptions = LocationComponentActivationOptions.builder(this, style).build();
                    locationComponent.activateLocationComponent(locationComponentActivationOptions);
                    locationComponent.setLocationComponentEnabled(true);
                    locationComponent.setCameraMode(CameraMode.TRACKING);
                    locationComponent.setRenderMode(RenderMode.COMPASS);

                    Location lastKnownLocation = locationComponent.getLastKnownLocation();
                    if (lastKnownLocation != null) {
                        map.setCameraPosition(new CameraPosition.Builder().target(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude())).zoom(15.0).build());
                    }
                }
            });
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}