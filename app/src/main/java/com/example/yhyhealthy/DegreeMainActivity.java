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
import android.os.CountDownTimer;
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
import java.text.DecimalFormat;
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
 * ??????????????????
 * ??????????????? - BluetoothLeAdapter
 * ?????????????????? - DegreeMainAdapter
 * ?????????????????? - RemoteAdapter
 * *****/

public class DegreeMainActivity extends DeviceBaseActivity implements View.OnClickListener, DegreeMainAdapter.DegreeMainAdapterListener {

    private static final String TAG = "DegreeMainActivity";

    private Button btnSupervise, btnRemote;
    private Button btnAddSupervise, btnAddRemoter;
    private Button selectedAccount;

    //?????????
    private RecyclerView rvSupervise;
    private List<BleUserData.SuccessBean> dataList;
    private BleUserData.SuccessBean memberBean;
    private DegreeMainAdapter dAdapter;

    //?????????
    private RecyclerView rvRemote;
    private ArrayAdapter<String> arrayAdapter;  //????????????????????????Array
    private List<RemoteAccountApi.SuccessBean> remoteList;
    private RemoteAdapter rAdapter;
    private String accountInfoClick;

    //????????????
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

    //??????
    private ChartDialog chartDialog;

    //Other
    boolean isBleList = true;  //?????????????????????????????????????????????????????????
    private BleUserData.SuccessBean statusMemberBean;
    private int statusPosition;

    //ble?????????
    private MyRun myRun;
    private ArrayMap<String, Runnable> bleScheduleMap = new ArrayMap<>();
    private CountDownTimer countDownTimer = null;
    private ArrayMap<String, CountDownTimer> bleCountDownMap = new ArrayMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //title??????
        setContentView(R.layout.activity_degree_main);
        setTitle(R.string.title_temp);
        setActionButton(R.string.edit, manager);

        //????????????
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        proxy = ApiProxy.getInstance();

