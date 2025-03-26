package com.example.projectnt118.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MapboxNameService {
    @GET("geocoding/v5/mapbox.places/{longitude},{latitude}.json")
    Call<GeocodingMapboxResponse> getPlaceName(
            @Query("longitude") double longitude,
            @Query("latitude") double latitude,
            @Query("access_token") String accessToken);
}