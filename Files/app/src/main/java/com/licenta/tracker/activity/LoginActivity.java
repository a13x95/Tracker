package com.licenta.tracker.activity;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.licenta.tracker.MainActivity;
import com.licenta.tracker.R;
import com.licenta.tracker.app.AppConfig;
import com.licenta.tracker.app.AppController;
import com.licenta.tracker.helper.SQLiteHandler;
import com.licenta.tracker.helper.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private Button btnLogin;
    private Button btnLinkToRegister;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);

        //Progress Dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        //SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        //Session Manager
        session = new SessionManager(getApplicationContext());

        //Check if user is already logged in or not
        if(session.isLoggedIn()){
            //User is logged in, take him to main activity.
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        //Login button click event
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                //Check for empty data in the login form
                if(!email.isEmpty() && !password.isEmpty()){
                    //login user
                    checkLogin(email,password);
                }
                else{
                    //Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(),"Please enter credentials!", Toast.LENGTH_LONG).show();
                }
            }
        });

        //Link to register screen
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    /*
    * Function to verify the login credentials in mysql database
    * */

    private void checkLogin(final String email, final String password){
        //Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Logging in ... ");
        showDialog();

        StringRequest stringRequest = new StringRequest(Method.POST, AppConfig.URL_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    boolean error = jsonObject.getBoolean("error");

                    //Check for error node in json
                    if (!error) {
                        //user successfully logged in
                        //Create login session
                        session.setLoggin(true);

                        //now store the user in SQLite database locally
                        String uid = jsonObject.getString("uid");

                        JSONObject user = jsonObject.getJSONObject("user");
                        String name = user.getString("name");
                        String password = user.getString("email");
                        String created_at = user.getString("created_at");

                        //Inserting row in users table
                        db.addUser(name, email, uid, created_at);
                        //Launch main activity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        //Error in login. Get the error message
                        String errorMsg = jsonObject.getString("error_msg");
                        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Posting parameters to login url
                Map<String, String> params = new HashMap<String, String >();
                params.put("email", email);
                params.put("password", password);

                return params;
            }
        };

        //Adding the request to request queue
        AppController.getInstance().addToRequestQueue(stringRequest,tag_string_req);
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
