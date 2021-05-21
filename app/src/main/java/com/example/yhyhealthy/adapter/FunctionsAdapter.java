package com.example.yhyhealthy.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yhyhealthy.R;

import java.util.List;

/**  ********
 * 歷史紀錄配適器
 * create 2012/05/20
 * ** ****** ****/
public class FunctionsAdapter extends RecyclerView.Adapter<FunctionsAdapter.viewHolder>{

    private Context context;
    private List<String> dataList;
    private String startDay;
    private String endDay;

    private FunctionsAdapter.onRecycleItemClickListener listener;

    public FunctionsAdapter(Context context, List<String> dataList, String startDay, String endDay, onRecycleItemClickListener listener) {
        this.context = context;
        this.dataList = dataList;
        this.startDay = startDay;
        this.endDay = endDay;
        this.listener = listener;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.records_item, parent,false);
        return new FunctionsAdapter.viewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        holder.textDay.setText(startDay + "~" + endDay);
        holder.textFunction.setText(dataList.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(dataList.get(position), startDay, endDay);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public interface onRecycleItemClickListener{
        void onClick(String functionName, String start, String end);
    }

    public class viewHolder extends RecyclerView.ViewHolder{

        TextView textFunction;
        TextView textDay;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            textFunction = itemView.findViewById(R.id.tvFunctionName);
            textDay = itemView.findViewById(R.id.tvDateRange);

        }
    }
}
