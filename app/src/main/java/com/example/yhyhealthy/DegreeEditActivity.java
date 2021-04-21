package com.example.yhyhealthy;


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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
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
import static com.example.yhyhealthy.module.ApiProxy.BLE_USER_UPDATE;

/****
 * 編輯觀測者資料(單一個體)
 * 相機功能
 * */

public class DegreeEditActivity extends DeviceBaseActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    private static final String TAG = "DegreeEditActivity";

    private EditText userName , userBirthday;
    private EditText userHeight, userWeight;
    private RadioGroup rdGroup;
    private String Gender = "F";
    private Button btnSave;
    private ImageView takePhoto;
    private ImageView photoShow;        //照片顯示
    private String photoPath = "";      //照片位置全域宣告

    //更新使用者需要此id
    private int targetId = 0;
    private File file;

    //api
    private ApiProxy proxy;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_degree_add);
        setTitle(R.string.title_edit_degree);

        initView();

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null){
            targetId = bundle.getInt("targetId");
            String degreeName = bundle.getString("name");
            String degreeGender = bundle.getString("gender");
            String degreeBirthday = bundle.getString("birthday");
            String degreeWeight = bundle.getString("weight");
            String degreeHeight = bundle.getString("height");
            String degreeHeadShot = bundle.getString("HeadShot");

            initData(degreeName, degreeGender, degreeBirthday, degreeWeight, degreeHeight, degreeHeadShot);
        }
    }

    private void initView(){
        userName = findViewById(R.id.edtInputName);
        userBirthday = findViewById(R.id.edtInputBirthday);
        userHeight = findViewById(R.id.edtInputHeight);
        userWeight= findViewById(R.id.edtInputWeight);
        photoShow = findViewById(R.id.circularImageView);
        btnSave = findViewById(R.id.btnUserAddOK);
        takePhoto = findViewById(R.id.ivTakePhoto);

        rdGroup = findViewById(R.id.rdGroup);
        rdGroup.setOnCheckedChangeListener(this);
        userBirthday.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        takePhoto.setOnClickListener(this);
    }

    private void initData(String name, String gender, String birthday, String weight, String height, String headShot) {
        userName.setText(name);
        userBirthday.setText(birthday);
        userHeight.setText(height);
        userWeight.setText(weight);
        if(gender.equals("F")){
            rdGroup.check(R.id.rdFemale);
        }else {
            rdGroup.check(R.id.rdMale);
        }

        //避免大頭貼沒資料而閃退
        if (headShot != null){
            file = new File(headShot);
            Uri imageUri = Uri.fromFile(file);
            Glide.with(this)
                    .load(imageUri)
                    .signature(new ObjectKey(Long.toString(System.currentTimeMillis())))
                    .into(photoShow);
        }

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
            case R.id.ivTakePhoto: //拍照
                openCamera();
                break;
            case R.id.btnUserAddOK:  //上傳到後台
                updateToApi();
                break;
        }
    }

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
                    userBirthday.setText(mDateTimeFormat(year) + "-" + mDateTimeFormat(monthOfYear + 1) + "-" + mDateTimeFormat(dayOfMonth));
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

    //更新的資料上傳到後台 2021/04/18
    private void updateToApi(){
        proxy = ApiProxy.getInstance(); //實例化

        String base64Str = ImageUtils.imageToBase64(photoPath);   //照片
        String Name = userName.getText().toString().trim();  //名稱
        String Birthday = userBirthday.getText().toString(); //生日
        float Height = Float.parseFloat(userHeight.getText().toString()); //身高
        float Weight = Float.parseFloat(userWeight.getText().toString()); //體重

        JSONObject json = new JSONObject();
        try {
            json.put("targetId", targetId);
            json.put("name", Name);
            json.put("gender", Gender);
            json.put("birthday",Birthday);
            json.put("height", Height);
            json.put("weight", Weight);
            json.put("headShot", base64Str);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        proxy.buildPOST(BLE_USER_UPDATE, json.toString(), updateListener);
    }

    private ApiProxy.OnApiListener updateListener = new ApiProxy.OnApiListener() {
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
                        if(errorCode == 0){
                            Toasty.success(DegreeEditActivity.this, R.string.update_success, Toast.LENGTH_SHORT, true).show();
                            setResult(RESULT_OK);
                            finish();
                        }else {
                            Log.d(TAG, "更新資料失敗回傳碼: " + errorCode);
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

    @Override  //照片回傳
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Activity.DEFAULT_KEYS_DIALER && resultCode == -1){

            new Thread(()->{
                //在BitmapFactory中以檔案URI路徑取得相片檔案，並處理為AtomicReference<Bitmap>，方便後續旋轉圖片
                AtomicReference<Bitmap> getHighImage = new AtomicReference<>(BitmapFactory.decodeFile(photoPath));
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

        }else {
            Toasty.info(this, getString(R.string.camera_not_action), Toast.LENGTH_SHORT, true).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //如果照片還存在本機端,要做刪除
        if(file.exists()) file.delete();
    }
}
