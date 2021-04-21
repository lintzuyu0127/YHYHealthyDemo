package com.example.yhyhealthy.dataBean;

import android.text.TextUtils;

import com.google.gson.Gson;

public class ChangeUserBasic {

    /**
     * email : sovapol993@lovomon.com
     * gender : F
     * height : 160.0
     * mobile :
     * name : fish0127
     * telCode : CN
     * userAccount : demo20
     * weight : 55.0
     * birthday : 2009-01-21
     */

    private String email;
    private String gender;
    private double height;
    private String mobile;
    private String name;
    private String telCode;
    private String userAccount;
    private String birthday;
    private double weight;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTelCode() {
        return telCode;
    }

    public void setTelCode(String telCode) {
        this.telCode = telCode;
    }

    public String getUserAccount() {
        return userAccount;
    }

    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
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
