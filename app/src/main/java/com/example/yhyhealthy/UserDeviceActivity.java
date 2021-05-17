package com.example.yhyhealthy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yhyhealthy.adapter.UserDeviceAdapter;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.tools.SpacesItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthy.module.ApiProxy.PRODUCTS_BIND;
import static com.example.yhyhealthy.module.ApiProxy.PRODUCTS_BIND_REMOVE;
import static com.example.yhyhealthy.module.ApiProxy.PRODUCTS_NO;

/***
 *  裝置序號 (排卵儀)
 *  功能:
 *   裝置查詢
 *   裝置新增綁定
 *   裝置解除綁定
 *   create 2021/03/29
 * **/
public class UserDeviceActivity extends AppPage implements UserDeviceAdapter.UserDeviceListener {

    private static final String TAG = "UserDeviceActivity";

    private EditText deviceNo;
    private RecyclerView deviceList;
    private UserDeviceAdapter adapter;

    //api
    private ApiProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //title隱藏
        setContentView(R.layout.activity_user_device);
        setTitle(getString(R.string.setting_device_no));

        initView();

        initData(); //裝置列表初始化

    }

    private void initView() {
        deviceNo = findViewById(R.id.edtDeviceNo);
        deviceNo.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    //上傳裝置序號到後台儲存
                    updateDeviceNoToApi();
                }
                return false;
            }
        });

        deviceList = findViewById(R.id.rvDeviceNoList);
    }

    //裝置列表初始化
    private void initData() {
        proxy = ApiProxy.getInstance();
        proxy.buildPOST(PRODUCTS_NO, "" , deviceListListener);
    }

    private ApiProxy.OnApiListener deviceListListener = new ApiProxy.OnApiListener() {
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
                            parserJson(result);
                        }else {
                            Log.d(TAG, "錯誤代碼: " + errorCode);
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

    //解析後台回覆的裝置列表
    private void parserJson(JSONObject result) {
        List<String> dataList = new ArrayList<>();
        try {
            JSONObject object = new JSONObject(result.toString());
            JSONArray array = object.getJSONArray("success");
            for (int i = 0; i < array.length(); i++){
                dataList.add(array.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //將資料傳到adapter
        adapter = new UserDeviceAdapter(this, dataList, this);
        deviceList.setAdapter(adapter);
        deviceList.setHasFixedSize(true);
        deviceList.setLayoutManager(new LinearLayoutManager(this));
        deviceList.addItemDecoration(new SpacesItemDecoration(20));
    }

    //上傳裝置序號到後台儲存
    private void updateDeviceNoToApi() {
        if (TextUtils.isEmpty(deviceNo.getText().toString()))
            return;

        JSONObject json = new JSONObject();
        try {
            json.put("serialNo", deviceNo.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        proxy.buildPOST(PRODUCTS_BIND, json.toString(), updateDeviceListener);
    }

    private ApiProxy.OnApiListener updateDeviceListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            buildProgress(R.string.progressdialog_else, R.string.progressdialog_wait);
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
                            Toasty.success(UserDeviceActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT, true).show();
                            initData();//重刷資料
                        }else if (errorCode == 33){
                            Toasty.error(UserDeviceActivity.this, getString(R.string.device_error_number), Toast.LENGTH_SHORT, true).show();
                        }else {
                            Log.d(TAG, "綁定裝置錯誤代碼: " + errorCode);
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
            hideProgress();
        }
    };

    @Override //解除裝置綁定
    public void onDelete(String deviceNo) {
        JSONObject json = new JSONObject();
        try {
            json.put("serialNo", deviceNo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        proxy.buildPOST(PRODUCTS_BIND_REMOVE, json.toString(), deleteDeviceListener);
    }

    private ApiProxy.OnApiListener deleteDeviceListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            buildProgress(R.string.progressdialog_else, R.string.progressdialog_wait);
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
                            Toasty.success(UserDeviceActivity.this, getString(R.string.delete_success), Toast.LENGTH_SHORT, true).show();
                            initData();//重刷資料
                        }else {
                            Log.d(TAG, "刪除裝置錯誤代碼: " + errorCode);
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
            hideProgress();
        }
    };
}