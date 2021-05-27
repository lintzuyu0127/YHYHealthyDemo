package com.example.yhyhealthy;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.tools.ProgressDialogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthy.module.ApiProxy.COMP;
import static com.example.yhyhealthy.module.ApiProxy.LOGIN;
import static com.example.yhyhealthy.module.ApiProxy.marriageSetting;
import static com.example.yhyhealthy.module.ApiProxy.menstrualSetting;
import static com.example.yhyhealthy.module.ApiProxy.userSetting;

/** *** ***
 * 登入頁面
 * 帳號開通與否需要檢查驗證碼
 * 登入成功後將帳號密碼婚姻狀況經期設定寫入檔案
 * 忘記密碼
 * * ****** ******/

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";

    Button login;
    EditText account, password;
    TextView register, forget;

    //api
    ApiProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_login);

        //hide keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        proxy = ApiProxy.getInstance();

        //init
        initView();

    }

    private void initView() {
        login = findViewById(R.id.btnLogin);
        account = findViewById(R.id.edtAccount);
        password = findViewById(R.id.edtPassword);
        //暫時
        account.setText("demo24");
        password.setText("123456");

        register = findViewById(R.id.textRegister);
        forget = findViewById(R.id.textForget);

        login.setOnClickListener(this);
        register.setOnClickListener(this);
        forget.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnLogin:      //成功後登入
                userLoginApi();
//                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                startActivity(intent);
//                finish();
                break;
            case R.id.textRegister:    //註冊onClick
                startActivity(new Intent(getBaseContext(), PrivacyActivity.class)); //隱私權頁面
                break;
            case R.id.textForget:     //忘記密碼
                startActivity(new Intent(getBaseContext(), ForgetPassActivity.class));
                break;
        }
    }

    //登入後跟後台要token
    private void userLoginApi() {

        //帳號與密碼不得為空
        if(TextUtils.isEmpty(account.getText().toString()) || TextUtils.isEmpty(password.getText().toString())){
            Toasty.error(LoginActivity.this, R.string.account_not_empty, Toast.LENGTH_SHORT, true).show();
            return;
        }

        //需要給後台的info : account & password
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("account", account.getText().toString());
            jsonObject.put("password", password.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        proxy.buildLogin(LOGIN, jsonObject.toString(), loginListener);
    }

    private ApiProxy.OnApiListener loginListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
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
            Log.d(TAG, "onFailure: ");
        }

        @Override
        public void onPostExecute() {
        }
    };

    //解析後台回傳的資訊
    private void parser(JSONObject result) {
        try {
            JSONObject object = new JSONObject(result.toString());
            int errorCode = object.getInt("errorCode");
            if(errorCode == 1){ //密碼或帳號錯誤
                Toasty.error(LoginActivity.this, getString(R.string.account_is_error), Toast.LENGTH_SHORT, true).show();

            } else if (errorCode == 6) {
                Toasty.error(LoginActivity.this, getString(R.string.account_not_register), Toast.LENGTH_SHORT, true).show();

            } else if (errorCode == 34) { //帳號未開通
                showCompInfo(); //驗證碼彈跳視窗

            } else if (errorCode == 0) { //登入成功
                //解析success內容並賦予全域變數
                JSONObject success = object.getJSONObject("success");
                marriageSetting = success.getBoolean("maritalSet");
                menstrualSetting = success.getBoolean("menstrualSet");
                userSetting = success.getBoolean("userSet");

                Toasty.success(LoginActivity.this, getString(R.string.login_success), Toast.LENGTH_SHORT, true).show();

                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();

            }else {
                Toasty.error(LoginActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //驗證碼Dialog
    private void showCompInfo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.please_input_comp_code));
        builder.setMessage(getString(R.string.need_comp_code));

        //set custom layout
        View compLayout = getLayoutInflater().inflate(R.layout.dialog_comp, null );
        builder.setView(compLayout);
        builder.setCancelable(false);

        //add ok button
        builder.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText edtCompCode = compLayout.findViewById(R.id.edtCompCode);
                if(TextUtils.isEmpty(edtCompCode.getText().toString())){
                    Toasty.error(LoginActivity.this, getString(R.string.comp_code_is_not_empty), Toast.LENGTH_SHORT, true).show();
                    return;
                }
                //傳至後台驗證
                checkCompCode(edtCompCode);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false); //英文字小寫顯示
    }

    //後台驗證(比對)
    private void checkCompCode(EditText edtCompCode) {
        JSONObject json = new JSONObject();
        try {
            json.put("account", account.getText().toString());
            json.put("verCode", edtCompCode.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        proxy.buildInit(COMP, json.toString(), compCodeListener);
    }

    private ApiProxy.OnApiListener compCodeListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            //buildProgress(R.string.progressdialog_else, R.string.progressdialog_wait);
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

        }

        @Override
        public void onPostExecute() {
            //hideProgress();
        }
    };

    private void parserJson(JSONObject result) {
        try {
            JSONObject object = new JSONObject(result.toString());
            int errorCode = object.getInt("errorCode");
            if(errorCode == 5){
                Toasty.error(LoginActivity.this, getString(R.string.comp_code_error), Toast.LENGTH_SHORT, true).show();
            }else if (errorCode == 0){
                Toasty.success(LoginActivity.this, getString(R.string.comp_code_correct), Toast.LENGTH_SHORT, true).show();
            }else {
                Toasty.error(LoginActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}