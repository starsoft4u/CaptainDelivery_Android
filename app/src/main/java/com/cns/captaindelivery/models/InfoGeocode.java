package com.cns.captaindelivery.models;

public class InfoGeocode {

    String place_id;
    String formatted_address;

    InfoGeometry geometry;

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public String getFormatted_address() {
        return formatted_address;
    }

    public void setFormatted_address(String formatted_address) {
        this.formatted_address = formatted_address;
    }

    public InfoGeometry getGeometry() {
        return geometry;
    }

    public void setGeometry(InfoGeometry geometry) {
        this.geometry = geometry;
    }
}


