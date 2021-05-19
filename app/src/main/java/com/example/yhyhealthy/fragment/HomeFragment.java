package com.example.yhyhealthy.fragment;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.example.yhyhealthy.DegreeMainActivity;
import com.example.yhyhealthy.OvulationActivity;
import com.example.yhyhealthy.PregnancyActivity;
import com.example.yhyhealthy.R;
import com.example.yhyhealthy.ShoppingActivity;
import com.example.yhyhealthy.TeachVideoActivity;
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

    private View         view;
    private ImageView    ovulation, temperature, pregnancy, monitor;
    private ImageView    shopping,guid;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (view != null) return view;
            view = inflater.inflate(R.layout.fragment_home, container, false);

        ovulation = view.findViewById(R.id.ivOvulation);
        temperature = view.findViewById(R.id.ivTemperature);
        pregnancy = view.findViewById(R.id.ivPregnancy);
        monitor = view.findViewById(R.id.ivBreath);

        shopping = view.findViewById(R.id.ivStore);
        guid = view.findViewById(R.id.ivGuid);

        ovulation.setOnClickListener(this);
        temperature.setOnClickListener(this);
        pregnancy.setOnClickListener(this);
        shopping.setOnClickListener(this);
        guid.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {

        Class<?> target = null;

        switch (v.getId()){
            case R.id.ivOvulation:
                checkBeforeSetting();  //檢查婚姻狀況和經期設定是否資料齊全
                break;
            case R.id.ivTemperature:
//                target = TemperatureActivity.class;
                target = DegreeMainActivity.class;
                break;
            case R.id.ivPregnancy:
                target = PregnancyActivity.class;
                break;
            case R.id.ivBreath:
                break;
            case R.id.ivStore:
                target = ShoppingActivity.class;
                break;
            case R.id.ivGuid:
                target = TeachVideoActivity.class;
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