package com.example.yhyhealthy.dataBean;

import com.google.gson.Gson;

public class ChangeUserMarriage {

    /**
     * contraception : false
     * hasChild : true
     * married : true
     */

    private boolean contraception;
    private boolean hasChild;
    private boolean married;

    public boolean isContraception() {
        return contraception;
    }

    public void setContraception(boolean contraception) {
        this.contraception = contraception;
    }

    public boolean isHasChild() {
        return hasChild;
    }

    public void setHasChild(boolean hasChild) {
        this.hasChild = hasChild;
    }

    public boolean isMarried() {
        return married;
    }

    public void setMarried(boolean married) {
        this.married = married;
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
