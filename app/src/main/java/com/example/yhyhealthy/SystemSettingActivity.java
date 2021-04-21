package com.example.yhyhealthy;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yhyhealthy.adapter.BluetoothLeAdapter;
import com.example.yhyhealthy.dataBean.ScannedData;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

import es.dmoral.toasty.Toasty;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

/**
 * 設定 - 系統設定
 * 溫度 dialog : sharePref
 * 藍芽 dialog
 * create 2021/04/04
 * */

public class SystemSettingActivity extends DeviceBaseActivity implements View.OnClickListener {

    private static final String TAG = "SystemSettingActivity";

    private ImageView toBleSetting, toUnitSetting;
    private EditText edtNewName;
    private int checkItem = 0;

    //藍芽
    private BleService mBleService;
    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BroadcastReceiver mBleReceiver;
    private ArrayList<ScannedData> findDevice = new ArrayList<>();
    private boolean isScanning = false;
    private Handler mHandler = new Handler();
    private BluetoothLeAdapter adapter;
    private String deviceName;
    private String deviceAddress;

    //彈跳視窗
    private AlertDialog bleDialog;
    private AlertDialog unitDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //title隱藏
        setContentView(R.layout.activity_system_setting);
        setTitle(R.string.system_setting);

        initView();
    }

    private void initView() {
        edtNewName = findViewById(R.id.editBleName);
        toBleSetting = findViewById(R.id.ivToBleSetting);
        toUnitSetting = findViewById(R.id.ivUnitSetting);
        toBleSetting.setOnClickListener(this);    //藍芽更名
        toUnitSetting.setOnClickListener(this);   //溫度單位

        edtNewName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    if(TextUtils.isEmpty(edtNewName.getText().toString())){
                        return false;
                    }
                    //改設備名稱command
                    sendCommand();

                    edtNewName.setVisibility(View.INVISIBLE); //隱藏
                }
                return false;
            }
        });
    }

    //改設備名稱command
    private void sendCommand() {
        String request = "AIDO,1," + edtNewName.getText().toString(); //更名command

        byte[] messageBytes = new byte[0];
        try {
            messageBytes = request.getBytes("UTF-8");       //Sting to byte
            mBleService.writeDataToDevice(messageBytes, deviceAddress); //寫入設備
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ivToBleSetting:  //藍芽連線設定
                if(ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    initBle();  //初始化藍芽
                }else {
                    requestPermission();
                }
                break;
            case R.id.ivUnitSetting:  //溫度單位選擇
                showUnitDialog();
                break;
        }
    }

    //溫度單位選擇彈跳視窗
    private void showUnitDialog(){

        SharedPreferences pref = getSharedPreferences("yhyHealthy", MODE_PRIVATE);

        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(SystemSettingActivity.this);
        builder.setTitle(getString(R.string.please_chose_unit));

        String[] values = getResources().getStringArray(R.array.unit_arr); //數據
        //檢查溫度單位:取得checkItem的數據
        checkUnit();

        builder.setSingleChoiceItems(values, checkItem, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int item) {

                switch(item)
                {
                    case 0:
                        Toasty.info(SystemSettingActivity.this,getString(R.string.your_chose_unit_is_celsius), Toast.LENGTH_SHORT, true).show();
                        pref.edit().putString("UNIT", "celsius").apply();
                        break;
                    case 1:
                        Toasty.info(SystemSettingActivity.this,getString(R.string.your_chose_unit_is_fahrenheit), Toast.LENGTH_SHORT, true).show();
                        pref.edit().putString("UNIT", "fahrenheit").apply();
                        break;
                }
                unitDialog.dismiss();
            }
        });

        unitDialog = builder.create();
        unitDialog.show();
    }

    //檢查溫度單位
    private void checkUnit() {
        String unit = getSharedPreferences("yhyHealthy", Context.MODE_PRIVATE).getString("UNIT", "");
        if (!TextUtils.isEmpty(unit)){
            if (unit.equals("celsius")){
                checkItem = 0; //攝氏
            }else if (unit.equals("fahrenheit")){
                checkItem = 1; //華氏
            }
        }
    }

    //初始化藍芽
    private void initBle() {
        //啟用藍芽配適器
        mBleService.initialize();

        //dialog or Activity for  ble search 開始掃描
        dialogBleConnect();
    }

    //掃描藍芽設備
    private void dialogBleConnect() {
        bleDialog = new AlertDialog.Builder(this).create();
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View bleView = layoutInflater.inflate(R.layout.dialog_bleconnect, null);
        RecyclerView bleRecyclerView = bleView.findViewById(R.id.rvBleSearch); //放置掃描到的藍芽設備List

        //List藍芽Adapter配適器
        adapter = new BluetoothLeAdapter();
        bleRecyclerView.setAdapter(adapter);
        bleRecyclerView.setHasFixedSize(true);
        bleRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        bleDialog.setView(bleView);
        bleDialog.setCancelable(false);
        bleDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));  //背景透明

        adapter.OnItemClick(itemClick);  //將使用者點擊的item傳給adapter

        //一啟動視窗就先掃描
        isScanning = true;
        if (isScanning){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    isScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    Log.d(TAG, "5秒停止搜尋: ");
                }
            }, 5000);
            isScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            findDevice.clear();
            adapter.clearDevice();
        }else {
            isScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }

        //取消Button
        Button btnCancel = bleView.findViewById(R.id.btnBleCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                Log.d(TAG, "使用者自行取消搜尋功能 ");
                bleDialog.dismiss(); //關閉視窗
            }
        });

        bleDialog.show();
    }

    /****  顯示掃描到物件 掃描 Callback *****/
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            new Thread(()->{
                /**如果裝置沒有名字，就不顯示*/
                if (device.getName()!= null){
                    /**將搜尋到的裝置加入陣列*/
                    findDevice.add(new ScannedData(device.getName()
                            , String.valueOf(rssi)
                            , device.getAddress()));
                    /**將陣列中重複Address的裝置濾除，並使之成為最新數據*/
                    ArrayList newList = getSingle(findDevice);
                    runOnUiThread(()->{
                        /**將陣列送到RecyclerView列表中*/
                        adapter.addDevice(newList);
                    });
                }
            }).start();
        }
    };

    /**濾除重複的藍牙裝置(以Address判定)*/
    private ArrayList getSingle(ArrayList list) {
        ArrayList tempList = new ArrayList<>();
        try {
            Iterator it = list.iterator();
            while (it.hasNext()) {
                Object obj = it.next();
                if (!tempList.contains(obj)) {
                    tempList.add(obj);
                } else {
                    tempList.set(getIndex(tempList, obj), obj);
                }
            }
            return tempList;
        } catch (ConcurrentModificationException e) {
            return tempList;
        }
    }

    /**
     * 以Address篩選陣列->抓出該值在陣列的哪處
     */
    private int getIndex(ArrayList temp, Object obj) {
        for (int i = 0; i < temp.size(); i++) {
            if (temp.get(i).toString().contains(obj.toString())) {
                return i;
            }
        }
        return -1;
    }

    /**取得欲連線之裝置後跳轉頁面*/
    private BluetoothLeAdapter.OnItemClick itemClick = new BluetoothLeAdapter.OnItemClick() {
        @Override
        public void onItemClick(ScannedData selectedDevice) {

            mBluetoothAdapter.stopLeScan(mLeScanCallback); //停止掃描

            deviceName = selectedDevice.getDeviceName(); //將藍芽名稱給予全域變數

            //啟動ble server連線
            mBleService.connect(selectedDevice.getAddress());

            //關閉視窗
            if (bleDialog.isShowing())
                bleDialog.dismiss();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        //藍芽廣播-註冊器
        registerBleReceiver();
    }

    private void registerBleReceiver(){
        /** 綁定後台服務 ***/
        Intent intent = new Intent(this, BleService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        mBleReceiver = new BleReceiver();
        registerReceiver(mBleReceiver, BleService.makeIntentFilter());
    }

    /** ble Service 背景服務 **/
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBleService = ((BleService.LocalBinder) iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBleService = null;
        }
    };

    /**
     * 藍牙信息接收器
     */
    private class BleReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            switch (action) {

                case BleService.ACTION_GATT_CONNECTED:
                    Toasty.info(context, "藍芽連接中...", Toast.LENGTH_SHORT, true).show();
                    Log.d(TAG, "onReceive: 藍芽連接中..." );
                    break;

                case BleService.ACTION_GATT_DISCONNECTED:
                    Toasty.info(context, "藍芽已斷開並釋放資源", Toast.LENGTH_SHORT, true).show();
                    Log.d(TAG, "onReceive: 藍芽已斷開並釋放資源" );
                    mBleService.disconnect();
                    mBleService.release();
                    break;

                case BleService.ACTION_CONNECTING_FAIL:
                    Toasty.info(context, "藍芽已斷開", Toast.LENGTH_SHORT, true).show();
                    Log.d(TAG, "onReceive: 藍芽已斷開" );
                    mBleService.disconnect();
                    break;

                case BleService.ACTION_NOTIFY_SUCCESS:
                    Log.d(TAG, "onReceive: 收到BLE通知服務 啟動成功: ");
                    edtNewName.setVisibility(View.VISIBLE);        //顯示目前連線的藍芽設備編輯
                    edtNewName.setText(deviceName);                //顯示目前連線的藍芽設備名稱
                    edtNewName.setSelection(deviceName.length());  //游標出現在字尾
                    deviceAddress = intent.getStringExtra(BleService.EXTRA_MAC);  //將mac給予全域變數
                    break;
                default:
                    break;
            }
        }
    }
}