package com.example.projectnt118.network;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MapboxGeocodingService {
    @GET("geocoding/v5/mapbox.places/{query}.json")
    Call<GeocodingResponse> getSuggestions(
            @Path("query") String query,
            @Query("access_token") String accessToken,
            @Query("autocomplete") boolean autocomplete,
            @Query("country") String country,
            @Query("limit") int limit
    );
}