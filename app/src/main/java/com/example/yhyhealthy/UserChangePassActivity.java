package com.example.yhyhealthy;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.tools.ProgressDialogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthy.module.ApiProxy.CHANGE_PASSWORD;

/*****************
 * 設定 - 個人設定 - 變更密碼
 *******/

public class UserChangePassActivity extends AppPage {

    private static final String TAG = "UserChangePassActivity";

    EditText oldPW, newPD;

    //Api
    ApiProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //title隱藏
        setContentView(R.layout.activity_user_change_pass);
        setTitle(getString(R.string.setting_change_password));
        setActionButton(R.string.update, save);

        proxy = ApiProxy.getInstance();

        initView();
    }

    private void initView() {
        oldPW = findViewById(R.id.edtOldPassword);
        newPD = findViewById(R.id.edtNewPassword);
    }

    //資料檢查
    private View.OnClickListener save = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String oldPassword = oldPW.getText().toString(); //舊密碼
            String newPassword = newPD.getText().toString(); //新密碼

            if(TextUtils.isEmpty(oldPassword) || (TextUtils.isEmpty(newPassword))){ //新舊密碼不得空白就往後台傳
                Toasty.error(UserChangePassActivity.this, getString(R.string.not_allow_empty), Toast.LENGTH_SHORT, true).show();
            }else {
                updateToApi(oldPassword, newPassword);
            }
        }
    };

    private void updateToApi(String oldPassword, String newPassword) {
        JSONObject json = new JSONObject();
        try {
            json.put("password", oldPassword);
            json.put("newPassword", newPassword);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        proxy.buildPOST(CHANGE_PASSWORD, json.toString(), changePasswordListener);
    }

    private ApiProxy.OnApiListener changePasswordListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            buildProgress(R.string.progressdialog_else, R.string.progressdialog_wait);
        }

        @Override
        public void onSuccess(JSONObject result) {
            Log.d(TAG, "onSuccess: " + result.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jsonObject = new JSONObject(result.toString());
                        int errorCode = jsonObject.getInt("errorCode");
                        if (errorCode == 0){
                            Toasty.success(UserChangePassActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT, true).show();
                        }else {
                            Toasty.error(UserChangePassActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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