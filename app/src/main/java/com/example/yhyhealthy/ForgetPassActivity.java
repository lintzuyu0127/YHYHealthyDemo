package com.example.yhyhealthy;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.example.yhyhealthy.module.ApiProxy;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.json.JSONException;
import org.json.JSONObject;
import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthy.module.ApiProxy.COMP_CODE_REQUEST;
import static com.example.yhyhealthy.module.ApiProxy.FORGET_PASSWORD;

/** ******
 * 忘記密碼
 * 流程 : 以帳號請求重發驗證碼 --> 輸入驗證碼與密碼
 * api : 重發驗證碼Api & 忘記密碼Api
 * 2021/02/04
 * **********/

public class ForgetPassActivity extends AppPage implements View.OnClickListener {

    private static final String TAG = "ForgetPassActivity";

    private Button btnSend, btnGetCompCode;
    private TextInputLayout verificationLayout, newPasswordLayout;
    private TextInputEditText accountInput, verificationInput, newPasswordInput;

    private ApiProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_forget_pass);

        //init Api
        proxy = ApiProxy.getInstance();

        initView();

    }

    private void initView() {
        accountInput = findViewById(R.id.edtForgetAccount);
        verificationInput = findViewById(R.id.edtNewCompCode);
        newPasswordInput = findViewById(R.id.edtNewPassword);

        verificationLayout = findViewById(R.id.verificationLayout);
        newPasswordLayout = findViewById(R.id.newPasswordLayout);

        btnSend = findViewById(R.id.btnForgetSend);
        btnGetCompCode = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);
        btnGetCompCode.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnForgetSend: //帳號取得驗證碼
                getCompCode();
                break;
            case R.id.btnSend:   //新密碼&驗證碼
                checkBeforeUpdate();
                break;
        }
    }

    private void checkBeforeUpdate() {
        String compCode = verificationInput.getText().toString();
        String newPass = newPasswordInput.getText().toString();

        if(TextUtils.isEmpty(compCode)){
            Toasty.error(ForgetPassActivity.this, getString(R.string.comp_code_is_not_empty), Toasty.LENGTH_SHORT,true).show();
            return;
        }

        if(newPass.length() < 6 || (TextUtils.isEmpty(newPass))){
            Toasty.error(ForgetPassActivity.this, getString(R.string.password_less_six), Toasty.LENGTH_SHORT,true).show();
            return;
        }

        updateToApi(); //資料更新送後端
    }

    //後端更新
    private void updateToApi() {
        JSONObject json = new JSONObject();
        try {
            json.put("account", accountInput.getText().toString());
            json.put("password", newPasswordInput.getText().toString());
            json.put("verCode", verificationInput.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //執行忘記密碼Api
        proxy.buildPW(FORGET_PASSWORD, json.toString(), forgetListener);
    }

    private ApiProxy.OnApiListener forgetListener = new ApiProxy.OnApiListener() {
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
                            Toasty.success(ForgetPassActivity.this, getString(R.string.data_change_success), Toasty.LENGTH_SHORT, true).show();
                            finish();
                        }else {
                            Log.d(TAG, getString(R.string.json_error_code) + errorCode);
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

    //像後端提出需求
    private void getCompCode() {

        //取得手機語系
        String language = getResources().getConfiguration().locale.getLanguage();
        String country = getResources().getConfiguration().locale.getCountry();
        String defaultLan = language + "-" + country; //ex:zh-TW, zh-CN

        //帳號不得空白
        if(TextUtils.isEmpty(accountInput.getText().toString())){
            Toasty.error(ForgetPassActivity.this, getString(R.string.account_is_not_empty), Toasty.LENGTH_SHORT,true).show();
            return;
        }

        String account = accountInput.getText().toString();
        JSONObject json = new JSONObject();
        try {
            json.put("account", account);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //執行後端request重發新的驗證碼
        proxy.buildCompCode(COMP_CODE_REQUEST, json.toString(), defaultLan, requestListener);
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
                        parser(result);
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

    //解析後台回傳的資料
    private void parser(JSONObject result) {
        try {
            JSONObject object = new JSONObject(result.toString());
            int errorCode = object.getInt("errorCode");
            if (errorCode == 0){
                JSONObject success = object.getJSONObject("success");
                int code = success.getInt("statusCode");
                if(code == 1){
                    showCompCodeLayout(); //顯示要求輸入密碼及驗證碼之Layout
                }else if (code == 2){
                    finish();
                }
            }else{
                Log.d(TAG, getString(R.string.json_error_code) + errorCode);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //顯示需要用到的Layout
    private void showCompCodeLayout() {
        verificationLayout.setVisibility(View.VISIBLE);
        newPasswordLayout.setVisibility(View.VISIBLE);
        btnGetCompCode.setVisibility(View.VISIBLE);
        btnSend.setVisibility(View.GONE);
    }
}