package com.licenta.tracker;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.licenta.tracker.activity.DisplayActivityDetails;
import com.licenta.tracker.activity.LoginActivity;
import com.licenta.tracker.activity.OSM_MapActivity;
import com.licenta.tracker.app.AppConfig;
import com.licenta.tracker.app.AppController;
import com.licenta.tracker.app.RouteDetails;
import com.licenta.tracker.helper.CustomListAdapter;
import com.licenta.tracker.helper.SQLiteHandler;
import com.licenta.tracker.helper.SessionManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Button btnStartActivity;
    private ListView listViewActivities;
    private TextView txtNrActivities;
    private ProgressDialog pDialog;
    private SQLiteHandler db;
    private SessionManager session;
    private List<RouteDetails> routeDetailsList = new ArrayList<RouteDetails>();
    private CustomListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String tag_string_req = "get_info_activity";
        JSONObject data = new JSONObject();

        //Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        txtNrActivities = (TextView) findViewById(R.id.txtNrOfActivities);
        btnStartActivity = (Button) findViewById(R.id.btnStartActivity);
        listViewActivities = (ListView) findViewById(R.id.activities_listView);

        adapter = new CustomListAdapter(this, routeDetailsList);
        listViewActivities.setAdapter(adapter);

        //SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        //Session Manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        //Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();
        final String userID = user.get("uid");
        String name = user.get("name");
        String email = user.get("email");

        try {
            data.put("request", "activityDetails");
            data.put("user_id", userID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        pDialog.setMessage("Fetching data from server... ");
        showDialog();
        //Create request -> returns as a response a json array
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Method.POST, AppConfig.URL_FETCH_DATA, data, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jsonObject = new JSONObject(response.toString());
                    boolean error = jsonObject.getBoolean("error");
                    if(!error){
                        JSONArray jsonArray = jsonObject.getJSONArray("activityDetails");
                        Log.d(TAG, "SIZE: " + jsonArray.length());
                        for(int i=0;i<jsonArray.length();i++){
                            JSONObject itemArray = jsonArray.getJSONObject(i);
                            RouteDetails routeDetails = new RouteDetails(itemArray.getString("activityName"),itemArray.getString("totalDistance"),itemArray.getString("totalTime"),itemArray.getString("track_id"));
                            routeDetailsList.add(routeDetails);
                            txtNrActivities.setText("Total Activities: "+routeDetailsList.size());
                        }
                        //Notify the list adapter that the array has data in it
                        adapter.notifyDataSetChanged();
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

        listViewActivities.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for(int i=0;i<routeDetailsList.size();i++){
                    if(position == i){
                        Intent activityDetails = new Intent(MainActivity.this, DisplayActivityDetails.class);
                        activityDetails.putExtra("track_id", routeDetailsList.get(position).getTrackID());
                        activityDetails.putExtra("user_id", userID);
                        startActivity(activityDetails);
                        finish();
                    }
                }
            }
        });
        btnStartActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTracking();
            }
        });
    }

    private void logoutUser() {
        session.setLoggin(false);

        db.deleteUsers();

        //Launching the Login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void startTracking() {
        //Lunch the OSM_Map activity
        Intent intent = new Intent(MainActivity.this, OSM_MapActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_id: {
                Toast.makeText(getApplicationContext(), "Settings Pressed", Toast.LENGTH_LONG).show();
                break;
            }
            case R.id.logOut_id: {
                logoutUser();
                Toast.makeText(getApplicationContext(), "User Logged Out", Toast.LENGTH_LONG).show();
                break;
            }
        }
        return true;
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}