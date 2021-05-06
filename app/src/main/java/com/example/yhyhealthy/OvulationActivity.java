package com.example.yhyhealthy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.example.yhyhealthy.calendar.MyEventDecorator;
import com.example.yhyhealthy.calendar.MySelectorDecorator;
import com.example.yhyhealthy.calendar.OneDayDecorator;
import com.example.yhyhealthy.dataBean.CycleMath;
import com.example.yhyhealthy.dataBean.CycleRecord;
import com.example.yhyhealthy.dataBean.Menstruation;
import com.example.yhyhealthy.dataBean.PeriodData;
import com.example.yhyhealthy.dataBean.Record;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.tools.CombinedChartManager;
import com.example.yhyhealthy.tools.MPAChartManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;
import com.prolificinteractive.materialcalendarview.OnMonthChangedListener;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.Period;
import org.threeten.bp.temporal.ChronoUnit;
import org.threeten.bp.temporal.TemporalAdjusters;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import static com.example.yhyhealthy.module.ApiProxy.CYCLE_RECORD;
import static com.example.yhyhealthy.module.ApiProxy.MENSTRUAL_RECORD_INFO;
import static com.example.yhyhealthy.module.ApiProxy.PERIOD_DELETE;
import static com.example.yhyhealthy.module.ApiProxy.PERIOD_UPDATE;
import static com.example.yhyhealthy.module.ApiProxy.RECORD_INFO;

public class OvulationActivity extends AppPage implements View.OnClickListener, OnDateSelectedListener {

    private static final String TAG = "OvulationActivity";

    private Button calendar, chart;

    private TextView periodShowDay, temperature, ovulationResult;

    //layout
    private LinearLayout chartLayout;
    private ScrollView scrollView;

    private Button btnSetting, btnEditor;
    private RatingBar bodySalivaRate, bodyDegreeRate;

    //圖表
    private CombinedChart combinedChart;
    private TextView  periodRange; //圖表日期範圍
    private ImageView preMonth, nextMonth; //上個月&下個月按鈕

    //月曆
    private MaterialCalendarView widget;
    private OneDayDecorator oneDayDecorator;
    private String selectedDay;
    private String firstDayOfThisMonth;
    private String lastDayOfThisMonth;

    //api
    private Record record;
    private ApiProxy proxy;
    private CycleRecord cycleRecord;
    private PeriodData periodData;

    //
    private AlertDialog dialog;

    private List<CycleRecord.SuccessBean> dataList;
    private CycleMath math;

    //
    private static final int PERIOD_RECORD = 1;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //title隱藏
        setContentView(R.layout.activity_ovulation);
        setTitle(R.string.title_ovul);

        //休眠禁止
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initData();

        initView();

