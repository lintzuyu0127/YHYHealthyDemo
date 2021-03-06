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
 * ??????????????????page
 * ??????
 * ????????????
 * ????????????DeviceBaseActivity
*****************/

public class RecordActivity extends DeviceBaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = RecordActivity.class.getSimpleName();

    private TextView  textRecordDate, photoResult;
    private Button    takePhoto,startMeasure,saveSetting, photoIdf;
    private ImageView searchBluetooth;
    private ImageView photoShow;

    private String mPath = ""; //??????????????????
    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;

    //??????
    private BleService mBleService;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BroadcastReceiver mBleReceiver;
    private BleDeviceListAdapter mDeviceListAdapter;
    private List<BluetoothDevice> mBluetoothDeviceList;
    private List<String> mRssiList;
    private String deviceAddress;

    private TextView bleConnectStatus;  //??????????????????
    private EditText editWeight;        //??????
    private TextView textTemperature;   //??????
    private Switch   bleeding, breastPain, intercourse;

    //??????,??????,??????,??????
    MyGridView gridViewColor, gridViewTaste, gridViewType, gridViewSymptom;

    //api
    private Record record;
    private ApiProxy proxy;
    private ChangeRecordData changeRecordData;
    private PhotoData photoData;

    //??????ovulationActivity????????????????????????
    private String strDay;

    //????????????
    private ProgressBar measureProgress;
    private LinearLayout linearLayout;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_record);
        setTitle(R.string.title_ovul_edit);

        //????????????
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        proxy = ApiProxy.getInstance();  //api?????????

        initView();

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            strDay = bundle.getString("DAY");
            textRecordDate.setText(strDay);
            initDateFromApi(strDay);
        }

    }

    //?????????
    @SuppressLint({"ClickableViewAccessibility", "NewApi"})
    private void initView() {
        textRecordDate = findViewById(R.id.tvRecordDate);      //??????
        photoShow = findViewById(R.id.ivPhoto);               //????????????????????????
        takePhoto = findViewById(R.id.btnTakePhoto);          //??????button
        photoIdf = findViewById(R.id.btnPhotoIdf);            //??????button
        photoResult =findViewById(R.id.textAnalysisResult);   //??????????????????????????????
        searchBluetooth = findViewById(R.id.btnAddBluetooth);
        startMeasure = findViewById(R.id.btnStartMeasure);
        saveSetting = findViewById(R.id.btnSaveSetting);
        bleConnectStatus = findViewById(R.id.tvBleConnectStatus); //??????????????????????????????

        measureProgress = findViewById(R.id.progressBar);           //???????????????Layout
        linearLayout = findViewById(R.id.ly_progressBar);           //???????????????
        measureProgress.setProgressTintList(ColorStateList.valueOf(Color.BLUE)); //?????????????????????

        //switchButton
        bleeding = findViewById(R.id.swBleeding);
        breastPain = findViewById(R.id.swPain);
        intercourse = findViewById(R.id.swIntercourse);

        //??????????????????
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

        textTemperature = findViewById(R.id.textBodyTemp); //??????

        gridViewColor = findViewById(R.id.gvColor);   //??????
        gridViewTaste = findViewById(R.id.gvTaste);   //??????
        gridViewType = findViewById(R.id.gvType);     //??????
        gridViewSymptom = findViewById(R.id.gvSymp);  //??????

        takePhoto.setOnClickListener(this);        //??????
        photoIdf.setOnClickListener(this);         //??????
        searchBluetooth.setOnClickListener(this);  //????????????
        startMeasure.setOnClickListener(this);     //????????????
        saveSetting.setOnClickListener(this);      //?????????????????????

        bleeding.setOnCheckedChangeListener(this);
        breastPain.setOnCheckedChangeListener(this);
        intercourse.setOnCheckedChangeListener(this);
    }

    //??????????????????????????????
    private void initDateFromApi(String selectDay) {
        changeRecordData = new ChangeRecordData(); //?????????

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
                    try {
                        JSONObject object = new JSONObject(result.toString());
                        int errorCode = object.getInt("errorCode");
                        if (errorCode == 0){
                            parserJson(result);
                        }else if (errorCode == 23){ //token??????
                            Toasty.error(RecordActivity.this, getString(R.string.idle_too_long), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(RecordActivity.this, LoginActivity.class));
                            finish();
                        }else {
                            Toasty.error(RecordActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //???????????????????????????

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

    //????????????????????? 2021/01/14
    private void parserJson(JSONObject JsonResult) {
        record = Record.newInstance(JsonResult.toString());
        Log.d(TAG, "????????????????????????: " + record.toJSONString());

        //2021/04/21 ????????????
        photoResult.setText(record.getSuccess().getMeasure().getParamName());
        changeRecordData.getMeasure().setParam(record.getSuccess().getMeasure().getParam());

        //??????
        String userWeight = String.valueOf(record.getSuccess().getMeasure().getWeight());
        editWeight.setText(userWeight);

        //??????
        String userTemperature = String.valueOf(record.getSuccess().getMeasure().getTemperature());
        textTemperature.setText(userTemperature);

        //?????? ?????? ??????
        boolean Bleeding = record.getSuccess().getStatus().isBleeding();
        bleeding.setChecked(Bleeding);
        boolean BeastPain = record.getSuccess().getStatus().isBreastPain();
        breastPain.setChecked(BeastPain);
        boolean Intercourse = record.getSuccess().getStatus().isIntercourse();
        intercourse.setChecked(Intercourse);

        //??????,??????,??????,????????????
        setSecretion();
    }

    //??????????????????????????????:??????,??????,??????,??????
    private void setSecretion() {
        setColorData();
        setTypeData();
        setTasteData();
        setSymptomData();
    }

    //????????????
    private void setSymptomData() {
        String[] symptoms = new String[]{ getString(R.string.normal), getString(R.string.hot),getString(R.string.allergy),
                getString(R.string.pain)};

        final SymptomViewAdapter mAdapter = new SymptomViewAdapter(this);
        String secretionSymptom = record.getSuccess().getSecretions().getSymptom();
        RecordSymptom symptom = RecordSymptom.getSymptom(secretionSymptom);
        int pos_symptom = symptom.getIndex();
        mAdapter.setData(symptoms, pos_symptom);     //?????????????????????default position
        changeRecordData.getSecretions().setSymptom(secretionSymptom);

        gridViewSymptom.setAdapter(mAdapter);
        gridViewSymptom.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                mAdapter.setSelection(position);   //????????????
                mAdapter.notifyDataSetChanged();

                RecordSymptom symptom = RecordSymptom.getEnName(position);
                String symptomName = symptom.getName();
                changeRecordData.getSecretions().setSymptom(symptomName);
            }
        });
    }

    //????????????
    private void setTypeData() {
        String[] types = new String[]{ getString(R.string.normal), getString(R.string.liquid), getString(R.string.thick),
                getString(R.string.liquid_milky)};

        final TypeViewAdapter yAdapter = new TypeViewAdapter(this);
        String secretionType = record.getSuccess().getSecretions().getSecretionType();
        RecordType Type = RecordType.getType(secretionType);
        int pos_type = Type.getIndex();
        yAdapter.setData(types, pos_type);     //?????????????????????default position
        changeRecordData.getSecretions().setSecretionType(secretionType);

        gridViewType.setAdapter(yAdapter);
        gridViewType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                yAdapter.setSelection(position);   //????????????
                yAdapter.notifyDataSetChanged();

                RecordType type = RecordType.getEnName(position);
                String typeName = type.getName();
                changeRecordData.getSecretions().setSecretionType(typeName);
            }
        });
    }

    //????????????
    private void setTasteData() {
        String[] taste = new String[]{ getString(R.string.normal), getString(R.string.fishy), getString(R.string.stink)};

        final TasteViewAdapter tAdapter = new TasteViewAdapter(this);
        String secretionTaste = record.getSuccess().getSecretions().getSmell();
        RecordTaste Taste = RecordTaste.getTaste(secretionTaste);
        int pos_taste = Taste.getIndex();
        tAdapter.setData(taste, pos_taste);     //?????????????????????default position
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

    //????????????
    private void setColorData() {

        String[] colors = new String[]{getString(R.string.normal), getString(R.string.white),
                getString(R.string.yellow), getString(R.string.milky), getString(R.string.brown),getString(R.string.greenish_yellow)};

        final ColorViewAdapter cAdapter = new ColorViewAdapter(this);
        String secretionColor = record.getSuccess().getSecretions().getColor();
        RecordColor color = RecordColor.getColor(secretionColor);
        int pos_color = color.getIndex();
        cAdapter .setData(colors, pos_color);     //?????????????????????default position
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
            case R.id.btnTakePhoto:          //??????
                checkIsToday();              //????????????????????????
                break;
            case R.id.btnAddBluetooth:      //????????????
                //??????????????????????????? 2021/03/11
                String todayStr = String.valueOf(LocalDate.now());
                if(strDay.equals(todayStr)){
                    openBleFunction();    //?????????????????????
                }else {
                    Toasty.info(RecordActivity.this, getString(R.string.function_only_today), Toast.LENGTH_SHORT, true).show();
                }
                break;
            case R.id.btnStartMeasure:  //????????????
                startCountDownTime();
                break;
            case R.id.btnSaveSetting:
                checkBeforeUpdate();  //????????????????????? 2021/02/19
                break;
            case R.id.btnPhotoIdf:
                upPhotoToApi(); //????????????????????????  2021/02/20
                break;
        }
    }

    //???????????? 2021/04/20
    private void startCountDownTime() {
        startMeasure.setVisibility(View.INVISIBLE); //??????????????????
        linearLayout.setVisibility(View.VISIBLE);  //?????????????????????
        sendCommand(deviceAddress);  //??????command

        //??????3??????,???10???????????????onTick??????
        countDownTimer = new CountDownTimer(180000, 1000){

            @Override
            public void onTick(long millisUntilFinished) {
                int progress = (int) (millisUntilFinished/1000);
                measureProgress.setProgress(measureProgress.getMax() - progress);
            }

            @Override
            public void onFinish() {
                sendCommand(deviceAddress);  //??????command
                Toasty.info(RecordActivity.this, R.string.measure_down, Toast.LENGTH_SHORT, true).show();
                linearLayout.setVisibility(View.INVISIBLE);  //?????????????????????
                startMeasure.setVisibility(View.VISIBLE);    //??????????????????
            }
        }.start();

    }

    //??????????????????
    private void upPhotoToApi() {
        //??????????????????base64??????
        String base64Str = ImageUtils.imageToBase64(mPath);
        //????????????
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
                        if (errorCode == 0) {
                            parserPhotoResult(result);
                        }else if (errorCode == 23){ //token??????
                            Toasty.error(RecordActivity.this, getString(R.string.idle_too_long), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(RecordActivity.this, LoginActivity.class));
                            finish();
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

    //???????????????????????????????????? 2021/02/20
    private void parserPhotoResult(JSONObject result) {
        photoData = PhotoData.newInstance(result.toString());
        String paramName = photoData.getSuccess().getParamName();
        String param = photoData.getSuccess().getParam();
        changeRecordData.getMeasure().setParam(param); // ????????????????????????
        photoResult.setText(paramName);  //??????????????????
    }

    //???????????????????????????????????????????????????????????? 2021/02/19
    private void checkBeforeUpdate() {

        //??????
        if (!TextUtils.isEmpty(editWeight.getText().toString())){
            changeRecordData.getMeasure().setWeight(Double.parseDouble(editWeight.getText().toString()));
        }

        //??????
        changeRecordData.getMeasure().setTemperature(Double.parseDouble(textTemperature.getText().toString()));

        updateToApi();
    }

    //????????????&??????????????????????????????????????????
    private void checkIsToday() {
        DateTime today = new DateTime(new Date());
        String todayStr = today.toString("yyyy-MM-dd");
        if (strDay.equals(todayStr)){
            openCamera();      //??????????????????
        }else {
            Toasty.info(RecordActivity.this, getString(R.string.camera_only_today), Toast.LENGTH_SHORT, true).show();
        }
    }

    //????????????????????????
    private void openBleFunction(){
        if(ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //????????????
            initBle();
        }else {
            requestPermission(); //??????
        }
    }

    //?????????????????? 2021/02/20
    private void openCamera() {
        if(ActivityCompat.checkSelfPermission(this, CAMERA) == PackageManager.PERMISSION_GRANTED){
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);   //??????????????????
            File imageFile = getImageFile(); //????????????????????????
            if (imageFile == null) return;
            //?????????????????????URL??????
            Uri imageUrl = FileProvider.getUriForFile(this,"com.example.yhyhealthy.fileprovider",imageFile);
            //????????????????????????????????????
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUrl);
            //???????????????
            startActivityForResult(intent, Activity.DEFAULT_KEYS_DIALER);
        }else {
            requestPermission(); //??????
        }
    }

    //?????????????????????URL
    private File getImageFile(){
        String time = new SimpleDateFormat("yyMMdd").format(new Date());
        String fileName = time + "_";
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File imageFile = File.createTempFile(fileName, ".jpg", dir);
            mPath = imageFile.getAbsolutePath(); //??????????????????
            return imageFile;
        } catch (IOException e) {
            return null;
        }
    }

    //??????????????????
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Activity.DEFAULT_KEYS_DIALER && resultCode == -1 ) {
            new Thread(()->{
                //???BitmapFactory????????????URI???????????????????????????????????????AtomicReference<Bitmap>???????????????????????????
                AtomicReference<Bitmap> getHighImage = new AtomicReference<>(BitmapFactory.decodeFile(mPath));
                Matrix matrix = new Matrix();
                matrix.setRotate(90f);//???90???
                getHighImage.set(Bitmap.createBitmap(getHighImage.get()
                        ,0,0
                        ,getHighImage.get().getWidth()
                        ,getHighImage.get().getHeight()
                        ,matrix,true));
                runOnUiThread(()->{
                    //???Glide????????????(?????????????????????????????????????????????LAG????????????????????????Thread?????????)
                    Glide.with(this)
                            .load(getHighImage.get())
                            .centerCrop()
                            .into(photoShow);
                });
            }).start();
            //????????????????????????
            takePhoto.setText(getString(R.string.take_photo_again));
            photoIdf.setVisibility(View.VISIBLE); //??????button??????
        }else {
            Toasty.info(RecordActivity.this, getString(R.string.camera_not_action), Toast.LENGTH_SHORT, true).show();
        }
    }

    //2021/02/19 ???????????????
    private void updateToApi() {
        //??????????????????
        changeRecordData.setTestDate(strDay);
        //Log.d(TAG, "?????????????????????: " + changeRecordData.toJSONString());
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
                    try {
                        JSONObject jsonObject = new JSONObject(result.toString());
                        int errorCode = jsonObject.getInt("errorCode");
                        if (errorCode == 0) {
                            Toasty.success(RecordActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT, true).show();
                            setResult(RESULT_OK);
                            finish();
                        }else if (errorCode == 23){ //token??????
                            Toasty.error(RecordActivity.this, getString(R.string.idle_too_long), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(RecordActivity.this, LoginActivity.class));
                            finish();
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


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void initBle() {
        mBluetoothManager = (BluetoothManager)this.getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if(mBluetoothAdapter == null){ //??????==null?????????finish()???????????????
            Toasty.error(getBaseContext(), getString(R.string.No_sup_Bluetooth), Toast.LENGTH_SHORT, true).show();
            finish();
            return;
        }else if (!mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.enable(); //????????????
        }

        //??????ble??????
        dialogBleConnect();
    }

    //Ble search  2021/03/15
    private void dialogBleConnect() {
        alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_bleconnect, null);
        RecyclerView dialogView = view.findViewById(R.id.rvBleSearch);

        //??????????????????
        mBluetoothDeviceList = new ArrayList<>();

        //????????????RSSI??????
        mRssiList = new ArrayList<>();

        mDeviceListAdapter = new BleDeviceListAdapter(mBluetoothDeviceList, mRssiList);
        dialogView.setLayoutManager(new LinearLayoutManager(this));
        dialogView.setAdapter(mDeviceListAdapter);
        alertDialog.setView(view);
        alertDialog.setCancelable(false);  //disable touch screen area only cancel's button can close dialog
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); //dialog????????????

        //??????item?????????????????????
        mDeviceListAdapter.setOnItemClickListener(new BleDeviceListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {    // ????????????
                Toasty.info(RecordActivity.this, getString(R.string.ble_start_connected), Toast.LENGTH_SHORT, true).show();

                //????????????
                mBluetoothAdapter.stopLeScan(mLeScanCallback);

                //????????????
                mBleService.connect(mBluetoothDeviceList.get(position).getAddress());

                //??????????????????
                alertDialog.dismiss();
            }
        });

        //????????????????????????
        scanBleDevice();

        //????????????BLE??????????????????dialog
        Button bleCancel = view.findViewById(R.id.btnBleCancel);
        bleCancel.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
            @Override
            public void onClick(View view) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                Log.d(TAG, "dialog : ?????????????????????????????????");
                alertDialog.dismiss();  //?????????dialog
            }
        });

        alertDialog.show();
    }

    //????????????fxn
    private void scanBleDevice() {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            //??????5???
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    Toasty.info(getBaseContext(), getString(R.string.ble_stop_in_5), Toast.LENGTH_SHORT, true).show();
                }
            },5000);
    }

    /*** ???????????????????????? callback ***/
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] bytes) {
            if(!mBluetoothDeviceList.contains(device)){
                if (device.getName() != null)           //????????????????????????
                    mBluetoothDeviceList.add(device);
                mRssiList.add(String.valueOf(rssi));
                mDeviceListAdapter.notifyDataSetChanged();
            }
        }
    };

    /*** ??????????????????????????? **/
    private void registerBleReceiver(){
        Log.d(TAG, "??????????????????????????? ");

        //??????BLEService????????????
        Intent intent = new Intent(this, BleService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        startService(intent);

        mBleReceiver = new BleReceiver();

        //????????????
        registerReceiver(mBleReceiver, mBleService.makeIntentFilter());
    }

    /**
     * ?????????????????????
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
                    if (countDownTimer != null)
                        countDownTimer.cancel();  //???????????????
                    break;

                case BleService.ACTION_CONNECTING_FAIL:
                    Toasty.info(RecordActivity.this, getString(R.string.ble_not_connect), Toast.LENGTH_SHORT,true).show();
                    mBleService.disconnect();
                    bleConnectStatus.setText(getString(R.string.ble_connected_fail));
                    if (countDownTimer != null)
                        countDownTimer.cancel();  //???????????????
                    break;

                case BleService.ACTION_NOTIFY_SUCCESS: //?????????????????????textView????????????
                    deviceAddress = intent.getStringExtra(BleService.EXTRA_MAC);
                    bleConnectStatus.setText(correctDeviceName + getString(R.string.ble_device_connected));
                    bleConnectStatus.setTextColor(Color.RED);
                    startMeasure.setVisibility(View.VISIBLE);        //?????????????????? 2021/04/20
                    searchBluetooth.setVisibility(View.INVISIBLE);   //?????????????????? 2021/04/21
                    break;

                case BleService.ACTION_DATA_AVAILABLE:  //2021/03/15
//                    Log.d(TAG, "?????????????????????: " + ByteUtils.byteArrayToString(data));
                    String[] str = ByteUtils.byteArrayToString(data).split(","); //???,??????
                    String degreeStr = str[2];
                    double degree = Double.parseDouble(degreeStr)/100;
                    textTemperature.setText(String.valueOf(degree)); //????????????
                    break;

                default:
                    break;
            }
        }
    }

    /**  ???????????? */
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

    //??????command
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
        //???????????????????????????
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

        mBleService.disconnect();   //????????????
        mBleService.release();      //????????????

        unregisterReceiver(mBleReceiver);    //????????????
        unbindService(mServiceConnection);   //???????????????
        mBleService = null;
    }
}