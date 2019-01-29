package com.licenta.tracker.activity;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;


public class OSM_MapActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private MapView map = null;
    private IMapController mapController;
    private MyLocationNewOverlay locationOverlay;
    private GeoPoint geoPoint;
    private TextView gpsLatitude;
    private TextView gpsLongitude;
    private TextView txtElapsedTime;
    private TextView txtGPSCurrentDistance;
    private Button btnStopActivity;
    private Button btnCapturePhoto;
    private boolean bound = false;
    private List<GeoPoint> geoPointsList = new ArrayList<>();
    private Polyline polyline = new Polyline();
    private LocationManager locationManager = null;
    private SQLiteHandler db;
    private JSONArray jsonArray = new JSONArray();
    private JSONArray imagesJsonArray = new JSONArray();
    private JSONObject jsonObject = new JSONObject();
    private Double currentSpeed = 0.0;
    private Double currentLatitude = 0.0;
    private Double currentLongitude = 0.0;
    private long startTime, timeInMilliseconds = 0;
    private Handler timerHandler = new Handler();
    private int imageContor = 0;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        geoPoint = new GeoPoint(0.0,0.0,0.0);

        //Context context = getApplicationContext();//load/initialize the osmdroid configuration,
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));//used for tiles
        setContentView(R.layout.activity_osm_map);
        gpsLatitude = (TextView)findViewById(R.id.gps_latitude);
        gpsLongitude = (TextView)findViewById(R.id.gps_longitude);
        txtGPSCurrentDistance = (TextView) findViewById(R.id.current_distance);
        txtElapsedTime = (TextView) findViewById(R.id.time_contor);
        btnStopActivity = (Button) findViewById(R.id.stop_activity);
        btnCapturePhoto = (Button) findViewById(R.id.take_a_photo);
        map = (MapView) findViewById(R.id.map);
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this),map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(15);
        locationOverlay.enableMyLocation();
        map.getOverlayManager().add(locationOverlay);
        startTimer();
        showGPSCoordinates();

        //Stop activity click event
        btnStopActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //In this event we want  to open a new activity that will contain all the data that has been tracked
                try {
                    jsonObject.put("total_distance", String.valueOf(mLocationServiceHandler.getDistanceContor()));
                    jsonObject.put("total_time", getFormatInMilliseconds(timeInMilliseconds));
                    jsonObject.put("avg_time", getAveragePeace(Integer.parseInt(String.valueOf(mLocationServiceHandler.getDistanceContor()).split("\\.")[0]), getSeconds(getFormatInMilliseconds(timeInMilliseconds))));
                    //jsonObject.put("avg_time", getAverageTime(4500,getSeconds("00:24:30"))); //example to check functions
                    jsonObject.put("gps_data", jsonArray);
                    jsonObject.put("images", imagesJsonArray);
                    Log.d("JSON", jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Intent startActivityIntent = new Intent(OSM_MapActivity.this, SaveTrackActivity.class);
                startActivityIntent.putExtra("JSONObjectWithGPSData", jsonObject.toString());
                startActivity(startActivityIntent);
                finish();
            }
        });

        /*If phone has no camera disable button*/
        if(hasCamera()){
            btnCapturePhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    capturePhoto();
                }
            });
        } else {
            btnCapturePhoto.setEnabled(false);
        }

    }

    private boolean hasCamera(){
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    private void capturePhoto(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // This method is called after the image was captured
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data!=null){
            //Get data from image + put encoded bitmap into JSON Array
            Bundle extras = data.getExtras();
            Bitmap bitmapImage = (Bitmap) extras.get("data");
            String encodedImage = bitmapToString(bitmapImage);
            String name = "image" + (imageContor);
            String latitudeName = "image" + (imageContor) + "lat";
            String longitudeName = "image" + (imageContor) + "long";
            imageContor++;
            JSONObject arrayElement = new JSONObject();
            try {
                arrayElement.put(name, encodedImage);
                arrayElement.put(latitudeName, getCurrentLatitude());
                arrayElement.put(longitudeName, getCurrentLongitude());
                imagesJsonArray.put(arrayElement);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private String bitmapToString(Bitmap bitmap){
        String result;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100,byteArrayOutputStream);
        byte[] b = byteArrayOutputStream.toByteArray();
        result = Base64.encodeToString(b, Base64.DEFAULT);
        return result;
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
                        if(geoPointsList.size() == 0) {//add first element to list
                            geoPointsList.add(geoPoint);
                            setCurrentSpeed(mLocationServiceHandler.getSpeed());
                            setCurrentLatitude(geoPoint.getLatitude());
                            setCurrentLongitude(geoPoint.getLongitude());
                            txtGPSCurrentDistance.setText(String.valueOf(mLocationServiceHandler.getDistanceContor()) + " m");
                            mapController.setCenter(geoPoint);
                            gpsLatitude.setText(String.valueOf(geoPoint.getLatitude()));
                            gpsLongitude.setText(String.valueOf(geoPoint.getLongitude()));
                            createJSON(geoPoint.getLatitude(), geoPoint.getLongitude(), geoPoint.getAltitude(), getCurrentSpeed(),getTimestamp("dd-MM-yyyy hh-mm-ss"));
                            //showLiveTrack(geoPointsList);
                        }else if(geoPointsList.size() > 0 && !(geoPointsList.get(geoPointsList.size()-1).equals(geoPoint))){//add geoPoint to list only if the current one is different than previous
                                geoPointsList.add(geoPoint);
                                setCurrentSpeed(mLocationServiceHandler.getSpeed());
                                txtGPSCurrentDistance.setText(String.valueOf(mLocationServiceHandler.getDistanceContor()) + " m");
                                mapController.setCenter(geoPoint);
                                gpsLatitude.setText(String.valueOf(geoPoint.getLatitude()));
                                gpsLongitude.setText(String.valueOf(geoPoint.getLongitude()));
                                createJSON(geoPoint.getLatitude(), geoPoint.getLongitude(), geoPoint.getAltitude(), getCurrentSpeed(),getTimestamp("dd-MM-yyyy hh-mm-ss"));
                                //showLiveTrack(geoPointsList);
                        }
                    }
                }
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
        }
    }

    public void startTimer(){
        startTime = SystemClock.uptimeMillis();
        timerHandler.postDelayed(timerThread, 0);
    }

    private Runnable timerThread = new Runnable() {
        @Override
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            txtElapsedTime.setText(getFormatInMilliseconds(timeInMilliseconds));
            timerHandler.postDelayed(this, 1000);
        }
    };

    public String getFormatInMilliseconds(long var){
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(var);
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

    public void setCurrentLatitude(Double latitude){this.currentLatitude = latitude;}

    public void setCurrentLongitude(Double longitude){this.currentLongitude = longitude;}

    public Double getCurrentLatitude() {return currentLatitude;}

    public Double getCurrentLongitude() {return currentLongitude;}

    private String getAveragePeace(int distanceInMeters, int timeInSeconds){
        if(distanceInMeters>0){
            double division = ((double)timeInSeconds)/distanceInMeters;
            String minutes = String.valueOf(((double)Integer.parseInt(String.valueOf(division).split("\\.")[1].substring(0,3)))/60).split("\\.")[0];
            String seconds  = String.valueOf(Integer.parseInt(String.valueOf(division).split("\\.")[1].substring(0,3)) - (Integer.parseInt(minutes) *60));

            return minutes + ":" + seconds;
        }
        else return (getFormatInMilliseconds(timeInMilliseconds));
    }

    private int getSeconds(String timer){
        int minutes = Integer.parseInt(timer.split(":")[1]);
        int seconds = Integer.parseInt(timer.split(":")[2]);
        return (minutes*60)+seconds;
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


}