        initView();
    }

    private void initView() {
        btnSupervise = findViewById(R.id.btnLocal);
        btnRemote = findViewById(R.id.btnRemote);
        btnAddSupervise = findViewById(R.id.btnAddLocal);
        btnAddRemoter = findViewById(R.id.btnAddRemote);
        selectedAccount = findViewById(R.id.btnSelectedAccount);

        //recyclerView init
        rvSupervise = findViewById(R.id.rvLocalView);  //?????????RecyclerView
        rvRemote = findViewById(R.id.rvRemoteView);    //?????????RecyclerView

        initBleConnectList();  //????????????????????????

        //onClick
        btnSupervise.setOnClickListener(this);
        btnRemote.setOnClickListener(this);
        btnAddSupervise.setOnClickListener(this);
        btnAddRemoter.setOnClickListener(this);
        selectedAccount.setOnClickListener(this);

        //???????????????????????????
        btnSupervise.setBackgroundResource(R.drawable.shape_temp_button);
    }

    //??????
    private View.OnClickListener manager = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(isBleList){ //???????????????????????????
                startActivity(new Intent(DegreeMainActivity.this, DegreeListEditActivity.class));
            }else { //??????????????????????????????
                startActivity(new Intent(DegreeMainActivity.this, RemoteListEditActivity.class));
            }
        }
    };

    //?????????init
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
                        if (errorCode == 0) {
                            parserJson(result);
                        }else if (errorCode == 6){ //????????????
                            Toasty.error(DegreeMainActivity.this, getString(R.string.no_date), Toast.LENGTH_SHORT, true).show();
                        }else if (errorCode == 23) { //??????
                            Toasty.error(DegreeMainActivity.this, getString(R.string.idle_too_long), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(DegreeMainActivity.this, LoginActivity.class));
                            finish();
                        }else {
                            Toasty.error(DegreeMainActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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

    //??????????????????????????????????????????
    private void parserJson(JSONObject result) {
        BleUserData connectUserData = BleUserData.newInstance(result.toString());
        dataList = connectUserData.getSuccess();

        //??????????????????Adapter???????????????
        dAdapter = new DegreeMainAdapter(this, dataList, this);
        //??????item????????????
        int spacingInPixels = 10;
        rvSupervise.setAdapter(dAdapter);
        rvSupervise.setHasFixedSize(true);
        rvSupervise.setLayoutManager(new LinearLayoutManager(this));
        rvSupervise.addItemDecoration(new SpacesItemDecoration(spacingInPixels)); //??????item??????
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnLocal:  //?????????onClick
                btnSupervise.setBackgroundResource(R.drawable.shape_temp_button);
                btnRemote.setBackgroundResource(R.drawable.shape_for_temperature);
                btnAddSupervise.setVisibility(View.VISIBLE);
                btnAddRemoter.setVisibility(View.GONE);
                rvSupervise.setVisibility(View.VISIBLE);
                rvRemote.setVisibility(View.GONE);
                selectedAccount.setVisibility(View.GONE);
                isBleList = true;
                initBleConnectList(); //????????????????????????
                break;
            case R.id.btnRemote: //?????????onClick
                btnSupervise.setBackgroundResource(R.drawable.shape_for_temperature);
                btnRemote.setBackgroundResource(R.drawable.shape_temp_button);
                btnAddSupervise.setVisibility(View.GONE);
                btnAddRemoter.setVisibility(View.VISIBLE);
                rvSupervise.setVisibility(View.GONE);
                rvRemote.setVisibility(View.VISIBLE);
                selectedAccount.setVisibility(View.VISIBLE);
                isBleList = false;
                initRemoteAccountList();   //????????????????????????List  2021/03/25
                break;
            case R.id.btnAddLocal: //???????????????
                Intent intent = new Intent(this, DegreeAddActivity.class);
                startActivityForResult(intent, 1);
                break;
            case R.id.btnAddRemote: //?????????????????????
                showRemote();       //????????????
                break;
            case R.id.btnSelectedAccount: //????????????
                initRemoteAccountList();  //?????????
                showAccountSelected();    //????????????
                break;

        }
    }

    //?????????:????????????
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
                        if (errorCode == 0) {
                            JSONArray array = object.getJSONArray("success");
                            for (int i = 0; i < array.length(); i++) {
                                //???????????????arrayAdapter
                                arrayAdapter.add(array.getString(i));
                            }
                        }else if (errorCode == 23) { //token??????
                            Toasty.error(DegreeMainActivity.this, getString(R.string.idle_too_long), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(DegreeMainActivity.this, LoginActivity.class));
                            finish();
                        }else {
                            Toasty.error(DegreeMainActivity.this, getString(R.string.json_error_code) + errorCode + errorCode, Toast.LENGTH_SHORT, true).show();
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


    //?????????????????????:????????????:??????&???????????????
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
                //????????????????????????
                if (TextUtils.isEmpty(account.getText().toString()))
                    return;
                if (TextUtils.isEmpty(authCode.getText().toString()))
                    return;
                //????????????
                updateRemoteToApi(account, authCode);
            }
        });

        alertDialog.show();
    }

    //????????????????????????
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
                        if (errorCode == 0) {
                            Toasty.success(DegreeMainActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT, true).show();
                            initRemoteAccountList(); //????????????
                        }else if (errorCode == 23){ //token??????
                            Toasty.error(DegreeMainActivity.this, getString(R.string.idle_too_long), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(DegreeMainActivity.this, LoginActivity.class));
                            finish();
                        }else {
                            Toasty.error(DegreeMainActivity.this, getString(R.string.json_error_code)+ errorCode, Toast.LENGTH_SHORT,true).show();
                        }
                    } catch (JSONException e) {
                        Log.d(TAG, "JSONException: " + e.toString());
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

    //????????????dialog
    private void showAccountSelected() {  //2021/03/29
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.please_select_one_account));

        //2021/04/19 ??????????????????
        if (arrayAdapter.isEmpty()){
            Toasty.info(this, R.string.no_date, Toast.LENGTH_SHORT, true).show();
        }else {
            builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //????????????????????????????????????
                    getAccountInfoFromApi(arrayAdapter.getItem(which));
                    //??????????????????????????????????????????
                    accountInfoClick = arrayAdapter.getItem(which);
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    //????????????????????????????????????
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
                        }else if (errorCode == 32) {
                            Toasty.error(DegreeMainActivity.this, getString(R.string.remote_auth_code_error), Toast.LENGTH_SHORT, true).show();
                        }else if (errorCode == 23){ //token??????
                            Toasty.error(DegreeMainActivity.this, getString(R.string.idle_too_long), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(DegreeMainActivity.this, LoginActivity.class));
                            finish();
                        }else {
                            Toasty.error(DegreeMainActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
                        }
                    } catch (JSONException e) {
                        Log.d(TAG, "JSONException: " + e.toString());
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

    //?????????????????????????????????????????????RecyclerView?????? 2021/03/29
    private void parserRemoteData(JSONObject result) {
        RemoteAccountApi remoteData = RemoteAccountApi.newInstance(result.toString());
        remoteList = remoteData.getSuccess();

        //???????????????adapter
        rAdapter = new RemoteAdapter(this, remoteList);
        rvRemote.setAdapter(rAdapter);
        rvRemote.setHasFixedSize(true);
        rvRemote.setLayoutManager(new LinearLayoutManager(this));
        rvRemote.addItemDecoration(new SpacesItemDecoration(10));

        //???????????????????????????Button???
        selectedAccount.setText(accountInfoClick);
        selectedAccount.setBackgroundResource(R.drawable.shape_transparent);
        selectedAccount.setTextColor(Color.RED);
    }

    //?????????????????????????????????????????? (?????????????????????????????????)
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
                        }else if (errorCode == 23){ //token??????
                            Toasty.error(DegreeMainActivity.this, getString(R.string.idle_too_long), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(DegreeMainActivity.this, LoginActivity.class));
                            finish();
                        }else {
                            Toasty.error(DegreeMainActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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
        //?????????????????????
        registerBleReceiver();
    }

    //????????????????????????????????????BLE??????
    private void registerBleReceiver() {
        //??????BLEService????????????
        Intent intent = new Intent(this, BleService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        startService(intent);

        mBleReceiver = new BleReceiver();

        //??????????????????
        registerReceiver(mBleReceiver, BleService.makeIntentFilter());
    }

    /*** ?????????????????? */
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
     * ?????????????????????
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
                    Log.d(TAG, "onReceive: ???????????????....");
                    //Toasty.info(DegreeMainActivity.this, getString(R.string.ble_device_name) + deviceName + getString(R.string.ble_is_connecting), Toast.LENGTH_SHORT,true).show();
                    break;

                case BleService.ACTION_GATT_DISCONNECTED:  //????????????
                    Log.d(TAG, "onReceive: ???????????????????????????");
                    //Toasty.info(DegreeMainActivity.this, getString(R.string.ble_device_name) + deviceName + getString(R.string.ble_disconnect_release), Toast.LENGTH_SHORT,true).show();
                    mBleService.closeGatt(deviceAddress);
                    updateDisconnectedStatus(deviceName, deviceAddress, getString(R.string.ble_unconnected));
                    break;
                case BleService.ACTION_GATT_DISCONNECTED_SPECIFIC: //?????????????????????????????????
                    Toasty.info(DegreeMainActivity.this, getString(R.string.ble_device_name) + deviceName + getString(R.string.ble_unconnected), Toast.LENGTH_SHORT,true).show();
                    updateDisconnectedStatus(deviceName,deviceAddress,getString(R.string.ble_unconnected));
                    break;

                case BleService.ACTION_CONNECTING_FAIL:
                    Log.d(TAG, "onReceive: ???????????????????????????");
                    mBleService.disconnect();
                    break;

                //?????????????????????????????????????????????????????????javaBean
                case BleService.ACTION_NOTIFY_SUCCESS:
                    Log.d(TAG, "onReceive: notify????????????");
                    updateConnectedStatus(deviceName, deviceAddress, getString(R.string.ble_device_connected));
                    break;

                //????????????command??????????????????
                case BleService.ACTION_DATA_AVAILABLE:
                    String receiverInfo  = ByteUtils.byteArrayToString(data);
                    //?????????????????????
                    updateBleData(receiverInfo, deviceAddress);

                default:
                    break;
            }
        }
    }

    /******  ??????????????????????????????  *******/
    //?????????????????????adapter
    private void updateDisconnectedStatus(String deviceName, String deviceAddress, String bleStatus){

        if(deviceAddress != null){
            if(dAdapter.findNameByMac(deviceAddress) != null){
                dAdapter.disconnectedDevice(deviceAddress, bleStatus, deviceName);

                //???????????? 2021/04/28
                bleScheduleMap.remove(deviceAddress);
                bleCountDownMap.remove(deviceAddress);
                //????????????
                countDownTimer.cancel();
                mHandler.removeCallbacks(myRun);

            }else { //??????????????????,???????????????
                Toasty.info(this, getString(R.string.ble_connected_fail_and_try_again), Toast.LENGTH_SHORT, true).show();
            }
        }
    }

    /**** ?????????????????????????????? ******/
    private void updateConnectedStatus(String devName, String devAddress, String bleStatus){
        if (devAddress != null){
            statusMemberBean.setBleMac(devAddress);
            statusMemberBean.setBleConnectStatus(devName+bleStatus);
            statusMemberBean.setBleDeviceName(devName);  //2021/05/21??????
            dAdapter.updateItem(statusMemberBean, statusPosition);
        }
    }

    /*** ????????????????????? ***/
    private void updateBleData(String receiverData, String macAddress) {
        DecimalFormat dt = new DecimalFormat("#.##");

        String[] str = receiverData.split(","); //???,??????
        double degree = Double.parseDouble(str[2])/100;
        double battery = Double.parseDouble(str[3]);
        String batteryStr = dt.format(battery);

        if (degree != 0){
            //??????????????????????????????adapter
            dAdapter.updateByMac(degree,batteryStr,macAddress);

            //??????chart??????????????????????????????
            if(chartDialog != null && chartDialog.isShowing())
                //???mac??????dataBean 2021/05/03
                chartDialog.update(dAdapter.getDegreeByMac(macAddress));

            //????????????40%??????  2021/05/21
            if (battery < 40) {
                String deviceName = dAdapter.findDeviceNameByMac(macAddress);
                Toasty.warning(DegreeMainActivity.this, deviceName + getString(R.string.battery_is_low_40), Toast.LENGTH_SHORT, true).show();
            }
            //????????????25???C 2021/05/21
            if (degree <= 25){
                String userName = dAdapter.findNameByMac(macAddress);
                Toasty.warning(DegreeMainActivity.this, userName + getString(R.string.under_25_degree), Toast.LENGTH_SHORT, true).show();
            }

            //??????????????????37.5????????????
            if (degree > 37.5)
                feverDialog(dAdapter.findNameByMac(macAddress),degree); //????????????????????????
        }
    }

    //???????????? 2021/04/22
    private void feverDialog(String userName, double degree) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.fever_dialog, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();

        //???????????????
        TextView feverName = view.findViewById(R.id.tvFeverName);
        feverName.setText(userName);

        //????????????
        TextView feverDegree = view.findViewById(R.id.tvFeverDegree);
        feverDegree.setText(String.valueOf(degree));

        ImageView close = view.findViewById(R.id.ivClosefever);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();  //????????????
            }
        });

        dialog.show();
    }

    /***  ???????????????????????? *****/
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

    /*** ?????? ?????????  ***/
    private void initBle(){
        mBleService.initialize();  //????????????
        dialogBleConnect(); //????????????
    }

    /*** ???????????? ****/
    private void dialogBleConnect(){
        mAlertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = LayoutInflater.from(this);
        View bleView = inflater.inflate(R.layout.dialog_bleconnect, null);
        RecyclerView bleRecycleView = bleView.findViewById(R.id.rvBleSearch);

        /*** ??????RecyclerView ?????? ***/
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
                    Log.d(TAG, "5???????????????: ");
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
                Log.d(TAG, "????????????????????????????????? ");
                mAlertDialog.dismiss(); //????????????
            }
        });
        mAlertDialog.show();
    }
    /** ???????????????????????? ****/
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            new Thread(()->{
                /**???????????????????????????????????????*/
                if (device.getName()!= null){
                    /**?????????????????????????????????*/
                    findDevice.add(new ScannedData(device.getName()
                            , String.valueOf(rssi)
                            , device.getAddress()));
                    /**??????????????????Address?????????????????????????????????????????????*/
                    ArrayList newList = getSingle(findDevice);
                    runOnUiThread(()->{
                        /**???????????????RecyclerView?????????*/
                        bluetoothLeAdapter.addDevice(newList);
                    });
                }
            }).start();
        }
    };

    /**???????????????????????????(???Address??????)*/
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
     * ???Address????????????->??????????????????????????????
     */
    private int getIndex(ArrayList temp, Object obj) {
        for (int i = 0; i < temp.size(); i++) {
            if (temp.get(i).toString().contains(obj.toString())) {
                return i;
            }
        }
        return -1;
    }

    /****  ?????????????????????????????????gatt?????? ****/
    private BluetoothLeAdapter.OnItemClick itemClick = new BluetoothLeAdapter.OnItemClick() {
        @Override
        public void onItemClick(ScannedData selectedDevice) {

            mBluetoothAdapter.stopLeScan(mLeScanCallback); //????????????

            //??????gatt??????
            mBleService.connect(selectedDevice.getAddress());

            //????????????
            if (mAlertDialog.isShowing())
                mAlertDialog.dismiss();
        }
    };

    @Override //??????????????????
    public void onBleConnect(BleUserData.SuccessBean data, int position) {
        statusMemberBean = data;     //???????????????
        statusPosition = position;   //?????????RecyclerView't item??????

        initBle();  //???????????????
    }

    @Override //????????????
    public void onBleMeasuring(BleUserData.SuccessBean data) {

        //5????????????5???????????????????????? 2021/05/12
        countDownTimer = new CountDownTimer(60000*5, 5000) {
            @Override
            public void onTick(long millisUntilFinished) {
                sendCommand(data.getBleMac());
            }

            @Override
            public void onFinish() {
                //2021/05/05 ??????????????????
                myRun = new MyRun(data.getBleMac());
                Thread thread = new Thread(myRun);
                thread.start();

                bleScheduleMap.put(data.getBleMac(), myRun); //??????????????????arrayMap

            }
        };
        countDownTimer.start();
        bleCountDownMap.put(data.getBleMac(), countDownTimer);
    }

    @Override  //???????????? 2021/04/29
    public void onBleDisconnected(BleUserData.SuccessBean data) {
        //???????????????????????????
        mBleService.closeGatt(data.getBleMac());
        //??????5???????????????
        mHandler.removeCallbacks(bleScheduleMap.get(data.getBleMac()));
        //??????5???????????????
        bleScheduleMap.remove(data.getBleMac());
        //??????5????????????
        countDownTimer.cancel();
        //??????5????????????
        bleCountDownMap.remove(data.getBleMac());
    }

    @Override //????????????????????????
    public void onBleChart(BleUserData.SuccessBean data, int position) {
        if(data.getBleMac() != null){
            chartDialog = new ChartDialog(this, data);
            chartDialog.setCancelable(false);
            chartDialog.show();
        }
    }

    @Override  // ????????????  2021/04/14
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

    @Override  //?????????????????????
    public void passTarget(int targetId, double degree) {
        //??????????????????????????????????????????
        updateDegreeValueToApi(targetId, degree);
    }

    //??????fxn 2021/05/05
    public class MyRun implements Runnable{

        private String mac;

        public MyRun(String mac) {
            this.mac = mac;
        }

        @Override
        public void run() {
            Log.d(TAG, "???5??????sendCommand: " + mac);
            sendCommand(mac);
            mHandler.postDelayed(this, 1000 * 60 * 5);
        }
    }

    @Override  //???????????????????????????
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK){

            //???????????????????????????RecyclerView
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
            //mBleService.disconnect();  //????????????
            //mBleService.release();
        }

        unbindService(mServiceConnection);
        mBleService = null;

        //???????????????handler
        if (mHandler != null)
            mHandler.removeCallbacksAndMessages(null);

        //???????????????????????????????????????
        if(!bleScheduleMap.isEmpty())
            bleScheduleMap.clear();

        if (!bleCountDownMap.isEmpty())
            bleCountDownMap.clear();

        //??????5?????????
        if (countDownTimer != null)
            countDownTimer.cancel();

        //??????????????????
        if(chartDialog != null && chartDialog.isShowing())
            chartDialog.dismiss();
    }

}