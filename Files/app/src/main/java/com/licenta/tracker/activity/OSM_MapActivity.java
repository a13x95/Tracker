package com.licenta.tracker.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import com.licenta.tracker.R;
import com.licenta.tracker.helper.LocationServiceHandler;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;


public class OSM_MapActivity extends AppCompatActivity {
    private MapView map;
    private IMapController mapController;
    private MyLocationNewOverlay locationOverlay;
    private GeoPoint geoPoint;
    private TextView gpsLatitude;
    private TextView gpsLongitude;
    private boolean bound = false;
    private Boolean locationServiceStatus = false;

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
        Context context = getApplicationContext();//load/initialize the osmdroid configuration,
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));//used for tiles
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

        if(!mLocationServiceHandler.getGpsStatus() && !mLocationServiceHandler.getNetworkStatus()){
            showSetingsAllert();
        }
        showGPSCoordinates();
    }

    private void showGPSCoordinates() {
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if(mLocationServiceHandler !=null){
                    geoPoint = mLocationServiceHandler.getLocation();
                }
                mapController.setCenter(geoPoint);
                gpsLatitude.setText(String.valueOf(geoPoint.getLatitude()));
                gpsLongitude.setText(String.valueOf(geoPoint.getLongitude()));
                handler.postDelayed(this, 1000);
            }
        });
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
}
