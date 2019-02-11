package com.licenta.tracker.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;

import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.licenta.tracker.MainActivity;
import com.licenta.tracker.R;
import com.licenta.tracker.app.AppConfig;
import com.licenta.tracker.app.AppController;
import com.licenta.tracker.helper.ImageAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SaveTrackActivity extends AppCompatActivity {

    private static final String TAG = SaveTrackActivity.class.getSimpleName();

    private JSONObject jsonGpsDataReceived;
    private Button btnSaveActivity;
    private Button cancelActicity;
    private ListView listViewActivityInfo;
    private ProgressDialog pDialog;
    private MapView saveActivityMapView = null;
    private IMapController mapController;
    private GridView imageGrid;
    private ArrayList<Bitmap> bitmapList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_track);

        //Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        btnSaveActivity = (Button) findViewById(R.id.saveActivityButton);
        cancelActicity = (Button) findViewById(R.id.deleteActivityButton);
        listViewActivityInfo = (ListView) findViewById(R.id.saveActivityListView);

        imageGrid = (GridView) findViewById(R.id.gridViewImage);
        bitmapList = new ArrayList<Bitmap>();

        saveActivityMapView = (MapView) findViewById(R.id.saveActivityMap);

        try {
            jsonGpsDataReceived = new JSONObject(getIntent().getStringExtra("JSONObjectWithGPSData"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        drawGPSTrack(jsonGpsDataReceived);

        displayInfoActivity();

        bitmapList = getBitmapList(jsonGpsDataReceived);
        imageGrid.setAdapter( new ImageAdapter(this, bitmapList));

        btnSaveActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postJSONRequest(jsonGpsDataReceived);
            }
        });

        cancelActicity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainActivity = new Intent(SaveTrackActivity.this, MainActivity.class);
                startActivity(mainActivity);
                finish();
            }
        });


    }

    private ArrayList<Bitmap> getBitmapList(JSONObject jsonObject){
        ArrayList<Bitmap> result = new ArrayList<Bitmap>();
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("images");
            for(int i = 0;i<jsonArray.length();i++){
                JSONObject jsonImageObj = jsonArray.getJSONObject(i);
                String imageKey = "image"+i;
                String jsonString = jsonImageObj.getString(imageKey);
                result.add(getBitmapFromString(jsonString));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
    private Bitmap getBitmapFromString(String jsonString){
        byte[] decodedString = Base64.decode(jsonString, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString,0,decodedString.length);
        return decodedByte;
    }

    private void drawGPSTrack(JSONObject jsonObject){
        List<GeoPoint> geoPointsList = getListGeopoints(jsonObject);
        Polyline polyline = new Polyline();
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));//used for tiles
        saveActivityMapView.setTileSource(TileSourceFactory.MAPNIK);
        saveActivityMapView.setBuiltInZoomControls(true);
        saveActivityMapView.setMultiTouchControls(true);
        mapController = saveActivityMapView.getController();
        mapController.setZoom(17);
        mapController.setCenter(geoPointsList.get(0));
        polyline.setPoints(geoPointsList);
        polyline.setWidth(3f);
        polyline.setOnClickListener(new Polyline.OnClickListener() {
            @Override
            public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos) {
                Toast.makeText(mapView.getContext(), "polyline with" +  + polyline.getPoints().size() + " pts was tapped", Toast.LENGTH_LONG).show();
                return false;
            }
        });
        saveActivityMapView.getOverlayManager().add(polyline);
    }

    private List<GeoPoint> getListGeopoints(JSONObject jsonObject){//using gps data from json object
        List<GeoPoint> geoPointsList = new ArrayList<>();
        //Parse jsonArray
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("gps_data");
            for(int i=0; i<jsonArray.length(); i++){
                GeoPoint geoPoint = new GeoPoint(0.0,0.0,0.0);
                JSONObject GPSData = jsonArray.getJSONObject(i);
                geoPoint.setLatitude(GPSData.getDouble("latitude"));
                geoPoint.setLongitude(GPSData.getDouble("longitude"));
                geoPoint.setAltitude(GPSData.getDouble("altitude"));
                geoPointsList.add(geoPoint);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return geoPointsList;
    }

    private void displayInfoActivity(){

        String[] itemsToDisplay = new String[3];
        try {
            itemsToDisplay = new String[]{jsonGpsDataReceived.getString("activity_name"),
                    jsonGpsDataReceived.getString("total_time"),
                    jsonGpsDataReceived.getString("total_distance"),
                    jsonGpsDataReceived.getString("avg_time")};
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ListAdapter listViewItems = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,itemsToDisplay);
        listViewActivityInfo.setAdapter(listViewItems);
    }

    private void postJSONRequest(JSONObject mJsonObject){
        //tag used to cancel request
        String tag_string_req = "req_save_activity";

        pDialog.setMessage("Saving data ... ");
        showDialog();

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Method.POST, AppConfig.URL_SEND_JSON_DATA, mJsonObject, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG,"Saving data response: "+response.toString());
                hideDialog();
                Toast.makeText(getApplicationContext(), "Activity saved", Toast.LENGTH_LONG).show();
                Intent mainActivity = new Intent(SaveTrackActivity.this, MainActivity.class);
                startActivity(mainActivity);
                finish();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG,"Sending data to server error: "+ error.getMessage());
                hideDialog();
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                return params;
            }
        };

        //Adding request to the request queue
        AppController.getInstance().addToRequestQueue(jsonObjectRequest,tag_string_req);
        //Redirect to home page
    }

    @Override
    protected void onResume() {
        super.onResume();
        saveActivityMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveActivityMapView.onPause();
    }

    private void showDialog() {
        if(!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog(){
        if(pDialog.isShowing())
            pDialog.dismiss();
    }
}