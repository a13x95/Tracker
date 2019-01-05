package com.licenta.tracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.licenta.tracker.activity.LoginActivity;
import com.licenta.tracker.activity.OSM_MapActivity;
import com.licenta.tracker.helper.SQLiteHandler;
import com.licenta.tracker.helper.SessionManager;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private TextView txtID;
    private TextView txtName;
    private TextView txtEmail;
    private Button btnLogout;
    private Button btnStartActivity;

    private SQLiteHandler db;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtID = (TextView) findViewById(R.id.user_id);
        txtName = (TextView) findViewById(R.id.name);
        txtEmail = (TextView) findViewById(R.id.email);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnStartActivity = (Button) findViewById(R.id.btnStartActivity);

        //SQLite database handler

        db = new SQLiteHandler(getApplicationContext());

        //Session Manager
        session = new SessionManager(getApplicationContext());

        if(!session.isLoggedIn()){
            logoutUser();
        }

        //Fetching user details from SQLite
        HashMap<String, String> user = db.getUserDetails();
        String userID = user.get("uid");
        String name = user.get("name");
        String email = user.get("email");

        //Displaying the user details on the screen
        txtID.setText(userID);
        txtName.setText(name);
        txtEmail.setText(email);

        //Logout button Click Event
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutUser();
            }
        });
        btnStartActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTracking();
            }
        });
    }
    /*
    * Loggin out the user. Will set isLoggedIn flag to false in shared preferences and will clear data from users table
    * */
    private void logoutUser() {
        session.setLoggin(false);

        db.deleteUsers();

        //Launching the Login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void startTracking(){
        //Lunch the OSM_Map activity
        Intent intent = new Intent(MainActivity.this, OSM_MapActivity.class);
        startActivity(intent);
        finish();
    }
}