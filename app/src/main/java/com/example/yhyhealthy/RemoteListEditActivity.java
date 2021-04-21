package com.example.yhyhealthy;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yhyhealthy.adapter.RemoteAdapter;
import com.example.yhyhealthy.adapter.RemoteListAdapter;
import com.example.yhyhealthy.module.ApiProxy;
import com.example.yhyhealthy.tools.SpacesItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;

import static com.example.yhyhealthy.module.ApiProxy.MONITOR_CODE_UPDATE;
import static com.example.yhyhealthy.module.ApiProxy.REMOTE_USER_ADD;
import static com.example.yhyhealthy.module.ApiProxy.REMOTE_USER_DELETE;
import static com.example.yhyhealthy.module.ApiProxy.REMOTE_USER_LIST;

public class RemoteListEditActivity extends AppPage implements RemoteListAdapter.RemoteEditListener {

    private static final String TAG = "RemoteListEditActivity";

    private RecyclerView rvRemoteView;

    private AlertDialog dialog;
    private AlertDialog alertDialog;

    //api
    private ApiProxy proxy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //title隱藏
        setContentView(R.layout.activity_remote_list_edit);
        setTitle(R.string.title_manager_remote_list);
        setActionButton(R.drawable.ic_baseline_edit_24, edit);

        rvRemoteView = findViewById(R.id.rvRemoteEdit);

        initData(); //初始化
    }

    private void initData() {
        proxy = ApiProxy.getInstance();
        proxy.buildPOST(REMOTE_USER_LIST,"",requestListener);
    }

    private ApiProxy.OnApiListener requestListener = new ApiProxy.OnApiListener() {
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
                            parserJson(result);
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

    //解析後台來的資料列表並以RecyclerView顯示
    private void parserJson(JSONObject result) {
        List<String> dataList = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(result.toString());
            JSONArray array = jsonObject.getJSONArray("success");
            for (int i = 0; i < array.length(); i++){
                dataList.add(array.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //將資料傳到adapter
        RemoteListAdapter adapter = new RemoteListAdapter(this, dataList, this);
        rvRemoteView.setAdapter(adapter);
        rvRemoteView.setHasFixedSize(true);
        rvRemoteView.setLayoutManager(new LinearLayoutManager(this));
        rvRemoteView.addItemDecoration(new SpacesItemDecoration(30));
    }

    //新增
    private View.OnClickListener edit = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dialogRemote();
        }
    };

    //遠端帳號新增之彈跳視窗
    private void dialogRemote() {
        alertDialog = new AlertDialog.Builder(this).create();
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

    //傳給後台
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
                            boolean success = object.getBoolean("success");
                            if (success){
                                Toasty.success(RemoteListEditActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT, true).show();
                                initData();//重刷資料
                                alertDialog.dismiss();//關閉視窗
                            }
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

        }
    };

    //更新遠端帳號授權碼fxn
    @Override
    public void onUpdateClick(String accountInfo, int position) {
        dialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_remote, null);
        dialog.setView(view);
        dialog.setCancelable(false);

        TextView title = view.findViewById(R.id.titleRemote);
        title.setText(getString(R.string.update_auth_code));

        EditText accountOther = view.findViewById(R.id.edtOtherAccount);
        accountOther.setFocusable(false);            //不可編輯
        accountOther.setFocusableInTouchMode(false); //不可編輯
        accountOther.setText(accountInfo);

        EditText otherAuthCode = view.findViewById(R.id.edtAuthorization);

        Button btnCancel = view.findViewById(R.id.btnRemoteCancel);
        Button btnSubmit = view.findViewById(R.id.btnRemoteSend);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(otherAuthCode.getText().toString()))
                    return;

                updateNewAuthCodeToApi(otherAuthCode.getText().toString(), accountOther.getText().toString());
            }
        });

        dialog.show();
    }

    //更新授權碼 2021/03/29
    private void updateNewAuthCodeToApi(String code, String account) {
        JSONObject json = new JSONObject();
        try {
            json.put("account", account);
            json.put("monitorCode", code);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        proxy.buildPOST(MONITOR_CODE_UPDATE, json.toString(), codeUpdateListener);
    }

    private ApiProxy.OnApiListener codeUpdateListener = new ApiProxy.OnApiListener() {
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
                            boolean success = object.getBoolean("success");
                            if (success){
                                Toasty.success(RemoteListEditActivity.this, getString(R.string.update_success), Toast.LENGTH_SHORT, true).show();
                                dialog.dismiss(); //關閉視窗
                            }
                        }else {
                            Log.d(TAG, "更新觀測者之授權碼失敗後台回覆碼: " + errorCode);
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

        }
    };

    //刪除遠端帳號
    @Override
    public void onDeleteClick(String accountInfo, int position) {
        JSONObject json = new JSONObject();
        try {
            json.put("account", accountInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        proxy.buildPOST(REMOTE_USER_DELETE, json.toString(), deleteListener);
    }

    private ApiProxy.OnApiListener deleteListener = new ApiProxy.OnApiListener() {
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
                            Toasty.success(RemoteListEditActivity.this, getString(R.string.delete_success), Toast.LENGTH_SHORT, true).show();
                            initData(); //重刷
                        }else {
                            Log.d(TAG, "刪除帳號失敗代碼: " + errorCode);
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
}