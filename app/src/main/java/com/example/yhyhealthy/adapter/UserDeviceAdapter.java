package com.example.yhyhealthy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.yhyhealthy.R;
import java.util.ArrayList;
import java.util.List;

/***
 * 裝置管理配適器
 * create 2021/03/29
 * **/

public class UserDeviceAdapter extends RecyclerView.Adapter<UserDeviceAdapter.ViewHolder>{

    private Context context;
    private List<String> dataList = new ArrayList<>();
    private UserDeviceAdapter.UserDeviceListener listener;

    //建構子
    public UserDeviceAdapter(Context context, List<String> dataList, UserDeviceListener listener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_device_item, parent, false);
        return new UserDeviceAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.deviceNo.setText(dataList.get(position));
        holder.deviceDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDelete(dataList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public interface UserDeviceListener{
        void onDelete(String deviceNo);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView deviceNo;
        ImageView deviceDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            deviceNo = itemView.findViewById(R.id.tvDeviceNo);
            deviceDelete = itemView.findViewById(R.id.ivDeleteDevice);
        }
    }
}
