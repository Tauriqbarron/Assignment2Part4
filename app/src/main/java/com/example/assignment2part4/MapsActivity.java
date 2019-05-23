package com.example.assignment2part4;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Button;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Button btnGetWebcams;
    private FusedLocationProviderClient fClient;
    private int requestLocation = 99;
    private String TAG = "Map App";
    private Double currentLat,currentLng;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getWebcams(this);
    }
    public void getWebcams(Context context){
        final RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        btnGetWebcams = findViewById(R.id.btnGetWebcams);
        fClient = LocationServices.getFusedLocationProviderClient(context);
        if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MapsActivity.this,new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION},requestLocation);
        }
        try{
            final Task<Location> currentLocation =fClient.getLastLocation();
            currentLocation.addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    currentLat = currentLocation.getResult().getLatitude();
                    currentLng = currentLocation.getResult().getLatitude();
                }
            });
        }catch(SecurityException e){
            Log.e(TAG,"Get Location: Security Exception: "+e.getMessage());
        }
        String url = Uri.parse("https://wordsapiv1.p.mashape.com/words/"+wordSearch+
                "/definitions").buildUpon().toString();
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}
