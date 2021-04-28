package com.example.yhyhealthy;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yhyhealthy.dataBean.ChangeUserBasic;
import com.example.yhyhealthy.dataBean.UsersData;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.tools.ProgressDialogUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthy.module.ApiProxy.USER_INFO;
import static com.example.yhyhealthy.module.ApiProxy.USER_UPDATE;

/**
 * 設定 - 個人設定 - 基本資料
 * */

public class UserBasicActivity extends AppPage implements View.OnClickListener {

    private static final String TAG = "UserBasicActivity";

    TextView accountInfo;
    TextView genderInfo, userBirthday, BMIValue;
    EditText accountName, userMail, telCode, mobileNo, userHeight, userWeight;

    //api
    ApiProxy proxy;
    UsersData usersData;
    ChangeUserBasic changeUserBasic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //title隱藏
        setContentView(R.layout.activity_user_basic);
        setTitle(R.string.setting_basic_info);
        setActionButton(R.drawable.ic_baseline_backup_white_32, save);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        initView();

        initData();

    }

    private void initData() {
        proxy = ApiProxy.getInstance();
        //取得使用者基本資料Api
        proxy.buildPOST(USER_INFO, "", userInfoListener);
        //修改使用者基本資料Api
        changeUserBasic = new ChangeUserBasic();
    }
    private ApiProxy.OnApiListener userInfoListener = new ApiProxy.OnApiListener() {
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
                            parserJson(result);
                        }else {
                            Toasty.error(UserBasicActivity.this,getString(R.string.json_error_code) + errorCode, Toasty.LENGTH_SHORT, true).show();
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

    //查詢-解析後台來的資料
    private void parserJson(JSONObject result) {
        //取得所有的資訊
        usersData = UsersData.newInstance(result.toString());

        //帳號
        accountInfo.setText(usersData.getSuccess().getUserAccount());

        //名稱
        accountName.setText(usersData.getSuccess().getName());

        //性別
        if(usersData.getSuccess().getGender().equals("F")){
            genderInfo.setText(getString(R.string.female));
            changeUserBasic.setGender("F");  //女性
        }else {
            genderInfo.setText(getString(R.string.male));
            changeUserBasic.setGender("M");  //男性
        }

        //信箱
        userMail.setText(usersData.getSuccess().getEmail());

        //生日
        userBirthday.setText(usersData.getSuccess().getBirthday());
        changeUserBasic.setBirthday(usersData.getSuccess().getBirthday());

        //國際區碼
        telCode.setText(usersData.getSuccess().getTelCode());

        //電話號碼
        mobileNo.setText(usersData.getSuccess().getMobile());

        //身高
        userHeight.setText(String.valueOf(usersData.getSuccess().getHeight()));
        double bodyHeight = usersData.getSuccess().getHeight();

        //體重
        userWeight.setText(String.valueOf(usersData.getSuccess().getWeight()));
        double bodyWeight = usersData.getSuccess().getWeight();

        //BMI計算
        if(bodyWeight == 0 || bodyHeight == 0){
         BMIValue.setText(getString(R.string.need_weight_and_height));
        }else {
            calculate(bodyHeight, bodyWeight);
        }
    }

    //BMI計算
    private void calculate(double height, double weight) {
        float h = (float) height/100;
        float value = (float) weight/(h*h);
        BMIValue.setText(String.valueOf(value));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initView() {
        accountInfo = findViewById(R.id.tvUserAccount);  //帳戶資訊
        accountName = findViewById(R.id.edtChangeName);  //名稱
        accountName.setInputType(InputType.TYPE_NULL);   //hide keyboard
        accountName.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                accountName.setInputType(InputType.TYPE_CLASS_TEXT);
                accountName.onTouchEvent(event);
                return true;
            }
        });

        genderInfo = findViewById(R.id.tvGender);      //性別
        userMail = findViewById(R.id.edtEmailAddress); //信箱
        userBirthday = findViewById(R.id.tvBornDay);   //生日
        telCode = findViewById(R.id.edtTelCode);       //國際區碼
        mobileNo = findViewById(R.id.edtPhoneNumber);  //電話號碼
        userHeight = findViewById(R.id.editHeight);     //身高
        userWeight = findViewById(R.id.editWeight);      //體重
        userWeight.addTextChangedListener(weightListener); //體重Listener
        BMIValue = findViewById(R.id.tvBMI);            //BMI

        genderInfo.setOnClickListener(this);            //性別onClick
        userBirthday.setOnClickListener(this);          //生日onClick
    }

    //資料檢查
    private View.OnClickListener save = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            checkBeforeUpdate();
        }
    };

    //上傳前先檢查資料是否齊全
    private void checkBeforeUpdate() {

        //名稱不得空白
        if(TextUtils.isEmpty(accountName.getText().toString())){
            Toasty.error(UserBasicActivity.this, getString(R.string.name_is_not_empty), Toast.LENGTH_SHORT, true).show();
            return;
        }

        //信箱不得空白
        if(TextUtils.isEmpty(userMail.getText().toString())){
            Toasty.error(UserBasicActivity.this, getString(R.string.mail_is_not_empty), Toast.LENGTH_SHORT, true).show();
            return;
        }

        //生日不得空白
        if(TextUtils.isEmpty(userBirthday.getText().toString())){
            Toasty.error(UserBasicActivity.this, getString(R.string.birthday_is_not_empty), Toast.LENGTH_SHORT, true).show();
            return;
        }

        //體重不得空白
        if(TextUtils.isEmpty(userWeight.getText().toString())){
            Toasty.error(UserBasicActivity.this, getString(R.string.weight_is_not_empty), Toast.LENGTH_SHORT, true).show();
            return;
        }

        //身高不得空白
        if(TextUtils.isEmpty(userHeight.getText().toString())){
            Toasty.error(UserBasicActivity.this, getString(R.string.height_is_not_empty), Toast.LENGTH_SHORT, true).show();
            return;
        }

        updateToApi(); //上傳至後台
    }

    private void updateToApi() {
        changeUserBasic.setUserAccount(accountInfo.getText().toString());
        changeUserBasic.setName(accountName.getText().toString());
        changeUserBasic.setEmail(userMail.getText().toString());
        changeUserBasic.setTelCode(telCode.getText().toString());
        changeUserBasic.setMobile(mobileNo.getText().toString());
        changeUserBasic.setHeight(Double.parseDouble(userHeight.getText().toString()));
        changeUserBasic.setWeight(Double.parseDouble(userWeight.getText().toString()));

        Log.d(TAG, "上傳到後台的資料: " + changeUserBasic.toJSONString());

        proxy.buildPOST(USER_UPDATE, changeUserBasic.toJSONString(), changeInfoListener);
    }

    private ApiProxy.OnApiListener changeInfoListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            buildProgress(R.string.progressdialog_else, R.string.progressdialog_wait);
        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //將使用者的基本資料往後台傳送回覆的結果
                    try {
                        JSONObject jsonObject = new JSONObject(result.toString());
                        int errorCode = jsonObject.getInt("errorCode");
                        if (errorCode == 0){
                            Toasty.success(UserBasicActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT, true).show();
                        }else {
                            Toasty.error(UserBasicActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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

    //體重EditText's Listener
    private TextWatcher weightListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            double w = Double.parseDouble(userWeight.getText().toString()); //體重
            double h = Double.parseDouble(userHeight.getText().toString()); //身高
            calculate(h,w);
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvGender:   //性別onClick
                dialogGender();
                break;
            case R.id.tvBornDay:  //生日onClick
                dialogBirthday();
                break;
        }
    }

    //生日彈跳視窗
    private void dialogBirthday() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        DatePickerDialog pickerDialog = new DatePickerDialog(UserBasicActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar start = Calendar.getInstance();
                start.set(year, month, dayOfMonth);
                userBirthday.setText(dateFormat.format(start.getTime()));
                changeUserBasic.setBirthday(userBirthday.getText().toString());
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        pickerDialog.show();
    }

    //性別彈跳視窗
    private void dialogGender() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.please_input_gender));
        String[] genderItems = { getString(R.string.female), getString(R.string.male)};
        int checkedItem = 0;
        builder.setSingleChoiceItems(genderItems, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case 0: // 女性
                            genderInfo.setText(getString(R.string.female));
                            changeUserBasic.setGender("F");
                            dialog.dismiss();
                            break;
                        case 1: //男性
                            genderInfo.setText(getString(R.string.male));
                            changeUserBasic.setGender("M");
                            dialog.dismiss();
                            break;
                    }
            }
        });
        AlertDialog alert = builder.create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }
}