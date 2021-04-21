package com.example.yhyhealthy.dataBean;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**  **************
 * 症狀專用DataBean
 * 因為value值有boolean跟Array所以要自己設計
 * create: 2021/04/14
 * *************       *****/

public class SymptomData {

    private int errorCode;

    private List<SwitchItemBean> switchItemBeanList;

    private List<CheckBoxGroup> checkBoxGroupList;

    public static class SwitchItemBean{
        private String key;
        private boolean value;

        //建構子
        public SwitchItemBean(String key, boolean value){
            this.key =  key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public boolean isValue() {
            return value;
        }

        public void setValue(boolean value) {
            this.value = value;
        }
    }

    public static class CheckBoxGroup{

        @Expose(serialize = true)
        @SerializedName("key")
        private String key;

        @Expose(serialize = false, deserialize = false)
        private List<String> value;

        @Expose(serialize = true)
        @SerializedName("value")
        private Set<String> checked = new HashSet<>();

        //建構子
        public CheckBoxGroup(String key, List<String> value){
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public List<String> getValue() {
            return value;
        }

        public List<String> getChecked()  {
            return Arrays.asList(checked.toArray(new String[checked.size()]));
        }

        public void setChecked(String checked) {
            if (this.checked.contains(checked)){
                this.checked.remove(checked);
            }else {
                this.checked.add(checked);
            }
        }
    }

    public int getErrorCode(){
        return errorCode;
    }
}
