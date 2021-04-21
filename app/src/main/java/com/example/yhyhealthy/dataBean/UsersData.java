package com.example.yhyhealthy.dataBean;

import android.text.TextUtils;

import com.google.gson.Gson;

/**
 * 使用者DataBean
 * */

public class UsersData {

    /**
     * success : {"userAccount":"demo20","name":"fish0127","gender":"F","email":"sovapol993@lovomon.com","birthday":"2003-01-29","telCode":"CN","mobile":"","height":160,"weight":55}
     * errorCode : 0
     */

    private SuccessBean success;
    private int errorCode;

    public SuccessBean getSuccess() {
        return success;
    }

    public void setSuccess(SuccessBean success) {
        this.success = success;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public static class SuccessBean {
        /**
         * userAccount : demo20
         * name : fish0127
         * gender : F
         * email : sovapol993@lovomon.com
         * birthday : 2003-01-29
         * telCode : CN
         * mobile :
         * height : 160
         * weight : 55
         */

        private String userAccount;
        private String name;
        private String gender;
        private String email;
        private String birthday;
        private String telCode;
        private String mobile;
        private double height;
        private double weight;

        public String getUserAccount() {
            return userAccount;
        }

        public void setUserAccount(String userAccount) {
            this.userAccount = userAccount;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getBirthday() {
            return birthday;
        }

        public void setBirthday(String birthday) {
            this.birthday = birthday;
        }

        public String getTelCode() {
            return telCode;
        }

        public void setTelCode(String telCode) {
            this.telCode = telCode;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public double getHeight() {
            return height;
        }

        public void setHeight(double height) {
            this.height = height;
        }

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }
    }

    /**
     * JSON 字串轉物件
     *
     * @param jsonString json 格式的資料
     * @return TemperatureReceives 物件
     */
    public static UsersData newInstance(String jsonString) {

        if (TextUtils.isEmpty(jsonString)) {
            return new UsersData();
        }

        Gson gson = new Gson();
        UsersData item;

        try {
            item = gson.fromJson(jsonString, UsersData.class);
        } catch (Exception e) {
            e.printStackTrace();
            item = new UsersData();
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
