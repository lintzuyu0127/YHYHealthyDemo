package com.example.yhyhealthy.tools;

import android.content.Context;
import android.graphics.Color;
import com.example.yhyhealthy.dataBean.CycleRecord;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

/***
 * 折線圖管理類
* ***/
public class MPAChartManager {

    private static final String TAG = "MPAChartManager";

    private final Context context;

    private final LineChart lineChart;        //折線圖
    private XAxis xAxis;                //X轴
    private YAxis leftYAxis;            //左侧Y轴
    private YAxis rightYAxis;           //右侧Y轴 自定义XY轴值

    private ArrayList<String> xValue = new ArrayList<>();

    //建構子
    public MPAChartManager(Context context,LineChart lineChart) {
        this.context = context;
        this.lineChart = lineChart;
        leftYAxis = lineChart.getAxisLeft();
        rightYAxis = lineChart.getAxisRight();
        xAxis = lineChart.getXAxis();

        initChart(lineChart);
    }

    /**
     * 初始化图表
     */
    private void initChart(LineChart lineChart) {

        //顯示格線
        lineChart.setDrawGridBackground(true);
        //不顯示邊線
        lineChart.setDrawBorders(false);
        //雙擊不進行縮放
        lineChart.setDoubleTapToZoomEnabled(false);
        //不用描述
        lineChart.getDescription().setEnabled(false);
        //不用圖例
        lineChart.getLegend().setEnabled(false);

        /*** X與Y軸的設置 ***/
        xAxis = lineChart.getXAxis();
        leftYAxis = lineChart.getAxisLeft();
        rightYAxis = lineChart.getAxisRight();

        //設置X軸網格線為虛線
        xAxis.setDrawGridLines(true);
        xAxis.setGridLineWidth(1.5f);
        //xAxis.enableGridDashedLine(10f, 10f, 0f);

        //設置Y軸網格線為虛線
        leftYAxis.setDrawGridLines(true);
        leftYAxis.setGridLineWidth(1.5f);
        //leftYAxis.enableGridDashedLine(10f, 10f, 0f);

        //Y軸右側隱藏
        rightYAxis.setEnabled(false);

        //Y軸右側網格線不顯示
        rightYAxis.setDrawGridLines(false);

        //X軸設置顯示位置在底部
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        //X軸最小間距
        xAxis.setGranularity(1f);

        //繪製X軸線
        xAxis.setDrawAxisLine(false);

        //最小刻度27
        leftYAxis.setAxisMinimum(27f);

        //最大刻度40
        leftYAxis.setAxisMaximum(40f);

        //custom markerView
//        LineChartMarkView mv = new LineChartMarkView(context, xAxis.getValueFormatter());
//        mv.setChartView(lineChart);
//        lineChart.setMarker(mv);
    }

    /**
     * 初始化折線(一條線)
     */
    private void initLineDataSet(LineDataSet lineDataSet, int color, LineDataSet.Mode mode) {
        lineDataSet.setColor(color);
        lineDataSet.setCircleColor(color);
        lineDataSet.setLineWidth(1f);
        lineDataSet.setCircleRadius(3f); //圓的半徑

        //曲線值的圓點是空心
        lineDataSet.setDrawCircles(true);  //不畫圓點
        lineDataSet.setDrawValues(false);  //不顯示數值
        lineDataSet.setDrawCircleHole(true);

        //圓點實心顏色
        lineDataSet.setCircleColorHole(Color.BLACK);
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setMode(mode);
    }

    //顯示折線圖
    public void showLineChart(final List<CycleRecord.SuccessBean> dataList, int color) {

        ArrayList<String> label = new ArrayList<>(); //日期

        List<Entry> entries = new ArrayList<>();    //體溫

        for (int i = 0; i < dataList.size(); i++) {

            CycleRecord.SuccessBean data = dataList.get(i);

            String[] str = data.getTestDate().split("-");
            String thisMonth = str[1];      //Month
            String DayOfMonth = str[2];     //day
            String testDay = thisMonth + "/" + DayOfMonth;  // Month/Day

            if (data.getTemperature() > 0) {
                Entry entry = new Entry(i, (float) data.getTemperature());
                entries.add(entry);             //體溫
            }

            label.add(DayOfMonth);   //Day
            //xValue.add(testDay);     //Month/Day
        }

        xAxis.setValueFormatter(new IndexAxisValueFormatter(label));
        xAxis.setLabelCount(label.size());

        //X軸最多顯示10筆(日期)
        xAxis.setLabelCount(10, true);

        //Y軸的最多顯示7筆(體溫)
        leftYAxis.setLabelCount(7, false);

        LineDataSet lineDataSet = new LineDataSet(entries, "");

        //LINEAR:折線圖
        initLineDataSet(lineDataSet, color, LineDataSet.Mode.LINEAR);

        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
    }

}
