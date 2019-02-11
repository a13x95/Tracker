package com.licenta.tracker.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.licenta.tracker.R;

public class DisplayImage extends AppCompatActivity {
    private ImageView imgView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.displayimage);

        imgView = (ImageView)findViewById(R.id.displayImage);
    }
}
