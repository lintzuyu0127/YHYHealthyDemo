package com.example.yhyhealthy;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yhyhealthy.dataBean.PeriodData;
import com.example.yhyhealthy.module.ApiProxy;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthy.module.ApiProxy.MENSTRUAL_RECORD_INFO;
import static com.example.yhyhealthy.module.ApiProxy.MENSTRUAL_RECORD_UPDATE;

/*****
 *  設定 - 個人設定 - 經期設定
 *  經期 : 28
 *  週期 : 5
 *  上次開始時間
 *  上次結束時間
 */

public class UserPeriodActivity extends AppPage implements View.OnClickListener {

    private static final String TAG = "UserPeriodActivity";

    EditText cycleLength;
    TextView lastDay, endDay;
    TextView periodLength;

    //api
    ApiProxy proxy;
    PeriodData periodData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //title隱藏
        setContentView(R.layout.activity_user_period);
        setTitle(getString(R.string.setting_period));
        setActionButton(R.drawable.ic_baseline_backup_white_32, save);

        initView();

        initData();
    }

    private void initData() {  //查詢
        proxy = ApiProxy.getInstance();

        //POST JSON Object
        proxy.buildPOST(MENSTRUAL_RECORD_INFO, "", periodListener);
    }

    private ApiProxy.OnApiListener periodListener = new ApiProxy.OnApiListener() {
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
                        if (errorCode == 0) {
                            parserJson(result);
                        }else  if (errorCode == 6){
                            setInit();  //新會員
                        }else {
                            Toasty.error(UserPeriodActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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

    //新會員fxn
    private void setInit(){
        String today = String.valueOf(LocalDate.now()); //Today
        cycleLength.setText("");
        periodLength.setText("");
        lastDay.setText(today);
        endDay.setText(today);
    }

    //解析後台來的資料
    private void parserJson(JSONObject result) {
        periodData = PeriodData.newInstance(result.toString());

        //週期長度
        String cycleSize = String.valueOf(periodData.getSuccess().getCycle());
        cycleLength.setText(cycleSize);
        cycleLength.setSelection(cycleSize.length()); //光標在字尾


        //經期長度
        periodLength.setText(String.valueOf(periodData.getSuccess().getPeriod()));

        //開始時間
        lastDay.setText(periodData.getSuccess().getLastDate());

        //結束時間
        endDay.setText(periodData.getSuccess().getEndDate());

    }

    private void initView() {
        cycleLength = findViewById(R.id.edtCycleLength);
        periodLength = findViewById(R.id.tvPeriodLength);
        lastDay = findViewById(R.id.tvDateStart);
        lastDay.addTextChangedListener(lastDayWatch); //起始日Listener
        endDay = findViewById(R.id.tvDateEnd);
        endDay.addTextChangedListener(endDayWatch); //結束日Listener

        lastDay.setOnClickListener(this);
        endDay.setOnClickListener(this);
    }

    //起始日Listener
    private TextWatcher lastDayWatch = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //不得選取未來日期
            checkRangeDays(lastDay.getText().toString());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    //結束日Listener
    private TextWatcher endDayWatch = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            calculate();  //計算經期OnClick
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    //資料檢查
    private View.OnClickListener save = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            checkBeforeUpdate();
        }
    };

    //檢查上傳的資訊是否齊全
    private void checkBeforeUpdate() {

        if(TextUtils.isEmpty(lastDay.getText().toString())){
            Toasty.error(UserPeriodActivity.this, getString(R.string.start_is_not_empty), Toast.LENGTH_SHORT, true).show();
            return;
        }

        if(TextUtils.isEmpty(endDay.getText().toString())){
            Toasty.error(UserPeriodActivity.this, getString(R.string.end_is_not_empty), Toast.LENGTH_SHORT, true).show();
            return;
        }

        if(TextUtils.isEmpty(cycleLength.getText().toString())){
            Toasty.error(UserPeriodActivity.this, getString(R.string.cycle_is_not_empty), Toast.LENGTH_SHORT, true).show();
            return;
        }

        if(TextUtils.isEmpty(periodLength.getText().toString())){
            Toasty.error(UserPeriodActivity.this, getString(R.string.period_is_not_empty), Toast.LENGTH_SHORT, true).show();
            return;
        }

        //上傳前先計算一下經期
        calculate();

        //上傳更新資料
        updateToApi();
    }

    //上傳至後台
    private void updateToApi() {
        JSONObject json = new JSONObject();
        try {
            json.put("cycle", cycleLength.getText().toString());
            json.put("period",periodLength.getText().toString());
            json.put("lastDate",lastDay.getText().toString());
            json.put("endDate", endDay.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //執行上傳
        proxy.buildPOST(MENSTRUAL_RECORD_UPDATE, json.toString(), changePeriodListener);
    }

    private ApiProxy.OnApiListener changePeriodListener = new ApiProxy.OnApiListener() {
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
                            Toasty.success(UserPeriodActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT, true).show();
                            writeToSharePreferences();
                        }else {
                            Toasty.error(UserPeriodActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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

    //經期設定寫入local
    private void writeToSharePreferences() {
        SharedPreferences pref = getSharedPreferences("yhyHealthy", MODE_PRIVATE);
        pref.edit().putInt("PERIOD", Integer.parseInt(periodLength.getText().toString())).apply();
        pref.edit().putInt("CYCLE", Integer.parseInt(cycleLength.getText().toString())).apply();
        pref.edit().putBoolean("MENSTRUAL", true).apply();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tvDateStart:
                pickLastDate();
                break;
            case R.id.tvDateEnd:
                pickEndDate();
                break;
        }
    }

    //檢查使用者輸入的日期是否為未來日期
    private void checkRangeDays(String days){
        Calendar calendar = Calendar.getInstance();
        CharSequence sequence = DateFormat.format("yyyy-MM-dd", calendar.getTime());
        DateTime today = new DateTime(sequence); //今天
        DateTime userDay = new DateTime(days);   //使用者傳來的日期

        if(today.isBefore(userDay)){
            Toasty.error(UserPeriodActivity.this, getString(R.string.days_is_not_allow_future), Toasty.LENGTH_SHORT, true).show();
            periodLength.setText("");
        }
    }

    //計算經期長度
    private void calculate() {
        //起始日與結束日不得空白
        if(TextUtils.isEmpty(lastDay.getText().toString()) || TextUtils.isEmpty(endDay.getText().toString()))
            return;

        Calendar mCalendar = Calendar.getInstance();
        CharSequence sequence = DateFormat.format("yyyy-MM-dd", mCalendar.getTime());
        DateTime today = new DateTime(sequence);

        //使用第三方庫 Joda-time
        DateTime d1 = new DateTime(lastDay.getText().toString());
        DateTime d2 = new DateTime(endDay.getText().toString());

        //結束日不得小於起始日
        if (d2.isBefore(d1)){
            Toasty.error(UserPeriodActivity.this, getString(R.string.end_is_not_before_last), Toasty.LENGTH_SHORT, true).show();
            return;
        }

        //禁止選擇未來日期
        if(today.isBefore(d1) || today.isBefore(d2)){
            Toasty.error(UserPeriodActivity.this, getString(R.string.days_is_not_allow_future), Toasty.LENGTH_SHORT, true).show();
            periodLength.setText("");
            return;
        }

        int days = Days.daysBetween(d1,d2).getDays() + 1;
        periodLength.setText(String.valueOf(days));
    }

    //經期結束日
    private void pickEndDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        DatePickerDialog pickerDialog = new DatePickerDialog(UserPeriodActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar start = Calendar.getInstance();
                start.set(year, month, dayOfMonth);
                endDay.setText(dateFormat.format(start.getTime()));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        pickerDialog.show();
    }

    //經期開始日
    private void pickLastDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        DatePickerDialog pickerDialog = new DatePickerDialog(UserPeriodActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar start = Calendar.getInstance();
                start.set(year, month, dayOfMonth);
                lastDay.setText(dateFormat.format(start.getTime()));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        pickerDialog.show();
    }
}