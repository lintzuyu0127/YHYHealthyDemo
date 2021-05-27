package com.example.yhyhealthy;

import android.os.Bundle;

public class OvulationRecordActivity extends AppPage {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_ovulation_record);
        setTitle(R.string.icon_ovulation);
    }
}