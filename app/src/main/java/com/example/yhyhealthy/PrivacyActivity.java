package com.example.yhyhealthy;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/** ****
* 服務條款
* *****/

public class PrivacyActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener, ViewTreeObserver.OnScrollChangedListener {

    private Button disagree, agree;
    private ScrollView scrollView;
    private WebView webView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //title隱藏
        setContentView(R.layout.activity_privacy);

        agree = findViewById(R.id.btnAgree);
        disagree = findViewById(R.id.btnDisagree);
        agree.setVisibility(View.GONE);
        disagree.setVisibility(View.GONE);

        agree.setOnClickListener(this);
        disagree.setOnClickListener(this);

        scrollView = findViewById(R.id.scrViewPrivacy);
        scrollView.setOnTouchListener(this);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(this);
        webView = findViewById(R.id.webViewPrivacy);
        webView.loadData(getResources().getString(R.string.privacy_content), "text/html", null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnAgree:
                startActivity(new Intent(getBaseContext(), RegisterActivity.class));
                finish();
                break;
            case R.id.btnDisagree:
                finish();
                break;
        }
    }

    public void onScrollChanged(){
        View view = scrollView.getChildAt(scrollView.getChildCount() - 1);
        //int topDetector = scrollView.getScrollY();
        int bottomDetector = view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY());
        if (bottomDetector == 0){ //底部
            agree.setVisibility(View.VISIBLE);
            disagree.setVisibility(View.VISIBLE);
        }

//        if(topDetector <= 0){ //頂部
//
//        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}