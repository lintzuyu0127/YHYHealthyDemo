package com.example.yhyhealthy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yhyhealthy.adapter.BleDeviceListAdapter;
import com.example.yhyhealthy.adapter.ColorViewAdapter;
import com.example.yhyhealthy.adapter.SymptomViewAdapter;
import com.example.yhyhealthy.adapter.TasteViewAdapter;
import com.example.yhyhealthy.adapter.TypeViewAdapter;
import com.example.yhyhealthy.dataBean.ChangeRecordData;
import com.example.yhyhealthy.dataBean.PhotoData;
import com.example.yhyhealthy.dataBean.Record;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.module.RecordColor;
import com.example.yhyhealthy.module.RecordSymptom;
import com.example.yhyhealthy.module.RecordTaste;
import com.example.yhyhealthy.module.RecordType;
import com.example.yhyhealthy.tools.ByteUtils;
import com.example.yhyhealthy.tools.ImageUtils;
import com.example.yhyhealthy.tools.MyGridView;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.SimpleFormatter;

import es.dmoral.toasty.Toasty;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.os.Build.VERSION_CODES.M;
import static com.example.yhyhealthy.module.ApiProxy.IMAGE_DETECTION;
import static com.example.yhyhealthy.module.ApiProxy.RECORD_INFO;
import static com.example.yhyhealthy.module.ApiProxy.RECORD_UPDATE;

/*****************
 * 排卵紀錄編輯page
 * 照相
 * 藍芽體溫
 * 權限繼承DeviceBaseActivity
*****************/

