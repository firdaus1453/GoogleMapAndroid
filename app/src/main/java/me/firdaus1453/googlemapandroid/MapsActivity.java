package me.firdaus1453.googlemapandroid;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.system.ErrnoException;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.OnStreetViewPanoramaReadyCallback;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.SupportStreetViewPanoramaFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import me.firdaus1453.googlemapandroid.databinding.ActivityMapsBinding;
import me.firdaus1453.googlemapandroid.helper.DirectionMapsV2;
import me.firdaus1453.googlemapandroid.helper.GPStrack;
import me.firdaus1453.googlemapandroid.helper.HeroHelper;
import me.firdaus1453.googlemapandroid.model.Distance;
import me.firdaus1453.googlemapandroid.model.Duration;
import me.firdaus1453.googlemapandroid.model.LegsItem;
import me.firdaus1453.googlemapandroid.model.OverviewPolyline;
import me.firdaus1453.googlemapandroid.model.ResponseWaypoint;
import me.firdaus1453.googlemapandroid.model.RoutesItem;
import me.firdaus1453.googlemapandroid.network.InitRetrofit;
import me.firdaus1453.googlemapandroid.network.RestApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {

    private GoogleMap mMap;
    // TODO 1 Membuat Binding
    ActivityMapsBinding mapsBinding;
    private GoogleApiClient googleApiClient;
    private GPStrack gpstrack;
    private double lat;
    private double lon;
    private double lat2;
    private double lon2;
    private String name_location;
    private LatLng lokasiku;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO 2 Menginisiasi binding ke mainactivity dengan layout
        mapsBinding = DataBindingUtil.setContentView(this,R.layout.activity_maps);

        cekstatusgps();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // TODO 3 Menginisiasi widget yg ada di layout
        mapsBinding.btnlokasiku.setOnClickListener(this);
        mapsBinding.btnpanorama.setOnClickListener(this);
        mapsBinding.edtawal.setOnClickListener(this);
        mapsBinding.edtakhir.setOnClickListener(this);
    }

    private void cekstatusgps() {
        // TODO 4 cek status gps
        final LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
            Toast.makeText(this, "Gps already enabled", Toast.LENGTH_SHORT).show();
            //     finish();
        }
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ) {
            Toast.makeText(this, "Gps not enabled", Toast.LENGTH_SHORT).show();
            enableLoc();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    // TODO 5 untuk menampilkan popup GPS sudah aktif
    private void enableLoc() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {

                        }

                        @Override
                        public void onConnectionSuspended(int i) {
                            googleApiClient.connect();
                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                            Log.d("Location error", "Location error " + connectionResult.getErrorCode());
                        }
                    }).build();
            googleApiClient.connect();

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(locationRequest);

            builder.setAlwaysShow(true);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(MapsActivity.this, MyConstants.REQ_REQUEST);

                                finish();
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                    }
                }
            });
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                android.Manifest.permission.ACCESS_FINE_LOCATION},
                        110);


            }
            return;
        }

        // method untuk akses lokasi
        akseslokasiku();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnlokasiku:

                // method untuk akses lokasi
                akseslokasiku();

                break;
            case R.id.btnpanorama:
                mapsBinding.relativemap.setVisibility(View.GONE);
                mapsBinding.frame1.setVisibility(View.VISIBLE);

                SupportStreetViewPanoramaFragment panoramaFragment = (SupportStreetViewPanoramaFragment)getSupportFragmentManager().findFragmentById(R.id.panorama);
                panoramaFragment.getStreetViewPanoramaAsync(new OnStreetViewPanoramaReadyCallback() {
                    @Override
                    public void onStreetViewPanoramaReady(StreetViewPanorama streetViewPanorama) {
                        streetViewPanorama.setPosition(lokasiku);
                    }
                });
                break;
            case R.id.edtawal:
                try {
                    intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(MapsActivity.this);
                    startActivityForResult(intent,MyConstants.REQAWAL);
                }catch (GooglePlayServicesNotAvailableException e){
                    e.printStackTrace();
                }catch (GooglePlayServicesRepairableException e){
                    e.printStackTrace();
                }
                break;
            case R.id.edtakhir:
                try {
                    intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY).build(MapsActivity.this);
                    startActivityForResult(intent,MyConstants.REQAKHIR);
                }catch (GooglePlayServicesNotAvailableException e){
                    e.printStackTrace();
                }catch (GooglePlayServicesRepairableException e){
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Place place = PlaceAutocomplete.getPlace(this, data);

        if (requestCode==MyConstants.REQAWAL && resultCode==RESULT_OK){
            lat  = place.getLatLng().latitude;
            lon = place.getLatLng().longitude;
            lat2  = place.getLatLng().latitude;
            lon2 = place.getLatLng().longitude;
            name_location = place.getName().toString();
            mapsBinding.edtawal.setText(name_location);
            addMarker(lat,lon);
            mMap.clear();
        }else if (requestCode==MyConstants.REQAKHIR && resultCode==RESULT_OK){
            lat  = place.getLatLng().latitude;
            lon = place.getLatLng().longitude;
            name_location = place.getName().toString();
            mapsBinding.edtakhir.setText(name_location);
            mMap.clear();
            addMarker(lat2,lon2);
            addMarker(lat,lon);
            aksesrute();
        }
    }

    private void aksesrute() {

        RestApi api = InitRetrofit.getInstanceRetrofit();
        Call<ResponseWaypoint> waypointCall = api.WAYPOINT_CALL(
                mapsBinding.edtawal.getText().toString(),
                mapsBinding.edtakhir.getText().toString()
        );
        waypointCall.enqueue(new Callback<ResponseWaypoint>() {
            @Override
            public void onResponse(Call<ResponseWaypoint> call, Response<ResponseWaypoint> response) {
                String status = response.body().getStatus();
                if (status.equals("OK")){
                    List<RoutesItem> routes = response.body().getRoutes();
                    List<LegsItem> legsItems = routes.get(0).getLegs();
                    Distance distance = legsItems.get(0).getDistance();
                    Duration duration = legsItems.get(0).getDuration();
                    String jarak = distance.getText();
                    String durasi = duration.getText();
                    mapsBinding.textjarak.setText(jarak);
                    mapsBinding.textwaktu.setText(durasi);
                    double nilaijarak = Double.valueOf(distance.getValue());
                    double harga = Math.ceil(nilaijarak/1000);
                    double total = harga * 1000;
                    mapsBinding.textharga.setText(HeroHelper.toRupiahFormat2(String.valueOf(total)));
                    DirectionMapsV2 mapsV2 = new DirectionMapsV2(MapsActivity.this);
                    OverviewPolyline overviewPolyline = routes.get(0).getOverviewPolyline();
                    String point = overviewPolyline.getPoints();
                    mapsV2.gambarRoute(mMap,point);
                }
            }

            @Override
            public void onFailure(Call<ResponseWaypoint> call, Throwable t) {

            }
        });


    }

    private void addMarker(double lat, double lon) {
        lokasiku = new LatLng(lat,lon);
        name_location = convertlocation(lat,lon);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lokasiku,18));
        mMap.addMarker(new MarkerOptions().position(lokasiku).title(name_location).icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker)));
    }

    private void akseslokasiku() {
        gpstrack = new GPStrack(MapsActivity.this);
        if (gpstrack.canGetLocation() && mMap!=null){
            lat = gpstrack.getLatitude();
            lon = gpstrack.getLongitude();
            mMap.clear();
            name_location = convertlocation(lat,lon);

            Toast.makeText(this, "lat : "+lat+"\n lon :"+lon,Toast.LENGTH_SHORT).show();
            lokasiku = new LatLng(lat,lon);
            mMap.addMarker(new MarkerOptions().position(lokasiku).title(name_location).icon(
                    BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker)
            ));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lokasiku,18));
            // Untuk menampilkan kompas
            mMap.getUiSettings().setCompassEnabled(true);
            // Untuk menampilkan zoom
            mMap.getUiSettings().setZoomControlsEnabled(true);
            // Untuk menampilkan my location
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    private String convertlocation(double lat, double lon) {
        name_location = null;
        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
        try {
            List<Address> list = geocoder.getFromLocation(lat, lon, 1);
            if (list != null && list.size() > 0) {
                name_location = list.get(0).getAddressLine(0) + "" + list.get(0).getCountryName();

                //fetch data from addresses
            } else {
                Toast.makeText(this, "kosong", Toast.LENGTH_SHORT).show();
                //display Toast message
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return name_location;
    }
}
