package com.licenta.tracker.helper;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import org.osmdroid.util.GeoPoint;

public class LocationServiceHandler extends Service {
    private static final String TAG = LocationServiceHandler.class.getSimpleName();
    private static final long MIN_DISTANCE_UPDATE = 0; //Minimum distance for updates in meters 0
    private static final long MIN_TIME_UPDATE = 5000; //Minimum time for location updates in milliseconds 5s
    GpsLocationBinder gpsLocationBinder = new GpsLocationBinder();
    LocationManager locationManager;
    LocationListener locationListener;
    boolean gps;
    boolean network;
    double latitude;
    double longitude;

    public LocationServiceHandler() {
        gps = false;
        network = false;
        latitude = 0.0;
        longitude = 0.0;
    }

    @Override
    public void onCreate() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.v("TAG","Status Changed");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.v("TAG",String.format("Provider enabled: %s",provider));
            }

            @Override
            public void onProviderDisabled(String provider) {
                gps = false;
                Log.v("TAG",String.format("Provider disabled: %s",provider));
            }
        };


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        }
        try {
            if (locationManager==null){
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            }
            // get gps and network status
            if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                gps = true;
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME_UPDATE,MIN_DISTANCE_UPDATE, locationListener);
                if(locationManager != null){
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
            }else{
                gps = false;
                Log.d("TAG:", "Location is not enabled");
            }

            if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                network = true;
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_UPDATE,MIN_DISTANCE_UPDATE, locationListener);
                if(locationManager != null){
                    locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }else{
                network = false;
                Log.d("TAG:", "Network is not enabled");
            }

            Log.v("TAG", "GPS:" + String.valueOf(gps) + "NET:" + String.valueOf(network));
//          Open the activity that lets you turn on the location without an alert dialog
//          Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//          startActivity(intent);
        }catch (Exception exception){
            Log.d("ERROR:", "Error occurred in LocationService" + exception.getMessage());
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return gpsLocationBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null){
            locationManager.removeUpdates(locationListener);
        }
    }

    public class GpsLocationBinder extends Binder {
        public LocationServiceHandler getBinder(){
            return LocationServiceHandler.this;
        }
    }

    public GeoPoint getLocation(){
        GeoPoint gpsCoordinates = new GeoPoint(0.0,0.0);
        gpsCoordinates.setCoords(latitude,longitude);
        return gpsCoordinates;
    }
    public boolean getGpsStatus(){return gps;}
    public boolean getNetworkStatus(){return network;}
}
