package com.cns.captaindelivery.models;

public class ResultPersonalInfo extends BaseResult {
    String ext_id;             //customer_id, driver_id
    String image;

    public String getExt_id() {
        return ext_id;
    }

    public void setExt_id(String ext_id) {
        this.ext_id = ext_id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
