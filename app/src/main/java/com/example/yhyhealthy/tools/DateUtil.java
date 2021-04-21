package com.example.yhyhealthy.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateUtil {

    //日期格式 11/23 轉成 23
    public static String formatDateToDD(String str) {
        SimpleDateFormat sf1 = new SimpleDateFormat("MM/dd");
        SimpleDateFormat sf2 = new SimpleDateFormat("dd");
        String formatStr = "";
        try {
            formatStr = sf2.format(sf1.parse(str));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formatStr;
    }

    //日期格式 2019/11/23 轉成 2019-11-23
    public static String formatDateToYMD(String str) {
        SimpleDateFormat sf1 = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat sf2 = new SimpleDateFormat("yyyy-MM-dd");
        String formatStr = "";
        try {
            formatStr = sf2.format(sf1.parse(str));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formatStr;
    }
}
