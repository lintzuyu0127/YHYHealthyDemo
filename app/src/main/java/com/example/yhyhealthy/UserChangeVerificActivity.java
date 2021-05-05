package com.example.yhyhealthy;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.yhyhealthy.module.ApiProxy;
import com.google.android.material.textfield.TextInputLayout;
import org.json.JSONException;
import org.json.JSONObject;
import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthy.module.ApiProxy.CHANGE_VERIFICATION_STYLE;
import static com.example.yhyhealthy.module.ApiProxy.COMP;

/****
 * 更新驗證方式
 *******/
public class UserChangeVerificActivity extends AppPage {

    private static final String TAG = "UserChangeVerificActivi";

    private TextInputLayout mailLayout, mobileLayout;
    private EditText editMail, editMobile;
    private RadioGroup styleGroup;
    private EditText password;
    private String style = "mail";

    //api
    ApiProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //title隱藏
        setContentView(R.layout.activity_user_change_verific);
        setTitle(getString(R.string.Title_change_verification_style));
        setActionButton(R.string.update, save);

        proxy = ApiProxy.getInstance();

        initView();

    }

    private void initView() {
        password = findViewById(R.id.edtStylePassword);
        editMail = findViewById(R.id.edtEmailStyle);
        editMobile =findViewById(R.id.edtPhoneStyle);

        mailLayout = findViewById(R.id.MailStyleLayout);
        mobileLayout = findViewById(R.id.PhoneStyleLayout);

        styleGroup = findViewById(R.id.rdGroupStyle);
        styleGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rdoEmailStyle){
                    mailLayout.setVisibility(View.VISIBLE);
                    mobileLayout.setVisibility(View.GONE);
                    style = "mail";
                }else {
                    mailLayout.setVisibility(View.GONE);
                    mobileLayout.setVisibility(View.VISIBLE);
                    style = "mobile";
                }
            }
        });
    }

    //資料檢查
    private View.OnClickListener save = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            checkBeforeUpdate();  //檢查資料的完整性
        }
    };

    //上傳前檢查資料是否齊全
    private void checkBeforeUpdate() {

        //檢查密碼
        if (TextUtils.isEmpty(password.getText().toString())){
            Toasty.error(UserChangeVerificActivity.this,getString(R.string.password_is_empty), Toasty.LENGTH_SHORT,true).show();
            return;
        }
        
        updateToApi();
    }

    private void updateToApi() {
        String language = getResources().getConfiguration().locale.getLanguage();
        String country = getResources().getConfiguration().locale.getCountry();
        String defaultLan = language + "-" + country;

        JSONObject json = new JSONObject();
        try {
            json.put("password", password.getText().toString());
            json.put("email", editMail.getText().toString());
            json.put("telCode", "CN"); //中國專用
            json.put("mobile", editMobile.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "updateToApi: " + json.toString());
        //執行後台
        proxy.buildVerification(CHANGE_VERIFICATION_STYLE, json.toString(), defaultLan, verificationChangeListener);
    }
    private ApiProxy.OnApiListener verificationChangeListener = new ApiProxy.OnApiListener() {
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
                        if (errorCode == 0){ //修改成功
                            JSONObject success = object.getJSONObject("success");
                            int code = success.getInt("statusCode");
                            if(code == 1){
                                showCompCode(); //需要開通帳號
                            }else if(code == 2){
                                finish();
                            }
                        }else { //修改失敗
                            Toasty.error(UserChangeVerificActivity.this, getString(R.string.failure), Toasty.LENGTH_SHORT,true).show();
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

    //輸入驗證碼以完成更新 2021/02/03 leona
    private void showCompCode() {
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
                    Toasty.error(UserChangeVerificActivity.this, getString(R.string.comp_code_is_not_empty), Toast.LENGTH_SHORT, true).show();
                    return;
                }
                //傳至後台驗證
                checkCompCode(edtCompCode);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //比對驗證碼
    private void checkCompCode(EditText edtCompCode) {
        String account = getSharedPreferences("yhyHealthy", MODE_PRIVATE).getString("ACCOUNT", "");
        JSONObject json = new JSONObject();
        try {
            json.put("account", account);
            json.put("verCode", edtCompCode.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        proxy.buildInit(COMP, json.toString(), compCodeListener);
    }

    private ApiProxy.OnApiListener compCodeListener = new ApiProxy.OnApiListener() {
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
                        if(errorCode == 5){  //驗證碼失敗
                            Toasty.error(UserChangeVerificActivity.this, getString(R.string.comp_code_error), Toast.LENGTH_SHORT, true).show();
                        }else if (errorCode == 0){  //驗證碼成功
                            Toasty.success(UserChangeVerificActivity.this, getString(R.string.change_verification_success), Toast.LENGTH_SHORT, true).show();
                            finish();
                        }else {
                            Toasty.error(UserChangeVerificActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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