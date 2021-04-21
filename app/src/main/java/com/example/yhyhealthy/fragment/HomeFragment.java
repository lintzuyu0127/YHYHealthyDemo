package com.example.yhyhealthy.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.yhyhealthy.DegreeMainActivity;
import com.example.yhyhealthy.OvulationActivity;
import com.example.yhyhealthy.R;
import com.example.yhyhealthy.TemperatureActivity;
import com.example.yhyhealthy.UserBasicActivity;
import com.example.yhyhealthy.UserMarriageActivity;
import com.example.yhyhealthy.UserPeriodActivity;

/***********
 *  首頁 4個功能
 *  目前僅有排卵記錄與藍芽體溫
 * *******/

public class HomeFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "HomeFragment";

    private View view;
    private Button ovulation, temperature;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) return view;
        view = inflater.inflate(R.layout.fragment_home, container, false);

        ovulation = view.findViewById(R.id.btnOvul);
        temperature = view.findViewById(R.id.btnTemp);

        ovulation.setOnClickListener(this);
        temperature.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {

        Class<?> target = null;

        switch (v.getId()){
            case R.id.btnOvul:
                checkBeforeSetting();  //檢查婚姻狀況和經期設定是否資料齊全
                break;
            case R.id.btnTemp:
//                target = TemperatureActivity.class;
                target = DegreeMainActivity.class;
                break;
        }

        if (target != null) startActivity(new Intent(getContext(), target));
    }

    private void checkBeforeSetting(){
        boolean marriageStatus = this.getActivity().getSharedPreferences("yhyHealthy", Context.MODE_PRIVATE).getBoolean("MARRIAGE", false);
        boolean menstrualStatus = this.getActivity().getSharedPreferences("yhyHealthy", Context.MODE_PRIVATE).getBoolean("MENSTRUAL", false);
        boolean userInfoStatus = this.getActivity().getSharedPreferences("yhyHealthy", Context.MODE_PRIVATE).getBoolean("USERSET", false);

        if (!userInfoStatus){
            startActivity(new Intent(getActivity(), UserBasicActivity.class));
        }else if (!menstrualStatus){
            startActivity(new Intent(getActivity(), UserPeriodActivity.class));
        }else if (!marriageStatus){
            startActivity(new Intent(getActivity(), UserMarriageActivity.class));
        }else {
            startActivity(new Intent(getActivity(), OvulationActivity.class));
        }
    }

}