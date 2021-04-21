package com.example.yhyhealthy.adapter;

import android.content.Context;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yhyhealthy.R;
import com.example.yhyhealthy.dataBean.Member;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{
    private static final String TAG = "RecyclerViewAdapter";

    private Context context;
    private List<Member> memberList;  //來自使用者DataBean

    private RecyclerViewAdapter.RecyclerViewListener listener;    //監聽

    public RecyclerViewAdapter(Context context, List<Member> memberList, RecyclerViewAdapter.RecyclerViewListener listener) {
        this.context = context;
        this.memberList = memberList;
        this.listener = listener;
    }

    public void ClearInfo(){
        this.memberList.clear();
        notifyDataSetChanged();
    }

    //更新參數 : 會員 , 位置
    public void updateItem(Member member, int pos){
     if (memberList.size() != 0 ){
         memberList.set(pos, member);
         notifyItemChanged(pos);
     }
    }

    //移除項目
    public void removeItem(int position){
        memberList.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.bodydegree_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Member member = memberList.get(position);
        holder.imagePhoto.setImageResource(member.getImage());
        holder.textName.setText(member.getName());
        holder.textDegree.setText(String.valueOf(member.getDegree()));
        holder.textBleStatus.setText(member.getStatus());

        holder.bleConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ble connect");
                listener.onBleConnect(member);
            }
        });

        holder.bleChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: open Chart");
                listener.onBleChart(member);
            }
        });

        holder.bleUserRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeItem(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imagePhoto;
        TextView  textName;
        TextView  textDegree;
        TextView  textBleStatus;
        TextView  textBleBattery;
        ImageView bleChart,bleConnect, bleUserRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imagePhoto = itemView.findViewById(R.id.ivUserShot);
            textName = itemView.findViewById(R.id.tvUserName);
            textDegree = itemView.findViewById(R.id.tvUserDegree);
            textBleStatus = itemView.findViewById(R.id.tvBleStatus);
            textBleBattery = itemView.findViewById(R.id.tvBleBattery);

            bleChart = itemView.findViewById(R.id.ivBleChart);
            bleConnect = itemView.findViewById(R.id.ivBleConnect);
            bleUserRemove = itemView.findViewById(R.id.ivBleDelete);

        }
    }

    public interface RecyclerViewListener {
        void onBleConnect(Member member);
        void onBleChart(Member member);
        void onBleMeasuring(Member member);
    }
}
