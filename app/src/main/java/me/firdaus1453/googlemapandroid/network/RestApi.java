package me.firdaus1453.googlemapandroid.network;


import me.firdaus1453.googlemapandroid.model.ResponseWaypoint;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RestApi {

    @GET("json")
    Call<ResponseWaypoint> WAYPOINT_CALL
        (@Query("origin") String asal,
        @Query("destination") String tujuan);
}
