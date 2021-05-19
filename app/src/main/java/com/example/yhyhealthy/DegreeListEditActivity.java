
package com.example.yhyhealthy;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yhyhealthy.adapter.DegreeListEditAdapter;
import com.example.yhyhealthy.dataBean.BleUserData;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.tools.ImageUtils;
import com.example.yhyhealthy.tools.SpacesItemDecoration;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthy.module.ApiProxy.BLE_USER_DELETE;
import static com.example.yhyhealthy.module.ApiProxy.BLE_USER_LIST;

/****  ********  *** ****
 *  藍芽體溫列表編輯頁面
 *  資料來源:BleConnectUserData.SuccessBean
 *  配適器:DegreeListEditAdapter
 *  介面 :
 *     onEditClick
 *     onRemoveClick
 *  create : 2021/03/25
 * * ********** ***   *****/

public class DegreeListEditActivity extends AppPage implements DegreeListEditAdapter.DegreeListEditListener {

    private static final String TAG = "DegreeListEditActivity";

    private RecyclerView rvDegreeView;
    private DegreeListEditAdapter adapter;
    private List<BleUserData.SuccessBean> dataList;
    private int position;

    //本機照片儲存位置全域
    private File tmpPhoto;

    //
    private static final int EDIT_CODE = 1;

    //api
    private ApiProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //title隱藏
        setContentView(R.layout.activity_degree_list_edit);
        setTitle(R.string.title_manager_ble_list);

        rvDegreeView = findViewById(R.id.rvDegreeList);

        initData();
    }

    private void initData() {
        proxy = ApiProxy.getInstance();
        proxy.buildPOST(BLE_USER_LIST, "", userListListener);
    }

    private ApiProxy.OnApiListener userListListener = new ApiProxy.OnApiListener() {
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
                        }else if (errorCode == 23){ //token失效
                            Toasty.error(DegreeListEditActivity.this, getString(R.string.idle_too_long), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(DegreeListEditActivity.this, LoginActivity.class));
                            finish();
                        }else {
                            Toasty.error(DegreeListEditActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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

    //解析後台來的觀測者List資料
    private void parserJson(JSONObject result) {
        BleUserData data = BleUserData.newInstance(result.toString());
        dataList = data.getSuccess();

        //將資料配置到adapter並顯示出來
        adapter = new DegreeListEditAdapter(this, dataList, this);
        rvDegreeView.setAdapter(adapter);
        rvDegreeView.setLayoutManager(new LinearLayoutManager(this));
        rvDegreeView.setHasFixedSize(true);
        rvDegreeView.addItemDecoration(new SpacesItemDecoration(10));
    }

    //將圖檔存到本機內
    public void saveBitmap(Bitmap bitmap) {

            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            tmpPhoto = new File(directory, "takePicture" + ".jpg");

            FileOutputStream fOut;
            try {
                fOut = new FileOutputStream(tmpPhoto);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.flush();
                fOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    @Override  //編輯使用者
    public void onEditClick(BleUserData.SuccessBean data) {

        //大頭貼的資料轉成bitmap
        Bitmap bitmap = ImageUtils.base64ToBitmap(data.getHeadShot());

        //將大頭貼bitmap存到本地端手機+避免沒資料而閃退 因為後台沒給url所以暫用將照片存到本地端
        if(bitmap != null)
            saveBitmap(bitmap);

        //將必要的資訊傳到編輯頁面
        Intent intent = new Intent();
        intent.setClass(this, DegreeEditActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt("targetId", data.getTargetId());
        bundle.putString("name", data.getBleConnectListUserName());
        bundle.putString("gender", data.getGender());
        bundle.putString("birthday", data.getBirthday());
        bundle.putString("weight", String.valueOf(data.getWeight()));
        bundle.putString("height", String.valueOf(data.getBleConnectListUserHeight()));
        bundle.putString("HeadShot", tmpPhoto.toString());
        intent.putExtras(bundle);
        startActivityForResult(intent, EDIT_CODE);
    }

    @Override //移除使用者
    public void onRemoveClick(BleUserData.SuccessBean data, int pos) {
        position = pos; //取得使用者在RecyclerView item位置

        JSONObject json = new JSONObject();
        try {
            json.put("targetId", data.getTargetId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        proxy.buildPOST(BLE_USER_DELETE, json.toString(), deleteListener);
    }

    private ApiProxy.OnApiListener deleteListener = new ApiProxy.OnApiListener() {
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
                            Toasty.success(DegreeListEditActivity.this, getString(R.string.delete_success), Toast.LENGTH_SHORT, true).show();
                            dataList.remove(position);
                            adapter.notifyItemRemoved(position);
                        }else if (errorCode == 23){ //token失效
                            Toasty.error(DegreeListEditActivity.this, getString(R.string.idle_too_long), Toast.LENGTH_SHORT, true).show();
                            startActivity(new Intent(DegreeListEditActivity.this, LoginActivity.class));
                            finish();
                        }else {
                            Toasty.error(DegreeListEditActivity.this, getString(R.string.json_error_code) + errorCode, Toast.LENGTH_SHORT, true).show();
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

    @Override  //2021/04/19
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_CODE && resultCode == -1){
            //更新成功後需要重新再跟後台要資料並刷新RecyclerView的內容
            initData();
        }
    }
}