package com.cns.captaindelivery.models;

import java.util.List;

public class ResultPackageList extends BaseResult {

    List<PakageModel> data ;

    public List<PakageModel> getData() {
        return data;
    }

    public void setData(List<PakageModel> data) {
        this.data = data;
    }


}
