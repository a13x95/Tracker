package com.licenta.tracker.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.licenta.tracker.R;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;


public class OSM_MapActivity extends AppCompatActivity implements LocationListener {

    //Minimum distance for updates in meters
    private static final long MIN_DISTANCE_UPDATE = 10; //10m
    //Minimum time for location updates in milliseconds
    private static final long MIN_TIME_UPDATE = 5000; //5s

    private MapView map;
    private IMapController mapController;
    private LocationManager locationManager;
    private MyLocationNewOverlay locationOverlay;
    private boolean gps;
    private boolean network;
    private GeoPoint geoPoint;
    private TextView gpsLatitude;
    private TextView gpsLongitude;
    public Location location;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        geoPoint = new GeoPoint(0.0, 0.0);
        super.onCreate(savedInstanceState);
        //Handle permissions first, before map is created. not depicted here
        //load/initialize the osmdroid configuration, this can be done by ...
        Context context = getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string
        //inflate and create the map

        setContentView(R.layout.activity_osm_map);
        gpsLatitude = (TextView)findViewById(R.id.gps_latitude);
        gpsLongitude = (TextView)findViewById(R.id.gps_longitude);
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this),map);
        mapController = map.getController();
        mapController.setZoom(15);
        initLocationService(getApplicationContext());
    }

    /*
     * After the permissions are granted, method sets up the location
     * */
    private void initLocationService(Context context) {

        if(Build.VERSION.SDK_INT>=23 && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context,Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        try{
            if(locationManager == null){
                locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            }

            // get gps and network status
            gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            Log.v("TAG", "GPS:" + String.valueOf(gps) + "NET:" + String.valueOf(network));

            if(!gps && !network){
                //Location is not enabled
                Log.d("TAG:", "Location is not enabled");
                Toast.makeText(context,"Please enable GPS!",Toast.LENGTH_LONG).show();
            }else{
                if(network){
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_UPDATE,MIN_DISTANCE_UPDATE, this);
                    if(locationManager != null){
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    }
                }

                if(gps){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,MIN_TIME_UPDATE,MIN_DISTANCE_UPDATE, this);
                    if(locationManager != null){
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    }
                }
            }

        } catch (Exception exception){
            Log.d("ERROR:", "Error occurred in LocationService" + exception.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    protected void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on pause.
        map.onPause();
    }

    @Override
    public void onLocationChanged(Location location) {
        geoPoint.setCoords(location.getLatitude(),location.getLongitude());
        mapController.setCenter(geoPoint);
        locationOverlay.enableMyLocation();
        map.getOverlayManager().add(locationOverlay);
        gpsLatitude.setText("Latitude: " + location.getLatitude());
        gpsLongitude.setText("Longitude: " + location.getLongitude());
        Log.v("TAG", String.format("Location changed: Long:%f, Lat:%f",(float)location.getLongitude(),(float)location.getLatitude()));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.v("TAG","Status Changed");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.v("TAG",String.format("Provider enabled: %s",provider));
        //Toast.makeText(this, "Provider enabled", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.v("TAG",String.format("Provider disabled: %s",provider));
    }
}
