package com.example.yhyhealthy.dataBean;

import android.text.TextUtils;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

/**  *
 *  藍芽體溫使用者列表DataBean
 *  來自api + 本地
 *  避免混淆,使用SerializedName變更SuccessBean's name
 *  create 2021/03/23
 * ***/

public class BleUserData {

    private int errorCode;
    private List<BleUserData.SuccessBean> success;

    public int getErrorCode() {
        return errorCode;
    }

    public List<SuccessBean> getSuccess() {
        return success;
    }

    //後台Api
    public static class SuccessBean {
        private int targetId;

        @SerializedName("name")
        private String bleConnectListUserName;

        private String gender;
        private String birthday;

        @SerializedName("height")
        private double bleConnectListUserHeight;

        private double weight;
        private String headShot;

        public int getTargetId() {
            return targetId;
        }

        public void setTargetId(int targetId) {
            this.targetId = targetId;
        }


        public String getBleConnectListUserName() {
            return bleConnectListUserName;
        }

        public void setBleConnectListUserName(String bleConnectListUserName) {
            this.bleConnectListUserName = bleConnectListUserName;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getBirthday() {
            return birthday;
        }

        public void setBirthday(String birthday) {
            this.birthday = birthday;
        }

        public double getBleConnectListUserHeight() {
            return bleConnectListUserHeight;
        }

        public void setBleConnectListUserHeight(double bleConnectListUserHeight) {
            this.bleConnectListUserHeight = bleConnectListUserHeight;
        }

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }

        public String getHeadShot() {
            return headShot;
        }

        public void setHeadShot(String headShot) {
            this.headShot = headShot;
        }

        //本地
        private double degree;            //ble體溫
        private String battery;           //ble電量
        private String BleConnectStatus;  //ble狀態
        private String bleMac;            //ble's mac
        private String bleDeviceName;    //ble's name

        private List<Degree> degreeList = new ArrayList<>();

        //將取得的體溫跟日期填入Degree's dataBean
        public void setDegree(Double degree, String date) {
            this.degree = degree;
            Degree degree1 = new Degree(degree, date);
            degreeList.add(degree1);
        }

        public Double getDegree() {
            return degree;
        }

        public String getBattery() {
            return battery;
        }

        public void setBattery(String battery) {
            this.battery = battery;
        }

        public String getBleConnectStatus() {
            return BleConnectStatus;
        }

        public void setBleConnectStatus(String bleConnectStatus) {
            BleConnectStatus = bleConnectStatus;
        }

        public String getBleMac() {
            return bleMac;
        }

        public void setBleMac(String bleMac) {
            this.bleMac = bleMac;
        }

        public String getBleDeviceName() {
            return bleDeviceName;
        }

        public void setBleDeviceName(String bleDeviceName) {
            this.bleDeviceName = bleDeviceName;
        }

        public List<Degree> getDegreeList() {
            return degreeList;
        }

        public void setDegreeList(List<Degree> degreeList) {
            this.degreeList = degreeList;
        }
    }

    /**
     * JSON 字串轉物件
     *
     * @param jsonString json 格式的資料
     * @return TemperatureReceives 物件
     */
    public static BleUserData newInstance(String jsonString) {

        if (TextUtils.isEmpty(jsonString)) {
            return new BleUserData();
        }

        Gson gson = new Gson();
        BleUserData item;

        try {
            item = gson.fromJson(jsonString, BleUserData.class);
        } catch (Exception e) {
            e.printStackTrace();
            item = new BleUserData();
        }

        return item;
    }

    /**
     * SignInAPI 物件轉 JSON字串
     *
     * @return json 格式的資料
     */
    public String toJSONString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
