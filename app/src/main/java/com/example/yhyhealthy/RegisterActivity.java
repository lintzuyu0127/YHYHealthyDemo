package com.example.yhyhealthy;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.tools.ProgressDialogUtil;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthy.module.ApiProxy.REGISTER;

public class RegisterActivity extends AppPage {

    private static final String TAG = "RegisterActivity";

    private Button btnRegister;
    private EditText editAccount, editPassword, editEmail;
    private EditText editTelCode, editMobile;
    private TextInputLayout mailLayout, telCodeLayout, mobileLayout;

    private RadioGroup registerGroup;
    private String verification = "email";
    String emailPattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";

    //api
    ApiProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //title隱藏
        setContentView(R.layout.activity_register);

        proxy = ApiProxy.getInstance();

        initView();
    }

    private void initView() {
        editAccount = findViewById(R.id.edtAccountInput);
        editPassword = findViewById(R.id.edtPasswordInput);
        editEmail = findViewById(R.id.edtEmailInput);
        editTelCode = findViewById(R.id.edtTelCodeInput);
        editTelCode.setText("CN");                  //中國專用
        editTelCode.setFocusable(false);            //不得編輯
        editTelCode.setFocusableInTouchMode(false); //不得編輯
        editMobile = findViewById(R.id.edtMobileInput);

        mailLayout = findViewById(R.id.EmailLayout);
        telCodeLayout = findViewById(R.id.TelCodeLayout);
        mobileLayout = findViewById(R.id.MobileLayout);


        //使用信箱或簡訊註冊(RadioButton)
        registerGroup = findViewById(R.id.rdGroupRegister);
        registerGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rdoBtnEmail){
                    mailLayout.setVisibility(View.VISIBLE);
                    telCodeLayout.setVisibility(View.GONE);
                    mobileLayout.setVisibility(View.GONE);
                    verification = "email";
                }else{
                    mailLayout.setVisibility(View.GONE);
                    telCodeLayout.setVisibility(View.VISIBLE);
                    mobileLayout.setVisibility(View.VISIBLE);
                    verification = "phone";
                }
            }
        });

        btnRegister = findViewById(R.id.btnRegisterSend);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //帳號與密碼不得為空
                if(TextUtils.isEmpty(editAccount.getText().toString()) || TextUtils.isEmpty(editPassword.getText().toString()))
                    return;

                //帳號不得少於6
                if (editAccount.getText().toString().trim().length() < 6){
                    Toasty.error(RegisterActivity.this, getString(R.string.account_less_six), Toast.LENGTH_SHORT, true).show();
                    return;
                }

                //密碼不得少於6
                if(editPassword.getText().toString().trim().length() < 6){
                    Toasty.error(RegisterActivity.this, getString(R.string.password_less_six), Toast.LENGTH_SHORT, true).show();
                    return;
                }

                //信箱驗證
                if(verification.equals("email")){
                    if(TextUtils.isEmpty(editEmail.getText().toString())){
                        //信箱不得空白
                        Toasty.error(RegisterActivity.this, getString(R.string.please_input_email), Toast.LENGTH_SHORT, true).show();
                        return;
                    }else{
                        if (editEmail.getText().toString().trim().matches(emailPattern)){
                            //信箱格式正確寫回後端
                            updateToApi();

                        }else {
                            //請輸入正確的信箱位址
                            Toasty.error(RegisterActivity.this, getString(R.string.please_input_currect_address), Toast.LENGTH_SHORT, true).show();
                        }
                    }
                }

                //簡訊驗證
                if(verification.equals("phone")){
                    if(TextUtils.isEmpty(editTelCode.getText().toString()) || TextUtils.isEmpty(editMobile.getText().toString())){
                        //電話號碼不得為空白
                        Toasty.error(RegisterActivity.this, getString(R.string.please_input_phone), Toast.LENGTH_SHORT, true).show();
                        return;
                    }else {
                        //寫回後端
                        updateToApi();
                    }
                }

            }
        });
    }

    //將資料帶給後端
    private void updateToApi() {
        //取得手機語系
        String language = getResources().getConfiguration().locale.getLanguage();
        String country = getResources().getConfiguration().locale.getCountry();
        String defaultLan = language + "-" + country; //ex:zh-TW, zh-CN

        JSONObject json = new JSONObject();
        try {
            json.put("account", editAccount.getText().toString().trim());
            json.put("password", editPassword.getText().toString().trim());
            json.put("email", editEmail.getText().toString().trim());
            json.put("telCode", editTelCode.getText().toString().trim());
            json.put("mobile", editMobile.getText().toString().trim());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        proxy.buildRegister(REGISTER, json.toString(), defaultLan, registerListener);
    }

    private ApiProxy.OnApiListener registerListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            buildProgress(R.string.progressdialog_register, R.string.progressdialog_wait);
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
            hideProgress();
        }
    };

    //解析後台回覆的訊息
    private void parserJson(JSONObject result) {
        Log.d(TAG, "註冊完成後: " + result.toString());
        try {
            JSONObject object = new JSONObject(result.toString());
            int errorCode = object.getInt("errorCode");
            if (errorCode == 0){  //註冊成功後,後台會回傳是否需要開通用的訊息
                JSONObject success = object.getJSONObject("success");
                int code = success.getInt("statusCode");
                if (code == 1){  //尚未開通
                    Toasty.success(RegisterActivity.this, getString(R.string.register_success), Toast.LENGTH_LONG, true).show();
                    finish();
                }else if (code == 2){ //不需要開通
                    finish(); //結束此畫面
                }
            }else if (errorCode == 2){ //帳號已存在
                Toasty.error(RegisterActivity.this, getString(R.string.account_has_already), Toast.LENGTH_SHORT, true).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}