package com.cns.captaindelivery.models;

public class ResultRequestPhoneVerify  {
    boolean success;
    String message;



    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }


    public String getMessage() {
        return message;
    }


    public void setMessage(String message) {
        this.message = message;
    }


}
