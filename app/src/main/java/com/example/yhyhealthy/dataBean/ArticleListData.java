package com.example.yhyhealthy.dataBean;

import android.text.TextUtils;
import com.google.gson.Gson;
import java.util.List;

public class ArticleListData {

    private List<ArticleListBean> articleList;

    public List<ArticleListBean> getArticleList() {
        return articleList;
    }

    public static class ArticleListBean {
        /**
         * id : 21
         * title : 孕媽咪“愛愛”的迷思
         * html : 21-zh-TW.html
         * img : 21-zh-TW.jpg
         */

        private String id;
        private String title;
        private String html;
        private String img;

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getHtml() {
            return html;
        }

        public String getImg() {
            return img;
        }
    }

    /**
     * JSON 字串轉 物件
     *
     * @param jsonString json 格式的資料
     * @return TemperatureReceives 物件
     */
    public static ArticleListData newInstance(String jsonString) {

        if (TextUtils.isEmpty(jsonString)) {
            return new ArticleListData();
        }

        Gson gson = new Gson();
        ArticleListData item;

        try {
            item = gson.fromJson(jsonString, ArticleListData.class);
        } catch (Exception e) {
            e.printStackTrace();
            item = new ArticleListData();
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
