package com.licenta.tracker.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
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

public class DisplayActivityDetails extends AppCompatActivity {
    private static String TAG = DisplayActivityDetails.class.getSimpleName();
    private Button btnBack;
    private Button btnDeleteItem;
    private MapView detailMapView = null;
    private IMapController mapController;
    private ProgressDialog pDialog;
    private ListView detailsActivityList;
    private GridView imageGrid;
    private ArrayList<Bitmap> bitmapList;
    private String track_id ="";
    private String userID ="";
    private int childPosition =0;
    private JSONObject data;
    private String tag_string_req ="";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        tag_string_req = "get_details_activitiy";
        data = new JSONObject();
        bitmapList = new ArrayList<Bitmap>();
        //Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        btnBack = (Button) findViewById(R.id.detailsBackButton);
        btnDeleteItem = (Button) findViewById(R.id.detailsDeleteItem);
        detailsActivityList = (ListView) findViewById(R.id.detailsActivityListView);
        imageGrid = (GridView) findViewById(R.id.detailsGridViewImage);
        detailMapView = (MapView) findViewById(R.id.detailsActivityMap);

        track_id = getIntent().getStringExtra("track_id");
        userID = getIntent().getStringExtra("user_id");
        try {
            data.put("request", "activityInfo");
            data.put("track_id", track_id);
            data.put("user_id", userID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backToMain = new Intent(DisplayActivityDetails.this, MainActivity.class);
                startActivity(backToMain);
                finish();
            }
        });

        btnDeleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTrack(track_id);
            }
        });

        pDialog.setMessage("Loading... ");
        showDialog();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, AppConfig.URL_FETCH_DATA, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jsonObject = new JSONObject(response.toString());
                    boolean error = jsonObject.getBoolean("error");
                    if(!error){
                        JSONArray gpsData = jsonObject.getJSONArray("gps_data");
                        drawRoute(gpsData);

                        String [] itemsToDisplay = new String[4];
                        try{
                            itemsToDisplay = new String[]{"Name: "+jsonObject.getString("activity_name"),
                                    "Distance: "+jsonObject.getString("total_distance"),
                                    "Time: "+jsonObject.getString("total_time"),
                                    "Peace: "+jsonObject.getString("avg_time"),
                                    "Timestamp: "+jsonObject.getString("timestamp")};
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                        ListAdapter listAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.listview_text, R.id.list_content, itemsToDisplay);
                        detailsActivityList.setAdapter(listAdapter);

                        JSONArray jsonImages = jsonObject.getJSONArray("images");
                        for(int i=0;i<jsonImages.length();i++){
                            JSONObject jsonBitmapString = jsonImages.getJSONObject(i);
                            String key="img"+i;
                            String bitmapString = jsonBitmapString.getString(key);
                            bitmapList.add(getBitmapFromString(bitmapString));
                            imageGrid.setAdapter(new ImageAdapter(getApplicationContext(), bitmapList));
                            imageGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    for(int i=0;i<bitmapList.size();i++){
                                        if(i==position){
//                                            //Toast.makeText(getApplicationContext(), i, Toast.LENGTH_LONG).show();
//                                            Intent imgActivity = new Intent(DisplayActivityDetails.this, DisplayImage.class);
//                                            imgActivity.putExtra("bitmapString", bitmapList.get(i));
//                                            startActivity(imgActivity);
//                                            finish();
                                        }
                                    }
                                }
                            });
                        }

                    } else{
                        Toast.makeText(getApplicationContext(), "JSON response has errors", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Sending request server error message: " + error.getMessage());
                hideDialog();
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                return params;
            }
        };

        //Adding the request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjectRequest,tag_string_req);
    }

    public void setChildPosition(int i){
        childPosition=i;
    }

    public int getChildPosition() {
        return childPosition;
    }

    private void drawRoute(JSONArray gps_data){
        List<GeoPoint> geoPointList = new ArrayList<>();
        //parse json array
        for(int i=0;i<gps_data.length();i++){
            GeoPoint geoPoint = new GeoPoint(0.0,0.0);
            try {
                JSONObject gpsObj = gps_data.getJSONObject(i);
                geoPoint.setLatitude(Double.valueOf(gpsObj.getString("latitude")));
                geoPoint.setLongitude(Double.valueOf(gpsObj.getString("longitude")));
                geoPointList.add(geoPoint);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Polyline polyline = new Polyline();
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));//used for tiles
        detailMapView.setTileSource(TileSourceFactory.MAPNIK);
        detailMapView.setBuiltInZoomControls(true);
        detailMapView.setMultiTouchControls(true);
        mapController = detailMapView.getController();
        mapController.setZoom(13);
        mapController.setCenter(geoPointList.get(0));
        polyline.setPoints(geoPointList);
        polyline.setWidth(5f);
        polyline.setColor(Color.RED);
        polyline.setOnClickListener(new Polyline.OnClickListener() {
            @Override
            public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos) {
                Toast.makeText(mapView.getContext(), "polyline with" +  + polyline.getPoints().size() + " pts was tapped", Toast.LENGTH_LONG).show();
                return false;
            }
        });

        detailMapView.getOverlayManager().add(polyline);
    }

    public void deleteTrack(String track_id){
        tag_string_req = "get_delete_track";
        try {
            data.put("request", "deleteActivity");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        pDialog.setMessage("Loading... ");
        showDialog();
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, AppConfig.URL_FETCH_DATA, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jsonObject = new JSONObject(response.toString());
                    boolean error = jsonObject.getBoolean("error");
                    if(!error){
                        Toast.makeText(getApplicationContext(), "Track successfully deleted!", Toast.LENGTH_LONG).show();
                        Intent mainActivity = new Intent(DisplayActivityDetails.this, MainActivity.class);
                        startActivity(mainActivity);
                        finish();
                    } else{
                        Toast.makeText(getApplicationContext(), "JSON response has errors", Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Sending request server error message: " + error.getMessage());
                hideDialog();
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                return params;
            }
        };

        //Adding the request to request queue
        AppController.getInstance().addToRequestQueue(jsonObjectRequest,tag_string_req);
    }

    private Bitmap getBitmapFromString(String jsonString){
        byte[] decodedString = Base64.decode(jsonString, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString,0,decodedString.length);
        return decodedByte;
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
