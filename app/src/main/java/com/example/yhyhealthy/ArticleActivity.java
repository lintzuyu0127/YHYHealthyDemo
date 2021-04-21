package com.example.yhyhealthy;

import android.os.Bundle;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yhyhealthy.adapter.ArticleAdapter;
import com.example.yhyhealthy.dataBean.ArticleData;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.tools.SpacesItemDecoration;

import org.json.JSONObject;

import static com.example.yhyhealthy.module.ApiProxy.EDU_ART_CATALOG;

/***
 * 衛教分類首頁
 * 採用熱更新
 * */

public class ArticleActivity extends AppPage {

    private static final String TAG = "ArticleActivity";

    private RecyclerView recyclerView;

    //api
    ApiProxy proxy;
    ArticleData articleData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //title隱藏
        setContentView(R.layout.activity_article);
        setTitle(R.string.title_article_catalog);

        proxy = ApiProxy.getInstance();
        articleData = new ArticleData();

        initView();

        initData();

    }

    private void initView() {
        int spacingInPixels = 10;  //設定item間距的距離
        recyclerView = findViewById(R.id.rvEdu);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels)); //設定item間距
    }

    private void initData() {
        //取得手機語系
        String language = getResources().getConfiguration().locale.getLanguage();
        String country = getResources().getConfiguration().locale.getCountry();
        String defaultLan = language + "-" + country;

        proxy.buildEdu(EDU_ART_CATALOG, "", defaultLan, requestListener);
    }

    private ApiProxy.OnApiListener requestListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            buildProgress(R.string.progressdialog_else, R.string.progressdialog_wait);
        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parserResult(result);
                }
            });
        }

        @Override
        public void onFailure(String message) {
            Log.d(TAG, "onFailure: " + message);
        }

        @Override
        public void onPostExecute() {
            hideProgress();
        }
    };


    private void parserResult(JSONObject result) {
        articleData = ArticleData.newInstance(result.toString());
        ArticleAdapter articleAdapter = new ArticleAdapter(this, articleData.getServiceItemList());
        recyclerView.setAdapter(articleAdapter);
    }
}