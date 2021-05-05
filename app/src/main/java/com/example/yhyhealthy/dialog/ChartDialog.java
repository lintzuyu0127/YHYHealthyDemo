package com.example.yhyhealthy.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import com.example.yhyhealthy.R;
import com.example.yhyhealthy.dataBean.BleUserData;
import com.example.yhyhealthy.dataBean.Degree;
import com.example.yhyhealthy.dataBean.Member;
import com.example.yhyhealthy.tools.DateUtil;
import com.example.yhyhealthy.tools.TargetZoneLineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/*******
 *  藍芽體溫圖表視窗
 *  使用者,大頭貼,溫度,開始時間,結束時間
 *  chart採用第三方庫MPAndroidChart
 *  最高溫度固定42
 *  最低溫度固定35.5以下
 *  create 2021/04/02
 ******* *****************/

public class ChartDialog extends Dialog {

    private static final String TAG = "ChartDialog";

    //chart畫面用
    private CircleImageView bleUserImage;
    private TextView  bleUserName;
    private TextView  bleUserDegree;
    private TextView  firstDateTime;    //開始時間
    private TextView  lastDateTime;     //結束時間
    private ImageView closeDialog;
    private TextView  nextMeasureTime;

    //private Member member;
    private BleUserData.SuccessBean dataBean; //使用者dateBean
    private ArrayList<Degree> degreeArrayList; //體溫List

    //
    private String correctDate;
    private Double correctDegree;

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
        bleUserImage = findViewById(R.id.imgBleUserShot);
        bleUserName = findViewById(R.id.tvBleUserName);
        bleUserDegree = findViewById(R.id.tvUserDegree);
        firstDateTime = findViewById(R.id.tvStartDate);
        lastDateTime = findViewById(R.id.tvEndDate);
        nextMeasureTime = findViewById(R.id.tvNextMeasureTime);

