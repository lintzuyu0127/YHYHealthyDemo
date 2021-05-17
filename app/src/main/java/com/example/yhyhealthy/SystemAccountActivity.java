package com.example.yhyhealthy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yhyhealthy.module.ApiProxy;

import org.json.JSONException;
import org.json.JSONObject;

import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthy.module.ApiProxy.MONITOR_CODE;
import static com.example.yhyhealthy.module.ApiProxy.MONITOR_CODE_RENEW;

/****
 * 設定 - 帳戶設定
 *  密碼設定
 *  驗證方式
 *  遠端授權碼 dialog
 *  裝置序號
 * */

public class SystemAccountActivity extends AppPage implements View.OnClickListener {

    private static final String TAG = "SystemAccountActivity";

    private ImageView changePW, verificationStyle, userAuthCode, deviceNo;

    //授權碼宣告為全域
    private int authCode = 0;

    //api
    private ApiProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //title隱藏
        setContentView(R.layout.activity_system_account);
        setTitle(getString(R.string.setting_account));

        initView();

        initData();

    }

    private void initData(){
        proxy = ApiProxy.getInstance();

        //取得授權碼
        getAuthCodeFromApi();
    }

    private void initView() {
        changePW = findViewById(R.id.ivChangePassword);
        verificationStyle = findViewById(R.id.ivVerificationStyle);
        userAuthCode = findViewById(R.id.ivUserAuthCode);
        deviceNo = findViewById(R.id.iVDeviceNo);

        changePW.setOnClickListener(this);
        verificationStyle.setOnClickListener(this);
        userAuthCode.setOnClickListener(this);
        deviceNo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ivChangePassword:  //變更密碼
                startActivity(new Intent(this, UserChangePassActivity.class));
                break;
            case R.id.ivVerificationStyle: //驗證方式
                startActivity(new Intent(this, UserChangeVerificActivity.class));
                break;
            case R.id.ivUserAuthCode:     //遠端授權碼
                dialogAuthCode();
                break;
            case R.id.iVDeviceNo:        //裝置序號
                startActivity(new Intent(this, UserDeviceActivity.class));
                break;
        }
    }

    //授權碼
    private void dialogAuthCode() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.update_auth_code));
        builder.setMessage(getString(R.string.please_press_submit));

        TextView textView = new TextView(this);
        textView.setText(String.valueOf(authCode));          //取得授權碼
        textView.setTextColor(Color.RED);
        textView.setTextSize(25);
        textView.setTypeface(null, Typeface.BOLD); //粗字體

        builder.setView(textView);

        //確定
        builder.setNeutralButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        //更新
        builder.setPositiveButton(getString(R.string.update), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                updateAuthCodeToApi(); //要求後台更新本機端的授權碼
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setView(textView, 350,0,0,0);
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setAllCaps(false);

    }

    //要求後台更新本機端的授權碼
    private void updateAuthCodeToApi() {
        proxy.buildPOST(MONITOR_CODE_RENEW, "", codeUpdateListener);
    }

    private ApiProxy.OnApiListener codeUpdateListener = new ApiProxy.OnApiListener() {
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
                            Toasty.success(SystemAccountActivity.this, getString(R.string.update_auth_code_success), Toast.LENGTH_SHORT, true).show();
                            //重新取得授權碼
                            getAuthCodeFromApi();
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

    //跟後台要求本機端的權限碼
    private void getAuthCodeFromApi() {
        proxy.buildPOST(MONITOR_CODE, "", requestCodeListener);
    }

    private ApiProxy.OnApiListener requestCodeListener = new ApiProxy.OnApiListener() {
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
                            authCode = object.getInt("success");
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