package com.example.yhyhealthy;

import android.os.Bundle;
import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yhyhealthy.adapter.VideoListAdapter;
import com.example.yhyhealthy.dataBean.VideoListData;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.tools.SpacesItemDecoration;

import org.json.JSONException;
import org.json.JSONObject;

import static com.example.yhyhealthy.module.ApiProxy.ARTICLE_LIST;
import static com.example.yhyhealthy.module.ApiProxy.VIDEO_LIST;

public class VideoListActivity extends AppPage {

    private static final String TAG = "VideoListActivity";

    private ApiProxy proxy;
    private VideoListData listData;

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //title隱藏
        setContentView(R.layout.activity_video_list);

        proxy = ApiProxy.getInstance();
        listData = new VideoListData();

        initView();

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            String attrId = bundle.getString("AttrID");
            String serviceItemId = bundle.getString("ServiceItemId");
            String videoName = bundle.getString("AttName");
            setTitle(videoName);
            initVideo(attrId, serviceItemId);
        }

    }

    private void initView() {
        recyclerView = findViewById(R.id.rv_video);

        int spacingInPixels = 10;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
    }

    private void initVideo(String attrId, String serviceItemId) {
        //取得手機語系
        String language = getResources().getConfiguration().locale.getLanguage();
        String country = getResources().getConfiguration().locale.getCountry();
        String defaultLan = language + "-" + country;

        JSONObject json = new JSONObject();
        try {
            json.put("serviceItemId", serviceItemId);
            json.put("attrId", attrId);
            json.put("offset",1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        proxy.buildEdu(VIDEO_LIST, json.toString(), defaultLan, requestListener);
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
                    parserJson(result);
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

    private void parserJson(JSONObject result) {
        listData = VideoListData.newInstance(result.toString());
        VideoListAdapter adapter = new VideoListAdapter(this, listData.getVideoList());
        recyclerView.setAdapter(adapter);
    }
}