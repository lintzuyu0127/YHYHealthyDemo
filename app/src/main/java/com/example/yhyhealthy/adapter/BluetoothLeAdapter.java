package com.example.yhyhealthy.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.yhyhealthy.R;
import com.example.yhyhealthy.dataBean.ScannedData;
import java.util.ArrayList;
import java.util.List;

public class BluetoothLeAdapter extends RecyclerView.Adapter<BluetoothLeAdapter.ViewHolder>{
    private static final String TAG = "TemperatureAdapter";

    private BluetoothLeAdapter.OnItemClick onItemClick;

    private List<ScannedData> scannedDataList = new ArrayList<>();

    public void OnItemClick(BluetoothLeAdapter.OnItemClick onItemClick){
        this.onItemClick = onItemClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvName.setText(scannedDataList.get(position).getDeviceName());
        holder.tvAddress.setText(scannedDataList.get(position).getAddress());
        holder.tvRssi.setText("rssi " + scannedDataList.get(position).getRssi());
        holder.itemView.setOnClickListener(v -> {
            onItemClick.onItemClick(scannedDataList.get(position));
            Log.d(TAG, "從TempViewAdapter得到使用者點擊的訊息(Address) : " + scannedDataList.get(position));
        });
    }

    @Override
    public int getItemCount() {
        return scannedDataList.size();
    }

    public interface OnItemClick{
        void onItemClick(ScannedData selectedDevice);
    }

    //清除搜尋到的裝置列表
    public void clearDevice(){
        this.scannedDataList.clear();
        notifyDataSetChanged();
    }

    //若有不重複的裝置出現則加入列表中
    public void addDevice(List<ScannedData> scannedDataList){
        this.scannedDataList = scannedDataList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView tvName, tvAddress, tvRssi;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvBleName);
            tvAddress = itemView.findViewById(R.id.tvBleAddress);
            tvRssi = itemView.findViewById(R.id.tvBleRssi);
        }
    }

}
