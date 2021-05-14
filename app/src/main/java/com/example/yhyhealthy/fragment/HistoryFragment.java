package com.example.yhyhealthy.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yhyhealthy.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import ru.slybeaver.slycalendarview.SlyCalendarDialog;

/**
 * 歷史紀錄
 * **/

public class HistoryFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "HistoryFragment";

    private View view;

    private Button btnFunction;
    private Button btnDate;
    private List<String> fxnList;
    private TextView textHint;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (view != null) return view;
        view = inflater.inflate(R.layout.fragment_history, container, false);

        btnFunction = view.findViewById(R.id.btnSelectFunction);  //功能選擇
        btnDate = view.findViewById(R.id.btnSelectDate);          //日期選擇
        textHint = view.findViewById(R.id.tvHint);                //提示messages

        btnFunction.setOnClickListener(this);
        btnDate.setOnClickListener(this);

        return view;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onClick(View v) {
        Drawable whiteFunsImage = btnFunction.getContext().getResources().getDrawable(R.drawable.ic_baseline_dashboard_white_24);
        Drawable functionImage = btnFunction.getContext().getResources().getDrawable(R.drawable.ic_baseline_dashboard_24dp);
        Drawable whiteDateImage = btnDate.getContext().getResources().getDrawable(R.drawable.ic_baseline_date_range_white_24);
        Drawable dateImage = btnDate.getContext().getResources().getDrawable(R.drawable.ic_baseline_date_range_24dp);

        switch (v.getId()){
            case R.id.btnSelectFunction:    //功能
                btnFunction.setBackgroundResource(R.drawable.shape_temp_button);
                btnDate.setBackgroundResource(R.drawable.shape_for_temperature);
                btnFunction.setCompoundDrawablesWithIntrinsicBounds(whiteFunsImage,null,null ,null);
                btnDate.setCompoundDrawablesWithIntrinsicBounds(dateImage, null, null, null);
                dialogSelectFunction();
                break;
            case R.id.btnSelectDate:        //日期
                btnFunction.setBackgroundResource(R.drawable.shape_for_temperature);
                btnDate.setBackgroundResource(R.drawable.shape_temp_button);
                btnFunction.setCompoundDrawablesWithIntrinsicBounds(functionImage,null,null ,null);
                btnDate.setCompoundDrawablesWithIntrinsicBounds(whiteDateImage, null,null,null);
                selectDataRange();
                break;
        }
    }

    //日期選擇彈跳視窗
    private void selectDataRange() {
        //第三方庫
        new SlyCalendarDialog()
                .setSingle(false)
                .setCallback(listener)
                .show(getActivity().getSupportFragmentManager(),"TAG");
    }

    SlyCalendarDialog.Callback listener = new SlyCalendarDialog.Callback() {
        @Override
        public void onCancelled() {

        }

        @SuppressLint("StringFormatInvalid")
        @Override
        public void onDataSelected(Calendar firstDate, Calendar secondDate, int hours, int minutes) {
            if (firstDate != null){
                if (secondDate == null){ //單日
                    //firstDate.set(Calendar.HOUR_OF_DAY, hours);

                }else {  //範圍
                    String SelectDate = getString(
                            R.string.slycalendar_period,
                            new SimpleDateFormat(getString(R.string.dateFormat), Locale.getDefault()).format(firstDate.getTime()),
                            new SimpleDateFormat(getString(R.string.timeFormat), Locale.getDefault()).format(secondDate.getTime())
                    );
                    Log.d(TAG, "onDataSelected: " + SelectDate);
                }
            }
            //隱藏提示messages
            textHint.setVisibility(View.INVISIBLE);
        }
    };

    //功能選擇彈跳視窗
    private void dialogSelectFunction() {
        fxnList = new ArrayList<>();
        //data source from String
        String arrays[] = getActivity().getResources().getStringArray(R.array.functions);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle(R.string.please_select_function);
        dialogBuilder.setMultiChoiceItems(R.array.functions, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which, boolean isChecked) {
                if(isChecked){
                    // If the user checked the item, add it to the selected items
                    fxnList.add(arrays[which]);
                }else{
                    // Else, if the item is already in the array, remove it
                    fxnList.remove(arrays[which]);
                }
            }
        });

        // Set the action buttons
        dialogBuilder.setPositiveButton(R.string.sure, (dialog, which) ->{
            String data = "";
            data = fxnList.toString().replace("[", "").replace("]", "");
            if (data.equals("")){
                Toasty.error(getActivity(),getString(R.string.functions_is_not_allow_empty), Toast.LENGTH_SHORT,true).show();
            }else{
                //顯示選擇的功能,要與日期合併去跟後端要資料

            }
        });

        dialogBuilder.setNegativeButton(R.string.dialog_cancel, (dialog, which) -> dialog.dismiss());

        AlertDialog alert = dialogBuilder.create();
        alert.setCanceledOnTouchOutside(false); //dismiss the dialog with click on outside of the dialog
        alert.show();
        //Button內的英文字小寫
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
    }

}