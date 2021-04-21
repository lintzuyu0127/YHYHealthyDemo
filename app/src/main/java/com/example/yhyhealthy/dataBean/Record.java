package com.example.yhyhealthy.dataBean;

import android.text.TextUtils;

import com.google.gson.Gson;

public class Record {

    /**
     * success : {"type":"","userId":"","testDate":"2021-01-18","menstruation":"","measure":{"param":"0.8","paramName":"Ovulation","temperature":33.23,"weight":70.2},"status":{"bleeding":false,"breastPain":false,"intercourse":false},"secretions":{"color":"yellow","secretionType":"none","smell":"none","symptom":"burning"},"ovuRate":{"salivaRate":4,"btRate":1}}
     * errorCode : 0
     */

    private SuccessBean success;
    private int errorCode;

    public SuccessBean getSuccess() {
        return success;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public static class SuccessBean {
        /**
         * type :
         * userId :
         * testDate : 2021-01-18
         * menstruation :
         * measure : {"param":"0.8","paramName":"Ovulation","temperature":33.23,"weight":70.2}
         * status : {"bleeding":false,"breastPain":false,"intercourse":false}
         * secretions : {"color":"yellow","secretionType":"none","smell":"none","symptom":"burning"}
         * ovuRate : {"salivaRate":4,"btRate":1}
         */

        private String type;
        private String userId;
        private String testDate;
        private String menstruation;
        private MeasureBean measure;
        private StatusBean status;
        private SecretionsBean secretions;
        private OvuRateBean ovuRate;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getTestDate() {
            return testDate;
        }

        public void setTestDate(String testDate) {
            this.testDate = testDate;
        }

        public String getMenstruation() {
            return menstruation;
        }

        public void setMenstruation(String menstruation) {
            this.menstruation = menstruation;
        }

        public MeasureBean getMeasure() {
            return measure;
        }

        public void setMeasure(MeasureBean measure) {
            this.measure = measure;
        }

        public StatusBean getStatus() {
            return status;
        }

        public void setStatus(StatusBean status) {
            this.status = status;
        }

        public SecretionsBean getSecretions() {
            return secretions;
        }

        public void setSecretions(SecretionsBean secretions) {
            this.secretions = secretions;
        }

        public OvuRateBean getOvuRate() {
            return ovuRate;
        }

        public void setOvuRate(OvuRateBean ovuRate) {
            this.ovuRate = ovuRate;
        }

        public static class MeasureBean {
            /**
             * param : 0.8
             * paramName : Ovulation
             * temperature : 33.23
             * weight : 70.2
             */

            private String param;
            private String paramName;
            private double temperature;
            private double weight;

            public String getParam() {
                return param;
            }

            public void setParam(String param) {
                this.param = param;
            }

            public String getParamName() {
                return paramName;
            }

            public void setParamName(String paramName) {
                this.paramName = paramName;
            }

            public double getTemperature() {
                return temperature;
            }

            public void setTemperature(double temperature) {
                this.temperature = temperature;
            }

            public double getWeight() {
                return weight;
            }

            public void setWeight(double weight) {
                this.weight = weight;
            }
        }

        public static class StatusBean {
            /**
             * bleeding : false
             * breastPain : false
             * intercourse : false
             */

            private boolean bleeding;
            private boolean breastPain;
            private boolean intercourse;

            public boolean isBleeding() {
                return bleeding;
            }

            public void setBleeding(boolean bleeding) {
                this.bleeding = bleeding;
            }

            public boolean isBreastPain() {
                return breastPain;
            }

            public void setBreastPain(boolean breastPain) {
                this.breastPain = breastPain;
            }

            public boolean isIntercourse() {
                return intercourse;
            }

            public void setIntercourse(boolean intercourse) {
                this.intercourse = intercourse;
            }
        }

        public static class SecretionsBean {
            /**
             * color : yellow
             * secretionType : none
             * smell : none
             * symptom : burning
             */

            private String color;
            private String secretionType;
            private String smell;
            private String symptom;

            public String getColor() {
                return color;
            }

            public void setColor(String color) {
                this.color = color;
            }

            public String getSecretionType() {
                return secretionType;
            }

            public void setSecretionType(String secretionType) {
                this.secretionType = secretionType;
            }

            public String getSmell() {
                return smell;
            }

            public void setSmell(String smell) {
                this.smell = smell;
            }

            public String getSymptom() {
                return symptom;
            }

            public void setSymptom(String symptom) {
                this.symptom = symptom;
            }
        }

        public static class OvuRateBean {
            /**
             * salivaRate : 4
             * btRate : 1
             */

            private int salivaRate;
            private int btRate;

            public int getSalivaRate() {
                return salivaRate;
            }

            public void setSalivaRate(int salivaRate) {
                this.salivaRate = salivaRate;
            }

            public int getBtRate() {
                return btRate;
            }

            public void setBtRate(int btRate) {
                this.btRate = btRate;
            }
        }
    }

    /**
     * JSON 字串轉物件
     *
     * @param jsonString json 格式的資料
     * @return TemperatureReceives 物件
     */
    public static Record newInstance(String jsonString) {

        if (TextUtils.isEmpty(jsonString)) {
            return new Record();
        }

        Gson gson = new Gson();
        Record item;

        try {
            item = gson.fromJson(jsonString, Record.class);
        } catch (Exception e) {
            e.printStackTrace();
            item = new Record();
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
