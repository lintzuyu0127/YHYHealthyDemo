package com.example.yhyhealthy;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.Toast;

import java.util.UUID;

import es.dmoral.toasty.Toasty;

public class BleService extends Service {

    private final String TAG = BleService.class.getSimpleName();

    private BluetoothGatt mBluetoothGatt;
    private BluetoothAdapter mBluetoothAdapter;
    //存放多個藍芽設備
    private ArrayMap<String,BluetoothGatt> gattArrayMap = new ArrayMap<>();

    // 藍芽連接狀態
    private int mConnectionState = 0;
    // 藍牙連接已斷開
    private final int STATE_DISCONNECTED = 0;
    // 藍芽正在連接
    private final int STATE_CONNECTING = 1;
    // 藍牙已連接
    private final int STATE_CONNECTED = 2;

    // 藍芽已連接
    public final static String ACTION_GATT_CONNECTED = "com.example.yhyhealthy.ACTION_GATT_CONNECTED";
    // 藍芽已斷開
    public final static String ACTION_GATT_DISCONNECTED = "com.example.yhyhealthy.ACTION_GATT_DISCONNECTED";
    // 發現GATT服務
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.yhyhealthy.ACTION_GATT_SERVICES_DISCOVERED";
    // 收到藍牙數據
    public final static String ACTION_DATA_AVAILABLE = "com.example.yhyhealthy.ACTION_DATA_AVAILABLE";
    // 連接失敗
    public final static String ACTION_CONNECTING_FAIL = "com.example.yhyhealthy.ACTION_CONNECTING_FAIL";
    // 藍芽數據
    public final static String EXTRA_DATA = "com.example.yhyhealthy.EXTRA_DATA";
    //啟動通知
    public final static String ACTION_NOTIFY_SUCCESS = "com.example.yhyhealthy.ACTION_NOTIFY_SUCCESS";
    // 藍芽位置
    public final static String EXTRA_MAC = "com.example.yhyhealthy.EXTRA_MAC";
    // 藍芽名稱
    public final static String EXTRA_DEVICE_NAME = "com.example.yhyhealthy.EXTRA_DEVICE_NAME";


    // 服務標誌
    private final UUID SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    // 特徵(讀取數據)
    private final UUID CHARACTERISTIC_READ_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");
    // 特徵(發送數據)
    private final UUID CHARACTERISTIC_WRITE_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    // 描述
    private final UUID DESCRIPTOR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    @Override
    public void onCreate() {
        super.onCreate();
        initialize();
    }

    //建立藍芽連線所需的物件
    public void initialize(){
        //取得配適器
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) return;

