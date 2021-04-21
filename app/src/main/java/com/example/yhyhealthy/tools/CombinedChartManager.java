package com.example.yhyhealthy.tools;

import android.content.Context;
import android.graphics.Color;

import com.example.yhyhealthy.dataBean.CycleRecord;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

/**  *******  *******
 * 線圖管理類
 * combinedChart : 折線圖 + 長條圖
 * 2021/03/10
 * ** ********   ***/

public class CombinedChartManager {

    private Context context;

    private CombinedChart combinedChart;
    private XAxis xAxis;                //X轴
    private YAxis leftYAxis;            //左侧Y轴
    private YAxis rightYAxis;           //右侧Y轴 自定义XY轴值

    //建構子
    public CombinedChartManager(Context context, CombinedChart combinedChart) {
        this.context = context;
        this.combinedChart = combinedChart;
        leftYAxis = combinedChart.getAxisLeft();
        rightYAxis = combinedChart.getAxisRight();
        xAxis = combinedChart.getXAxis();

        initChart(combinedChart);
    }

    private void initChart(CombinedChart combinedChart) {
        //顯示格線
        combinedChart.setDrawGridBackground(true);
        //不顯示邊線
        combinedChart.setDrawBorders(false);
        //雙擊不進行縮放
        combinedChart.setDoubleTapToZoomEnabled(false);
        //不用描述
        combinedChart.getDescription().setEnabled(false);
        //不用圖例
        combinedChart.getLegend().setEnabled(false);

        //是否繪製X軸網格線
        xAxis.setDrawGridLines(true);
        xAxis.setGridLineWidth(1.5f);

        //是否繪製Y軸網格線
        leftYAxis.setDrawGridLines(true);
        leftYAxis.setGridLineWidth(1.5f);

        //Y軸右側隱藏
        rightYAxis.setEnabled(false);

        //Y軸右側網格線不顯示
        rightYAxis.setDrawGridLines(false);

        //X軸設置顯示位置在底部
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        //最小刻度27
        leftYAxis.setAxisMinimum(25f);

        //最大刻度40
        leftYAxis.setAxisMaximum(40f);

        //X軸最小間距
        xAxis.setGranularity(1f);

        //繪製X軸線
        xAxis.setDrawAxisLine(false);
    }

    public void showCombinedChart(List<CycleRecord.SuccessBean> dataList) {
        CombinedData combinedData = new CombinedData();

        combinedData.setData(getLineDate(dataList));
        combinedData.setData(getBarDate(dataList));

        combinedChart.setData(combinedData);
        combinedChart.invalidate();

        //MarkerView 2021/04/20
        combinedChart.setDrawMarkers(true);
        combinedChart.setMarker(new CustomMarker(context,dataList));
    }

    private BarData getBarDate(List<CycleRecord.SuccessBean> dataList) {
        BarData barData = new BarData();

        List<BarEntry> entries1 = new ArrayList<BarEntry>();
        List<BarEntry> entries2 = new ArrayList<BarEntry>();

        for(int i = 0; i < dataList.size(); i++){
            CycleRecord.SuccessBean data = dataList.get(i);
            if (data.getCycleStatus().contains(4)){
                data.setTemperature(40);
                entries1.add(new BarEntry(i, (float) data.getTemperature()));
            }else if (data.getCycleStatus().contains(6)){
                data.setTemperature(40);
                entries2.add(new BarEntry(i, (float) data.getTemperature()));
            }
        }

        BarDataSet barDataSet = new BarDataSet(entries1, "經期"); // add entries to dataset
        barDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);

        BarDataSet barDataSet2 = new BarDataSet(entries2, "排卵期"); // add entries to dataset
        barDataSet2.setAxisDependency(YAxis.AxisDependency.LEFT);

        //设置数据显示颜色：柱子颜色
        barDataSet.setColor(Color.rgb(225,63,174));
        barDataSet2.setColor(Color.rgb(225,186,63));

        //不顯示數據點數值
        barDataSet.setDrawValues(false);
        barDataSet2.setDrawValues(false);

        barData.addDataSet(barDataSet);
        barData.addDataSet(barDataSet2);

        barData.setBarWidth(1.2f);

        return barData;
    }

    private LineData getLineDate(List<CycleRecord.SuccessBean> dataList) {
        LineData lineData = new LineData();

        ArrayList<String> label = new ArrayList<>();

        ArrayList<Entry> entries = new ArrayList<>();

        for (int i = 0; i < dataList.size(); i++){
            double degree = dataList.get(i).getTemperature();
            String[] str = dataList.get(i).getTestDate().split("-");
            String testDay = str[2];

            //if (degree > 0){ //會閃退
                entries.add(new Entry(i, (float) degree));
            //}


            label.add(testDay);
        }

        xAxis.setValueFormatter(new IndexAxisValueFormatter(label));
        xAxis.setLabelCount(label.size());
        xAxis.setLabelCount(10,true);

        LineDataSet set = new LineDataSet(entries, "");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(Color.BLACK);
        set.setLineWidth(0.9f);
        set.setCircleColor(Color.BLUE);
        set.setCircleRadius(2f);
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawValues(false); //是否顯示圓點的數值
        lineData.addDataSet(set);

        return lineData;
    }

}
