package com.example.yhyhealthy.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.yhyhealthy.R;

import java.util.Calendar;

/*****
 *  新增觀測者 Dialog
 *  照片,名稱,性別,生日,身高,體重
* ****/

public class TemperatureDiaolg extends Dialog {

    private Context context;
    private Button save;
    private ImageView cancel;
    private EditText userName , userBirthday;
    private EditText userHeight, userWeight;
    private RadioGroup rdGroup;
    private String Gender = "F";

    /**
     * 自定義 Dialog listener
     * **/
    public interface PriorityListener{
        void setActivity(String name, String gender, String birthday);
    }

    private PriorityListener listener;

    public TemperatureDiaolg (Context context, int theme, PriorityListener listener){
        super(context, theme);
        this.context = context;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_supervise, null);
        setContentView(view);

        //設置dialog大小
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics(); //獲取螢幕的寬跟高

        layoutParams.width = (int) (displayMetrics.widthPixels * 0.8); //寬度設置為螢幕的0.8
        window.setAttributes(layoutParams);

        userName = view.findViewById(R.id.edtInputName);

        rdGroup = view.findViewById(R.id.rdGroup);
        rdGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if(checkedId == R.id.rdMale){
                    Gender = "M";
                }else{
                    Gender = "F";
                }
            }
        });

        userBirthday = view.findViewById(R.id.edtInputBirthday);
        userBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });


        userHeight = findViewById(R.id.edtInputHeight);
        userWeight = findViewById(R.id.edtInputWeight);

        cancel = view.findViewById(R.id.imgCancel);
        save = view.findViewById(R.id.btnUserAddOK);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Name = userName.getText().toString().trim();
                String Birthday = userBirthday.getText().toString();
                String Height = userHeight.getText().toString();
                String Weight = userWeight.getText().toString();

                if(TextUtils.isEmpty(Name)) {
                    Toast.makeText(getContext(), "請填寫名稱", Toast.LENGTH_SHORT).show();
                }else if (TextUtils.isEmpty(Birthday)){
                    Toast.makeText(getContext(), "請填寫出生", Toast.LENGTH_SHORT).show();
                }else if (TextUtils.isEmpty(Height)){
                    Toast.makeText(getContext(), "請填寫身高", Toast.LENGTH_SHORT).show();
                }else if (TextUtils.isEmpty(Weight)){
                    Toast.makeText(getContext(), "請填寫體重", Toast.LENGTH_SHORT).show();
                }else {
                    listener.setActivity(Name, Gender, Birthday);
                    dismiss();
                }
            }
        });
    }

    //日期dialog
    public void showDatePickerDialog(){
        //設定初始日期
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR) - 12;
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        // 跳出日期選擇器
        DatePickerDialog dpd = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                if (year <= mYear) {
                    // 完成選擇，顯示日期
                    userBirthday.setText(mDateTimeFormat(year) + "-" + mDateTimeFormat(monthOfYear + 1) + "-" + mDateTimeFormat(dayOfMonth));
                } else {

                }
            }
        }, mYear, mMonth, mDay);
        dpd.show();
    }

    private String mDateTimeFormat(int value) {
        String RValue = String.valueOf(value);
        if (RValue.length() == 1)
            RValue = "0" + RValue;
        return RValue;
    }
}
