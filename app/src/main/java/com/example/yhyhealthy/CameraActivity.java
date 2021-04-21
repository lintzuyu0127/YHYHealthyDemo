package com.example.yhyhealthy;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowInsetsAnimation;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = CameraActivity.class.getSimpleName();

    Button capture, identify;
    ImageView photoShow;
    File currentImageFile = null;
    String filePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_camera);

        initView();
    }

    private void initView() {
        capture = findViewById(R.id.btnCapture);
        identify = findViewById(R.id.btnIdentify);
        photoShow = findViewById(R.id.ivPhotoShow);

        capture.setOnClickListener(this);
        identify.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnCapture: //拍照

                Uri imageUri = FileCreate(); //創建檔案

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);   //呼叫原生相機
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);            //通知相機新照片儲存的位置
                startActivityForResult(intent, Activity.DEFAULT_KEYS_DIALER);  //將圖片帶回

                break;
//            case R.id.btnIdentify: //辨識
//                //上傳照片到後台去辨識並將結果導回到紀錄頁
//                Intent it = new Intent();
//                it.setClass(CameraActivity.this, RecordActivity.class);
//                it.putExtra("url", currentImageFile);
//                startActivity(it);
//                finish(); //結束
//                break;
        }
    }

    //創建檔案
    private Uri FileCreate() {
        File dir = this.getExternalFilesDir("pictures");
        if(dir.exists()){
            dir.mkdirs();
        }
        currentImageFile = new File(dir,System.currentTimeMillis() + ".jpg");
        if(!currentImageFile.exists()){ //if file does not exist
            try {
                currentImageFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Uri imageUri = null;
        if (Build.VERSION.SDK_INT>=24)
        {
            //取得uri
            imageUri = FileProvider.getUriForFile(this,getPackageName() + ".fileprovider",currentImageFile);
        } else
        {
            imageUri = Uri.fromFile(currentImageFile);
        }
        return imageUri;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Activity.DEFAULT_KEYS_DIALER){

            Uri uri = Uri.fromFile(currentImageFile);
            photoShow.setImageURI(uri);
            Log.d(TAG, "onActivityResult: " + currentImageFile);
//            photoShow.setImageURI(Uri.fromFile(currentImageFile));

        }
    }


}