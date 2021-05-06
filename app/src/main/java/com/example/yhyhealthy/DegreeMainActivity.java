package com.example.yhyhealthy;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.yhyhealthy.adapter.BluetoothLeAdapter;
import com.example.yhyhealthy.adapter.DegreeMainAdapter;
import com.example.yhyhealthy.adapter.RemoteAdapter;
import com.example.yhyhealthy.dataBean.BleUserData;
import com.example.yhyhealthy.dataBean.RemoteAccountApi;
import com.example.yhyhealthy.dataBean.ScannedData;
import com.example.yhyhealthy.dialog.ChartDialog;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.tools.ByteUtils;
import com.example.yhyhealthy.tools.SpacesItemDecoration;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthy.module.ApiProxy.BLE_USER_ADD_VALUE;
import static com.example.yhyhealthy.module.ApiProxy.BLE_USER_LIST;
import static com.example.yhyhealthy.module.ApiProxy.REMOTE_USER_ADD;
import static com.example.yhyhealthy.module.ApiProxy.REMOTE_USER_LIST;
import static com.example.yhyhealthy.module.ApiProxy.REMOTE_USER_UNDER_LIST;

/*** *****
 * 藍芽量測首頁
 * 藍芽配適器
 * *****/

public class DegreeMainActivity extends DeviceBaseActivity implements View.OnClickListener, DegreeMainAdapter.DegreeMainAdapterListener {

    private static final String TAG = "DegreeMainActivity";

    private Button btnSupervise, btnRemote;
    private Button btnAddSupervise, btnAddRemoter;
    private Button selectedAccount;

    //觀測者
    private RecyclerView rvSupervise;
    private List<BleUserData.SuccessBean> dataList;
    private BleUserData.SuccessBean memberBean;
    private DegreeMainAdapter dAdapter;

    //遠端者
    private RecyclerView rvRemote;
    private ArrayAdapter<String> arrayAdapter;  //選擇遠端觀測帳號Array
    private List<RemoteAccountApi.SuccessBean> remoteList;
    private RemoteAdapter rAdapter;
    private String accountInfoClick;

    //藍芽相關
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BleService mBleService;
    private BroadcastReceiver mBleReceiver;
    private boolean isScanning = false;
    private ArrayList<ScannedData> findDevice = new ArrayList<>();
    private BluetoothLeAdapter bluetoothLeAdapter;
    private Handler mHandler = new Handler();
    private AlertDialog mAlertDialog;

    //Api
    private ApiProxy proxy;

    //圖表
    private ChartDialog chartDialog;


    //Other
    boolean isBleList = true;  //判斷目前是觀測者列表還是遠端觀測者列表
    private BleUserData.SuccessBean statusMemberBean;
    private int statusPosition;

    //ble定時用
    private MyRun myRun;
    private ArrayMap<String, Runnable> bleScheduleMap = new ArrayMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //title隱藏
        setContentView(R.layout.activity_degree_main);
        setTitle(R.string.title_temp);
        setActionButton(R.string.edit, manager);

        //休眠禁止
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        proxy = ApiProxy.getInstance();

        initView();

