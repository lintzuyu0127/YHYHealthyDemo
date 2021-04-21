package com.example.yhyhealthy;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.yhyhealthy.dataBean.ChangeUserMarriage;
import com.example.yhyhealthy.dataBean.MarriageData;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.tools.ProgressDialogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthy.module.ApiProxy.MARRIAGE_INFO;
import static com.example.yhyhealthy.module.ApiProxy.MARRIAGE_UPDATE;

/**  ******** ******
 *  設定 - 個人設定 - 婚姻狀況
 * * * ******* ********/

public class UserMarriageActivity extends AppPage implements CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "UserMarriageActivity";

    Switch marriageStatus, contraceptionStatus, childStatus;

    //api
    ApiProxy proxy;
    MarriageData marriageData;
    ChangeUserMarriage changeUserMarriage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_user_marriage);
        setTitle(getString(R.string.setting_marriage));
        setActionButton(R.drawable.ic_baseline_backup_white_32, save);

        initData();

        initView();

    }

    private void initData() {
        proxy = ApiProxy.getInstance();
        changeUserMarriage = new ChangeUserMarriage();
        proxy.buildPOST(MARRIAGE_INFO, "", marriageListener);
    }

    private ApiProxy.OnApiListener marriageListener = new ApiProxy.OnApiListener() {
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
                        if (errorCode == 6){ //查無資料
                            marriageStatus.setChecked(false);
                            childStatus.setChecked(false);
                            contraceptionStatus.setChecked(false);
                        }else if (errorCode == 0){
                            parser(result);
                        }else {
                            Log.d(TAG, "後台回覆的錯誤碼: " + errorCode);
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

    //解析後台來的資料
    private void parser(JSONObject result) {
        marriageData = MarriageData.newInstance(result.toString());

        //婚姻
        boolean married = marriageData.getSuccess().isMarried();
        marriageStatus.setChecked(married);
        changeUserMarriage.setMarried(married);

        //孩子
        boolean child = marriageData.getSuccess().isHasChild();
        childStatus.setChecked(child);
        changeUserMarriage.setHasChild(child);

        //避孕
        boolean contraception = marriageData.getSuccess().isContraception();
        contraceptionStatus.setChecked(contraception);
        changeUserMarriage.setContraception(contraception);
    }

    private void initView() {
        marriageStatus = findViewById(R.id.swMarriage);
        contraceptionStatus = findViewById(R.id.swContraception);
        childStatus = findViewById(R.id.swChild);

        marriageStatus.setOnCheckedChangeListener(this);
        contraceptionStatus.setOnCheckedChangeListener(this);
        childStatus.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.swMarriage: //婚姻狀況
                if(isChecked){
                    marriageStatus.setChecked(true);
                    changeUserMarriage.setMarried(true);
                }else {
                    marriageStatus.setChecked(false);
                    changeUserMarriage.setMarried(false);
                }
                break;
            case R.id.swContraception: //避孕
                if(isChecked){
                    contraceptionStatus.setChecked(true);
                    changeUserMarriage.setContraception(true);
                }else {
                    contraceptionStatus.setChecked(false);
                    changeUserMarriage.setContraception(false);
                }
                break;
            case R.id.swChild:  //小孩
                if(isChecked){
                    childStatus.setChecked(true);
                    changeUserMarriage.setHasChild(true);
                }else {
                    childStatus.setChecked(false);
                    changeUserMarriage.setHasChild(false);
                }
        }
    }

    //資料檢查
    private View.OnClickListener save = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            updateToApi();  //上傳至後台
        }
    };

    //存擋=上傳
    private void updateToApi(){
        Log.d(TAG, "updateToApi: " + changeUserMarriage.toJSONString());
        proxy.buildPOST(MARRIAGE_UPDATE, changeUserMarriage.toJSONString(), changeMarriageListener);
    }

    private ApiProxy.OnApiListener changeMarriageListener = new ApiProxy.OnApiListener() {
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
                        JSONObject jsonObject = new JSONObject(result.toString());
                        int errorCode = jsonObject.getInt("errorCode");
                        if (errorCode == 0){
                            boolean success = jsonObject.getBoolean("success");
                            if (success){
                                Toasty.success(UserMarriageActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT, true).show();
                            }
                        }else {
                            Toasty.error(UserMarriageActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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

    //寫到local
    private void writeToSharePreferences() {
        SharedPreferences pref = getSharedPreferences("yhyHealthy", MODE_PRIVATE);
        pref.edit().putBoolean("MARRIAGE", true).apply();
    }
}