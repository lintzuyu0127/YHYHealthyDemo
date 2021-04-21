package com.example.yhyhealthy.dataBean;

public class Remote {
    int image;
    String name;
    Double degree;

    public Remote(int image, String name, Double degree) {
        this.image = image;
        this.name = name;
        this.degree = degree;
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

    public void setDegree(Double degree) {
        this.degree = degree;
    }
}
