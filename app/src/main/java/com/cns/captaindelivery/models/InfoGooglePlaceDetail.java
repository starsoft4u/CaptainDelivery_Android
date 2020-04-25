package com.cns.captaindelivery.models;

public class InfoGooglePlaceDetail extends InfoGooglePlace{


    String adr_address;
    String international_phone_number;

    InfoGeometry geometry;
    InfoOpenHours opening_hours;

    public String getAdr_address() {
        return adr_address;
    }

    public void setAdr_address(String adr_address) {
        this.adr_address = adr_address;
    }

    public String getInternational_phone_number() {
        return international_phone_number;
    }

    public void setInternational_phone_number(String international_phone_number) {
        this.international_phone_number = international_phone_number;
    }

    public InfoGeometry getGeometry() {
        return geometry;
    }

    public void setGeometry(InfoGeometry geometry) {
        this.geometry = geometry;
    }

    public InfoOpenHours getOpening_hours() {
        return opening_hours;
    }

    public void setOpening_hours(InfoOpenHours opening_hours) {
        this.opening_hours = opening_hours;
    }
}


