package com.example.yhyhealthy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.yhyhealthy.adapter.CheckBoxAdapter;
import com.example.yhyhealthy.adapter.SwitchItemAdapter;
import com.example.yhyhealthy.dataBean.SymptomData;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.tools.SpacesItemDecoration;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthy.module.ApiProxy.SYMPTOM_ADD;
import static com.example.yhyhealthy.module.ApiProxy.SYMPTOM_LIST;

/***  **** *****
 * 症狀
 * UI design from Api
 * create 2021/04/14
 * *   ****   **/

public class SymptomActivity extends AppPage implements View.OnClickListener {

    private static final String TAG = "SymptomActivity";

    private RecyclerView viewSymptomSW, viewSymptomCH;

    private int targetId;

    private Button update;

    //api
    private ApiProxy proxy;

    //
    private List<SymptomData.SwitchItemBean> switchItemBeanList = new ArrayList<>();
    private List<SymptomData.CheckBoxGroup> checkBoxGroupList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //title隱藏
        setContentView(R.layout.activity_symptom);
        setTitle(R.string.title_symptom);

        //休眠禁止
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //接受來自TemperatureActivity的資料
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            targetId = bundle.getInt("targetId");  //使用全域變數
        }

        proxy = ApiProxy.getInstance();

        initView();

        initUiData();
    }

    private void initView() {
        viewSymptomSW = findViewById(R.id.rvSwitchItem);
        viewSymptomCH = findViewById(R.id.rvCheckBox);
        update = findViewById(R.id.btUpdate);
        update.setOnClickListener(this);
    }

    private void initUiData() {
        proxy.buildPOST(SYMPTOM_LIST, "", symptomListener);
    }

    private ApiProxy.OnApiListener symptomListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {

        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject object = new JSONObject(result.toString());
                        int errorCode = object.getInt("errorCode");
                        if (errorCode == 0){
                            parserSymptom(result);
                        }else {
                            Log.d(TAG, "初始化症狀的錯誤代碼: " + errorCode);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void onFailure(String message) {
            Log.d(TAG, "onFailure: " + message);
        }

        @Override
        public void onPostExecute() {

        }
    };

    //症狀UI來源
    private void parserSymptom(JSONObject result) {

        try {
            JSONObject jsonObject = new JSONObject(result.toString());
            JSONArray array = jsonObject.getJSONArray("success");
            for (int i = 0; i < array.length(); i++){
                JSONObject newObject = array.getJSONObject(i);
                String key = newObject.getString("key");
                Object value = newObject.get("value");
                if (value instanceof Boolean){
                    boolean booleanValue = newObject.getBoolean("value");
                    switchItemBeanList.add(new SymptomData.SwitchItemBean(key, booleanValue));
                }else if (value instanceof JSONArray){
                    JSONArray jsonValue = newObject.getJSONArray("value");
                    List<String> listData = new ArrayList<>();
                    for (int k = 0; k < jsonValue.length(); k++){
                        listData.add(jsonValue.getString(k));
                    }
                    checkBoxGroupList.add(new SymptomData.CheckBoxGroup(key,listData));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //解析出來的布林資料傳到Switch的Adapter
        SwitchItemAdapter switchItemAdapter = new SwitchItemAdapter(this, switchItemBeanList);
        viewSymptomSW.setAdapter(switchItemAdapter);
        viewSymptomSW.setHasFixedSize(true);
        viewSymptomSW.setLayoutManager(new LinearLayoutManager(this));
        viewSymptomSW.addItemDecoration(new SpacesItemDecoration(10));

        //解析出來的陣列資料傳到checkbox的Adapter
        CheckBoxAdapter checkBoxAdapter = new CheckBoxAdapter(this, checkBoxGroupList);
        viewSymptomCH.setAdapter(checkBoxAdapter);
        viewSymptomCH.setHasFixedSize(true);
        viewSymptomCH.setLayoutManager(new LinearLayoutManager(this));
        viewSymptomCH.addItemDecoration(new SpacesItemDecoration(10));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btUpdate:
                updateToApi();
                break;
        }
    }

    //上傳資料給後台
    private void updateToApi() {
        DateTime dt1 = new DateTime();
        String SymptomRecordTime = dt1.toString("yyyy-MM-dd,HH:mm:ss");

        JSONArray array = new JSONArray();

        //switch
        for(int i=0; i < switchItemBeanList.size(); i++){
            JSONObject objectSwitch = new JSONObject();
            try {
                objectSwitch.put("key", switchItemBeanList.get(i).getKey());
                objectSwitch.put("value",switchItemBeanList.get(i).isValue());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            array.put(objectSwitch);
        }

        //checkBox
        for(int j = 0; j < checkBoxGroupList.size(); j++){
            JSONObject objectCheckBox = new JSONObject();
            try {
                objectCheckBox.put("key", checkBoxGroupList.get(j).getKey());

                objectCheckBox.put("value", new JSONArray(checkBoxGroupList.get(j).getChecked()));

            } catch (JSONException e) {
                e.printStackTrace();
            }
            array.put(objectCheckBox);
        }

        JSONObject finalObject = new JSONObject();
        try {
            finalObject.put("targetId", targetId);
            finalObject.put("createDate", SymptomRecordTime);
            finalObject.put("symptoms", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        proxy.buildPOST(SYMPTOM_ADD, finalObject.toString(), addListener);
    }

    private ApiProxy.OnApiListener addListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {

        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject object = new JSONObject(result.toString());
                        int errorCode = object.getInt("errorCode");
                        if (errorCode == 0){
                            Toasty.success(SymptomActivity.this, R.string.update_success, Toast.LENGTH_SHORT, true).show();
                            finish();  //回到上一頁
                        }else {
                            Log.d(TAG, "新增症狀後台錯誤回覆碼: " + errorCode);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void onFailure(String message) {
            Log.d(TAG, "onFailure: " + message);
        }

        @Override
        public void onPostExecute() {

        }
    };
}