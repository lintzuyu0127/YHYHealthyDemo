package com.example.yhyhealthy.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yhyhealthy.DegreeEditActivity;
import com.example.yhyhealthy.R;
import com.example.yhyhealthy.dataBean.BleUserData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class DegreeListEditAdapter extends RecyclerView.Adapter<DegreeListEditAdapter.ViewHolder>{

    private Context context;

    //data source
    private List<BleUserData.SuccessBean> dataList;

    //interface
    private DegreeListEditAdapter.DegreeListEditListener listEditListener;

    public DegreeListEditAdapter(Context context, List<BleUserData.SuccessBean> dataList, DegreeListEditListener listEditListener) {
        this.context = context;
        this.dataList = dataList;
        this.listEditListener = listEditListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.degree_edit_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BleUserData.SuccessBean data = dataList.get(position);

        holder.name.setText(data.getBleConnectListUserName());

        //使用第三方庫解碼大頭貼會自動將照片轉正
        Glide.with(context)
                .asBitmap()
                .load(Base64.decode(data.getHeadShot(), Base64.DEFAULT))
                .into(holder.editPhoto);

        if (data.getGender().equals("F")){
            holder.gender.setText(R.string.female);
        }else {
            holder.gender.setText(R.string.male);
        }

        holder.birthday.setText(data.getBirthday());

        //刪除
        holder.remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listEditListener.onRemoveClick(data, position);
            }
        });

        //編輯  2021/04/18
        holder.revise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listEditListener.onEditClick(data);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    //刪除&編輯interface
    public interface DegreeListEditListener{
        void onEditClick(BleUserData.SuccessBean data);
        void onRemoveClick(BleUserData.SuccessBean data, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView editPhoto;
        private TextView name, gender, birthday;
        private TextView revise, remove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            editPhoto = itemView.findViewById(R.id.ivEditPhoto);
            name = itemView.findViewById(R.id.tvEditName);
            gender = itemView.findViewById(R.id.tvEditGender);
            birthday = itemView.findViewById(R.id.tvEditBirthday);
            revise = itemView.findViewById(R.id.tvEditRevise);   //編輯
            remove = itemView.findViewById(R.id.tvEditRemove);   //移除
        }
    }
}
