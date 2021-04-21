package com.example.yhyhealthy.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yhyhealthy.R;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment implements View.OnClickListener {

    private View view;

    private Button functionClick;
    private Button dateClick;
    private List<String> fxnList;
    private TextView funResult, dateResult;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) return view;
        view = inflater.inflate(R.layout.fragment_history, container, false);

        functionClick = view.findViewById(R.id.btnSelectFunction);  //功能選擇
        dateClick = view.findViewById(R.id.btnSelectDate);          //日期選擇
        funResult = view.findViewById(R.id.textSelectFunction);     //選擇後的功能顯示
        dateResult = view.findViewById(R.id.textDateRange);         //選擇後的日期顯示

        functionClick.setOnClickListener(this);
        dateClick.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnSelectFunction:
                dialogSelectFunction();     //功能選擇彈跳視窗
                break;
            case R.id.btnSelectDate:        //日期選擇

                break;
        }
    }

    //功能選擇彈跳視窗
    private void dialogSelectFunction() {
        fxnList = new ArrayList<>();
        String arrays[] = getActivity().getResources().getStringArray(R.array.functions);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle("請選擇功能:");
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
        dialogBuilder.setPositiveButton("確定", (dialog, which) ->{
            String data = "";
            data = fxnList.toString().replace("[", "").replace("]", "");
            if (data.equals("")){
                Toast.makeText(getActivity(), "請務必選擇功能!!" , Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getActivity(), "您選擇的功能有:" + data, Toast.LENGTH_SHORT).show();
                funResult.setText(getActivity().getString(R.string.select_function) + " " + data);

            }
        });

        dialogBuilder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());

        AlertDialog alert = dialogBuilder.create();
        alert.setCanceledOnTouchOutside(false); //dismiss the dialog with click on outside of the dialog
        alert.show();
    }
}