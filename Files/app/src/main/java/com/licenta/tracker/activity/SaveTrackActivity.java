package com.licenta.tracker.activity;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.licenta.tracker.R;
import com.licenta.tracker.app.AppConfig;
import com.licenta.tracker.app.AppController;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;


public class SaveTrackActivity extends AppCompatActivity {

    private static final String TAG = SaveTrackActivity.class.getSimpleName();

    private JSONObject jsonGpsDataReceived;
    private TextView txtJSONExample;
    private Button btnSaveActivity;
    private ProgressDialog pDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_track);

        //Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        txtJSONExample = (TextView) findViewById(R.id.jsonexampletxtview);

        btnSaveActivity = (Button) findViewById(R.id.saveActivityButton);
        try {
            jsonGpsDataReceived = new JSONObject(getIntent().getStringExtra("JSONObjectWithGPSData"));
            txtJSONExample.setText(jsonGpsDataReceived.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        btnSaveActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postJSONRequest(jsonGpsDataReceived);
            }
        });

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
                Toast.makeText(getApplicationContext(), "it works", Toast.LENGTH_LONG).show();
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