package com.cns.captaindelivery.models;

import java.util.List;

public class ResultOrderList extends BaseResult {

    List<InfoOrder> data ;

    public List<InfoOrder> getData() {
        return data;
    }

    public void setData(List<InfoOrder> data) {
        this.data = data;
    }


}
