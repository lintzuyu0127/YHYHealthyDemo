package com.example.yhyhealthy.tools;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.LineChart;

public class DetailLineChart extends LineChart {

    protected Paint mYAxisSafeZonePaint;
    protected Paint mXAxisSafeZonePaint;
    final float INTERVAL = 50f;
//    String[] arrayColors = {"#99EC2436","#99FEBF54","#992FB758","#99277BB7","#99E8E8E8"};
    String[] arrayColors = {"#99E8E8E8", "#99277BB7"};
    public DetailLineChart(Context context) {
        super(context);
    }

    public DetailLineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DetailLineChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();
        mYAxisSafeZonePaint = new Paint();
        mYAxisSafeZonePaint.setStyle(Paint.Style.FILL);

        mXAxisSafeZonePaint = new Paint();
        mXAxisSafeZonePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float[] pts = new float[4];
        pts[1] = 200f;
        float tempPts ;
        for (int i = 0; i < 2; i++) {
            pts[3] = pts[1] - INTERVAL;
            tempPts = pts[3];
            mLeftAxisTransformer.pointValuesToPixel(pts);
            mXAxisSafeZonePaint.setColor(Color.parseColor(arrayColors[i]));
            canvas.drawRect(pts[1], mViewPortHandler.contentTop(), pts[3], mViewPortHandler.contentBottom(), mXAxisSafeZonePaint);
            pts[1] = tempPts;
        }
        super.onDraw(canvas);
    }

    public void setSafeZoneColor(int color) {
        mYAxisSafeZonePaint.setColor(color);
    }
}
