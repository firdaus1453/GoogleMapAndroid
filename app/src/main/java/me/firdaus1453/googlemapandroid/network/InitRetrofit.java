package me.firdaus1453.googlemapandroid.network;

import me.firdaus1453.googlemapandroid.MyConstants;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class InitRetrofit {
//todo 1 inisialisasi retrofit
    private static Retrofit getRetrofit(){
        Retrofit r = new Retrofit.Builder().baseUrl(MyConstants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        return r;
    }
//todo 2 bikin instance retrofit berdasarkan interface
    public static RestApi getInstanceRetrofit(){
        return getRetrofit().create(RestApi.class);
    }

}
