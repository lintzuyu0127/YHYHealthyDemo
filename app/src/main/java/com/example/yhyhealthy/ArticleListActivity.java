package com.example.yhyhealthy;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.util.Log;
import com.example.yhyhealthy.adapter.ArticleListAdapter;
import com.example.yhyhealthy.dataBean.ArticleListData;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.tools.SpacesItemDecoration;
import org.json.JSONException;
import org.json.JSONObject;
import static com.example.yhyhealthy.module.ApiProxy.ARTICLE_LIST;

public class ArticleListActivity extends AppPage {

    private static final String TAG = "ArticleListActivity";

    private String attrID = "";
    private String serviceItemId ="";
    private String attrName = "";

    RecyclerView rvArt;

    //api
    ApiProxy proxy;
    ArticleListData listData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //title隱藏
        setContentView(R.layout.activity_article_list);

        proxy = ApiProxy.getInstance();
        listData = new ArticleListData();

        initView();

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            attrID = bundle.getString("AttrID");
            serviceItemId = bundle.getString("ServiceItemId");
            attrName = bundle.getString("AttName"); //文章title
            setTitle(attrName);
            loadInfo(); //呼叫後端資料
        }
    }

    private void initView() {
        int spacingInPixels = 20;  //設定item間距的距離
        rvArt = findViewById(R.id.rv_article);
        rvArt.setLayoutManager(new LinearLayoutManager(this));
        rvArt.setHasFixedSize(true);
        rvArt.addItemDecoration(new SpacesItemDecoration(spacingInPixels)); //設定item間距
    }

    private void loadInfo() {
        //取得手機語系
        String language = getResources().getConfiguration().locale.getLanguage();
        String country = getResources().getConfiguration().locale.getCountry();
        String defaultLan = language + "-" + country;

        JSONObject json = new JSONObject();
        try {
            json.put("serviceItemId", serviceItemId);
            json.put("attrId", attrID);
            json.put("offset",1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        proxy.buildEdu(ARTICLE_LIST, json.toString(), defaultLan, requestListener);
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

    //2021/02/13 leona
    private void parserResult(JSONObject result) {
        listData = ArticleListData.newInstance(result.toString());
        ArticleListAdapter adapter = new ArticleListAdapter(this, listData.getArticleList());
        rvArt.setAdapter(adapter);
    }
}