package com.example.yhyhealthy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yhyhealthy.R;
import com.example.yhyhealthy.dataBean.SymptomData;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class SwitchItemAdapter extends RecyclerView.Adapter<SwitchItemAdapter.ViewHolder>{

    private static final String TAG = "SwitchItemAdapter";

    private Context context;
    private List<SymptomData.SwitchItemBean> switchItemBeanList = new ArrayList<>();

    //建構子
    public SwitchItemAdapter(Context context, List<SymptomData.SwitchItemBean> switchItemBeanList) {
        this.context = context;
        this.switchItemBeanList = switchItemBeanList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.symptom_switch_item, parent, false);
        return new SwitchItemAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Dictionary dictionary = getDictionary();

        holder.textName.setText((CharSequence) dictionary.get(switchItemBeanList.get(position).getKey()));

        holder.aSwitch.setText(R.string.no);
        holder.aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    switchItemBeanList.get(position).setValue(true);
                    holder.aSwitch.setText(R.string.yes);
                }else {
                    switchItemBeanList.get(position).setValue(false);
                    holder.aSwitch.setText(R.string.no);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return switchItemBeanList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView textName;
        Switch   aSwitch;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textName = itemView.findViewById(R.id.tvSymptomItem);
            aSwitch = itemView.findViewById(R.id.swSymptom);
        }
    }

    private Dictionary getDictionary() {
        Dictionary dictionary = new Hashtable();
        dictionary.put("headache","頭痛");
        dictionary.put("fatigue","疲勞");
        dictionary.put("soreMusclesJoints","肌肉痠痛");
        dictionary.put("soreThroat","咽喉痛");
        dictionary.put("cough","咳嗽");
        dictionary.put("runnyNose","流鼻涕");
        dictionary.put("diarrhea","腹瀉");
        dictionary.put("chestTightness","胸悶");
        dictionary.put("shortnessBreath","呼吸急促");
        dictionary.put("taste","味覺");
        dictionary.put("vomiting","嘔吐");

        return dictionary;
    }
}
