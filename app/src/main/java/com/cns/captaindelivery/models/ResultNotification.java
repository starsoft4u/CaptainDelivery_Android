package com.cns.captaindelivery.models;

import java.util.List;

public class ResultNotification extends BaseResult {

    List<InfoNotification> data ;

    public List<InfoNotification> getData() {
        return data;
    }

    public void setData(List<InfoNotification> data) {
        this.data = data;
    }


}
