package com.example.yhyhealthy;

import androidx.annotation.NonNull;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.yhyhealthy.fragment.EducationFragment;
import com.example.yhyhealthy.fragment.HistoryFragment;
import com.example.yhyhealthy.fragment.HomeFragment;
import com.example.yhyhealthy.fragment.SettingFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppPage {

    private BottomNavigationView navigationView;

    private HomeFragment homeFragment;
    private HistoryFragment historyFragment;
    private EducationFragment educationFragment;
    private SettingFragment settingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide(); //title隱藏
        setContentView(R.layout.activity_main);

        navigationView = findViewById(R.id.navigation);
        navigationView.inflateMenu(R.menu.navigation_tw);
        navigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);

        //init Fragment
        homeFragment = new HomeFragment();
        historyFragment = new HistoryFragment();
        educationFragment = new EducationFragment();
        settingFragment = new SettingFragment();

        replaceFragment(R.id.container_home, homeFragment);

    }

    @Override
    protected void onResume() {
        super.onResume();

        disableBackButton(); //主功能頁面不需要返回鍵
    }

    private final BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            setTitle(item.getTitle());

            switch (item.getItemId()){
                case R.id.title_home: //首頁
                    replaceFragment(R.id.container_home, homeFragment);
                    return true;
                case R.id.title_edu:   //衛教
                    replaceFragment(R.id.container_home, educationFragment);
                    return true;
                case R.id.title_record: //歷史紀錄
                    replaceFragment(R.id.container_home, historyFragment);
                    return true;
                case R.id.title_setting:  //設定
                    replaceFragment(R.id.container_home, settingFragment);
                    return true;
            }
            return false;
        }
    };
}