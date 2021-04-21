package com.example.yhyhealthy;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

/*
* 服務 & 隱私權
* */

public class PrivacyActivity extends AppCompatActivity {

    private CheckBox privacy1, privacy2;
    private Button confirm;
    private TextView privacyContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //title隱藏
        setContentView(R.layout.activity_privacy);

        privacy1 = findViewById(R.id.chkPrivacy1);
        privacy2 = findViewById(R.id.chkPrivacy2);
        confirm = findViewById(R.id.btnConfirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(privacy1.isChecked() && privacy2.isChecked()){ //同意後才能去註冊
                    startActivity(new Intent(getBaseContext(), RegisterActivity.class)); //註冊
                    finish();
                }else{
                    Toast.makeText(PrivacyActivity.this, getString(R.string.privacy_not_pass), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}