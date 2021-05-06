package com.example.yhyhealthy.dataBean;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.example.yhyhealthy.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class CycleMath {

    private CycleRecord.SuccessBean date;
    private int YEAR = 0;
    private int MONTH = 1;
    private int DAY = 2;
    private int SINGLE = 0;
    private int FILLED = 0;
    private int DOTTED = 1;

    private Context context;

    public CycleMath(Context context, CycleRecord.SuccessBean date) {
        this.context = context;
        this.date = date;
    }

    public CalendarDay getDateData(){
        String dateStr = date.getTestDate();
        List<String> items = Arrays.asList(dateStr.split("-"));
        int year = Integer.parseInt(items.get(YEAR));
        int month = Integer.parseInt(items.get(MONTH));
        int day = Integer.parseInt(items.get(DAY));
        return CalendarDay.from(year, month, day);
    }

    @SuppressLint({"UseCompatLoadingForDrawables", "NewApi"})
    public Drawable getCalenderDrawable(){

        List<Integer> statusCode = date.getCycleStatus();

        if (statusCode.size() == 1){

            if(statusCode.get(SINGLE) == 1) //月經
                return context.getDrawable(R.drawable.ic_baseline_brightness_1_24);
            if(statusCode.get(SINGLE) == 2) //排卵期
                return context.getDrawable(R.drawable.ic_baseline_brightness_2_24);
            if (statusCode.get(SINGLE) == 3) //非排卵期
                return context.getDrawable(R.drawable.ic_baseline_brightness_3_24);
            if (statusCode.get(SINGLE) == 4) //預計經期
                return context.getDrawable(R.mipmap.ic_yhy_4);
            if (statusCode.get(SINGLE) == 5) //預測排卵期
                return context.getDrawable(R.mipmap.ic_yhy_5);
            if (statusCode.get(SINGLE) == 6) //預測排卵日
                return context.getDrawable(R.mipmap.ic_yhy_6);
            if (statusCode.get(SINGLE) == 7) //高黃體濾泡期
                return context.getDrawable(R.drawable.ic_baseline_brightness_8_24);
            if (statusCode.get(SINGLE) == 8) //低黃體濾泡期
                return context.getDrawable(R.drawable.ic_baseline_brightness_9_24);

        }else if (statusCode.size() == 2){
            if (statusCode.get(DOTTED) == 4){
                if (statusCode.get(FILLED) == 0) //[0,4] 預計經期
                    return context.getDrawable(R.drawable.ic_baseline_brightness_1_24);
                if (statusCode.get(FILLED) == 1) //[1,4] 月經&預計經期
                    return context.getDrawable(R.mipmap.ic_yhy_1_4);
                if (statusCode.get(FILLED) == 2) //[2,4] 無此情況
                    return null;
                if (statusCode.get(FILLED) == 3) //[3,4] 排卵期&預計經期
                    return context.getDrawable(R.mipmap.ic_yhy_3_4);
                if (statusCode.get(FILLED) == 7) //[7,4] 高黃體期&預計經期
                    return context.getDrawable(R.mipmap.ic_yhy_7_4);
                if (statusCode.get(FILLED) == 8) //[8,4] 低黃體期&預計經期
                    return context.getDrawable(R.mipmap.ic_yhy_8_4);
            }

            if (statusCode.get(DOTTED) == 5){
                if (statusCode.get(FILLED) == 0) //[0,5] 預計排卵期
                    return context.getDrawable(R.mipmap.ic_yhy_5);
                if (statusCode.get(FILLED) == 1) //[1,5] 月經&預測排卵期
                    return context.getDrawable(R.mipmap.ic_yhy_1_5);
                if (statusCode.get(FILLED) == 2) //[2,5]
                    return context.getDrawable(R.mipmap.ic_yhy_2_5);
                if (statusCode.get(FILLED) == 3) //[3,5]
                    return context.getDrawable(R.mipmap.ic_yhy_3_5);
                if (statusCode.get(FILLED) == 7) //[7,5] 高黃體&預測排卵期
                    return context.getDrawable(R.mipmap.ic_yhy_7_5);
                if (statusCode.get(FILLED) == 8) //[8,5] 低黃體&預測排卵期
                    return context.getDrawable(R.mipmap.ic_yhy_8_5);
            }

            if (statusCode.get(DOTTED) == 6){
                if (statusCode.get(FILLED) == 0) //[0,6] 預計排卵日
                    return context.getDrawable(R.mipmap.ic_yhy_6);
                if (statusCode.get(FILLED) == 1) //[1,6] 經期&預計排卵日
                    return null;
                if (statusCode.get(FILLED) == 2) //[2,6]
                    return context.getDrawable(R.mipmap.ic_yhy_2_6);
                if (statusCode.get(FILLED) == 3)  //[3,6]
                    return context.getDrawable(R.mipmap.ic_yhy_3_6);
                if (statusCode.get(FILLED) == 7)  //[7,6] 高濾泡黃體&預計排卵日
                    return context.getDrawable(R.mipmap.ic_yhy_7_6);
                if (statusCode.get(FILLED) == 8)  //[8,6] 低濾泡黃體&預計排卵日
                    return context.getDrawable(R.mipmap.ic_yhy_8_6);
            }
        }

        //default
        return null;
    }
}
