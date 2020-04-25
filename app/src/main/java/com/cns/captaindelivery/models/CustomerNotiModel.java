package com.cns.captaindelivery.models;

import com.google.gson.annotations.SerializedName;

public class CustomerNotiModel {
    private String driver_noti_id;
    private String driver_id;
    private String description;
    private String noti_time;
    private String phone;
    private String token;
    private String rate;
    @SerializedName("image")
    private String driver_image;
    @SerializedName("name")
    private String driver_name;
    private String package_id;

    public String getDriver_noti_id() {
        return driver_noti_id;
    }

    public void setDriver_noti_id(String driver_noti_id) {
        this.driver_noti_id = driver_noti_id;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public void setDriver_id(String driver_id) {
        this.driver_id = driver_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNoti_time() {
        return noti_time;
    }

    public void setNoti_time(String noti_time) {
        this.noti_time = noti_time;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getDriver_name() {
        return driver_name;
    }



    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getDriver_image() {
        return driver_image;
    }



    public String getPackage_id() {
        return package_id;
    }

    public void setPackage_id(String package_id) {
        this.package_id = package_id;
    }
}
