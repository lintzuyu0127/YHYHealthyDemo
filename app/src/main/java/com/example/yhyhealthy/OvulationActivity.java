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
import android.util.TypedValue;
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

    //??????
    private CombinedChart combinedChart;
    private TextView  periodRange; //??????????????????
    private ImageView preMonth, nextMonth; //?????????&???????????????

    //??????
    private MaterialCalendarView widget;
    private OneDayDecorator oneDayDecorator;
    private String selectedDay;
    private String firstDayOfThisMonth;
    private String lastDayOfThisMonth;
    //??????
    private List<CycleRecord.SuccessBean> dataList;
    private CycleMath math;

    //api
    private Record record;
    private ApiProxy proxy;
    private CycleRecord cycleRecord;
    private PeriodData periodData;

    //Other
    private AlertDialog dialog;
    private static final int PERIOD_RECORD = 1;
    private String beginPeriodDay; //???????????????
    private int periodLength;     //????????????

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //title??????
        setContentView(R.layout.activity_ovulation);
        setTitle(R.string.title_ovul);

        //????????????
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initData();

        initView();

        //???????????????
        initCalendar();
    }

    //?????????dataBean & Api
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initData() {
        record = new Record();
        cycleRecord = new CycleRecord();
        proxy = ApiProxy.getInstance();

        oneDayDecorator = new OneDayDecorator(this);

        firstDayOfThisMonth = String.valueOf(LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).plusDays(-5)); //????????????5???
        lastDayOfThisMonth = String.valueOf(LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).plusDays(+5)); //????????????5???
    }

    private void initView() {
        calendar = findViewById(R.id.btnCalendar);
        chart = findViewById(R.id.btnChart);
        scrollView = findViewById(R.id.lyScrollView);    //????????????layout
        chartLayout = findViewById(R.id.lyChart);        //??????layout

        periodShowDay = findViewById(R.id.tvShowPeriodDay);      //??????????????????????Text
        ovulationResult = findViewById(R.id.tvIdentify);      //??????????????????Text
        temperature = findViewById(R.id.tvShowDegree);       //????????????Text
        bodySalivaRate = findViewById(R.id.rtSaliva);          //??????????????????Rate
        bodyDegreeRate = findViewById(R.id.rtBt);              //??????????????????Rate

        btnSetting = findViewById(R.id.btnPeriodSetting);    //????????????Button
        btnEditor = findViewById(R.id.btnPeriodWrite);      //????????????Button

        //?????? 2021/03/01
        widget = findViewById(R.id.calendar);
        widget.setOnDateChangedListener(this);
        widget.setShowOtherDates(MaterialCalendarView.SHOW_ALL);
        LocalDate instance = LocalDate.now();  //?????????????????????
        widget.setSelectedDate(instance);

        calendar.setOnClickListener(this);          //??????onclick
        chart.setOnClickListener(this);             //??????onclick
        btnSetting.setOnClickListener(this);       //????????????onclick
        btnEditor.setOnClickListener(this);        //????????????onclick

        //??????:????????????
        //lineChart = findViewById(R.id.lineChart);
        combinedChart = findViewById(R.id.chart);
        periodRange = findViewById(R.id.tvMonthName);  //????????????????????????
        preMonth = findViewById(R.id.imgPreMonth);
        nextMonth = findViewById(R.id.imgNextMonth);
        preMonth.setOnClickListener(this);
        nextMonth.setOnClickListener(this);

        //???????????????
        calendar.setBackgroundResource(R.drawable.shape_temp_button);
        scrollView.setVisibility(View.VISIBLE);
    }

    //??????????????????
    private void initCalendar() {

        //????????????????????????
//        widget.addDecorators(
//                new MySelectorDecorator(this),
//                oneDayDecorator
//        );
        widget.addDecorator(oneDayDecorator);

        //?????????api???????????????(?????????&?????????)
        setCycleData(firstDayOfThisMonth, lastDayOfThisMonth);

        //???????????????????????????
        checkTodayInfo(String.valueOf(LocalDate.now()));

        //??????????????????
        monthListener();

        //?????????????????????&?????????????????????(????????????????????????????????????) 2021/05/26??????
        checkPeriodDayInfo();
    }

    //??????????????????
    private void monthListener() {
        widget.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onMonthChanged(MaterialCalendarView widget, CalendarDay date) {

                String firstDay = String.valueOf(LocalDate.from(date.getDate()).with(TemporalAdjusters.firstDayOfMonth()).plusDays(-5));
                String lastDay = String.valueOf(LocalDate.from(date.getDate()).with(TemporalAdjusters.lastDayOfMonth()).plusDays(+5));

                firstDayOfThisMonth = firstDay;
                lastDayOfThisMonth = lastDay;

                setCycleData(firstDayOfThisMonth, lastDayOfThisMonth);  //????????????
                widget.removeDecorators();            //?????????????????????Decorator
                widget.addDecorator(oneDayDecorator); //??????
            }
        });

    }

    //????????????????????????????????????
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
                        if (errorCode == 0) {
                            parserJson(result); //??????json??????
                        }else if (errorCode == 23) { //token??????
                            Toasty.error(OvulationActivity.this, getString(R.string.idle_too_long), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(OvulationActivity.this, LoginActivity.class));
                            finish();
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
            Log.d(TAG, "onFailure: ???????????? Failure");
        }

        @Override
        public void onPostExecute() {
            hideProgress();
        }
    };

    //2021/01/13 ?????????????????? (?????????????????? ???????????? ?????????????????? ??????????????????
    @SuppressLint("SetTextI18n")
    private void parserJson(JSONObject result) {
        Log.d(TAG, "??????????????????: " + result.toString());
        record = Record.newInstance(result.toString());

        //??????????????????
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
                    ovulationResult.setText(getString(R.string.in_low_cell));  //2021/05/20??????
                    ovulationResult.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_FollicularORLutealPhase));
                    break;
                case "HighFollicularORLutealPhase":
                    ovulationResult.setText(getString(R.string.in_high_cell));  //2021/05/20??????
                    ovulationResult.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_HighFollicularORLutealPhase));
                    break;
                case "Unrecognizable":
                    ovulationResult.setText(getString(R.string.unknow));
                    break;
            }
        }else {
            btnEditor.setText(getString(R.string.ovual_add));
            ovulationResult.setText("");
        }

        //????????????
        String bodyDegree = String.valueOf(record.getSuccess().getMeasure().getTemperature());
        temperature.setText(bodyDegree + " \u2103");

        //???????????????????????????????????????
        int salivaRate = record.getSuccess().getOvuRate().getSalivaRate();
        bodySalivaRate.setRating(salivaRate);

        //?????????????????????????????????
        int btRate = record.getSuccess().getOvuRate().getBtRate();
        bodyDegreeRate.setRating(btRate);

    }

    //?????????????????????????????????
    private void setCycleData(String startDay, String endDay) {
        JSONObject json = new JSONObject();
        try {
            json.put("startDate", startDay);  //?????????
            json.put("endDate", endDay);     //?????????
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
                        if (errorCode == 0) {
                            parserCycleData(result); //???????????????????????????
                        }else if (errorCode == 23){ //token??????
                            Toasty.error(OvulationActivity.this, getString(R.string.idle_too_long), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(OvulationActivity.this, LoginActivity.class));
                            finish();
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

    //2021/03/04 leona ?????????????????????????????????(?????????)
    private void parserCycleData(JSONObject result) {
        cycleRecord = CycleRecord.newInstance(result.toString());
        Log.d(TAG, "?????????????????????????????????: " + cycleRecord.toJSONString());

        List<String> firstPeriodDayList = new ArrayList<>(); //?????????????????????

        dataList = cycleRecord.getSuccess();

        for (int i = 0; i < dataList.size(); i ++){

            math = new CycleMath(this, dataList.get(i));

            //??????????????????
            if (math.getCalenderDrawable() != null)
             widget.addDecorator(new MyEventDecorator(math.getCalenderDrawable(), Collections.singletonList(math.getDateData())));

            //??????????????? 2021/03/04
            boolean isFirstDay = dataList.get(i).isFirstDay();
            if (isFirstDay){
                String dayStr = dataList.get(i).getTestDate(); //?????????????????????
                firstPeriodDayList.add(dayStr);
            }
        }

        //?????? 2021/03/10
        CombinedChartManager chartManager = new CombinedChartManager(this, combinedChart);
        chartManager.showCombinedChart(dataList);

        //?????????????????????????????????
        if(firstPeriodDayList.get(0) != null)
            beginPeriodDay = firstPeriodDayList.get(0);

        //????????????????(????????????????????????????????????????????????????????????????????????) 2021/05/26
        checkPeriodDayOfThisMonth(LocalDate.now());

    }

    //??????????????? 2021/03/02
    private void checkPeriodDayOfThisMonth(LocalDate day) {

        if (TextUtils.isEmpty(beginPeriodDay)){
            periodShowDay.setText(getString(R.string.period_Day_no_data));
        }else {
            LocalDate begin = LocalDate.parse(beginPeriodDay);
            long numOfDays = ChronoUnit.DAYS.between(begin, day);
            if (numOfDays >= 0){
                numOfDays = numOfDays + 1;
                periodShowDay.setText(getString(R.string.period_day) + " " + numOfDays + " "+ getString(R.string.day));
            }else {
                periodShowDay.setText(getString(R.string.period_out_range));
            }
        }

    }

    //????????????????????????
    private void periodEdit(String strDay) {
        //???????????????????????????????????????????????????
        if (strDay == null){
            strDay = String.valueOf(LocalDate.now());
        }

        Intent intent = new Intent();
        intent.setClass(OvulationActivity.this, RecordActivity.class);

        Bundle bundle = new Bundle();
        bundle.putString("DAY", strDay);//????????????????????????????????????RecordActivity
        intent.putExtras(bundle);
        startActivityForResult(intent, PERIOD_RECORD);
    }

    //????????????????????? 20201/02/23
    private void showPeriodDialog(String daySelect) {
        //???????????????????????????????????????????????????
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

        final EditText toDate = view.findViewById(R.id.text_to_date);      //????????????
        final EditText fromDate = view.findViewById(R.id.text_from_date);  //????????????

        toDate.setText(daySelect); //???????????????????????????????????????????????????
        DateTime startDay = new DateTime(daySelect);

        //????????????????????????????????????????????????????????????
        DateTime endDay = startDay.plusDays(periodLength);
        fromDate.setText(endDay.toString("yyyy-MM-dd"));  //????????????????????????

        //button'init
        final Button cancel = view.findViewById(R.id.btnDateCancel);  //??????
        final Button delete = view.findViewById(R.id.btnDateDelete);  //??????
        final Button btnUpdate = view.findViewById(R.id.btnDateSave); //??????

        dialog = builder.create();

        //?????????
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

        //??????
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        //??????api (?????????????????????)
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject json = new JSONObject();
                try {
                    json.put("startDate" , toDate.getText().toString()); //???????????????
                    json.put("endDate", fromDate.getText().toString());  //??????????????????
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                proxy.buildPOST(PERIOD_UPDATE, json.toString(), periodListener);
            }
        });

        //??????Api
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //???????????????????????????????????????????????????????????????,?????????????????????????????????
                if(toDate.getText().toString().equals(beginPeriodDay)){
                    JSONObject json = new JSONObject();
                    try {
                        json.put("startDate", toDate.getText().toString());
                        json.put("endDate", fromDate.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    proxy.buildPOST(PERIOD_DELETE, json.toString(), deleteListener);
                }else{
                    Toasty.error(OvulationActivity.this, getString(R.string.please_chose_really_day),Toast.LENGTH_SHORT, true).show();
                }
            }
        });

        dialog.show();
    }

    //??????????????????????????? 2021/03/04
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
                    periodData = PeriodData.newInstance(result.toString());
                    //????????????????????????????????? 2021/05/16
                    periodLength = periodData.getSuccess().getPeriod() - 1;
                }else if (errorCode == 23) { //token??????
                    Toasty.error(OvulationActivity.this, getString(R.string.idle_too_long), Toast.LENGTH_SHORT, true).show();
                    startActivity(new Intent(OvulationActivity.this, LoginActivity.class));
                    finish();
                }else {
                    Toasty.error(OvulationActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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

    //????????????api
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
                    //?????????????????????make
                    widget.removeDecorators();

                    //??????????????????
                    initCalendar();

                    //???????????????
                    dialog.dismiss();
                }else if (errorCode == 23){ //token??????
                    Toasty.error(OvulationActivity.this, getString(R.string.idle_too_long), Toast.LENGTH_SHORT, true).show();
                    startActivity(new Intent(OvulationActivity.this, LoginActivity.class));
                    finish();
                }else {
                    Toasty.error(OvulationActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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

    //????????????api
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
                if(errorCode == 0) {
                    Toasty.success(OvulationActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT, true).show();

                    //??????????????????mark
                    widget.removeDecorators();

                    //?????????????????? 2021/02/22
                    initCalendar();

                    //???????????????
                    dialog.dismiss();
                }else if (errorCode == 23){ //token??????
                    Toasty.error(OvulationActivity.this, getString(R.string.idle_too_long), Toast.LENGTH_SHORT, true).show();
                    startActivity(new Intent(OvulationActivity.this, LoginActivity.class));
                    finish();
                }else {
                    Toasty.error(OvulationActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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

    /***** ??????????????????????????? *****/
    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        LocalDate choseDay = LocalDate.from(date.getDate());
        Toasty.info(OvulationActivity.this, getString(R.string.your_chose_day) + choseDay, Toast.LENGTH_SHORT,true).show();

        //????????????????????????????????????????????????????????? 2021/03/09
        oneDayDecorator.setDate(date.getDate());
        widget.invalidateDecorators(); // ????????????

        //?????????????????????????????????????????????????????????
        checkTodayInfo(String.valueOf(choseDay));

        //??????????????????????????????????????????...
        if (choseDay.equals(LocalDate.now())){
            btnSetting.setEnabled(true);
            btnEditor.setEnabled(true);
        }else { //??????????????????&??????????????????
            boolean flag = LocalDate.now().isAfter(choseDay);
            btnSetting.setEnabled(flag);
            btnEditor.setEnabled(flag);
        }

        //??????????????????????????????????????????
        selectedDay = String.valueOf(choseDay);

        //????????????????
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
                initChartData(); //??????init
                break;
            case R.id.btnPeriodSetting:
                showPeriodDialog(selectedDay); //?????????????????????
                break;
            case R.id.btnPeriodWrite:
                periodEdit(selectedDay);  //??????????????????????????????(Camera)
                break;
            case R.id.imgPreMonth:      //?????????
                preMonthListener();
                break;
            case R.id.imgNextMonth:
                nextMonthListener();  //?????????
                break;
        }
    }

    //?????????
    private void preMonthListener() {
        String startLastMonth = String.valueOf(LocalDate.parse(firstDayOfThisMonth).plusDays(-30));
        String endLastMonth = String.valueOf(LocalDate.parse(startLastMonth).plusDays(40));
        firstDayOfThisMonth = startLastMonth;
        lastDayOfThisMonth = endLastMonth;
        initChartData();
        widget.goToPrevious(); //???????????????
    }

    //?????????
    private void nextMonthListener() {
        String endNextMonth = String.valueOf(LocalDate.parse(lastDayOfThisMonth).plusDays(30));
        String startNextMonth = String.valueOf(LocalDate.parse(endNextMonth).plusDays(-40));
        firstDayOfThisMonth = startNextMonth;
        lastDayOfThisMonth = endNextMonth;
        initChartData();
        widget.goToNext();    //???????????????
    }

    //???????????????
    @SuppressLint("SetTextI18n")
    private void initChartData() {
        //??????????????????
        periodRange.setText(firstDayOfThisMonth + " ~ " + lastDayOfThisMonth);
    }

    @Override  //2021/04/19
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PERIOD_RECORD && resultCode == -1){
            widget.removeDecorators(); //???????????????????????????
            //????????????
            initCalendar();
        }
    }

    //?????????Y???(???????????????C?????????)
    public class MyValueFormatter implements IAxisValueFormatter{
        private DecimalFormat format;

        public MyValueFormatter(){
            format = new DecimalFormat("###,###,##0.0");
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return format.format(value) + "\u2103"; //u2103??????C
        }
    }

}