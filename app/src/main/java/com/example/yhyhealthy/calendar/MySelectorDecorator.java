package com.example.yhyhealthy.calendar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;

import com.example.yhyhealthy.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

public class MySelectorDecorator implements DayViewDecorator {

    private Drawable drawable;

    @SuppressLint({"NewApi", "UseCompatLoadingForDrawables"})
    public MySelectorDecorator(Activity context) {
        drawable = context.getDrawable(R.drawable.my_selector);
    }

    public MySelectorDecorator() {

    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return true;
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setSelectionDrawable(drawable);
    }
}
