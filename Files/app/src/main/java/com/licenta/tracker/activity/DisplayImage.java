package com.licenta.tracker.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.licenta.tracker.R;

public class DisplayImage extends AppCompatActivity {
    private ImageView imgView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.displayimage);

        imgView = (ImageView)findViewById(R.id.displayImage);
    }
}
