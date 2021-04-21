package com.example.yhyhealthy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

/***
 * 設定 - 個人設定
 *  基本資料
 *  婚姻狀況
 *  經期設定
 *
 * */

public class SystemUserActivity extends AppPage implements View.OnClickListener {

    ImageView changeBasicInfo, marriageInfo;
    ImageView periodInfo, pregnancyInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //title隱藏
        setContentView(R.layout.activity_system_user);
        setTitle(R.string.system_user_setting);

        initView();
    }

    private void initView() {
        changeBasicInfo = findViewById(R.id.ivBasicInfo);
        periodInfo = findViewById(R.id.ivSettingPeriod);
        pregnancyInfo = findViewById(R.id.ivSettingPreg);
        marriageInfo = findViewById(R.id.ivMarriageInfo);

        changeBasicInfo.setOnClickListener(this);
        periodInfo.setOnClickListener(this);
        pregnancyInfo.setOnClickListener(this);
        marriageInfo.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ivBasicInfo:  // 基本資料
                startActivity(new Intent(this, UserBasicActivity.class));
                break;
            case R.id.ivMarriageInfo:  //婚姻狀況
                startActivity(new Intent(this, UserMarriageActivity.class));
                break;
            case R.id.ivSettingPeriod: //經期設定
                startActivity(new Intent(this, UserPeriodActivity.class));
                break;
//            case R.id.ivSettingPreg:   //懷孕設定
//                startActivity(new Intent(this, UserPregnancyActivity.class));
//                break;
        }
    }
}