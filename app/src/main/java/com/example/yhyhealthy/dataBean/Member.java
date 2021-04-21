package com.example.yhyhealthy.dataBean;

import java.util.ArrayList;
import java.util.List;

/********************************
 * 藍芽體溫Recycler使用者DataBean
 * 大頭貼 , 姓名
 * ******************************/

public class Member {
    private int image;      //user大頭貼
    private String name;    //user姓名
    private double degree;  //ble體溫
    private String battery; //ble電量
    private String Status;  //ble狀態

    private List<Degree> degreeList = new ArrayList<>();

    public Member(int image, String name, String status) {
        this.image = image;
        this.name = name;
        Status = status;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getDegree() {
        return degree;
    }

    //將取得的體溫跟日期填入Degree's dataBean
    public void setDegree(Double degree, String date) {
        this.degree = degree;
        Degree degree1 = new Degree(degree, date);
        degreeList.add(degree1);
    }

    public String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        this.Status = status;
    }

    public List<Degree> getDegreeList() {
        return degreeList;
    }

    public void setDegreeList(List<Degree> degreeList) {
        this.degreeList = degreeList;
    }
}
