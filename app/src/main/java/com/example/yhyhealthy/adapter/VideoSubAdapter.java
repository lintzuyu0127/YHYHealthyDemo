package com.example.yhyhealthy.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.yhyhealthy.R;
import com.example.yhyhealthy.VideoListActivity;
import com.example.yhyhealthy.dataBean.VideoData;
import com.squareup.picasso.Picasso;

import java.util.List;

public class VideoSubAdapter extends RecyclerView.Adapter<VideoSubAdapter.VideoSubViewHolder>{
    private static final String TAG = "VideoSubAdapter";

    private Context context;
    private List<VideoData.ServiceItemListBean.AttrlistBean> list;

    public VideoSubAdapter(Context context, List<VideoData.ServiceItemListBean.AttrlistBean> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public VideoSubViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_sub_item_video, parent, false);
        return new VideoSubViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoSubViewHolder holder, int position) {
        holder.itemName.setText(list.get(position).getAttrName());
        Picasso.get().load(list.get(position).getIconImg()).into(holder.itemIcon);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(context, VideoListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("AttrID", list.get(position).getAttrId());
                bundle.putString("ServiceItemId", list.get(position).getServiceItemId());
                bundle.putString("AttName", list.get(position).getAttrName());
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class VideoSubViewHolder extends RecyclerView.ViewHolder{

        ImageView itemIcon;
        TextView  itemName;

        public VideoSubViewHolder(@NonNull View itemView) {
            super(itemView);

            itemIcon = itemView.findViewById(R.id.videoItemIcon);
            itemName = itemView.findViewById(R.id.videoItemName);
        }
    }
}
