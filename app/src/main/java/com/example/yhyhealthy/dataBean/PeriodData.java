package com.example.yhyhealthy.dataBean;

import android.text.TextUtils;

import com.google.gson.Gson;

public class PeriodData {

    /**
     * success : {"cycle":28,"period":6,"lastDate":"2021-01-05","endDate":"2021-01-10"}
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
         * cycle : 28
         * period : 6
         * lastDate : 2021-01-05
         * endDate : 2021-01-10
         */

        private int cycle;
        private int period;
        private String lastDate;
        private String endDate;

        public int getCycle() {
            return cycle;
        }

        public void setCycle(int cycle) {
            this.cycle = cycle;
        }

        public int getPeriod() {
            return period;
        }

        public void setPeriod(int period) {
            this.period = period;
        }

        public String getLastDate() {
            return lastDate;
        }

        public void setLastDate(String lastDate) {
            this.lastDate = lastDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }
    }

    /**
     * JSON 字串轉物件
     *
     * @param jsonString json 格式的資料
     * @return 物件
     */
    public static PeriodData newInstance(String jsonString) {

        if (TextUtils.isEmpty(jsonString)) {
            return new PeriodData();
        }

        Gson gson = new Gson();
        PeriodData item;

        try {
            item = gson.fromJson(jsonString, PeriodData.class);
        } catch (Exception e) {
            e.printStackTrace();
            item = new PeriodData();
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
