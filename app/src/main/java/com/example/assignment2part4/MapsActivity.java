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
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
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
    //vars
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
        fClient = LocationServices.getFusedLocationProviderClient(this);
        getWebcams(this);
        // This Location request checks and updates the current location every 2 seconds. and assigns
        // the current latitude and longitude to two variables.
        LocationRequest req = new LocationRequest();
        req.setInterval(2000);
        req.setFastestInterval(500);
        req.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        try{
            fClient.requestLocationUpdates(req,new LocationCallback(){
                @Override
                public void onLocationResult(LocationResult locationResult) {
                        currentLat = locationResult.getLastLocation().getLatitude();
                        currentLng = locationResult.getLastLocation().getLongitude();
                }
            },null);
        }catch(SecurityException e){
            Log.e("errror",e.toString());
        }


    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
    public void getWebcams(final Context context){
        // this statement check if the app has permission to use location services
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION},requestLocation);
        }
        btnGetWebcams = findViewById(R.id.btnGetWebcams);
        // This method sends a string request to the webcams travel api using the current location as
        // part of the request and displays webcam locations with markers around the current location
        // within a 20 km radius.
        btnGetWebcams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng moveCam = new LatLng(currentLat,currentLng);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(moveCam,12));
                Toast.makeText(MapsActivity.this,"Assigning LatLng to Url",
                        Toast.LENGTH_LONG).show();
                String strLat = Double.toString(currentLat) ;
                String strlng = Double.toString(currentLng);
                final RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                String url = Uri.parse("https://webcamstravel.p.mashape.com/webcams/list/nearby="+strLat+","
                        +strlng+",20?show=webcams:location").buildUpon().toString();
                StringRequest stRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String resp = response;
                        try{
                            JsonObject respJson = jsonFromString(resp);
                            JsonObject respJsonResults = respJson.getJsonObject("result");
                            JsonArray respJsonArray = respJsonResults.getJsonArray("webcams");
                            for (int index = 0; index < (respJsonArray.size()-1);index++){
                                JsonObject webcam = respJsonArray.getJsonObject(index);
                                JsonObject curLocation = webcam.getJsonObject("location");
                                Double lat = curLocation.getJsonNumber("latitude").doubleValue();
                                Double lng = curLocation.getJsonNumber("longitude").doubleValue();
                                LatLng curmarker = new LatLng(lat,lng);
                                mMap.addMarker(new MarkerOptions().position(curmarker));
                            }
                        }catch (Throwable t){
                            Log.e("Error","Could not parse");
                        }
                        System.err.println(response);
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
        });
    }
    // this method takes in  the string responce from webcamstrvel api and converts it to a Json
    // object.
    private static JsonObject jsonFromString(String jsonObjectStr) {
        JsonReader jsonReader = Json.createReader(new StringReader(jsonObjectStr));
        JsonObject object = jsonReader.readObject();
        jsonReader.close();
        return object;
    }
}
