package com.cns.captaindelivery.models;

import java.util.List;

public class ResultGooglePlaceDetail {
    InfoGooglePlaceDetail result;
    String status;              //"OK"
    String error_message;

    public InfoGooglePlaceDetail getResult() {
        return result;
    }

    public void setResult(InfoGooglePlaceDetail result) {
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError_message() {
        return error_message;
    }

    public void setError_message(String error_message) {
        this.error_message = error_message;
    }
}
