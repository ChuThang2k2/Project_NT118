package com.example.projectnt118.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.projectnt118.DirectionActivity;
import com.example.projectnt118.R;
import com.example.projectnt118.api.ApiService;
import com.example.projectnt118.api.RetrofitClient;
import com.example.projectnt118.modle.PotholeResponse;
import com.example.projectnt118.network.Suggestion;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.mapbox.bindgen.Expected;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.EdgeInsets;
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor;
import com.mapbox.maps.plugin.animation.MapAnimationOptions;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.mapbox.navigation.core.MapboxNavigation;
import com.mapbox.navigation.core.directions.session.RoutesObserver;
import com.mapbox.navigation.core.directions.session.RoutesUpdatedResult;
import com.mapbox.navigation.ui.base.util.MapboxNavigationConsumer;
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider;
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi;
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView;
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineError;
import com.mapbox.navigation.ui.maps.route.line.model.RouteLineResources;
import com.mapbox.navigation.ui.maps.route.line.model.RouteSetValue;
import com.mapbox.search.SearchEngine;
import com.mapbox.search.autocomplete.PlaceAutocomplete;
import com.mapbox.search.ui.view.SearchResultsView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;

public class MapFragment extends Fragment {
    private final NavigationLocationProvider navigationLocationProvider = new NavigationLocationProvider();
    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean result) {
            if (result) {
                // Toast.makeText(getContext(), "Permission granted", Toast.LENGTH_SHORT).show();
                enableLocationComponent();
            } else {
                Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    });
    boolean focusLocation = true;
    //    private SearchEngineUiAdapter searchEngineUiAdapter;
    private PlaceAutocomplete placeAutocomplete;
    private ListView searchResultsListView;
    private MapView mapView;
    private MapboxMap mapboxMap;

    private FusedLocationProviderClient fusedLocationClient;
    private LatLng originLatLng;

    private TextInputEditText searchBar;
    private FloatingActionButton focusLocationButton, zoomInButton, zoomOutButton, navigationButton, soundButton;
    private SearchResultsView searchResultsView;
    private MapboxRouteLineApi mapboxRouteLineApi;
    public RoutesObserver routesObserver = new RoutesObserver() {
        @Override
        public void onRoutesChanged(@NonNull RoutesUpdatedResult routesUpdatedResult) {
            mapboxRouteLineApi.setNavigationRoutes(routesUpdatedResult.getNavigationRoutes(), new MapboxNavigationConsumer<Expected<RouteLineError, RouteSetValue>>() {
                @Override
                public void accept(Expected<RouteLineError, RouteSetValue> routeLineErrorRouteSetValueExpected) {

                }
            });
        }
    };
    private SearchEngine searchEngine;
    private MapboxRouteLineView mapboxRouteLineView;
    private MapboxNavigation mapboxNavigation;
    private TextInputEditText searchET;
    private List<Suggestion> searchResults;
    private CustomAdapter adapter;

    private SharedPreferences sharedPreferences;

    String mapId = "streets-v2";
    // Get the API Key by app's BuildConfig
    String key = "OKRTVHPBV2gz7AUYTtTT";
    String apiKey = "2290ccec-c3da-4649-ab99-6844a908759d";
    String mapboxKey = "sk.eyJ1IjoibWFpdGhpaGlldSIsImEiOiJjbTMweWpkajgwbzh1MmpzNzFudGg0aHRtIn0.gf_3XXj87bvRYpnvzV5qiQ";
    String styleUrl = "https://api.maptiler.com/maps/" + mapId + "/style.json?key=" + key;

    private void updateCamera(Point point, double bearing) {
        MapAnimationOptions animationOptions = new MapAnimationOptions.Builder().duration(1500L).build();
        CameraOptions options = new CameraOptions.Builder().center(point).zoom(18.0).bearing(bearing).pitch(45.0).padding(new EdgeInsets(1000.0, 0.0, 0.0, 0.0)).build();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        sharedPreferences = requireContext().getSharedPreferences("PotholeSettings", Context.MODE_PRIVATE);
        searchET = view.findViewById(R.id.searchET);
        searchResultsListView = view.findViewById(R.id.searchResultsListView);

        searchResults = new ArrayList<>();
        adapter = new CustomAdapter(requireContext(), searchResults);
        adapter.setListener(suggestion -> {
            searchResultsListView.setVisibility(View.GONE);
            mapboxMap.clear();
            addMaker(suggestion.getLatLng().getLatitude(), suggestion.getLatLng().getLongitude(), suggestion.getPlaceName(), "Địa chỉ tìm kiếm");

            mapboxMap.setCameraPosition(new CameraPosition.Builder().target(new LatLng(suggestion.getLatLng().getLatitude(), suggestion.getLatLng().getLongitude())).zoom(15.0).build());
        });
        searchResultsListView.setAdapter(adapter);

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Call API to get search results
                searchResultsListView.setVisibility(View.VISIBLE);
                fetchSearchResults(s.toString());

//                // clear maker
//                mapboxMap.clear();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return view;
    }

    private OkHttpClient client = new OkHttpClient();
    private Call realCall;

    private void fetchSearchResults(String query) {
        searchResults.clear();
        if (!query.isEmpty()) {
            String accessToken = getString(R.string.mapbox_access_token);
            String url = "https://api.mapbox.com/geocoding/v5/mapbox.places/" + query + ".json?access_token=" + accessToken + "&bbox=102.14441,8.17966,109.46463,23.39339";

            if (realCall != null) {
                realCall.cancel();
            }
            Request request = new Request.Builder().url(url).build();
            realCall = client.newCall(request);
            realCall.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseData = response.body().string();
                        try {
                            JSONObject json = new JSONObject(responseData);
                            JSONArray features = json.getJSONArray("features");

                            for (int i = 0; i < features.length(); i++) {
                                JSONObject feature = features.getJSONObject(i);
                                String address = feature.getString("place_name");
                                JSONObject geometry = feature.getJSONObject("geometry");
                                JSONArray coordinates = geometry.getJSONArray("coordinates");
                                double longitude = coordinates.getDouble(0);
                                double latitude = coordinates.getDouble(1);

                                searchResults.add(new Suggestion(address, new LatLng(latitude, longitude)));
                            }

                            Log.d("GT45_x", "fetchSearchResults: key = " + query + " result = " + searchResults.size());
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.notifyDataSetChanged();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } else {
            searchResultsListView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(map -> {
            // Gán đối tượng MapboxMap vào biến toàn cục
            mapboxMap = map;

            // Thiết lập Style cho MapboxMap
            mapboxMap.setStyle(new Style.Builder().fromUri(styleUrl), style -> {
                // Thực hiện các thao tác khác sau khi tải Style thành công
            });
            loadPotholes();
            // add marker
            // addMaker(20.087424, 105.859294, "Cảnh báo ổ gà", "Vị trí ổ gà");
            // addMaker(21.087424, 102.859294, "Cảnh báo ổ gà", "Vị trí ổ gà");
        });

        focusLocationButton = view.findViewById(R.id.my_location);
        zoomInButton = view.findViewById(R.id.zoom_in);
        zoomOutButton = view.findViewById(R.id.zoom_out);
        searchBar = view.findViewById(R.id.searchET);
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
        navigationButton = view.findViewById(R.id.navigation);
        String accessToken = getString(R.string.mapbox_access_token);

        MapboxRouteLineOptions options = new MapboxRouteLineOptions.Builder(requireContext()).withRouteLineResources(new RouteLineResources.Builder().build()).withRouteLineBelowLayerId("road-label").build();
        mapboxRouteLineApi = new MapboxRouteLineApi(options);
        mapboxRouteLineView = new MapboxRouteLineView(options);

        if (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            // activityResultLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION);
        } else {
            getLastLocation();
            enableLocationComponent();
        }
        navigationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DirectionActivity.class);
                startActivity(intent);
            }
        });
        focusLocationButton.setOnClickListener(v -> {
            enableLocationComponent();
        });
    }

    private final ArrayList<PotholeResponse> potholeList = new ArrayList<>();

    public void loadPotholes() {
        if (mapboxMap != null) {
            mapboxMap.clear();
        }
        potholeList.clear();

        Retrofit retrofit = RetrofitClient.getLocalClient();
        ApiService apiService = retrofit.create(ApiService.class);

        apiService.getPotholes().enqueue(new retrofit2.Callback<List<PotholeResponse>>() {
            @Override
            public void onResponse(@NonNull retrofit2.Call<List<PotholeResponse>> call, @NonNull retrofit2.Response<List<PotholeResponse>> response) {
                if (response.body() != null) {
                    potholeList.addAll(response.body());
                    Log.d("GT63_x", "potholeList = " + potholeList.size());
                    boolean showSmall = sharedPreferences.getBoolean("bl_small_pothole", false);
                    boolean showMedium = sharedPreferences.getBoolean("bl_medium_pothole", false);
                    boolean showLarge = sharedPreferences.getBoolean("bl_large_pothole", false);

                    // lọc
                    for (PotholeResponse pothole : potholeList) {
                        if (pothole.getSeverity() == 1 && showSmall || pothole.getSeverity() == 2 && showMedium || pothole.getSeverity() == 3 && showLarge) {
                            addMaker(pothole.getLat(), pothole.getLang(), "Cảnh báo ổ gà", "Vị trí ổ gà");
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull retrofit2.Call<List<PotholeResponse>> call, @NonNull Throwable t) {
                Log.d("GT63_x", "onFailure: " + t.getMessage());
            }
        });
    }

    private void addMaker(double lat, double lng, String title, String snippet) {
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.setTitle(title);
        markerOptions.setSnippet(snippet);
        markerOptions.setPosition(new LatLng(lat, lng));
        mapboxMap.addMarker(markerOptions);
    }

    private void addRandomLocationAndNavigate() {
        double randomLat = 38.9072 + (Math.random() - 0.5) * 0.1;
        double randomLng = -77.0369 + (Math.random() - 0.5) * 0.1;
        Point randomPoint = Point.fromLngLat(randomLng, randomLat);

//        AnnotationPlugin annotationPlugin = AnnotationPluginImplKt.getAnnotations(mapView);
//        PointAnnotationManager pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(AnnotationConfig);
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 4;
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.location, options);

//        if (bitmap != null) {
////            PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions().withPoint(randomPoint).withIconImage(bitmap);
////            pointAnnotationManager.create(pointAnnotationOptions);
//
//            // Update camera view to the random location
//            CameraOptions cameraOptions = new CameraOptions.Builder().center(randomPoint).zoom(15.0).build();
//            mapView.getMapboxMap().setCamera(cameraOptions);
//
//            // Show detailed information about the location
//            String locationInfo = "Latitude: " + randomLat + "\nLongitude: " + randomLng;
//            Toast.makeText(getContext(), locationInfo, Toast.LENGTH_LONG).show();
//
//            fetchRoute(randomPoint);
//        } else {
//            Toast.makeText(getContext(), "Failed to load icon image", Toast.LENGTH_SHORT).show();
//        }
//        addMarkerAnnotation(pointAnnotationManager, R.drawable.location);
    }

    private void setupSearchView() {
    }

    @SuppressLint("MissingPermission")
    private void fetchRoute(Point destination) {
        Location lastLocation = navigationLocationProvider.getLastLocation();
        if (lastLocation == null) {
            Toast.makeText(getContext(), "Current location not available", Toast.LENGTH_SHORT).show();
        }

//        Point origin = Point.fromLngLat(lastLocation.getLongitude(), lastLocation.getLatitude());
//        RouteOptions routeOptions = RouteOptions.builder()
//                .coordinatesList(Arrays.asList(origin, destination))
//                .profile(DirectionsCriteria.PROFILE_DRIVING)
//                .build();

//        mapboxNavigation.requestRoutes(routeOptions, new NavigationRouterCallback() {
//        });
    }

    @SuppressLint("Lifecycle")
    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }
//    private void showToast(String message) {
//        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
//    }
//    private void addImageToStyle(@NonNull Style style, @NonNull String imageId) {
//        Bitmap iconBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.baseline_location_on_24);
//        style.addImage(imageId, iconBitmap);
//    }

    private void addMarkerAnnotation(@NonNull PointAnnotationManager pointAnnotationManager,
                                     @NonNull String imageId) {
        PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
                .withPoint(Point.fromLngLat(20.0, 20.0))
                .withIconImage(imageId)
                .withIconAnchor(IconAnchor.BOTTOM);
        pointAnnotationManager.create(pointAnnotationOptions);
    }

    @SuppressLint("Lifecycle")
    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @SuppressLint("Lifecycle")
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
//        mapboxNavigation.unregisterRoutesObserver(routesObserver);
//        mapboxNavigation.unregisterLocationObserver(locationObserver);
    }

    @SuppressLint("Lifecycle")
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                originLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                Log.d("GT63_x", "lati = " + originLatLng.getLatitude() + "; long = " + originLatLng.getLongitude());
            } else {
                Log.e("Error_Map", "Không thể lấy vị trí hiện tại");
            }
        });
    }

    private void enableLocationComponent() {
        mapView.getMapAsync(map -> {
            map.setStyle(new com.mapbox.mapboxsdk.maps.Style.Builder().fromUri(styleUrl), style -> {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    LocationComponent locationComponent = map.getLocationComponent();
                    LocationComponentActivationOptions locationComponentActivationOptions = LocationComponentActivationOptions.builder(requireContext(), style).build();
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

}