package com.example.yhyhealthy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class PregnancyActivity extends DeviceBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //title隱藏
        setContentView(R.layout.activity_pregnancy);
        setTitle(R.string.icon_pregnancy);
    }
}