        closeDialog = findViewById(R.id.imgCloseDialog);
        closeDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //關閉視窗
                dismiss();
            }
        });

        //塞入相對的資料
        bleUserName.setText(dataBean.getBleConnectListUserName());

        //base64解碼大頭貼
        byte[] imageByteArray = Base64.decode(dataBean.getHeadShot(), Base64.DEFAULT);
        Bitmap decodedImage = BitmapFactory.decodeByteArray(imageByteArray,0,imageByteArray.length);
        bleUserImage.setImageBitmap(decodedImage);

        //使用者目前體溫
        bleUserDegree.setText(String.valueOf(dataBean.getDegree()));

        degreeArrayList = new ArrayList<>();

        //避免沒有量測時按下圖表功能造成閃退
        if (dataBean != null && dataBean.getDegreeList().size() > 0){
            //開始時間
            firstDateTime.setText(dataBean.getDegreeList().get(0).getDate());
            //結束時間
            String lastTime = dataBean.getDegreeList().get(dataBean.getDegreeList().size()-1).getDate();
            lastDateTime.setText(lastTime);
            DateTimeFormatter dft = DateTimeFormat.forPattern("MM/dd HH:mm");
            DateTime dateTime = dft.parseDateTime(lastTime).plusMinutes(3);
            nextMeasureTime.setText(getContext().getString(R.string.next_measuring) + dateTime.toString("HH:mm"));

            for (int i = 0; i < dataBean.getDegreeList().size(); i++){
                correctDate = DateUtil.fromDateToTime(dataBean.getDegreeList().get(i).getDate());
                correctDegree = dataBean.getDegreeList().get(i).getDegree();
                setChart();
            }
        }
    }

    //初始化折線圖
    private void setChart() {
        //將從data取得的資料塞到DegreeBean內
        degreeArrayList.add(new Degree(correctDegree,correctDate));

        ArrayList<String> label = new ArrayList<>();  //X軸(時間)
        ArrayList<Entry> entries = new ArrayList<>(); //Y軸(體溫)

        //將資料填入X(日期)與Y軸(日期)
        for(int i=0; i < degreeArrayList.size(); i++){
            String xValues = degreeArrayList.get(i).getDate();
            double yValues = degreeArrayList.get(i).getDegree();
            entries.add(new Entry(i , (float)yValues));
            label.add(xValues);
            Log.d(TAG, "setChart: " + yValues);
        }

        LineDataSet lineDataSet = new LineDataSet(entries,"");
        lineDataSet.setColor(Color.RED);  //軸線顏色
        LineData data = new LineData(lineDataSet);
        bleLineChart.setData(data);

        XAxis xAxis = bleLineChart.getXAxis(); //取得X軸
        xAxis.setValueFormatter(new IndexAxisValueFormatter(label)); //x軸放入自定義的時間
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); //日期顯示在底層
        xAxis.setGranularity(1f);                      //x軸最小間隔
        xAxis.setLabelRotationAngle(-30);              //X軸傾斜30度
        xAxis.setLabelCount(label.size());             //X軸的數量來自資料集
        xAxis.setGridColor(Color.BLACK);               //設置X軸的網格線的顏色
        xAxis.setAxisLineColor(Color.BLACK);
        xAxis.enableGridDashedLine(10f,10f,0f); //X軸格線虛線

        YAxis rightAxis = bleLineChart.getAxisRight();         //獲取右側的Y軸
        rightAxis.setEnabled(false);                           //不顯示右側Y軸
        YAxis leftAxis = bleLineChart.getAxisLeft();           //獲取左側的Y軸線
        leftAxis.setDrawGridLines(false);                      //隱藏Y軸的格線

        leftAxis.setLabelCount(7);    //35-42七組數據
        leftAxis.setAxisMaximum(42);  //最高體溫
        leftAxis.setAxisMinimum(35);  //最低體溫

        //體溫微高
        float rangeHigh = 42f;
        float rangeLow = 37.5f;

        //正常體溫
        float rangeHigh1 = 37.5f;
        float rangeLow1 = 35.5f;

        //低體溫
        float rangeHigh2 = 35.5f;
        float rangeLow2 = 35.0f;

        //體溫微高背景顏色
        bleLineChart.addTargetZone(new TargetZoneLineChart.TargetZone(Color.parseColor("#f5c6cb"),rangeLow,rangeHigh));
        //正常體溫背景顏色
        bleLineChart.addTargetZone(new TargetZoneLineChart.TargetZone(Color.parseColor("#ffffff"),rangeLow1,rangeHigh1));
        //低體溫背景顏色
        bleLineChart.addTargetZone(new TargetZoneLineChart.TargetZone(Color.parseColor("#ffff35"),rangeLow2,rangeHigh2));

        bleLineChart.getLegend().setEnabled(false);            //隱藏圖例
        bleLineChart.getDescription().setEnabled(false);       //隱藏描述
        bleLineChart.invalidate();                             //重新刷圖表
    }

    //當藍芽的體溫值有變化時將會透過此方法將舊有的value更新
    public void update(BleUserData.SuccessBean newMemberBean) {
        //當使用者與藍芽回來的資料是同一個人才進行圖表的更新
        if(dataBean.getBleConnectListUserName().equals(newMemberBean.getBleConnectListUserName())){

            //結束時間 2021/05/04
            String lastTime = newMemberBean.getDegreeList().get(newMemberBean.getDegreeList().size()-1).getDate();
            lastDateTime.setText(lastTime);
            DateTimeFormatter dft = DateTimeFormat.forPattern("MM/dd HH:mm");
            DateTime dateTime = dft.parseDateTime(lastTime).plusMinutes(3);
            nextMeasureTime.setText(getContext().getString(R.string.next_measuring) + dateTime.toString("HH:mm"));

            bleUserDegree.setText(String.valueOf(newMemberBean.getDegree()));
            correctDate = DateUtil.fromDateToTime(newMemberBean.getDegreeList().get(newMemberBean.getDegreeList().size()-1).getDate());
            correctDegree = newMemberBean.getDegree();
            setChart();
        }
    }

}
