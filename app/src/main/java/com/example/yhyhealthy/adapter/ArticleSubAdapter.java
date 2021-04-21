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
import com.example.yhyhealthy.ArticleListActivity;
import com.example.yhyhealthy.R;
import com.example.yhyhealthy.dataBean.ArticleData;
import com.squareup.picasso.Picasso;
import java.util.List;

public class ArticleSubAdapter extends RecyclerView.Adapter<ArticleSubAdapter.ArticleSubViewHolder>{

    private static final String TAG = "ArticleSubAdapter";

    private Context context;
    private List<ArticleData.ServiceItemListBean.AttrlistBean> list;

    public ArticleSubAdapter(Context context, List<ArticleData.ServiceItemListBean.AttrlistBean> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ArticleSubViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_sub_item_edu, parent, false);
        return new ArticleSubAdapter.ArticleSubViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleSubViewHolder holder, int position) {
        holder.articleName.setText(list.get(position).getAttrName());
        Picasso.get().load(list.get(position).getIconImg()).into(holder.articleIcon);
        holder.articleIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String attrID = list.get(position).getAttrId();
                String ServiceItemId = list.get(position).getServiceItemId();
                String AttrName = list.get(position).getAttrName();
                //將點擊的icon資料傳到另一個頁面
                Intent intent = new Intent(context, ArticleListActivity.class); //文章頁面
                Bundle bundle = new Bundle();
                bundle.putString("AttrID", attrID);
                bundle.putString("ServiceItemId", ServiceItemId);
                bundle.putString("AttName", AttrName);
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class ArticleSubViewHolder extends RecyclerView.ViewHolder{

        TextView articleName;
        ImageView articleIcon;

        public ArticleSubViewHolder(@NonNull View itemView) {
            super(itemView);

            articleName = itemView.findViewById(R.id.tv_item_name);
            articleIcon = itemView.findViewById(R.id.iv_item_icon);

        }
    }
}