        //月曆初始化
        initCalendar();
    }

    //初始化dataBean & Api
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initData() {
        record = new Record();
        cycleRecord = new CycleRecord();
        proxy = ApiProxy.getInstance();

        oneDayDecorator = new OneDayDecorator(this);

        firstDayOfThisMonth = String.valueOf(LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).plusDays(-5)); //起始日減5天
        lastDayOfThisMonth = String.valueOf(LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).plusDays(+5)); //結束日加5天
    }

    private void initView() {
        calendar = findViewById(R.id.btnCalendar);
        chart = findViewById(R.id.btnChart);
        scrollView = findViewById(R.id.lyScrollView);    //經期解釋layout
        chartLayout = findViewById(R.id.lyChart);        //圖表layout

        periodShowDay = findViewById(R.id.tvShowPeriodDay);      //今天是週期第?天Text
        ovulationResult = findViewById(R.id.tvIdentify);      //唾液辨識結果Text
        temperature = findViewById(R.id.tvShowDegree);       //基礎體溫Text
        bodySalivaRate = findViewById(R.id.rtSaliva);          //唾液辨識機率Rate
        bodyDegreeRate = findViewById(R.id.rtBt);              //基礎體溫機率Rate

        btnSetting = findViewById(R.id.btnPeriodSetting);    //經期設定Button
        btnEditor = findViewById(R.id.btnPeriodWrite);      //新增紀錄Button

        //月曆 2021/03/01
        widget = findViewById(R.id.calendar);
        widget.setOnDateChangedListener(this);
        widget.setShowOtherDates(MaterialCalendarView.SHOW_ALL);
        LocalDate instance = LocalDate.now();  //第三方日期套件
        widget.setSelectedDate(instance);

        calendar.setOnClickListener(this);          //月曆onclick
        chart.setOnClickListener(this);             //圖表onclick
        btnSetting.setOnClickListener(this);       //經期設定onclick
        btnEditor.setOnClickListener(this);        //經期編輯onclick

        //圖表:第三方庫
        //lineChart = findViewById(R.id.lineChart);
        combinedChart = findViewById(R.id.chart);
        periodRange = findViewById(R.id.tvMonthName);  //圖表經期日期範圍
        preMonth = findViewById(R.id.imgPreMonth);
        nextMonth = findViewById(R.id.imgNextMonth);
        preMonth.setOnClickListener(this);
        nextMonth.setOnClickListener(this);

        //月曆先顯示
        calendar.setBackgroundResource(R.drawable.shape_temp_button);
        scrollView.setVisibility(View.VISIBLE);
    }

    //月曆的初始化
    private void initCalendar() {

        //點擊日期後的背景
//        widget.addDecorators(
//                new MySelectorDecorator(this),
//                oneDayDecorator
//        );
        widget.addDecorator(oneDayDecorator);

        //跟後台api要週期資料
        setCycleData(firstDayOfThisMonth, lastDayOfThisMonth);

        //週期第幾天?
        checkPeriodDayOfThisMonth(LocalDate.now());

        //檢查今天是否有資料
        checkTodayInfo(String.valueOf(LocalDate.now()));

        //監聽月曆滑動
        monthListener();
    }

    //監聽月曆滑動
    private void monthListener() {
        widget.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {

                String firstDay = String.valueOf(LocalDate.from(date.getDate()).with(TemporalAdjusters.firstDayOfMonth()).plusDays(-5));
                String lastDay = String.valueOf(LocalDate.from(date.getDate()).with(TemporalAdjusters.lastDayOfMonth()).plusDays(+5));

                firstDayOfThisMonth = firstDay;
                lastDayOfThisMonth = lastDay;

                setCycleData(firstDayOfThisMonth, lastDayOfThisMonth);  //週期資料
                widget.removeDecorators();            //清除之前殘留的Decorator
                widget.addDecorator(oneDayDecorator); //重繪
            }
        });

    }

    //向後台詢問單日是否有資料
    private void checkTodayInfo(String dayStr) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("testDate", dayStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        proxy.buildPOST(RECORD_INFO, jsonObject.toString(), requestListener);
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
                    try {
                        JSONObject object = new JSONObject(result.toString());
                        int errorCode = object.getInt("errorCode");
                        if (errorCode == 0){
                            parserJson(result); //解析json資料
                        }else {
                            Toasty.error(OvulationActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void onFailure(String message) {
            Log.d(TAG, "onFailure: 經期紀錄 Failure");
        }

        @Override
        public void onPostExecute() {
            hideProgress();
        }
    };

    //2021/01/13 解析單日資料
    @SuppressLint("SetTextI18n")
    private void parserJson(JSONObject result) {
        Log.d(TAG, "parserJson: " + result.toString());
        record = Record.newInstance(result.toString());

        //唾液辨識結果
        String paramName = record.getSuccess().getMeasure().getParamName();
        if(!paramName.equals("")){
            btnEditor.setText(getString(R.string.ovual_edit));
            switch (paramName) {
                case "Ovulation":
                    ovulationResult.setText(getString(R.string.in_period));
                    break;
                case "General":
                    ovulationResult.setText(getString(R.string.non_period));
                    break;
                case "FollicularORLutealPhase":
                    ovulationResult.setText(getString(R.string.in_low_cell));
                    break;
                case "HighFollicularORLutealPhase":
                    ovulationResult.setText(getString(R.string.in_high_cell));
                    break;
                case "Unrecognizable":
                    ovulationResult.setText(getString(R.string.unknow));
                    break;
            }
        }else {
            btnEditor.setText(getString(R.string.ovual_add));
            ovulationResult.setText("");
        }

        //基礎體溫
        String bodyDegree = String.valueOf(record.getSuccess().getMeasure().getTemperature());
        temperature.setText(bodyDegree + " \u2103");

        //根據唾液辨識結果得到的機率
        int salivaRate = record.getSuccess().getOvuRate().getSalivaRate();
        bodySalivaRate.setRating(salivaRate);

        //根據體溫結果得到的機率
        int btRate = record.getSuccess().getOvuRate().getBtRate();
        bodyDegreeRate.setRating(btRate);

    }

    //週期月曆json
    private void setCycleData(String startDay, String endDay) {
        JSONObject json = new JSONObject();
        try {
            json.put("startDate", startDay);
            json.put("endDate", endDay);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        proxy.buildPOST(CYCLE_RECORD, json.toString(), cycleRecordListener);
    }

    private ApiProxy.OnApiListener cycleRecordListener = new ApiProxy.OnApiListener() {
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
                            parserCycleData(result); //解析後台回來的資料
                        }else {
                            Toasty.error(OvulationActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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

    //2021/03/04 leona 解析後台回來的週期資料
    private void parserCycleData(JSONObject result) {
        cycleRecord = CycleRecord.newInstance(result.toString());
        Log.d(TAG, "解析後台回來的週期資料: " + cycleRecord.toJSONString());

        List<String> firstPeriodDayList = new ArrayList<>(); //經期第一天陣列

        dataList = cycleRecord.getSuccess();

        for (int i = 0; i < dataList.size(); i ++){

            math = new CycleMath(this, dataList.get(i));

            //月曆
            if (math.getCalenderDrawable() != null)
             widget.addDecorator(new MyEventDecorator(math.getCalenderDrawable(), Collections.singletonList(math.getDateData())));

            //經期第一天 2021/03/04
            boolean isFirstDay = dataList.get(i).isFirstDay();
            if (isFirstDay){
                String dayStr = dataList.get(i).getTestDate(); //偶會有兩個日期
                firstPeriodDayList.add(dayStr);
            }
        }

        //圖表 2021/03/10
        CombinedChartManager chartManager = new CombinedChartManager(this, combinedChart);
        chartManager.showCombinedChart(dataList);

        //經期第一天寫入sharePref 2021/03/04
        if(firstPeriodDayList.get(0) != null){
            SharedPreferences pref = getSharedPreferences("yhyHealthy", MODE_PRIVATE);
            pref.edit().putString("BEGIN", firstPeriodDayList.get(0)).apply();
        }

    }

    //週期第幾天 2021/03/02
    private void checkPeriodDayOfThisMonth(LocalDate day) {
        //先取得存在sharePerf內的經期第一天資料
        String beginStr = getSharedPreferences("yhyHealthy" , MODE_PRIVATE).getString("BEGIN", "");

        if (TextUtils.isEmpty(beginStr)){
            periodShowDay.setText(getString(R.string.period_Day_no_data));
        }else {
            LocalDate begin = LocalDate.parse(beginStr);
            long numOfDays = ChronoUnit.DAYS.between(begin, day);
            if (numOfDays >= 0){
                numOfDays = numOfDays + 1;
                periodShowDay.setText(getString(R.string.period_day) + numOfDays + getString(R.string.day));
            }else {
                periodShowDay.setText(getString(R.string.period_out_range));
            }
        }

    }

    //導引到編輯的頁面
    private void periodEdit(String strDay) {
        //如果使用者沒有選擇日期則以今天為主
        if (strDay == null){
            strDay = String.valueOf(LocalDate.now());
        }

        Intent intent = new Intent();
        intent.setClass(OvulationActivity.this, RecordActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString("DAY", strDay);//將使用者點擊的日期傳遞到RecordActivity
        intent.putExtras(bundle);
        //startActivity(intent);
        startActivityForResult(intent, PERIOD_RECORD);
    }

    //經期設定對話框 20201/02/23
    private void showPeriodDialog(String daySelect) {
        //如果使用者沒有選擇日期則以今天為主
        if (daySelect == null){
            daySelect = String.valueOf(LocalDate.now()); //Today
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_period, null);
        builder.setView(view);

        //init dialog function 2021/03/02 need to reDesign
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        final Calendar c = Calendar.getInstance(Locale.getDefault());

        final EditText toDate = view.findViewById(R.id.text_to_date);      //起始日期
        final EditText fromDate = view.findViewById(R.id.text_from_date);  //結束日期

        toDate.setText(daySelect); //由系統根據使用者選擇的日期自動戴入
        DateTime startDay = new DateTime(daySelect);

        //2021/03/04 由後台取得週期&經期天數的資料
        checkPeriodDayInfo();

        //經由使用者輸入的經期長度自動計算結束日期
        int periodLength = getSharedPreferences("yhyHealthy" , MODE_PRIVATE).getInt("PERIOD", 0) - 1;
        DateTime endDay = startDay.plusDays(periodLength);
        fromDate.setText(endDay.toString("yyyy-MM-dd"));  //自動計算結束日期

        //button'init
        final Button cancel = view.findViewById(R.id.btnDateCancel);
        final Button delete = view.findViewById(R.id.btnDateDelete);
        final Button btnUpdate = view.findViewById(R.id.btnDateSave);

        dialog = builder.create();

        //結束日
        fromDate.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(OvulationActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                //todo
                                Calendar newDate = Calendar.getInstance();
                                newDate.set(year, month, dayOfMonth);
                                fromDate.setText(dateFormat.format(newDate.getTime()));
                            }
                        },c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        //取消
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        //更新api
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //將經期第一天寫入檔案
                WriteDateIntoSharePref(toDate.getText().toString(), fromDate.getText().toString());
                //去跟後台做更新
                JSONObject json = new JSONObject();
                try {
                    json.put("startDate" , toDate.getText().toString());
                    json.put("endDate", fromDate.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                proxy.buildPOST(PERIOD_UPDATE, json.toString(), periodListener);
            }
        });

        //刪除Api
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //從sharePref取出經期第一天與最後一天
                SharedPreferences pref = getSharedPreferences("yhyHealthy", MODE_PRIVATE);
                String startDateStr = pref.getString("BEGIN", "");
                String endDateStr = pref.getString("END", "");

                //判斷使用者點擊的日期是否與檔案內的日期吻合,吻合才可以做刪除的動作
                if(toDate.getText().toString().equals(startDateStr)){
                    JSONObject json = new JSONObject();
                    try {
                        json.put("startDate", startDateStr);
                        json.put("endDate", endDateStr);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    proxy.buildPOST(PERIOD_DELETE, json.toString(), deleteListener);
                }else{
                    Toasty.error(OvulationActivity.this, getString(R.string.please_chose_really_day) + startDateStr
                            + getString(R.string.delete_really_day), Toast.LENGTH_SHORT, true).show();
                }

            }
        });

        dialog.show();
    }

    //查詢經期和週期天數 2021/03/04
    private void checkPeriodDayInfo() {
        proxy.buildPOST(MENSTRUAL_RECORD_INFO, "", periodDayListener);
    }

    private ApiProxy.OnApiListener periodDayListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            buildProgress(R.string.progressdialog_else, R.string.progressdialog_wait);
        }

        @Override
        public void onSuccess(JSONObject result) {
            try {
                JSONObject object = new JSONObject(result.toString());
                int errorCode = object.getInt("errorCode");
                if (errorCode == 0){
                    //將資料寫入SharePref
                    periodData = PeriodData.newInstance(result.toString());
                    SharedPreferences pref = getSharedPreferences("yhyHealthy", MODE_PRIVATE);
                    pref.edit().putInt("PERIOD", periodData.getSuccess().getPeriod()).apply();
                    pref.edit().putInt("CYCLE", periodData.getSuccess().getCycle()).apply();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
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

    //經期刪除api
    private ApiProxy.OnApiListener deleteListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            buildProgress(R.string.progressdialog_else, R.string.progressdialog_wait);
        }

        @Override
        public void onSuccess(JSONObject result) {
            try {
                JSONObject object = new JSONObject(result.toString());
                int errorCode = object.getInt("errorCode");
                if (errorCode == 0){
                    //要砍掉sharePref檔案內的起始日跟結束日
                    DeletePeriodDate();

                    //清除之前日期的make
                    widget.removeDecorators();

                    //重新載入資料
                    initCalendar();

                    //關閉對話框
                    dialog.dismiss();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
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

    //經期更新api
    private ApiProxy.OnApiListener periodListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            buildProgress(R.string.progressdialog_else, R.string.progressdialog_wait);
        }

        @Override
        public void onSuccess(JSONObject result) {
            try {
                JSONObject object = new JSONObject(result.toString());
                int errorCode = object.getInt("errorCode");
                if(errorCode == 0){
                    Toasty.success(OvulationActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT, true).show();

                    //清除之前日期mark
                    widget.removeDecorators();

                    //重新載入資料 2021/02/22
                    initCalendar();

                    //關閉對話框
                    dialog.dismiss();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
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

    //將經期的日期刪除時其sharePref也需要更新
    private void DeletePeriodDate(){
        SharedPreferences pref = getSharedPreferences("yhyHealthy", MODE_PRIVATE);
        pref.edit().putString("BEGIN", "").apply();
        pref.edit().putString("END","").apply();
    }

    //將經期第一天與最後一天寫入sharePref內
    private void WriteDateIntoSharePref(String beginStr, String endStr) {
        SharedPreferences pref = getSharedPreferences("yhyHealthy", MODE_PRIVATE);
        pref.edit().putString("BEGIN", beginStr).apply();  //經期第一天
        pref.edit().putString("END",endStr).apply();       //經期最後一天
    }

    /***** 日期被選到時的動作 *****/
    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        LocalDate choseDay = LocalDate.from(date.getDate());
        Toasty.info(OvulationActivity.this, getString(R.string.your_chose_day) + choseDay, Toast.LENGTH_SHORT,true).show();

        //使用者點擊後要去設置日期並重新繪製圖層 2021/03/09
        oneDayDecorator.setDate(date.getDate());
        widget.invalidateDecorators(); // 重新繪製

        //根據使用者點擊到的日期去跟後台查詢資料
        checkTodayInfo(String.valueOf(choseDay));

        //使用者點擊的日期與今天同一天...
        if (choseDay.equals(LocalDate.now())){
            btnSetting.setEnabled(true);
            btnEditor.setEnabled(true);
        }else { //判斷經期設定&編輯是否禁止
            boolean flag = LocalDate.now().isAfter(choseDay);
            btnSetting.setEnabled(flag);
            btnEditor.setEnabled(flag);
        }

        //使用者點擊的日期給予全域變數
        selectedDay = String.valueOf(choseDay);

        //週期第幾天?
        checkPeriodDayOfThisMonth(choseDay);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnCalendar:
                calendar.setBackgroundResource(R.drawable.shape_temp_button);
                chart.setBackgroundResource(R.drawable.shape_for_temperature);
                scrollView.setVisibility(View.VISIBLE);
                chartLayout.setVisibility(View.GONE);
                break;
            case R.id.btnChart:
                calendar.setBackgroundResource(R.drawable.shape_for_temperature);
                chart.setBackgroundResource(R.drawable.shape_temp_button);
                scrollView.setVisibility(View.GONE);
                chartLayout.setVisibility(View.VISIBLE);
                initChartData(); //圖表init
                break;
            case R.id.btnPeriodSetting:
                showPeriodDialog(selectedDay); //經期設定對話框
                break;
            case R.id.btnPeriodWrite:
                periodEdit(selectedDay);  //針對日期進行編輯資料(Camera)
                break;
            case R.id.imgPreMonth:      //上個月
                preMonthListener();
                break;
            case R.id.imgNextMonth:
                nextMonthListener();  //下個月
                break;
        }
    }

    //上個月
    private void preMonthListener() {
        String startLastMonth = String.valueOf(LocalDate.parse(firstDayOfThisMonth).plusDays(-30));
        String endLastMonth = String.valueOf(LocalDate.parse(startLastMonth).plusDays(40));
        firstDayOfThisMonth = startLastMonth;
        lastDayOfThisMonth = endLastMonth;
        initChartData();
        widget.goToPrevious(); //上個月月曆
    }

    //下個月
    private void nextMonthListener() {
        String endNextMonth = String.valueOf(LocalDate.parse(lastDayOfThisMonth).plusDays(30));
        String startNextMonth = String.valueOf(LocalDate.parse(endNextMonth).plusDays(-40));
        firstDayOfThisMonth = startNextMonth;
        lastDayOfThisMonth = endNextMonth;
        initChartData();
        widget.goToNext();    //下個月月曆
    }

    //圖表資料集
    @SuppressLint("SetTextI18n")
    private void initChartData() {
        //圖表日期範圍
        periodRange.setText(firstDayOfThisMonth + " ~ " + lastDayOfThisMonth);
    }

    @Override  //2021/04/19
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PERIOD_RECORD && resultCode == -1){
            widget.removeDecorators(); //移除之前的週期資料
            //重刷資料
            initCalendar();
        }
    }

    //重新寫Y軸(為了補上度C的符號)
    public class MyValueFormatter implements IAxisValueFormatter{


        private DecimalFormat format;

        public MyValueFormatter(){
            format = new DecimalFormat("###,###,##0.0");
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return format.format(value) + "\u2103"; //u2103＝度C
        }
    }

}