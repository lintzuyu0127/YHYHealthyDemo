package com.example.yhyhealthy.adapter;

import android.content.Context;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yhyhealthy.R;
import com.example.yhyhealthy.dataBean.Remote;
import com.example.yhyhealthy.dataBean.RemoteAccountApi;

import java.util.List;

public class RemoteAdapter extends RecyclerView.Adapter<RemoteAdapter.ViewHolder>{
    private Context context;
    private List<RemoteAccountApi.SuccessBean> dataList;

    public RemoteAdapter(Context context, List<RemoteAccountApi.SuccessBean> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.remote_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RemoteAccountApi.SuccessBean data = dataList.get(position);
        holder.textName.setText(data.getRemoteName());
        holder.textMeasureTime.setText(data.getMeasuredTime());
        holder.textDegree.setText(String.valueOf(data.getRemoteCelsius())); //攝氏

        Glide.with(context)
                .asBitmap()
                .load(Base64.decode(data.getRemoteHeadShot(), Base64.DEFAULT))
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        TextView  textName, textDegree;
        TextView  textMeasureTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.ivRemoteShot);
            textName = itemView.findViewById(R.id.tvRemoteUserName);
            textDegree = itemView.findViewById(R.id.tvRemoteUserDegree);
            textMeasureTime = itemView.findViewById(R.id.tvMeasuredTime);
        }
    }
}
