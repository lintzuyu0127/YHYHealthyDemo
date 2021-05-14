package com.example.yhyhealthy.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yhyhealthy.LoginActivity;
import com.example.yhyhealthy.R;
import com.example.yhyhealthy.SystemAccountActivity;
import com.example.yhyhealthy.SystemSettingActivity;
import com.example.yhyhealthy.SystemUserActivity;

import es.dmoral.toasty.Toasty;

public class SettingFragment extends Fragment implements View.OnClickListener {

    private View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) return view;
        view = inflater.inflate(R.layout.fragment_setting, container, false);

        ImageView setting = view.findViewById(R.id.imgSystemSetting);
        ImageView UserSetting = view.findViewById(R.id.imgSystemUserSetting);
        ImageView userAccount = view.findViewById(R.id.imgSystemAccount);
        ImageView video = view.findViewById(R.id.imgSystemVideo);
        ImageView provision = view.findViewById(R.id.imgSystemProvision);
        TextView  version = view.findViewById(R.id.tvVersion);
        TextView  logout = view.findViewById(R.id.tvLogout);

        setting.setOnClickListener(this);
        UserSetting.setOnClickListener(this);
        userAccount.setOnClickListener(this);
        video.setOnClickListener(this);
        provision.setOnClickListener(this);
        logout.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.imgSystemSetting:  //系統設定
                startActivity(new Intent(getActivity(), SystemSettingActivity.class));
                break;
            case R.id.imgSystemUserSetting: //個人設定
                startActivity(new Intent(getActivity(), SystemUserActivity.class));
                break;
            case R.id.imgSystemAccount: //帳戶設定
                startActivity(new Intent(getActivity(), SystemAccountActivity.class));
                break;
            case R.id.imgSystemVideo:   //教學影片
                break;
            case R.id.imgSystemProvision:  //使用條款
                break;
            case R.id.tvLogout:       //登出
                startActivity(new Intent(getActivity(), LoginActivity.class));
                getActivity().finish();
                Toasty.success(getActivity(), getString(R.string.logout_success), Toasty.LENGTH_SHORT, true).show();
                break;

        }
    }
}