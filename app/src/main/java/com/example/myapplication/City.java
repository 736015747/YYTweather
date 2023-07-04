package com.example.myapplication;

import java.util.List;

public class City {
    private int id;
    private int pid;

    public City(int id, int pid, String cityCode, String cityName, String postCode, String areaCode) {
        this.id = id;
        this.pid = pid;
        this.cityCode = cityCode;
        this.cityName = cityName;
        this.postCode = postCode;
        this.areaCode = areaCode;
    }

    public City() {
    }

    private String cityCode;
    private String cityName;
    private String postCode;
    private String areaCode;
    private String ctime;
    private List<City> children;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getCtime() {
        return ctime;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime;
    }

    public List<City> getChildren() {
        return children;
    }

    public void setChildren(List<City> children) {
        this.children = children;
    }



    // 省略getter和setter方法
}

