package com.example.yhyhealthy;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.yhyhealthy.adapter.RecyclerViewAdapter;
import com.example.yhyhealthy.adapter.RemoteAdapter;
import com.example.yhyhealthy.adapter.BluetoothLeAdapter;
import com.example.yhyhealthy.dataBean.Remote;
import com.example.yhyhealthy.dataBean.ScannedData;
import com.example.yhyhealthy.dialog.ChartDialog;
import com.example.yhyhealthy.dialog.TemperatureDiaolg;
import com.example.yhyhealthy.dataBean.Member;
import com.example.yhyhealthy.tools.SpacesItemDecoration;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class TemperatureActivity extends AppPage implements View.OnClickListener, RecyclerViewAdapter.RecyclerViewListener {

    private final static String TAG = "TemperatureActivity";

    private Button supervise, remote;
    private Button temperatureUserAdd, remoteUserAdd;

    //使用者
    private RecyclerView recyclerView;
    private RecyclerViewAdapter mAdapter;
    private List<Member> memberList;
    private String name;
    private Double degree = 00.00;
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm");

    //遠端
    private RecyclerView  remoteRecycle;
    private RemoteAdapter remoteAdapter;
    private List<Remote>  remoteList;

    //藍芽相關
    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothGattService gattService;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic characteristic;
    private static final int REQUEST_FINE_LOCATION_PERMISSION = 102;
    private static final int REQUEST_ENABLE_BT = 2;
    private boolean isScanning = false;
    private ArrayList<ScannedData> findDevice = new ArrayList<>();
    private RecyclerView bleRecycleView;
    private BluetoothLeAdapter bluetoothLeAdapter;
    private Handler mHandler;                    //Handler用來搜尋Devices10秒後，自動停止搜尋
    //藍芽服務UUID設置
    private static final UUID TEMPERATURE_SERVICE_UUID = UUID
            .fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID TEMPERATURE_NOTIF_UUID = UUID
            .fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    private static final UUID TEMPERATURE_WRITE_DATA = UUID
            .fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //title隱藏
        setContentView(R.layout.activity_temperature);
        setTitle(R.string.title_temp);

        initView();
    }

    private void initView() {
        supervise = findViewById(R.id.btnSeletSupervise);
        remote = findViewById(R.id.btnSelectRemote);
        temperatureUserAdd = findViewById(R.id.btnAddTempUser);
        remoteUserAdd = findViewById(R.id.btnAddRemote);

        //init RecyclerView's data
        recyclerView = findViewById(R.id.rvTempUser);
        remoteRecycle = findViewById(R.id.rvRemoteUser);
        bleRecycleView = findViewById(R.id.rvSignUser);

        setInfo();   //觀測者初始化

        supervise.setOnClickListener(this);
        remote.setOnClickListener(this);
        temperatureUserAdd.setOnClickListener(this);
        remoteUserAdd.setOnClickListener(this);

        supervise.setBackgroundResource(R.drawable.shape_temp_button); //預先顯示觀測者頁面
    }

    //觀測者init
    private void setInfo() {
        int spacingInPixels = 10;       //item間距
        memberList = new ArrayList<>(); //帶入資料

        setMemberData();

        mAdapter = new RecyclerViewAdapter(this, memberList, this);
        recyclerView.setAdapter(mAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new SpacesItemDecoration(spacingInPixels)); //設定item間距
    }

    private void setMemberData() { //日後要從api抓資料
        Member user1 = new Member(R.mipmap.imageview, "Matt Bomer", "未連線");
        Member user2 = new Member(R.mipmap.imageview3, "Brad Pitt", "未連線");

        memberList.add(user1);
        memberList.add(user2);
    }

    //新增遠端 - 彈跳視窗
    private void dialogRemote() {
        AlertDialog remoteDialog = new AlertDialog.Builder(this).create();
        LayoutInflater remoteLayout = LayoutInflater.from(this);
        View remoteView = remoteLayout.inflate(R.layout.dialog_remote, null);
        remoteDialog.setView(remoteView);
        remoteDialog.setCancelable(false);

        Button cancel = remoteView.findViewById(R.id.btnRemoteCancel);
        Button submit = remoteView.findViewById(R.id.btnRemoteSend);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                remoteDialog.dismiss();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //將資料送至後台Api
                //remoteApi();
                remoteDialog.dismiss();
            }
        });
        remoteDialog.show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSeletSupervise:
                supervise.setBackgroundResource(R.drawable.shape_temp_button);
                remote.setBackgroundResource(R.drawable.shape_for_temperature);
                temperatureUserAdd.setVisibility(View.VISIBLE);
                remoteUserAdd.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                remoteRecycle.setVisibility(View.GONE);
                break;
            case R.id.btnSelectRemote:
                supervise.setBackgroundResource(R.drawable.shape_for_temperature);
                remote.setBackgroundResource(R.drawable.shape_temp_button);
                temperatureUserAdd.setVisibility(View.GONE);
                remoteUserAdd.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                remoteRecycle.setVisibility(View.VISIBLE);
                break;
            case R.id.btnAddTempUser: //新增觀測者

                break;
            case R.id.btnAddRemote:  //新增監看者
                dialogRemote();
                break;
        }
    }

    /////////////// 藍芽 //////////////////////////////////
    private void initBle() {

        //啟用藍芽配適器
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        //開始掃描
        mBluetoothAdapter.startLeScan(mLeScanCallback);
        isScanning = true;

        //設置RecyclerView列表
        bluetoothLeAdapter = new BluetoothLeAdapter();
        bleRecycleView.setAdapter(bluetoothLeAdapter);
        bleRecycleView.setHasFixedSize(true);
        bleRecycleView.setLayoutManager(new LinearLayoutManager(this));

        //開始掃描
        mHandler = new Handler();
        if (isScanning){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                isScanning = false;
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }, 10 * 500); //0.5秒
            isScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            findDevice.clear();
            bluetoothLeAdapter.clearDevice();
        }else {
            isScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    /*** 顯示掃描到的物件 ***/
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

    /**取得欲連線之裝置後跳轉頁面*/
    private BluetoothLeAdapter.OnItemClick itemClick = new BluetoothLeAdapter.OnItemClick() {
        @Override
        public void onItemClick(ScannedData selectedDevice) {

            String bleAddress = selectedDevice.getAddress();
            Log.d(TAG, "點擊到的藍牙設備: " + bleAddress);

            /** 觀測者 layout顯示 , 而藍芽layout隱藏 */
            recyclerView.setVisibility(View.VISIBLE);
            temperatureUserAdd.setVisibility(View.VISIBLE);
            bleRecycleView.setVisibility(View.GONE);

            //獲取設備
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(bleAddress);
            //藍芽gett開始連線
            gatt = device.connectGatt(TemperatureActivity.this, false, mGattCallback);
        }
    };

    //gatt callback
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            switch(status){
                case BluetoothGatt.GATT_SUCCESS:
                    Log.w(TAG,"BluetoothGatt.GATT_SUCCESS");
                    break;
                case BluetoothGatt.GATT_FAILURE:
                    Log.w(TAG,"BluetoothGatt.GATT_FAILURE");
                    break;
                case BluetoothGatt.GATT_CONNECTION_CONGESTED:
                    Log.w(TAG,"BluetoothGatt.GATT_CONNECTION_CONGESTED");
                    break;
                case BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION:
                    Log.w(TAG,"BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION");
                    break;
                case BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION:
                    Log.w(TAG,"BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION");
                    break;
                case BluetoothGatt.GATT_INVALID_OFFSET:
                    Log.w(TAG,"BluetoothGatt.GATT_INVALID_OFFSET");
                    break;
                case BluetoothGatt.GATT_READ_NOT_PERMITTED:
                    Log.w(TAG,"BluetoothGatt.GATT_READ_NOT_PERMITTED");
                    break;
                case BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED:
                    Log.w(TAG,"BluetoothGatt.GATT_REQUEST_NOT_SUPPORTED");
                    break;
            }

            if (newState == BluetoothGatt.STATE_CONNECTED){
                Log.d(TAG, "連結成功 : 跑到discoverServices");
                gatt.discoverServices();  //啟動服務
            }else if (newState == BluetoothGatt.STATE_DISCONNECTED){
                Log.d(TAG, "斷開連結並釋放資源");
                gatt.close();
                update(); //20201210 刷新RecyclerView
            }else if (newState == BluetoothGatt.STATE_CONNECTING){
                Log.d(TAG, "正在連結.." );

            }else if (newState == BluetoothGatt.STATE_DISCONNECTING){
                Log.d(TAG, "正在斷開..");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, String.format("onServicesDiscovered:%s,%s", gatt.getDevice().getName(), status));
            if (status == gatt.GATT_SUCCESS) { //發現BLE服務成功
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gattService = gatt.getService(TEMPERATURE_SERVICE_UUID); //獲取64e0001服務
                        if (gattService != null){
                            characteristic = gattService.getCharacteristic(TEMPERATURE_NOTIF_UUID); //獲取64e0003服務通知
                            if (characteristic != null){
                                for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()){
                                    Log.d(TAG, "onServicesDiscovered: descriptor : " + descriptor.getUuid().toString());
                                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);  //啟動notif通知
                                    boolean sucess = gatt.writeDescriptor(descriptor);
                                    Log.d(TAG, "onServicesDiscovered : writeDescriptor = " + sucess);
                                }
                                gatt.setCharacteristicNotification(characteristic, true); //notif listener
                                Log.d(TAG, "onServicesDiscovered的通知啟動成功");
                                //要顯示"已連線"的訊息在RecyclerView's 項目
                                updateStatus(name);
                            }
                        }
                    }
                });
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d(TAG, "onCharacteristicRead: ");
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d(TAG, "onCharacteristicWrite: ");
        }

        /**** 接受到手機端的command後藍芽回覆的資料 ***/
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            if (characteristic.getValue() != null){
                String result = new String(characteristic.getValue());
                String[] str = result.split(","); //以","切割
                String temp = str[2];
                degree = Double.parseDouble(temp)/100;  //25.0
                Log.d(TAG, "onCharacteristicChanged: Characteristic get value : " + degree);  //result : AIDO,0,2500,100
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        update(); //更新  20201216
                    }
                });

            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
        }
    };

    //更新藍芽連線狀態
    private void updateStatus(String name){
        if(this.name != null){
            for (int j = 0; j < memberList.size(); j++){
                if (memberList.get(j).getName().equals(name)){
                    Member user = memberList.get(j);
                    user.setStatus("已連線");
                    memberList.set(j, user);
                    mAdapter.updateItem(user, j);
                }
            }
        }
    }

    //更新收到體溫的訊息給item項目
    private void update(){
        String currentDateTime;
        currentDateTime = sdf.format(new Date());  // 目前時間

        if (name != null){
            for(int i = 0; i < memberList.size(); i++){
                Log.d(TAG, "update: " + name + " time: " + currentDateTime + " degree :" + degree);
                if (memberList.get(i).getName().equals(name)) {
                    Member user = memberList.get(i);
                    user.setDegree(degree, currentDateTime);
                    user.setBattery("80%");
                    memberList.set(i, user);
                    mAdapter.updateItem(user, i);

//                    //如果chart視窗存在就將使用者的資訊傳遞到ChartDialog 20201218
//                    if (chartDialog != null && chartDialog.isShowing())
//                    {
//                        chartDialog.update(user);
//                    }
                }
            }
        }
    }

    //詢問溫度command
    private void sendCommand() {
        if (characteristic != null) { //確保write uuid要有資料才能寫資料
            characteristic = gattService.getCharacteristic(TEMPERATURE_WRITE_DATA); //AIDO寫入uuid : 64e00002
            String request = "AIDO,0"; //詢問溫度command
            byte[] messageBytes = new byte[0];
            try {
                messageBytes = request.getBytes("UTF-8"); //Sting to byte
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Failed to convert message string to byte array");
            }
            characteristic.setValue(messageBytes);    //詢問溫度Command
            boolean success = gatt.writeCharacteristic(characteristic);

            Log.d(TAG, "sendCommand 2: " + success);
        }
    }

    ///////////// 來自RecyclerAdapter的callback ///////////////
    @Override
    public void onBleConnect(Member member) {
        name = member.getName(); //取得使用者名稱

        //init ble 相關設定
        initBle();

        bluetoothLeAdapter.OnItemClick(itemClick);

        /** 觀測者layout隱藏 , 藍芽layout顯示 **/
        recyclerView.setVisibility(View.GONE);
        temperatureUserAdd.setVisibility(View.GONE);
        bleRecycleView.setVisibility(View.VISIBLE);

        supervise.setEnabled(false);  //禁用Button
        remote.setEnabled(false);     //禁用Button
    }

    @Override
    public void onBleChart(Member member) {
        //自定義Dialog
        //ChartDialog chartDialog = new ChartDialog(this, member);
//        chartDialog.setCancelable(false);    //Touch screen disable
        //chartDialog.show();
    }

    @Override
    public void onBleMeasuring(Member member) {
        sendCommand(); //量測開始
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isScanning){
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        gattClose();
    }

    private void gattClose() {
        if (gatt == null)
            return;
        gatt.close();
        gatt = null;
        Toast.makeText(TemperatureActivity.this, "藍芽斷開", Toast.LENGTH_SHORT).show();
    }
}