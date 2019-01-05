package com.licenta.tracker.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.licenta.tracker.R;
import com.licenta.tracker.helper.LocationServiceHandler;
import com.licenta.tracker.helper.SQLiteHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class OSM_MapActivity extends AppCompatActivity {
    private MapView map = null;
    private IMapController mapController;
    private MyLocationNewOverlay locationOverlay;
    private GeoPoint geoPoint;
    private TextView gpsLatitude;
    private TextView gpsLongitude;
    private boolean bound = false;
    private List<GeoPoint> geoPointsList = new ArrayList<>();
    private Polyline polyline = new Polyline();
    private LocationManager locationManager = null;
    private SQLiteHandler db;
    private JSONArray jsonArray = new JSONArray();
    private JSONObject jsonObject = new JSONObject();
    private Double currentSpeed = 0.0;

    LocationServiceHandler mLocationServiceHandler = new LocationServiceHandler();

    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationServiceHandler.GpsLocationBinder gpsLocationBinder = (LocationServiceHandler.GpsLocationBinder)service;
            mLocationServiceHandler = gpsLocationBinder.getBinder();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        db = new SQLiteHandler(getApplicationContext());
        try {
            jsonObject.put("activity_name", getTimestamp("EEEE dd-MM-yyyy") + " track"); // Track name
            jsonObject.put("user_id", db.getUserDetails().get("uid"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(locationManager == null){
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            showSetingsAllert();
        }
        Intent intent = new Intent(this, LocationServiceHandler.class);
        bindService(intent,mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound){
            unbindService(mServiceConnection);
            bound = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    protected void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on pause.
        map.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        geoPoint = new GeoPoint(0.0, 0.0);

        //Context context = getApplicationContext();//load/initialize the osmdroid configuration,
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));//used for tiles
        setContentView(R.layout.activity_osm_map);
        gpsLatitude = (TextView)findViewById(R.id.gps_latitude);
        gpsLongitude = (TextView)findViewById(R.id.gps_longitude);
        map = (MapView) findViewById(R.id.map);
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this),map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(15);
        locationOverlay.enableMyLocation();
        map.getOverlayManager().add(locationOverlay);
        showGPSCoordinates();
    }

    private void showGPSCoordinates() {
        /*
        * This method will get the gps data to be performed on a different thread
        * */

        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(mLocationServiceHandler !=null){
                    geoPoint = mLocationServiceHandler.getLocation();
                    if(geoPoint.getLatitude() != 0.0 && geoPoint.getLongitude() != 0.0){
                        geoPointsList.add(geoPoint);
                        setCurrentSpeed(mLocationServiceHandler.getSpeed());
                    }
                }
                mapController.setCenter(geoPoint);
                gpsLatitude.setText(String.valueOf(geoPoint.getLatitude()));
                gpsLongitude.setText(String.valueOf(geoPoint.getLongitude()));
                createJSON(geoPoint.getLatitude(), geoPoint.getLongitude(), geoPoint.getAltitude(), getCurrentSpeed(),getTimestamp("dd-MM-yyyy hh-mm-ss"));
                //showLiveTrack(geoPointsList);
                handler.postDelayed(this, 1000);
            }
        });
    }

    private void showLiveTrack (List<GeoPoint> mGeoPointsList){
        polyline.setPoints(mGeoPointsList);
        polyline.setOnClickListener(new Polyline.OnClickListener() {
            @Override
            public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos) {
                Toast.makeText(mapView.getContext(), "polyline with" +  + polyline.getPoints().size() + " pts was tapped", Toast.LENGTH_LONG).show();
                return false;
            }
        });
        map.getOverlayManager().add(polyline);
    }

    public void showSetingsAllert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("GPS is not enabled!");
        alertDialog.setMessage("App needs GPS to be enabled, do you want to turn on GPS?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.show();
    }

    public void createJSON(double mLatitude, double mLongitude, double mAltitude, double speed, String timestamp){
        if(mLatitude != 0.0 & mLongitude != 0.0){
            JSONObject arrayElement = new JSONObject();
            try {
                arrayElement.put("latitude", mLatitude);
                arrayElement.put("longitude", mLongitude);
                arrayElement.put("altitude", mAltitude);
                arrayElement.put("speed", speed);
                arrayElement.put("timestamp", timestamp);
                jsonArray.put(arrayElement);
            } catch (JSONException e) {
                e.printStackTrace();
            }

//            try {
//                jsonObject.put("gps_data", jsonArray);
//                Log.d("JSON", jsonObject.toString());
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
        }
    }

    public String getTimestamp(String format) {
        Calendar calendar;
        SimpleDateFormat dateFormat;
        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(calendar.getTime());
    }

    public Double getCurrentSpeed() {
        return currentSpeed;
    }

    public void setCurrentSpeed(Double currentSpeed) {
        this.currentSpeed = currentSpeed;
    }

}