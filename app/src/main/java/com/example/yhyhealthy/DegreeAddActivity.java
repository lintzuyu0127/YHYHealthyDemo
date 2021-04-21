package com.example.yhyhealthy;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.tools.ImageUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import es.dmoral.toasty.Toasty;

import static android.Manifest.permission.CAMERA;
import static com.example.yhyhealthy.module.ApiProxy.BLE_USER_ADD;

/****
 * 新增觀測者資料(單一個體)
 * 相機功能
 * */
public class DegreeAddActivity extends DeviceBaseActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    private static final String TAG = "DegreeAddActivity";

    private EditText userNameInput , userBirthdayInput;
    private EditText userHeightInput, userWeightInput;
    private RadioGroup rdGroup;
    private String Gender = "F";
    private Button btnSave;
    private ImageView takePhoto;
    private ImageView photoShow;        //照片顯示
    private String photoPath = "";      //照片位置全域宣告

    //api
    private ApiProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //title隱藏
        setContentView(R.layout.activity_degree_add);
        setTitle(R.string.title_add_ble_user);

        initView();
    }

    private void initView() {
        userNameInput = findViewById(R.id.edtInputName);
        userBirthdayInput = findViewById(R.id.edtInputBirthday);
        userHeightInput = findViewById(R.id.edtInputHeight);
        userWeightInput = findViewById(R.id.edtInputWeight);
        photoShow = findViewById(R.id.circularImageView);
        btnSave = findViewById(R.id.btnUserAddOK);
        takePhoto = findViewById(R.id.ivTakePhoto);

        rdGroup = findViewById(R.id.rdGroup);
        rdGroup.setOnCheckedChangeListener(this);
        userBirthdayInput.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        takePhoto.setOnClickListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if(checkedId == R.id.rdMale){
            Gender = "M";
        }else{
            Gender = "F";
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.edtInputBirthday: //日期選擇
                showDatePickerDialog();
                break;
            case R.id.btnUserAddOK:  //上傳到後台
                checkBeforeUpdate();
                break;
            case R.id.ivTakePhoto: //拍照
                if(ActivityCompat.checkSelfPermission(this, CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    openCamera(); //開啟相機
                }else { //如果沒有權限就不能用相機
                    requestPermission(); //要求權限
                }
                break;
        }
    }

    //檢查資料是否齊全
    private void checkBeforeUpdate() {

        if(TextUtils.isEmpty(userNameInput.getText().toString().trim())) {
            Toasty.error(this, getString(R.string.please_input_name), Toast.LENGTH_SHORT,true).show();
        }else if (TextUtils.isEmpty(userBirthdayInput.getText().toString())){
            Toasty.error(this, getString(R.string.please_input_birthday), Toast.LENGTH_SHORT,true).show();
        }else if (TextUtils.isEmpty(userHeightInput.getText().toString())){
            Toasty.error(this, getString(R.string.please_input_height), Toast.LENGTH_SHORT,true).show();
        }else if (TextUtils.isEmpty(userWeightInput.getText().toString())){
            Toasty.error(this, getString(R.string.please_input_weight), Toast.LENGTH_SHORT,true).show();
        }else {
            updateToApi(); //傳給後端
        }
    }

    //執行後台存檔
    private void updateToApi() {
        proxy = ApiProxy.getInstance();

        String base64Str = ImageUtils.imageToBase64(photoPath);   //照片
        String Name = userNameInput.getText().toString().trim();  //名稱
        String Birthday = userBirthdayInput.getText().toString(); //生日
        String Height = userHeightInput.getText().toString();     //身高
        String Weight = userWeightInput.getText().toString();     //體重

        JSONObject json = new JSONObject();
        try {
            json.put("name", Name);
            json.put("gender", Gender);
            json.put("birthday", Birthday);
            json.put("height", Height);
            json.put("weight", Weight);
            json.put("headShot", base64Str);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //新增使用者上傳到後台
        proxy.buildPOST(BLE_USER_ADD, json.toString(), addUserListener);
    }

    private ApiProxy.OnApiListener addUserListener = new ApiProxy.OnApiListener() {
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
                            Toasty.success(DegreeAddActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT,true).show();
                            Intent resultIntent = new Intent();
                            setResult(RESULT_OK, resultIntent);
                            finish(); //回到上一頁
                        }else {
                            Toasty.error(DegreeAddActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT,true).show();
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

    //日期選擇
    private void showDatePickerDialog() {
        //設定初始日期
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR) - 12;
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        // 跳出日期選擇器
        DatePickerDialog dpd = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                if (year <= mYear) {
                    // 完成選擇，顯示日期
                    userBirthdayInput.setText(mDateTimeFormat(year) + "-" + mDateTimeFormat(monthOfYear + 1) + "-" + mDateTimeFormat(dayOfMonth));
                }
            }
        }, mYear, mMonth, mDay);
        dpd.show();
    }

    private String mDateTimeFormat(int value) {
        String RValue = String.valueOf(value);
        if (RValue.length() == 1)
            RValue = "0" + RValue;
        return RValue;
    }

    //開啟相機
    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); //原生相機
        File imageFile = getImageFile();   //取得相片位置
        if (imageFile == null) return;
        //取得相片檔案在本機的位置
        Uri imageUri = FileProvider.getUriForFile(this,"com.example.yhyhealthy.fileprovider", imageFile);
        //通知相機將照片存放位置
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        //將照片載回來
        startActivityForResult(intent, Activity.DEFAULT_KEYS_DIALER);
    }

    //取得相片位置
    private File getImageFile() {
        String time = new SimpleDateFormat("yyMMdd").format(new Date());
        String fileName = time + "_";
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        try {
            File imageFile = File.createTempFile(fileName, ".jpg", dir);
            photoPath = imageFile.getAbsolutePath(); //照片檔案位置
            return imageFile;
        } catch (IOException e) {
            return null;
        }
    }

    @Override  //照片回傳
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Activity.DEFAULT_KEYS_DIALER && resultCode == -1) {
            new Thread(() -> {
                //在BitmapFactory中以檔案URI路徑取得相片檔案，並處理為AtomicReference<Bitmap>，方便後續旋轉圖片
                AtomicReference<Bitmap> getHighImage = new AtomicReference<>(BitmapFactory.decodeFile(photoPath));
                Matrix matrix = new Matrix();
                matrix.setRotate(90f);//轉90度
                getHighImage.set(Bitmap.createBitmap(getHighImage.get()
                        , 0, 0
                        , getHighImage.get().getWidth()
                        , getHighImage.get().getHeight()
                        , matrix, true));
                runOnUiThread(() -> {
                    //以Glide設置圖片(因為旋轉圖片屬於耗時處理，故會LAG一下，且必須使用Thread執行緒)
                    Glide.with(this)
                            .load(getHighImage.get())
                            .centerCrop()
                            .into(photoShow);
                });
            }).start();

        } else {
            Toasty.info(this, getString(R.string.camera_not_action), Toast.LENGTH_SHORT, true).show();
        }

    }
}