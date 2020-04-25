package com.cns.captaindelivery.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ResultCustomerNoti extends BaseResult {

    List<CustomerNotiModel> data ;

    public List<CustomerNotiModel> getData() {
        return data;
    }

    public void setData(List<CustomerNotiModel> data) {
        this.data = data;
    }


}
