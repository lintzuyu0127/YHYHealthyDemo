package com.example.yhyhealthy.dataBean;

public class ObserverData {
    int obServerImageView;
    String observerName;
    String observerGender;
    String observerBirthday;

    public ObserverData(int obServerImageView, String observerName, String observerGender, String observerBirthday) {
        this.obServerImageView = obServerImageView;
        this.observerName = observerName;
        this.observerGender = observerGender;
        this.observerBirthday = observerBirthday;
    }

    public int getObServerImageView() {
        return obServerImageView;
    }

    public void setObServerImageView(int obServerImageView) {
        this.obServerImageView = obServerImageView;
    }

    public String getObserverName() {
        return observerName;
    }

    public void setObserverName(String observerName) {
        this.observerName = observerName;
    }

    public String getObserverGender() {
        return observerGender;
    }

    public void setObserverGender(String observerGender) {
        this.observerGender = observerGender;
    }

    public String getObserverBirthday() {
        return observerBirthday;
    }

    public void setObserverBirthday(String observerBirthday) {
        this.observerBirthday = observerBirthday;
    }
}
