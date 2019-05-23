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

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
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

import org.json.JSONObject;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

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
        String url = Uri.parse("https://webcams/list/nearby="+currentLat+","
                +currentLng+",60").buildUpon().toString();
        StringRequest stRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String resp = response;
                try{
                    JsonObject respJson = jsonFromString(resp);
                    JsonObject respJsonResults = respJson.getJsonObject("results");
                    JsonArray respJsonArray = respJsonResults.getJsonArray("webcams");
                    for (int index = 0; index < (respJsonArray.size()-1);index++){
                        JsonObject webcam = respJsonArray.getJsonObject(index);
                        JsonObject locations = webcam.getJsonObject("location");
                        JsonObject lat = locations.getJsonObject("latitude");
                        JsonObject lng = locations.getJsonObject("longitude");
                    }
                }catch (Throwable t){
                    Log.e("Error","Could not parse");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error",error.toString());
            }
        }){
            @Override
            public Map<String,String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("X-Mashape-Key","dd1d560155msh2a3a50aa39ffde2p13e582jsne6443e91eb58");
                params.put("Accept","text/plain");
                return params;
            }

        };
        queue.add(stRequest);
    }
    private static JsonObject jsonFromString(String jsonObjectStr) {
        JsonReader jsonReader = Json.createReader(new StringReader(jsonObjectStr));
        JsonObject object = jsonReader.readObject();
        jsonReader.close();
        return object;
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}
