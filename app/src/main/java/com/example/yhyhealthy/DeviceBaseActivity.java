package com.example.yhyhealthy;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION_CODES.M;

public class DeviceBaseActivity extends AppPage {

    private static final String TAG = "DeviceBaseActivity";

    private static final int REQUEST_CODE = 100;
    private String[] neededPermissions = new String[]{CAMERA, WRITE_EXTERNAL_STORAGE, ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION};

    private BleService mBleService;

    @Override
    protected void onStart() {
        super.onStart();

        requestPermission();

    }

    //權限
    protected void requestPermission() {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion >= M){
            ArrayList<String> permissionsNotGranted = new ArrayList<>();
            for(String permission: neededPermissions){
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionsNotGranted.add(permission);
                }
            }
            if(permissionsNotGranted.size() > 0){
                boolean shouldShowAlert = false;
                for (String permission : permissionsNotGranted){
                    shouldShowAlert = ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
                }
                if (shouldShowAlert){
                    showPermissionAlert(permissionsNotGranted.toArray(new String[permissionsNotGranted.size()]));
                }else {
                    requestPermissions(permissionsNotGranted.toArray(new String[permissionsNotGranted.size()]));
                }
            }
        }
    }

    private void requestPermissions(String[] permissions){
        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
    }

    private void showPermissionAlert(final String[] permissions){
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle(R.string.permission_required);
        alertBuilder.setMessage(R.string.permission_message);
        alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                requestPermissions(permissions);
            }
        });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case REQUEST_CODE:
                for (int result:grantResults){
                    if (result == PackageManager.PERMISSION_DENIED){
                        Toast.makeText(DeviceBaseActivity.this,R.string.permission_warning, Toast.LENGTH_SHORT).show();
                        return;
                    }

                }
                //開啟BLE背景服務器
                 //initBle();

                break;
        }
    }

    private void initBle() {
        Log.d(TAG, "initBle: ");

        /**綁定BLE服務**/
        Intent intent = new Intent(this, BleService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        registerReceiver(mBleReceiver, BleService.makeIntentFilter());
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            mBleService = ((BleService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private final BroadcastReceiver mBleReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            switch (action) {

            }

        }
    };
}
