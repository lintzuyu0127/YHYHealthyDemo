package com.example.yhyhealthy.dataBean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;

/**BLE掃描到的所有資訊*/

public class ScannedData implements Serializable {
    /**這邊是拿取掃描到的所有資訊*/
    private String deviceName;
    private String rssi;
    private String address;

    public ScannedData(String deviceName, String rssi, String address) {
        this.deviceName = deviceName;
        this.rssi = rssi;
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public String getRssi() {
        return rssi;
    }

    public String getDeviceName() {
        return deviceName;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        ScannedData p = (ScannedData)obj;

        return this.address.equals(p.address);
    }

    @NonNull
    @Override
    public String toString() {
        return this.address;
    }
}
