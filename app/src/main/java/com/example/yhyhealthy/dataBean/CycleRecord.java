package com.example.yhyhealthy.dataBean;

import android.text.TextUtils;
import com.google.gson.Gson;
import java.util.List;

/**
 * 週期狀態dataBean
 * 月曆專用
 * */

public class CycleRecord {

    /**
     * success : [{"testDate":"2021-02-05","temperature":0,"firstDay":true,"cycleStatus":[1,4]}]
     * errorCode : 0
     */

    private int errorCode;
    private List<SuccessBean> success;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public List<SuccessBean> getSuccess() {
        return success;
    }

    public void setSuccess(List<SuccessBean> success) {
        this.success = success;
    }

    public static class SuccessBean {
        /**
         * testDate : 2021-02-05
         * temperature : 0.0
         * firstDay : true
         * cycleStatus : [1,4]
         */

        private String testDate;
        private double temperature;
        private boolean firstDay;
        private List<Integer> cycleStatus;

        public String getTestDate() {
            return testDate;
        }

        public void setTestDate(String testDate) {
            this.testDate = testDate;
        }

        public double getTemperature() {
            return temperature;
        }

        public void setTemperature(double temperature) {
            this.temperature = temperature;
        }

        public boolean isFirstDay() {
            return firstDay;
        }

        public void setFirstDay(boolean firstDay) {
            this.firstDay = firstDay;
        }

        public List<Integer> getCycleStatus() {
            return cycleStatus;
        }

        public void setCycleStatus(List<Integer> cycleStatus) {
            this.cycleStatus = cycleStatus;
        }
    }
    /**
     * JSON 字串轉物件
     *
     * @param jsonString json 格式的資料
     * @return TemperatureReceives 物件
     */
    public static CycleRecord newInstance(String jsonString) {

        if (TextUtils.isEmpty(jsonString)) {
            return new CycleRecord();
        }

        Gson gson = new Gson();
        CycleRecord item;

        try {
            item = gson.fromJson(jsonString, CycleRecord.class);
        } catch (Exception e) {
            e.printStackTrace();
            item = new CycleRecord();
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
