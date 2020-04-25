package com.cns.captaindelivery.models;

public class InfoOrderDetail extends InfoOrder {
    int customer_id;
    String src_addr;
    String src_lat;
    String src_lng;
    String src_placeid;
    String src_placeicon;
    String dst_addr;
    String dst_lat;
    String dst_lng;
    long delivery_time;
    String ord_desc;

    public int getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(int customer_id) {
        this.customer_id = customer_id;
    }

    public String getSrc_addr() {
        return src_addr;
    }

    public void setSrc_addr(String src_addr) {
        this.src_addr = src_addr;
    }

    public String getSrc_lat() {
        return src_lat;
    }

    public void setSrc_lat(String src_lat) {
        this.src_lat = src_lat;
    }

    public String getSrc_lng() {
        return src_lng;
    }

    public void setSrc_lng(String src_lng) {
        this.src_lng = src_lng;
    }

    public String getSrc_placeid() {
        return src_placeid;
    }

    public void setSrc_placeid(String src_placeid) {
        this.src_placeid = src_placeid;
    }

    public String getSrc_placeicon() {
        return src_placeicon;
    }

    public void setSrc_placeicon(String src_placeicon) {
        this.src_placeicon = src_placeicon;
    }

    public String getDst_addr() {
        return dst_addr;
    }

    public void setDst_addr(String dst_addr) {
        this.dst_addr = dst_addr;
    }

    public String getDst_lat() {
        return dst_lat;
    }

    public void setDst_lat(String dst_lat) {
        this.dst_lat = dst_lat;
    }

    public String getDst_lng() {
        return dst_lng;
    }

    public void setDst_lng(String dst_lng) {
        this.dst_lng = dst_lng;
    }

    public long getDelivery_time() {
        return delivery_time;
    }

    public void setDelivery_time(long delivery_time) {
        this.delivery_time = delivery_time;
    }

    public String getOrd_desc() {
        return ord_desc;
    }

    public void setOrd_desc(String ord_desc) {
        this.ord_desc = ord_desc;
    }
}