        if (!mBluetoothAdapter.isEnabled())
            mBluetoothAdapter.enable();
    }

    // 相關服務
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public BleService getService() {
            return BleService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        release();  //釋放藍芽資源
        return super.onUnbind(intent);
    }

    /**
     * 藍牙操作callBack
     * 藍牙有連接狀態才會發生
     * 當連接狀態發生改變時一定會回調這個方法
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {

            //從gatt取得目前連線的mac
            String address = gatt.getDevice().getAddress();

            // 藍牙已連接
            if (newState == BluetoothProfile.STATE_CONNECTED) {

                //此時將資料塞到map去
                gattArrayMap.put(address, gatt);

                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(ACTION_GATT_CONNECTED, gatt);

                // 搜索GATT服務
                mBluetoothGatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) { // 藍牙已斷開連接

                Log.d(TAG, "onConnectionStateChange: 斷開了" );

                mConnectionState = STATE_DISCONNECTED;
                broadcastUpdate(ACTION_GATT_DISCONNECTED, gatt);
            }
        }

        @Override  // 發現GATT服務
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, gatt);
                setBleNotification(); //啟動通知fxn
            }
        }

        @Override  // 收到數據
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicRead: 收到數據");
            broadcastUpdate(ACTION_DATA_AVAILABLE, gatt);
        }

        @Override  //寫資料
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d(TAG, "onCharacteristicWrite: 寫資料");
        }

        @Override  //接受到手機端的command後藍芽回覆的資料
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d(TAG, "onCharacteristicChanged: 收到數據" );

            if (characteristic.getValue() != null){
                broadcastUpdate(ACTION_DATA_AVAILABLE, gatt, characteristic);
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.d(TAG, "onDescriptorRead: ");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.d(TAG, "開啟通知模式成功: ");
        }
    };

    /***
     *   發送通知
     *   @param action 廣播Action
     *   @param gatt   位置
     * */

    public void broadcastUpdate(String action, BluetoothGatt gatt){
        final Intent intent = new Intent(action);
        intent.putExtra(EXTRA_MAC, gatt.getDevice().getAddress());
        intent.putExtra(EXTRA_DEVICE_NAME, gatt.getDevice().getName());
        sendBroadcast(intent);
    }

    /***
     *   發送通知
     *   @param action 廣播Action
     *   @param gatt   位置
     *   @param characteristic 數據
     * */
    public void broadcastUpdate(String action, BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
        final Intent intent = new Intent(action);
        if (CHARACTERISTIC_READ_UUID.equals(characteristic.getUuid())){
            intent.putExtra(EXTRA_DATA, characteristic.getValue());
            intent.putExtra(EXTRA_MAC, gatt.getDevice().getAddress());
            intent.putExtra(EXTRA_DEVICE_NAME, gatt.getDevice().getName());
        }
        sendBroadcast(intent);
    }

    /**
     * 藍芽連接
     *
     * @param address          設備mac位址
     *
     */
    public synchronized void  connect(final String address) {
        BluetoothGatt gatt = gattArrayMap.get(address); //取得map內容物
        if (gatt != null){
            gatt.disconnect();
            gatt.close();
            gattArrayMap.remove(address);
        }

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            return;
        }

        mBluetoothGatt = device.connectGatt(this, true, mGattCallback);

        mConnectionState = STATE_CONNECTING;
    }

    /**
     * 藍芽斷開連接
     */
    public void disconnect() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
        Toasty.info(BleService.this, getString(R.string.ble_not_connect), Toast.LENGTH_SHORT).show();
    }

    /**
     * 釋放相關資源
     */
    public void release() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
        Toasty.info(BleService.this, getString(R.string.ble_release), Toast.LENGTH_SHORT).show();
    }

    /**
     * 設置藍牙設備在數據改變時，通知App
     */

    private void setBleNotification(){
        BluetoothGattService gattService = mBluetoothGatt.getService(SERVICE_UUID);
        if (gattService != null){
            BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(DESCRIPTOR_UUID);
            if(gattCharacteristic != null){
                boolean success = mBluetoothGatt.setCharacteristicNotification(gattCharacteristic,true);
                if(success){
                    for(BluetoothGattDescriptor dp: gattCharacteristic.getDescriptors()){
                        if (dp != null){
                            dp.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            mBluetoothGatt.writeDescriptor(dp);
                            broadcastUpdate(ACTION_NOTIFY_SUCCESS, mBluetoothGatt);
                        }
                    }
                }
            }
        }
    }

    /**
     * 發送數據Fxn
     *
     * @param data    數據
     * @param address 位址 :  判斷設備用
     */

    public synchronized void writeDataToDevice(byte[] data, String address){
        BluetoothGatt mBluetoothGatt = gattArrayMap.get(address); //取出

        if (mBluetoothGatt == null) return;
        BluetoothGattService gattService = mBluetoothGatt.getService(SERVICE_UUID);

        if (gattService == null) return;
        // 獲取藍牙設備的特徵
        BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(CHARACTERISTIC_WRITE_UUID);

        if (gattCharacteristic == null) return;

        //發送訊息
        gattCharacteristic.setValue(data);
        mBluetoothGatt.writeCharacteristic(gattCharacteristic);
    }

    @Override
    public void onDestroy() {
        gattArrayMap.clear();
        super.onDestroy();
    }

    public static IntentFilter makeIntentFilter(){
        //註冊藍芽信息廣播接受器
        final IntentFilter filter = new IntentFilter();
        filter.addAction(BleService.ACTION_GATT_CONNECTED);
        filter.addAction(BleService.ACTION_GATT_DISCONNECTED);
        filter.addAction(BleService.ACTION_GATT_SERVICES_DISCOVERED);
        filter.addAction(BleService.ACTION_DATA_AVAILABLE);
        filter.addAction(BleService.ACTION_CONNECTING_FAIL);
        filter.addAction(BleService.ACTION_NOTIFY_SUCCESS);
        filter.addAction(BleService.EXTRA_DATA);
        filter.addAction(BleService.EXTRA_MAC);
        filter.addAction(BleService.EXTRA_DEVICE_NAME);
        return filter;
    }
}