public class RecordActivity extends DeviceBaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = RecordActivity.class.getSimpleName();

    private TextView  textRecordDate, photoResult;
    private Button    takePhoto,startMeasure,saveSetting, photoIdf;
    private ImageView searchBluetooth;
    private ImageView photoShow;

    private String mPath = ""; //設置照片位址
    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;

    //藍芽
    private BleService mBleService;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BroadcastReceiver mBleReceiver;
    private BleDeviceListAdapter mDeviceListAdapter;
    private List<BluetoothDevice> mBluetoothDeviceList;
    private List<String> mRssiList;
    private String deviceAddress;

    private TextView bleConnectStatus;  //藍芽連線狀態
    private EditText editWeight;        //體重
    private TextView textTemperature;   //體溫
    private Switch   bleeding, breastPain, intercourse;

    //顏色,味道,型態,症狀
    MyGridView gridViewColor, gridViewTaste, gridViewType, gridViewSymptom;

    //api
    private Record record;
    private ApiProxy proxy;
    private ChangeRecordData changeRecordData;
    private PhotoData photoData;

    //來自ovulationActivity使用者選擇的日期
    private String strDay;

    //量測進度
    private ProgressBar measureProgress;
    private LinearLayout linearLayout;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_record);
        setTitle(R.string.title_ovul_edit);

        //休眠禁止
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        proxy = ApiProxy.getInstance();  //api初始化

        initView();

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            strDay = bundle.getString("DAY");
            textRecordDate.setText(strDay);
            initDateFromApi(strDay);
        }

    }

    //初始化
    @SuppressLint({"ClickableViewAccessibility", "NewApi"})
    private void initView() {
        textRecordDate = findViewById(R.id.tvRecordDate);      //日期
        photoShow = findViewById(R.id.ivPhoto);               //拍照後的照片顯示
        takePhoto = findViewById(R.id.btnTakePhoto);          //拍照button
        photoIdf = findViewById(R.id.btnPhotoIdf);            //辨識button
        photoResult =findViewById(R.id.textAnalysisResult);   //照片分析後的結果顯示
        searchBluetooth = findViewById(R.id.btnAddBluetooth);
        startMeasure = findViewById(R.id.btnStartMeasure);
        saveSetting = findViewById(R.id.btnSaveSetting);
        bleConnectStatus = findViewById(R.id.tvBleConnectStatus); //藍芽設備連結顯示與否

        measureProgress = findViewById(R.id.progressBar);           //量測進度條Layout
        linearLayout = findViewById(R.id.ly_progressBar);           //量測進度條
        measureProgress.setProgressTintList(ColorStateList.valueOf(Color.BLUE)); //量測進度條顏色

        //switchButton
        bleeding = findViewById(R.id.swBleeding);
        breastPain = findViewById(R.id.swPain);
        intercourse = findViewById(R.id.swIntercourse);

        //體重自行輸入
        editWeight = findViewById(R.id.edtWeight);
        editWeight.setInputType(InputType.TYPE_NULL); //hide keyboard
        editWeight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                editWeight.setInputType(InputType.TYPE_CLASS_NUMBER);
                editWeight.onTouchEvent(event);
                return true;
            }
        });

        textTemperature = findViewById(R.id.textBodyTemp); //體溫

        gridViewColor = findViewById(R.id.gvColor);   //顏色
        gridViewTaste = findViewById(R.id.gvTaste);   //味道
        gridViewType = findViewById(R.id.gvType);     //型態
        gridViewSymptom = findViewById(R.id.gvSymp);  //症狀

        takePhoto.setOnClickListener(this);        //拍照
        photoIdf.setOnClickListener(this);         //辨識
        searchBluetooth.setOnClickListener(this);  //搜尋藍芽
        startMeasure.setOnClickListener(this);     //開始測量
        saveSetting.setOnClickListener(this);      //上傳資料至後台

        bleeding.setOnCheckedChangeListener(this);
        breastPain.setOnCheckedChangeListener(this);
        intercourse.setOnCheckedChangeListener(this);
    }

    //初始化來自後台的資料
    private void initDateFromApi(String selectDay) {
        changeRecordData = new ChangeRecordData(); //實體化

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("testDate", selectDay);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        proxy.buildPOST(RECORD_INFO, jsonObject.toString(), requestListener);
    }

    private ApiProxy.OnApiListener requestListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if (progressDialog == null) {
                progressDialog = ProgressDialog.show(RecordActivity.this, getString(R.string.progressdialog_else), getString(R.string.progressdialog_wait));
            }

            if (!progressDialog.isShowing()) progressDialog.show();
        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //解析後台來的的資料
                    parserJson(result);
                }
            });
        }

        @Override
        public void onFailure(String message) {
            Log.d(TAG, "onFailure: " + message);
        }

        @Override
        public void onPostExecute() {
            if (progressDialog != null) progressDialog.dismiss();
        }
    };

    //解析後台的資料 2021/01/14
    private void parserJson(JSONObject JsonResult) {
        record = Record.newInstance(JsonResult.toString());
        Log.d(TAG, "解析後台過來資料: " + record.toJSONString());

        //2021/04/21 辨識結果
        photoResult.setText(record.getSuccess().getMeasure().getParamName());
        changeRecordData.getMeasure().setParam(record.getSuccess().getMeasure().getParam());

        //體重
        String userWeight = String.valueOf(record.getSuccess().getMeasure().getWeight());
        editWeight.setText(userWeight);

        //體溫
        String userTemperature = String.valueOf(record.getSuccess().getMeasure().getTemperature());
        textTemperature.setText(userTemperature);

        //脹痛 出血 行房
        boolean Bleeding = record.getSuccess().getStatus().isBleeding();
        bleeding.setChecked(Bleeding);
        boolean BeastPain = record.getSuccess().getStatus().isBreastPain();
        breastPain.setChecked(BeastPain);
        boolean Intercourse = record.getSuccess().getStatus().isIntercourse();
        intercourse.setChecked(Intercourse);

        //顏色,狀態,氣味,症狀設定
        setSecretion();
    }

    //設置從後台得到的資訊:顏色,狀態,氣味,症狀
    private void setSecretion() {
        setColorData();
        setTypeData();
        setTasteData();
        setSymptomData();
    }

    //症狀設定
    private void setSymptomData() {
        String[] symptoms = new String[]{ getString(R.string.normal), getString(R.string.hot),getString(R.string.allergy),
                getString(R.string.pain)};

        final SymptomViewAdapter mAdapter = new SymptomViewAdapter(this);
        String secretionSymptom = record.getSuccess().getSecretions().getSymptom();
        RecordSymptom symptom = RecordSymptom.getSymptom(secretionSymptom);
        int pos_symptom = symptom.getIndex();
        mAdapter.setData(symptoms, pos_symptom);     //導入資料並指定default position
        changeRecordData.getSecretions().setSymptom(secretionSymptom);

        gridViewSymptom.setAdapter(mAdapter);
        gridViewSymptom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                mAdapter.setSelection(position);   //傳直更新
                mAdapter.notifyDataSetChanged();

                RecordSymptom symptom = RecordSymptom.getEnName(position);
                String symptomName = symptom.getName();
                changeRecordData.getSecretions().setSymptom(symptomName);
            }
        });
    }

    //形狀設定
    private void setTypeData() {
        String[] types = new String[]{ getString(R.string.normal), getString(R.string.liquid), getString(R.string.thick),
                getString(R.string.liquid_milky)};

        final TypeViewAdapter yAdapter = new TypeViewAdapter(this);
        String secretionType = record.getSuccess().getSecretions().getSecretionType();
        RecordType Type = RecordType.getType(secretionType);
        int pos_type = Type.getIndex();
        yAdapter.setData(types, pos_type);     //導入資料並指定default position
        changeRecordData.getSecretions().setSecretionType(secretionType);

        gridViewType.setAdapter(yAdapter);
        gridViewType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                yAdapter.setSelection(position);   //傳直更新
                yAdapter.notifyDataSetChanged();

                RecordType type = RecordType.getEnName(position);
                String typeName = type.getName();
                changeRecordData.getSecretions().setSecretionType(typeName);
            }
        });
    }

    //味道設定
    private void setTasteData() {
        String[] taste = new String[]{ getString(R.string.normal), getString(R.string.fishy), getString(R.string.stink)};

        final TasteViewAdapter tAdapter = new TasteViewAdapter(this);
        String secretionTaste = record.getSuccess().getSecretions().getSmell();
        RecordTaste Taste = RecordTaste.getTaste(secretionTaste);
        int pos_taste = Taste.getIndex();
        tAdapter.setData(taste, pos_taste);     //導入資料並指定default position
        changeRecordData.getSecretions().setSmell(secretionTaste);

        gridViewTaste.setAdapter(tAdapter);
        gridViewTaste.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                tAdapter.setSelection(position);
                tAdapter.notifyDataSetChanged();

                RecordTaste taste = RecordTaste.getEnName(position);
                String tasteName = taste.getName();
                changeRecordData.getSecretions().setSmell(tasteName);
            }
        });
    }

    //顏色設定
    private void setColorData() {

        String[] colors = new String[]{getString(R.string.normal), getString(R.string.white),
                getString(R.string.yellow), getString(R.string.milky), getString(R.string.brown),getString(R.string.greenish_yellow)};

        final ColorViewAdapter cAdapter = new ColorViewAdapter(this);
        String secretionColor = record.getSuccess().getSecretions().getColor();
        RecordColor color = RecordColor.getColor(secretionColor);
        int pos_color = color.getIndex();
        cAdapter .setData(colors, pos_color);     //導入資料並指定default position
        changeRecordData.getSecretions().setColor(secretionColor);

        gridViewColor.setAdapter(cAdapter);
        gridViewColor.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                cAdapter.setSelection(position);
                cAdapter.notifyDataSetChanged();

                RecordColor color = RecordColor.getEnName(position);
                String colorName = color.getName();
                changeRecordData.getSecretions().setColor(colorName);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnTakePhoto:          //拍照
                checkIsToday();              //僅限今天可以拍照
                break;
            case R.id.btnAddBluetooth:      //搜尋藍芽
                //僅限今天可以量體溫 2021/03/11
                String todayStr = String.valueOf(LocalDate.now());
                if(strDay.equals(todayStr)){
                    openBleFunction();    //今天可以量體溫
                }else {
                    Toasty.info(RecordActivity.this, getString(R.string.function_only_today), Toast.LENGTH_SHORT, true).show();
                }
                break;
            case R.id.btnStartMeasure:  //開始量測
                startCountDownTime();
                break;
            case R.id.btnSaveSetting:
                checkBeforeUpdate();  //上傳資料至後台 2021/02/19
                break;
            case R.id.btnPhotoIdf:
                upPhotoToApi(); //由後台去辨識照片  2021/02/20
                break;
        }
    }

    //倒數計時 2021/04/20
    private void startCountDownTime() {
        startMeasure.setVisibility(View.INVISIBLE); //量測按鈕隱藏
        linearLayout.setVisibility(View.VISIBLE);  //量測進度條顯示
        sendCommand(deviceAddress);  //量測command

        //計時3分鐘,每10秒執行一次onTick方法
        countDownTimer = new CountDownTimer(180000, 1000){

            @Override
            public void onTick(long millisUntilFinished) {
                int progress = (int) (millisUntilFinished/1000);
                measureProgress.setProgress(measureProgress.getMax() - progress);
            }

            @Override
            public void onFinish() {
                sendCommand(deviceAddress);  //量測command
                Toasty.info(RecordActivity.this, R.string.measure_donw, Toast.LENGTH_SHORT, true).show();
                linearLayout.setVisibility(View.INVISIBLE);  //量測進度條隱藏
                startMeasure.setVisibility(View.VISIBLE);    //量測按鈕顯示
            }
        }.start();

    }

    //後台辨識照片
    private void upPhotoToApi() {
        //照片須要轉成base64格式
        String base64Str = ImageUtils.imageToBase64(mPath);
        //今天日期
        DateTime today = new DateTime();
        String todayStr = today.toString("yyyy-MM-dd");

        JSONObject json = new JSONObject();
        try {
            json.put("testDate", todayStr);
            json.put("img", base64Str);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        proxy.buildPOST(IMAGE_DETECTION, json.toString(), identifyListener);
    }

    private ApiProxy.OnApiListener identifyListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if (progressDialog == null) {
                progressDialog = ProgressDialog.show(RecordActivity.this, getString(R.string.progressdialog_else), getString(R.string.progressdialog_wait));
            }

            if (!progressDialog.isShowing()) progressDialog.show();
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
                                parserPhotoResult(result);
                        }else {
                            Toasty.error(RecordActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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
            if (progressDialog != null) progressDialog.dismiss();
        }
    };

    //解析照片從後台回傳的結果 2021/02/20
    private void parserPhotoResult(JSONObject result) {
        photoData = PhotoData.newInstance(result.toString());
        String paramName = photoData.getSuccess().getParamName();
        String param = photoData.getSuccess().getParam();
        changeRecordData.getMeasure().setParam(param); // 後台需要這個資料
        photoResult.setText(paramName);  //顯示分析結果
    }

    //上傳改變後的資料前進性資料是否齊全的檢查 2021/02/19
    private void checkBeforeUpdate() {

        //體重
        if (!TextUtils.isEmpty(editWeight.getText().toString())){
            changeRecordData.getMeasure().setWeight(Double.parseDouble(editWeight.getText().toString()));
        }

        //體溫
        changeRecordData.getMeasure().setTemperature(Double.parseDouble(textTemperature.getText().toString()));

        updateToApi();
    }

    //拍照辨識&藍牙量體溫須先檢查是否是當日
    private void checkIsToday() {
        DateTime today = new DateTime(new Date());
        String todayStr = today.toString("yyyy-MM-dd");
        if (strDay.equals(todayStr)){
            openCamera();      //開啟相機功能
        }else {
            Toasty.info(RecordActivity.this, getString(R.string.camera_only_today), Toast.LENGTH_SHORT, true).show();
        }
    }

    //開啟藍牙相關功能
    private void openBleFunction(){
        if(ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //啟動藍芽
            initBle();
        }else {
            requestPermission(); //權限
        }
    }

    //開啟照相功能 2021/02/20
    private void openCamera() {
        if(ActivityCompat.checkSelfPermission(this, CAMERA) == PackageManager.PERMISSION_GRANTED){
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);   //呼叫原生相機
            File imageFile = getImageFile(); //取得相片檔案位置
            if (imageFile == null) return;
            //取得相片檔案的URL位置
            Uri imageUrl = FileProvider.getUriForFile(this,"com.example.yhyhealthy.fileprovider",imageFile);
            //通知相機新照片儲存的位置
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUrl);
            //將圖片帶回
            startActivityForResult(intent, Activity.DEFAULT_KEYS_DIALER);
        }else {
            requestPermission(); //權限
        }
    }

    //取得相片檔案的URL
    private File getImageFile(){
        String time = new SimpleDateFormat("yyMMdd").format(new Date());
        String fileName = time + "_";
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File imageFile = File.createTempFile(fileName, ".jpg", dir);
            mPath = imageFile.getAbsolutePath(); //照片檔案位置
            return imageFile;
        } catch (IOException e) {
            return null;
        }
    }

    //取得照片回傳
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Activity.DEFAULT_KEYS_DIALER && resultCode == -1 ) {
            new Thread(()->{
                //在BitmapFactory中以檔案URI路徑取得相片檔案，並處理為AtomicReference<Bitmap>，方便後續旋轉圖片
                AtomicReference<Bitmap> getHighImage = new AtomicReference<>(BitmapFactory.decodeFile(mPath));
                Matrix matrix = new Matrix();
                matrix.setRotate(90f);//轉90度
                getHighImage.set(Bitmap.createBitmap(getHighImage.get()
                        ,0,0
                        ,getHighImage.get().getWidth()
                        ,getHighImage.get().getHeight()
                        ,matrix,true));
                runOnUiThread(()->{
                    //以Glide設置圖片(因為旋轉圖片屬於耗時處理，故會LAG一下，且必須使用Thread執行緒)
                    Glide.with(this)
                            .load(getHighImage.get())
                            .centerCrop()
                            .into(photoShow);
                });
            }).start();
            //當日可以連續拍照
            takePhoto.setText(getString(R.string.take_photo_again));
            photoIdf.setVisibility(View.VISIBLE); //辨識button顯示
        }else {
            Toasty.info(RecordActivity.this, getString(R.string.camera_not_action), Toast.LENGTH_SHORT, true).show();
        }
    }

    //2021/02/19 上傳到後台
    private void updateToApi() {
        //需要日期上傳
        changeRecordData.setTestDate(strDay);
        //Log.d(TAG, "上傳更新的資料: " + changeRecordData.toJSONString());
        proxy.buildPOST(RECORD_UPDATE, changeRecordData.toJSONString(), changeRecordListener);
    }

    private ApiProxy.OnApiListener changeRecordListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {
            if (progressDialog == null) {
                progressDialog = ProgressDialog.show(RecordActivity.this, getString(R.string.progressdialog_else), getString(R.string.progressdialog_wait));
            }

            if (!progressDialog.isShowing()) progressDialog.show();
        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    parserUpdateResult(result);
                }
            });
        }

        @Override
        public void onFailure(String message) {
            Log.d(TAG, "onFailure: " + message);
        }

        @Override
        public void onPostExecute() {
            if (progressDialog != null) progressDialog.dismiss();
        }
    };

    private void parserUpdateResult(JSONObject result) {
        try {
            JSONObject jsonObject = new JSONObject(result.toString());
            int errorCode = jsonObject.getInt("errorCode");
            if (errorCode == 0){
                Toasty.success(RecordActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT, true).show();
                setResult(RESULT_OK);
                finish();
            }else {
                Log.d(TAG, "parserUpdateResult: 錯誤code : " + errorCode);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void initBle() {
        mBluetoothManager = (BluetoothManager)this.getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if(mBluetoothAdapter == null){ //如果==null，利用finish()取消程式。
            Toasty.error(getBaseContext(), getString(R.string.No_sup_Bluetooth), Toast.LENGTH_SHORT, true).show();
            finish();
            return;
        }else if (!mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.enable(); //啟動藍芽
        }

        //搜尋ble設備
        dialogBleConnect();
    }

    //Ble search  2021/03/15
    private void dialogBleConnect() {
        alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_bleconnect, null);
        RecyclerView dialogView = view.findViewById(R.id.rvBleSearch);

        //藍芽設備列表
        mBluetoothDeviceList = new ArrayList<>();

        //藍芽設備RSSI列表
        mRssiList = new ArrayList<>();

        mDeviceListAdapter = new BleDeviceListAdapter(mBluetoothDeviceList, mRssiList);
        dialogView.setLayoutManager(new LinearLayoutManager(this));
        dialogView.setAdapter(mDeviceListAdapter);
        alertDialog.setView(view);
        alertDialog.setCancelable(false);  //disable touch screen area only cancel's button can close dialog
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //dialog背景透明

        //點擊item則連接藍芽設備
        mDeviceListAdapter.setOnItemClickListener(new BleDeviceListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {    // 連接設備
                Toasty.info(RecordActivity.this, getString(R.string.ble_start_connected), Toast.LENGTH_SHORT, true).show();

                //停止搜尋
                mBluetoothAdapter.stopLeScan(mLeScanCallback);

                //連接藍芽
                mBleService.connect(mBluetoothDeviceList.get(position).getAddress());

                //關閉對話視窗
                alertDialog.dismiss();
            }
        });

        //自動搜尋藍芽設備
        scanBleDevice();

        //停止搜尋BLE裝置並關閉此dialog
        Button bleCancel = view.findViewById(R.id.btnBleCancel);
        bleCancel.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View view) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                Log.d(TAG, "dialog : 使用者自行取消搜尋功能");
                alertDialog.dismiss();  //關閉此dialog
            }
        });

        alertDialog.show();
    }

    //藍芽掃描fxn
    private void scanBleDevice() {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            //搜尋5秒
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    Toasty.info(getBaseContext(), getString(R.string.ble_stop_in_5), Toast.LENGTH_SHORT, true).show();
                }
            },5000);
    }

    /*** 搜尋藍芽設備回調 callback ***/
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] bytes) {
            if(!mBluetoothDeviceList.contains(device)){
                if (device.getName() != null)           //名稱沒有則不顯示
                    mBluetoothDeviceList.add(device);
                mRssiList.add(String.valueOf(rssi));
                mDeviceListAdapter.notifyDataSetChanged();
            }
        }
    };

    /*** 註冊藍芽信息接受器 **/
    private void registerBleReceiver(){
        Log.d(TAG, "註冊藍芽信息接受器 ");

        //綁定BLEService背景服務
        Intent intent = new Intent(this, BleService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        startService(intent);

        mBleReceiver = new BleReceiver();

        //註冊廣播
        registerReceiver(mBleReceiver, mBleService.makeIntentFilter());
    }

    /**
     * 藍芽信息接受器
     */
    private class BleReceiver extends BroadcastReceiver {

        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }

            String correctDeviceName = intent.getStringExtra(BleService.EXTRA_DEVICE_NAME);
            byte[] data = intent.getByteArrayExtra(BleService.EXTRA_DATA);

            switch (action) {
                case BleService.ACTION_GATT_CONNECTED:
                    Toasty.info(RecordActivity.this, getString(R.string.ble_connected), Toast.LENGTH_SHORT,true).show();
                    break;
                case BleService.ACTION_GATT_DISCONNECTED:
                    Toasty.info(RecordActivity.this, getString(R.string.ble_not_connect), Toast.LENGTH_SHORT,true).show();
                    mBleService.release();
                    bleConnectStatus.setText(getString(R.string.ble_is_not_connected));
                    countDownTimer.cancel();  //取消定時器
                    break;

                case BleService.ACTION_CONNECTING_FAIL:
                    Toasty.info(RecordActivity.this, getString(R.string.ble_not_connect), Toast.LENGTH_SHORT,true).show();
                    mBleService.disconnect();
                    bleConnectStatus.setText(getString(R.string.ble_connected_fail));
                    countDownTimer.cancel();  //取消定時器
                    break;

                case BleService.ACTION_NOTIFY_SUCCESS: //通知成功後變更textView顯示內容
                    deviceAddress = intent.getStringExtra(BleService.EXTRA_MAC);
                    bleConnectStatus.setText(correctDeviceName + getString(R.string.ble_device_connected));
                    bleConnectStatus.setTextColor(Color.RED);
                    startMeasure.setVisibility(View.VISIBLE);        //量測按鈕顯示 2021/04/20
                    searchBluetooth.setVisibility(View.INVISIBLE);   //搜尋按鈕隱藏 2021/04/21
                    break;
                case BleService.ACTION_DATA_AVAILABLE:  //2021/03/15
//                    Log.d(TAG, "收到的原始數據: " + ByteUtils.byteArrayToString(data));
                    String[] str = ByteUtils.byteArrayToString(data).split(","); //以,切割
                    String degreeStr = str[2];
                    double degree = Double.parseDouble(degreeStr)/100;
                    textTemperature.setText(String.valueOf(degree)); //體溫顯示
                    break;
                default:
                    break;
            }
        }
    }

    /**  背景服務 */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBleService = ((BleService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBleService = null;
        }
    };

    //量測command
    private void sendCommand(String address){
        String requestStr = "AIDO,0";
        byte[] messageBytes = new byte[0];
        try {
            messageBytes = requestStr.getBytes("UTF-8");
            mBleService.writeDataToDevice(messageBytes,address);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        //註冊藍芽信息接受器
        registerBleReceiver();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.swBleeding:
                if (isChecked){
                    changeRecordData.getStatus().setBleeding(true);
                }else {
                    changeRecordData.getStatus().setBleeding(false);
                }
                break;
            case R.id.swPain:
                if (isChecked){
                    changeRecordData.getStatus().setBreastPain(true);
                }else {
                    changeRecordData.getStatus().setBreastPain(false);
                }
                break;
            case R.id.swIntercourse:
                if (isChecked){
                    changeRecordData.getStatus().setIntercourse(true);
                }else {
                    changeRecordData.getStatus().setIntercourse(false);
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if (mBleService == null)
            return;

        mBleService.disconnect();   //藍芽斷開
        mBleService.release();      //釋放資源

        unregisterReceiver(mBleReceiver);    //取消註冊
        unbindService(mServiceConnection);   //鬆綁服務器
        mBleService = null;
    }
}