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
import com.example.yhyhealthy.ArticleDetailActivity;
import com.example.yhyhealthy.R;
import com.example.yhyhealthy.dataBean.ArticleListData;
import com.squareup.picasso.Picasso;
import java.util.List;

public class ArticleListAdapter extends RecyclerView.Adapter<ArticleListAdapter.ArticleListViewHolder>{

    private Context context;
    private List<ArticleListData.ArticleListBean> articleList;


    public ArticleListAdapter(Context context, List<ArticleListData.ArticleListBean> articleList) {
        this.context = context;
        this.articleList = articleList;
    }

    @NonNull
    @Override
    public ArticleListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.article_list_item, parent, false);
        return new ArticleListAdapter.ArticleListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleListViewHolder holder, int position) {
        holder.textView.setText(articleList.get(position).getTitle());
        Picasso.get().load(articleList.get(position).getImg()).resize(600,900).onlyScaleDown().into(holder.imageView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(context, ArticleDetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("HTML", articleList.get(position).getHtml());
                intent.putExtras(bundle);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return articleList.size();
    }

    public class ArticleListViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        TextView  textView;

        public ArticleListViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageArtIcon);
            textView = itemView.findViewById(R.id.textArtTitle);
        }
    }
}
