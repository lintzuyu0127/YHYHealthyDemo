package com.example.yhyhealthy;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;
import com.example.yhyhealthy.adapter.VideoAdapter;
import com.example.yhyhealthy.dataBean.VideoData;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.tools.SpacesItemDecoration;
import org.json.JSONObject;
import static com.example.yhyhealthy.module.ApiProxy.EDU_VIDEO_CATALOG;

public class VideoActivity extends AppPage {

    private static final String TAG = "VideoActivity";

    private RecyclerView recycleVideo;

    //api
    private ApiProxy proxy;
    private VideoData videoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //title隱藏
        setContentView(R.layout.activity_video);
        setTitle(R.string.title_video_catalog);

        proxy = ApiProxy.getInstance();
        videoData = new VideoData();

        initView();

        initData();
    }

    private void initView() {
        int spacingInPixels = 10;  //設定item間距的距離
        recycleVideo = findViewById(R.id.rvVideo);
        recycleVideo.setLayoutManager(new LinearLayoutManager(this));
        recycleVideo.setHasFixedSize(true);
        recycleVideo.addItemDecoration(new SpacesItemDecoration(spacingInPixels));
    }

    private void initData() {
        //取得手機語系
        String language = getResources().getConfiguration().locale.getLanguage();
        String country = getResources().getConfiguration().locale.getCountry();
        String defaultLan = language + "-" + country;

        proxy.buildEdu(EDU_VIDEO_CATALOG, "", defaultLan, requestListener);
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
            Log.d(TAG, "onFailure: " + message.toString());
        }

        @Override
        public void onPostExecute() {
            hideProgress();
        }
    };

    private void parserJson(JSONObject result){
        videoData = VideoData.newInstance(result.toString());
        VideoAdapter adapter = new VideoAdapter(VideoActivity.this, videoData.getServiceItemList());
        recycleVideo.setAdapter(adapter);
    }
}