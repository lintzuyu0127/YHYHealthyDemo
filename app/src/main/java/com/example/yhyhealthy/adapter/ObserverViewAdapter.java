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
import com.example.yhyhealthy.dataBean.ObserverData;

import java.util.List;

public class ObserverViewAdapter extends RecyclerView.Adapter<ObserverViewAdapter.ViewHolder>{

    private Context context;
    private List<ObserverData> observerDataList;

    public ObserverViewAdapter(Context context, List<ObserverData> observerDataList) {
        this.context = context;
        this.observerDataList = observerDataList;
    }

    public void removeItem(int position){
        observerDataList.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.observer_item, parent, false);
        return new ObserverViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ObserverData observerData = observerDataList.get(position);
        holder.imageView.setImageResource(observerData.getObServerImageView());
        holder.textName.setText(observerData.getObserverName());
        holder.textGender.setText(observerData.getObserverGender());
        holder.textBirthday.setText(observerData.getObserverBirthday());
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeItem(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return observerDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imageView;
        TextView  textName;
        TextView  textGender;
        TextView  textBirthday;
        TextView  edit, delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.ivObserverShot);
            textName = itemView.findViewById(R.id.tvObserverName);
            textGender = itemView.findViewById(R.id.tvObserverGender);
            textBirthday = itemView.findViewById(R.id.tvObserverBirthday);
            edit = itemView.findViewById(R.id.tvObserverEdit);
            delete = itemView.findViewById(R.id.tvObserverDelete);
        }
    }
}