        //啟動定時器功能
        //requestDegree.run();
    }

    private void initView() {
        btnSupervise = findViewById(R.id.btnLocal);
        btnRemote = findViewById(R.id.btnRemote);
        btnAddSupervise = findViewById(R.id.btnAddLocal);
        btnAddRemoter = findViewById(R.id.btnAddRemote);
        selectedAccount = findViewById(R.id.btnSelectedAccount);

        //recyclerView init
        rvSupervise = findViewById(R.id.rvLocalView);  //觀測者RecyclerView
        rvRemote = findViewById(R.id.rvRemoteView);    //遠端者RecyclerView

        initBleConnectList();  //初始化觀測者列表

        //onClick
        btnSupervise.setOnClickListener(this);
        btnRemote.setOnClickListener(this);
        btnAddSupervise.setOnClickListener(this);
        btnAddRemoter.setOnClickListener(this);
        selectedAccount.setOnClickListener(this);

        //預先顯示觀測者頁面
        btnSupervise.setBackgroundResource(R.drawable.shape_temp_button);
    }

    //管理
    private View.OnClickListener manager = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(isBleList){ //編輯觀測者列表資料
                startActivity(new Intent(DegreeMainActivity.this, DegreeListEditActivity.class));
            }else { //編輯遠端帳號列表資料
                startActivity(new Intent(DegreeMainActivity.this, RemoteListEditActivity.class));
            }
        }
    };

    //觀測者init
    private void initBleConnectList(){
        proxy.buildPOST(BLE_USER_LIST, "", bleUserListListener);
    }

    private ApiProxy.OnApiListener bleUserListListener = new ApiProxy.OnApiListener() {
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
                            Toasty.error(DegreeMainActivity.this, getString(R.string.no_date), Toast.LENGTH_SHORT, true).show();
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

    //解析後台回來的觀測者列表資料
    private void parserJson(JSONObject result) {
        BleUserData connectUserData = BleUserData.newInstance(result.toString());
        dataList = connectUserData.getSuccess();

        //將資料配置到Adapter並顯示出來
        dAdapter = new DegreeMainAdapter(this, dataList, this);
        //設定item之間距離
        int spacingInPixels = 10;
        rvSupervise.setAdapter(dAdapter);
        rvSupervise.setHasFixedSize(true);
        rvSupervise.setLayoutManager(new LinearLayoutManager(this));
        rvSupervise.addItemDecoration(new SpacesItemDecoration(spacingInPixels)); //設定item間距
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnLocal:  //觀測者onClick
                btnSupervise.setBackgroundResource(R.drawable.shape_temp_button);
                btnRemote.setBackgroundResource(R.drawable.shape_for_temperature);
                btnAddSupervise.setVisibility(View.VISIBLE);
                btnAddRemoter.setVisibility(View.GONE);
                rvSupervise.setVisibility(View.VISIBLE);
                rvRemote.setVisibility(View.GONE);
                selectedAccount.setVisibility(View.GONE);
                isBleList = true;
                initBleConnectList(); //初始化觀測者列表
                break;
            case R.id.btnRemote: //遠端者onClick
                btnSupervise.setBackgroundResource(R.drawable.shape_for_temperature);
                btnRemote.setBackgroundResource(R.drawable.shape_temp_button);
                btnAddSupervise.setVisibility(View.GONE);
                btnAddRemoter.setVisibility(View.VISIBLE);
                rvSupervise.setVisibility(View.GONE);
                rvRemote.setVisibility(View.VISIBLE);
                selectedAccount.setVisibility(View.VISIBLE);
                isBleList = false;
                initRemoteAccountList();   //初始化監控者帳號List  2021/03/25
                break;
            case R.id.btnAddLocal: //新增觀測者
                Intent intent = new Intent(this, DegreeAddActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.btnAddRemote: //新增遠端觀測者
                showRemote();       //彈跳視窗
                break;
            case R.id.btnSelectedAccount: //選擇帳號
                initRemoteAccountList();  //先呼叫
                showAccountSelected();    //彈跳視窗
                break;

        }
    }

    //初始化:選擇帳號
    private void initRemoteAccountList() {
        proxy.buildPOST(REMOTE_USER_LIST, "", remoteAccountListener);
    }

    private ApiProxy.OnApiListener remoteAccountListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {

        }

        @Override
        public void onSuccess(JSONObject result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    arrayAdapter = new ArrayAdapter<String>(DegreeMainActivity.this, android.R.layout.select_dialog_item);
                    try {
                        JSONObject object = new JSONObject(result.toString());
                        int errorCode = object.getInt("errorCode");
                        if (errorCode == 0){
                            JSONArray array = object.getJSONArray("success");
                            for (int i=0; i < array.length(); i++){
                                //將資料塞入arrayAdapter
                                arrayAdapter.add(array.getString(i));
                            }
                        }else {
                            Log.d(TAG, "後端系統代碼: " + errorCode);
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

        }
    };


    //新增遠端觀測者:彈跳視窗:帳號&授權碼輸入
    private void showRemote() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View remoteView  = layoutInflater.inflate(R.layout.dialog_remote, null);
        alertDialog.setView(remoteView);
        alertDialog.setCancelable(false);

        EditText account = remoteView.findViewById(R.id.edtOtherAccount);
        EditText authCode = remoteView.findViewById(R.id.edtAuthorization);

        Button cancel = remoteView.findViewById(R.id.btnRemoteCancel);
        Button submit = remoteView.findViewById(R.id.btnRemoteSend);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //檢查資料是否齊全
                if (TextUtils.isEmpty(account.getText().toString()))
                    return;
                if (TextUtils.isEmpty(authCode.getText().toString()))
                    return;
                //傳給後台
                updateRemoteToApi(account, authCode);
            }
        });

        alertDialog.show();
    }

    //資料更新傳給後台
    private void updateRemoteToApi(EditText account, EditText authCode) {
        JSONObject json = new JSONObject();
        try {
            json.put("account", account.getText().toString());
            json.put("monitorCode", authCode.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        proxy.buildPOST(REMOTE_USER_ADD, json.toString(), remoteAddListener);
    }

    private ApiProxy.OnApiListener remoteAddListener = new ApiProxy.OnApiListener() {
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
                            Toasty.success(DegreeMainActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT, true).show();
                            initRemoteAccountList(); //重刷資料
                        }else {
                            Log.d(TAG, "新增觀測者失敗後台回覆碼: " + errorCode);
                        }
                    } catch (JSONException e) {

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

    //選擇帳號dialog
    private void showAccountSelected() {  //2021/03/29
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.please_select_one_account));

        //2021/04/19 增加一個判斷
        if (arrayAdapter.isEmpty()){
            Toasty.info(this, R.string.no_date, Toast.LENGTH_SHORT, true).show();
        }else {
            builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //取得監控者底下的資料列表
                    getAccountInfoFromApi(arrayAdapter.getItem(which));
                    //使用者點擊的項目給予全域變數
                    accountInfoClick = arrayAdapter.getItem(which);
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    //取得監控者底下的資料列表
    private void getAccountInfoFromApi(String accountNo) {
        JSONObject json = new JSONObject();
        try {
            json.put("account", accountNo);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        proxy.buildPOST(REMOTE_USER_UNDER_LIST, json.toString(), remoteUnderListListener);
    }

    private ApiProxy.OnApiListener remoteUnderListListener = new ApiProxy.OnApiListener() {
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
                            parserRemoteData(result);
                        }else if (errorCode == 6) {
                            Toasty.error(DegreeMainActivity.this, getString(R.string.you_chose_account_no_data), Toast.LENGTH_SHORT, true).show();
                        }else if (errorCode == 32){
                            Toasty.error(DegreeMainActivity.this, getString(R.string.remote_auth_code_error), Toast.LENGTH_SHORT, true).show();
                        }else {
                            Log.d(TAG, "後台回覆之錯誤代碼: " + errorCode);
                        }
                    } catch (JSONException e) {

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

    //解析後台回來的遠端帳戶資料並以RecyclerView顯示 2021/03/29
    private void parserRemoteData(JSONObject result) {
        RemoteAccountApi remoteData = RemoteAccountApi.newInstance(result.toString());
        remoteList = remoteData.getSuccess();

        //將資料傳到adapter
        rAdapter = new RemoteAdapter(this, remoteList);
        rvRemote.setAdapter(rAdapter);
        rvRemote.setHasFixedSize(true);
        rvRemote.setLayoutManager(new LinearLayoutManager(this));
        rvRemote.addItemDecoration(new SpacesItemDecoration(10));

        //將選擇的帳號顯示在Button上
        selectedAccount.setText(accountInfoClick);
        selectedAccount.setTextColor(Color.RED);
    }

    //上傳觀測的體溫量測資料到後端 (可作為日後的歷史資料用)
    private void updateDegreeValueToApi(int targetId, double degree) {
        DateTime dt1 = new DateTime();
        String degreeMeasureStr = dt1.toString("yyyy-MM-dd,HH:mm:ss");

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("targetId",targetId);
            jsonObject.put("celsius", degree);
            jsonObject.put("measuredTime",degreeMeasureStr);
            jsonObject.put("first", false);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONArray jsonArray = new JSONArray();
        jsonArray.put(jsonObject);

        JSONObject object = new JSONObject();
        try {
            object.put("infos", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        proxy.buildPOST(BLE_USER_ADD_VALUE, object.toString(), addValueListener);
    }

    private ApiProxy.OnApiListener addValueListener = new ApiProxy.OnApiListener() {
        @Override
        public void onPreExecute() {

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
                            Toasty.success(DegreeMainActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT, true).show();
                        }else {
                            Log.d(TAG, "新增體溫量測資料失敗代碼: " + errorCode);
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

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        //註冊藍芽接受器
        registerBleReceiver();
    }

    //註冊藍芽接受器並同時啟動BLE服務
    private void registerBleReceiver() {
        //綁定BLEService背景服務
        Intent intent = new Intent(this, BleService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        startService(intent);

        mBleReceiver = new BleReceiver();

        //註冊藍芽廣播
        registerReceiver(mBleReceiver, BleService.makeIntentFilter());
    }

    /*** 藍芽背景服務 */
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

    /**
     * 藍芽信息接受器
     */
    private class BleReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action))
                return;

            String deviceName = intent.getStringExtra(BleService.EXTRA_DEVICE_NAME);
            String deviceAddress = intent.getStringExtra(BleService.EXTRA_MAC);
            byte[] data = intent.getByteArrayExtra(BleService.EXTRA_DATA);

            switch (action){
                case BleService.ACTION_GATT_CONNECTED:
                    Log.d(TAG, "onReceive: 藍芽連接中....");
                    //Toasty.info(DegreeMainActivity.this, getString(R.string.ble_device_name) + deviceName + getString(R.string.ble_is_connecting), Toast.LENGTH_SHORT,true).show();
                    break;

                case BleService.ACTION_GATT_DISCONNECTED:  //自動斷線
                    Log.d(TAG, "onReceive: 藍芽斷開並釋放資源");
                    //Toasty.info(DegreeMainActivity.this, getString(R.string.ble_device_name) + deviceName + getString(R.string.ble_disconnect_release), Toast.LENGTH_SHORT,true).show();
                    mBleService.closeGatt(deviceAddress);
                    updateDisconnectedStatus(deviceName, deviceAddress, getString(R.string.ble_unconnected));
                    break;
                case BleService.ACTION_GATT_DISCONNECTED_SPECIFIC: //手動停止量測並斷開連結
                    Toasty.info(DegreeMainActivity.this, getString(R.string.ble_device_name) + deviceName + getString(R.string.ble_unconnected), Toast.LENGTH_SHORT,true).show();
                    updateDisconnectedStatus(deviceName,deviceAddress,getString(R.string.ble_unconnected));
                    break;

                case BleService.ACTION_CONNECTING_FAIL:
                    Log.d(TAG, "onReceive: 藍芽連結失敗並斷開");
                    mBleService.disconnect();
                    break;

                //這裡才將連線成功顯示出來且要將資料寫回javaBean
                case BleService.ACTION_NOTIFY_SUCCESS:
                    Log.d(TAG, "onReceive: notify啟動成功");
                    updateConnectedStatus(deviceName, deviceAddress, getString(R.string.ble_device_connected));
                    break;

                //藍芽受到command後回覆的資料
                case BleService.ACTION_DATA_AVAILABLE:
                    Log.d(TAG, "onReceive: 體溫原始資料" + ByteUtils.byteArrayToString(data));
                    String receiverInfo  = ByteUtils.byteArrayToString(data);
                    //更新體溫跟電量
                    updateBleData(receiverInfo, deviceAddress);

                default:
                    break;
            }
        }
    }

    /******  更新藍芽斷開後的資訊  *******/
    //斷線狀態回傳給adapter
    private void updateDisconnectedStatus(String deviceName, String deviceAddress, String bleStatus){

        if(deviceAddress != null){
            if(dAdapter.findNameByMac(deviceAddress) != null){
                dAdapter.disconnectedDevice(deviceAddress, bleStatus, deviceName);

                //移除佇列 2021/04/28
                bleScheduleMap.remove(deviceAddress);

            }else { //藍芽連接失敗,請重新連接
                Toasty.info(this, getString(R.string.ble_connected_fail_and_try_again), Toast.LENGTH_SHORT, true).show();
            }
        }
    }

    /**** 更新藍芽連線後的資訊 ******/
    private void updateConnectedStatus(String devName, String devAddress, String bleStatus){
        if (devAddress != null){
            statusMemberBean.setBleMac(devAddress);
            statusMemberBean.setBleConnectStatus(devName+bleStatus);
            dAdapter.updateItem(statusMemberBean, statusPosition);
        }
    }

    /*** 更新體溫跟電量 ***/
    private void updateBleData(String receiverData, String macAddress) {
        String[] str = receiverData.split(","); //以,分割
        String degreeStr = str[2];
        String batteryStr = str[3];
        double degree = Double.parseDouble(degreeStr)/100;
        double battery = Double.parseDouble(batteryStr);

        if (degree != 0){
            //將溫度電量等資訊傳到adapter
            dAdapter.updateByMac(degree,battery,macAddress);

            //如果chart存在就將資料直接傳遞
            if(chartDialog != null && chartDialog.isShowing())
                //以mac分開dataBean 2021/05/03
                chartDialog.update(dAdapter.getDegreeByMac(macAddress));

            //假如體溫超過37.5出現警示
            if (degree > 37.5)
                feverDialog(dAdapter.findNameByMac(macAddress),degree); //發燒警告彈跳視窗
        }
    }

    //發燒警告 2021/04/22
    private void feverDialog(String userName, double degree) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.fever_dialog, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        //顯示發燒者
        TextView feverName = view.findViewById(R.id.tvFeverName);
        feverName.setText(userName);

        //顯示體溫
        TextView feverDegree = view.findViewById(R.id.tvFeverDegree);
        feverDegree.setText(String.valueOf(degree));

        ImageView close = view.findViewById(R.id.ivClosefever);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();  //關閉視窗
            }
        });

        dialog.show();
    }

    /***  對藍芽設備下命令 *****/
    private void sendCommand(String deviceMac){
        String request = "AIDO,0";
        byte[] messagesBytes = new byte[0];
        try {
            messagesBytes = request.getBytes("UTF-8");
            mBleService.writeDataToDevice(messagesBytes, deviceMac); //command
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /*** 藍芽 初始化  ***/
    private void initBle(){
        mBleService.initialize();  //開啟藍芽
        dialogBleConnect(); //開始掃描
    }

    /*** 開啟掃描 ****/
    private void dialogBleConnect(){
        mAlertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = LayoutInflater.from(this);
        View bleView = inflater.inflate(R.layout.dialog_bleconnect, null);
        RecyclerView bleRecycleView = bleView.findViewById(R.id.rvBleSearch);

        /*** 設置RecyclerView 列表 ***/
        bluetoothLeAdapter = new BluetoothLeAdapter();
        bleRecycleView.setAdapter(bluetoothLeAdapter);
        bleRecycleView.setHasFixedSize(true);
        bleRecycleView.setLayoutManager(new LinearLayoutManager(this));

        mAlertDialog.setView(bleView);
        mAlertDialog.setCancelable(false);
        mAlertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        bluetoothLeAdapter.OnItemClick(itemClick);

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
            bluetoothLeAdapter.clearDevice();
        }else {
            isScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }

        Button btnCancel = bleView.findViewById(R.id.btnBleCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                Log.d(TAG, "使用者自行取消搜尋功能 ");
                mAlertDialog.dismiss(); //關閉視窗
            }
        });
        mAlertDialog.show();
    }
    /** 顯示掃描到的物件 ****/
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
                        bluetoothLeAdapter.addDevice(newList);
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

    /****  取得欲連線之裝置後啟動gatt連線 ****/
    private BluetoothLeAdapter.OnItemClick itemClick = new BluetoothLeAdapter.OnItemClick() {
        @Override
        public void onItemClick(ScannedData selectedDevice) {

            mBluetoothAdapter.stopLeScan(mLeScanCallback); //停止搜尋

            //啟動gatt連線
            mBleService.connect(selectedDevice.getAddress());

            //關閉視窗
            if (mAlertDialog.isShowing())
                mAlertDialog.dismiss();
        }
    };

    @Override //藍芽連線介面
    public void onBleConnect(BleUserData.SuccessBean data, int position) {
        statusMemberBean = data;     //使用者資料
        statusPosition = position;   //使用者RecyclerView't item位置

        initBle();  //初始化藍芽
    }

    @Override //啟動量測
    public void onBleMeasuring(BleUserData.SuccessBean data) {

        //2021/05/05 增加定時功能
        myRun = new MyRun(data.getBleMac());
        Thread thread = new Thread(myRun);
        thread.start();

        bleScheduleMap.put(data.getBleMac(), myRun); //塞定時資料給arrayMap

        sendCommand(data.getBleMac());  //command + device : 判斷用
    }

    @Override  //停止量測 2021/04/29
    public void onBleDisconnected(BleUserData.SuccessBean data) {
        //停止量測並斷開連結
        mBleService.closeGatt(data.getBleMac());
        //移除定時
        mHandler.removeCallbacks(bleScheduleMap.get(data.getBleMac()));
        //移除佇列
        bleScheduleMap.remove(data.getBleMac());
    }

    @Override //藍芽圖表介面呼叫
    public void onBleChart(BleUserData.SuccessBean data, int position) {
        if(data.getBleMac() != null){
            chartDialog = new ChartDialog(this, data);
            chartDialog.setCancelable(false);
            chartDialog.show();
        }
    }

    @Override  // 症狀輸入  2021/04/14
    public void onSymRecord(BleUserData.SuccessBean data, int position) {
        int targetId = data.getTargetId();

        Intent intent = new Intent();
        intent.setClass(this, SymptomActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("targetId",targetId);
        bundle.putInt("position",position);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override  //上傳資料到後台
    public void passTarget(int targetId, double degree) {
        //上傳觀測的體溫量測資料到後端
        updateDegreeValueToApi(targetId, degree);
    }

    //定時fxn 2021/05/05
    public class MyRun implements Runnable{

        private String mac;

        public MyRun(String mac) {
            this.mac = mac;
        }

        @Override
        public void run() {
            Log.d(TAG, "每5分鐘sendCommand: " + mac);
            sendCommand(mac);
            mHandler.postDelayed(this, 1000 * 60 * 5);
        }
    }

    @Override  //新增觀測者資料返回
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK){

            //跟後台要資料並刷新RecyclerView
            initBleConnectList();

        }else {
            Toasty.info(this, getString(R.string.nothing), Toast.LENGTH_SHORT, true).show();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
        if (mBleService != null){
            unregisterReceiver(mBleReceiver);
            mBleService = null;
            //mBleService.disconnect();  //斷開藍芽
            //mBleService.release();
        }

        unbindService(mServiceConnection);
        mBleService = null;

        //移除所有的handler
        if (mHandler != null)
            mHandler.removeCallbacksAndMessages(null);

        //移除所有在佇列中的藍芽設備
        if(!bleScheduleMap.isEmpty())
            bleScheduleMap.clear();

        //關閉彈跳視窗
        if(chartDialog != null && chartDialog.isShowing())
            chartDialog.dismiss();
    }

}