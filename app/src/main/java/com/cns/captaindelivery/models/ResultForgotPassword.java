package com.cns.captaindelivery.models;

public class ResultForgotPassword extends BaseResult {
    int user_id;
    String code;

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
