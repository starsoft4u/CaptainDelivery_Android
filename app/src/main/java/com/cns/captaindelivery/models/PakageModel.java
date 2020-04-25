package com.cns.captaindelivery.models;

import com.google.gson.annotations.SerializedName;

public class PakageModel {

    @SerializedName("package_id")
    public String p_id;
    @SerializedName("name")
    public String p_name;
    @SerializedName("location")
    public String p_location;
    @SerializedName("image")
    public String p_image;

    public String getP_id() {
        return p_id;
    }

    public String getP_name() {
        return p_name;
    }

    public String getP_location() {
        return p_location;
    }

    public String getP_image() {
        return p_image;
    }


}
