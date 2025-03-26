package com.example.projectnt118.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GraphHopperService {
    @GET("route")
    Call<RouteResponse> getRoute(
            @Query("point") String startPoint, // Ví dụ "10.8231,106.6297"
            @Query("point") String endPoint,   // Ví dụ "10.7769,106.7009"
            @Query("vehicle") String vehicle,  // Loại phương tiện, ví dụ "car"
            @Query("locale") String locale,    // Ngôn ngữ, ví dụ "en"
            @Query("key") String apiKey        // API key của GraphHopper
    );
}