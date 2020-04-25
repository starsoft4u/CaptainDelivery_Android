package com.cns.captaindelivery.models;

public class InfoUser {
    String user_id;
    String name;
    String email;
    String country_code;
    String phone;
    String image;
    String country;
    String city;
    String region_street;

    String lat;
    String lng;

    String email_verify_status;
    String phone_verify_status;
    String signup_step;
    String auth;            //1:driver, 0:customer
    String ext_id;             //customer_id, driver_id

    float rate;

    int is_active;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getRegion_street() {
        return region_street;
    }

    public void setRegion_street(String region_street) {
        this.region_street = region_street;
    }

    public String getEmail_verify_status() {
        return email_verify_status;
    }

    public void setEmail_verify_status(String email_verify_status) {
        this.email_verify_status = email_verify_status;
    }

    public String getPhone_verify_status() {
        return phone_verify_status;
    }

    public void setPhone_verify_status(String phone_verify_status) {
        this.phone_verify_status = phone_verify_status;
    }

    public String getSignup_step() {
        return signup_step;
    }

    public void setSignup_step(String signup_step) {
        this.signup_step = signup_step;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getExt_id() {
        return ext_id;
    }

    public void setExt_id(String ext_id) {
        this.ext_id = ext_id;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public float getRate() {
        return rate;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    public int getIs_active() {
        return is_active;
    }

    public void setIs_active(int is_active) {
        this.is_active = is_active;
    }
}
