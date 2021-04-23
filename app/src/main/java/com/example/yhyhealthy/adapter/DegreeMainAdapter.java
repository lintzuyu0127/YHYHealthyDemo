package com.example.yhyhealthy.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.yhyhealthy.R;
import com.example.yhyhealthy.dataBean.BleUserData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/***  *****************
 * 查詢觀測對象列表-配適器
 * 資料來源 : BleConnectUserData
 * 介面:
 *     onBleConnect:觀測者-藍芽連線
 *     onDelUser : 刪除觀測者
 *     onBleChart : 觀測者藍芽體溫即時圖表
 *     onBleMeasuring:啟動量測
 * create Date : 2021/03/20
 * ************************/

public class DegreeMainAdapter extends RecyclerView.Adapter<DegreeMainAdapter.ViewHolder>{

    private static final String TAG = "DegreeMainAdapter";

    private Context context;

    //data resource
    private List<BleUserData.SuccessBean> dataList;

    //Listener
    private DegreeMainAdapter.DegreeMainAdapterListener listener;

    //建構子
    public DegreeMainAdapter(Context context, List<BleUserData.SuccessBean> dataList, DegreeMainAdapterListener listener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;
    }

    //更新項目
    public void updateItem(BleUserData.SuccessBean data, int pos){
        if (dataList.size() != 0){
            dataList.set(pos, data);
            notifyItemChanged(pos);
        }
    }

    //更新溫度電量項目
    public void updateByMac(double degree, double battery, String mac){
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm");
        String todayWithTime = sdf.format(new Date());
        if (dataList.size() != 0){
            for (int i = 0; i < dataList.size(); i++){
                BleUserData.SuccessBean data = dataList.get(i);
                if (!TextUtils.isEmpty(data.getBleMac())){
                    if (data.getBleMac().equals(mac)){
                        data.setBattery(String.valueOf(battery) + "%");
                        data.setDegree(degree,todayWithTime);  //將得到的體溫跟時間往資料塞,圖表需要用到
                        notifyItemChanged(i); //刷新
                        updateBeforeApi(mac); //上傳後台前的檢查
                    }
                }
            }
        }
    }

    //上傳後台前的檢查
    public void updateBeforeApi(String mac){
        for (int i = 0; i < dataList.size(); i++){
            BleUserData.SuccessBean data = dataList.get(i);
            if (!TextUtils.isEmpty(data.getBleMac())){
                if (data.getBleMac().equals(mac)){
                    int targetId = data.getTargetId();
                    double degree = data.getDegree();
                    listener.passTarget(targetId, degree);
                }
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.bodydegree_item, parent, false);
        return new DegreeMainAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BleUserData.SuccessBean data = dataList.get(position);

        holder.textName.setText(data.getBleConnectListUserName());   //姓名
        holder.textBleStatus.setText(data.getBleConnectStatus());    //連線狀態
        holder.textBleBattery.setText(data.getBattery());            //電量
        holder.textDegree.setText(String.valueOf(data.getDegree())); //體溫

        //以base64解圖
        Glide.with(context)
                .asBitmap()
                .load(Base64.decode(data.getHeadShot(), Base64.DEFAULT))
                .into(holder.imagePhoto);

        //根據藍芽連線狀態變更icon及功能
        if (data.getBleConnectStatus() != null){
            if (data.getBleConnectStatus().contains("連線")){
//                if(data.getBattery() != null){
//                    holder.bleConnect.setImageResource(R.drawable.ic_baseline_close_24);
//                    holder.bleConnect.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            listener.onBleStopConnect(data, position);
//                        }
//                    });
//                }else {
                    holder.bleConnect.setImageResource(R.drawable.ic_baseline_play_circle_outline_24);
                    holder.bleConnect.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listener.onBleMeasuring(data);
                        }
                    });
//                }

            }else if (data.getBleConnectStatus().contains("斷開")){
                holder.bleConnect.setImageResource(R.drawable.ic_baseline_add_24);
                holder.bleConnect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onBleConnect(data, position);
                    }
                });
            }

        }else {
            holder.bleConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onBleConnect(data, position);
                }
            });
        }

        //圖表
        holder.bleChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onBleChart(data, position);
            }
        });

        //症狀
        holder.SymIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onSymRecord(data, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public interface DegreeMainAdapterListener {
        void onBleConnect(BleUserData.SuccessBean data, int position);
        void onBleChart(BleUserData.SuccessBean data, int position);
        void onBleMeasuring(BleUserData.SuccessBean data);
        void onBleStopConnect(BleUserData.SuccessBean data, int position);
        void onSymRecord(BleUserData.SuccessBean data, int position);
        void passTarget(int targetId, double degree);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView  imagePhoto;
        TextView   textName;
        TextView   textDegree;
        TextView   textBleStatus;
        TextView   textBleBattery;
        ImageView  bleChart,bleConnect;
        ImageView  SymIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imagePhoto = itemView.findViewById(R.id.ivUserShot);
            textName = itemView.findViewById(R.id.tvUserName);
            textDegree = itemView.findViewById(R.id.tvUserDegree);
            textBleStatus = itemView.findViewById(R.id.tvBleStatus);
            textBleBattery = itemView.findViewById(R.id.tvBleBattery);

            bleChart = itemView.findViewById(R.id.ivBleChart);
            bleConnect = itemView.findViewById(R.id.ivBleConnect);
            SymIcon = itemView.findViewById(R.id.ivSymIcon);
        }
    }
}
