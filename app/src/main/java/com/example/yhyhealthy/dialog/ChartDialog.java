package com.example.yhyhealthy.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import com.example.yhyhealthy.R;
import com.example.yhyhealthy.dataBean.BleUserData;
import com.example.yhyhealthy.dataBean.Degree;
import com.example.yhyhealthy.dataBean.Member;
import com.example.yhyhealthy.tools.TargetZoneLineChart;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/*******
 *  藍芽體溫圖表視窗
 *  使用者,大頭貼,溫度,開始時間,結束時間
 *  chart採用第三方庫MPAndroidChart
 *  最高溫度固定42
 *  最低溫度固定35.5以下
 *  create 2021/04/02
 *******/

public class ChartDialog extends Dialog {

    private static final String TAG = "ChartDialog";

    //chart畫面用
    private CircleImageView bleUserImage;
    private TextView  bleUserName;
    private TextView  bleUserDegree;
    private TextView  firstDateTime;    //開始時間
    private TextView  lastDateTime;     //結束時間
    private ImageView closeDialog;

    //private Member member;
    private BleUserData.SuccessBean dataBean; //使用者dateBean
    private ArrayList<Degree> degreeArrayList; //體溫List

    //圖表自定義
    private TargetZoneLineChart bleLineChart;

    //建構子
    public ChartDialog(@NonNull Context context, BleUserData.SuccessBean dataBean) {
        super(context);
        this.dataBean = dataBean;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_ble_chart);

        //chart init
        bleLineChart = findViewById(R.id.lineChartBle);
    }
}
