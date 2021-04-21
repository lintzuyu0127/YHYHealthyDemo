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
import com.example.yhyhealthy.VideoShowActivity;
import com.example.yhyhealthy.dataBean.VideoListData;
import com.squareup.picasso.Picasso;

import java.util.List;

public class VideoListAdapter extends RecyclerView.Adapter<VideoListAdapter.VideoListViewHolder>{

    private Context context;
    private List<VideoListData.VideoListBean> videoListBeanList;

    public VideoListAdapter(Context context, List<VideoListData.VideoListBean> videoListBeanList) {
        this.context = context;
        this.videoListBeanList = videoListBeanList;
    }

    @NonNull
    @Override
    public VideoListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_list_item, parent, false);
        return new VideoListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoListViewHolder holder, int position) {
        holder.title.setText(videoListBeanList.get(position).getVideo_title());
        Picasso.get().load(videoListBeanList.get(position).getVideo_img()).resize(600,800).onlyScaleDown().into(holder.icon);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(context, VideoShowActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("FILE", videoListBeanList.get(position).getVideo_file());
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return videoListBeanList.size();
    }

    public class VideoListViewHolder extends RecyclerView.ViewHolder{

        ImageView icon;
        TextView  title;

        public VideoListViewHolder(@NonNull View itemView) {
            super(itemView);

            icon = itemView.findViewById(R.id.imageVideoIcon);
            title = itemView.findViewById(R.id.textVideoTitle);
        }
    }
}
