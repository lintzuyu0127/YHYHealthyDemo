package com.example.yhyhealthy;


import android.os.Bundle;
import android.webkit.WebView;
import android.widget.ScrollView;

public class SystemProvisionActivity extends AppPage {

    private ScrollView scrollView;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //title隱藏 //title隱藏
        setContentView(R.layout.activity_system_provision);
        setTitle(R.string.privacy_title);

        scrollView = findViewById(R.id.scViewProvision);
        webView = findViewById(R.id.wbProvision);
        webView.loadData(getResources().getString(R.string.privacy_content), "text/html" , null);

    }
}