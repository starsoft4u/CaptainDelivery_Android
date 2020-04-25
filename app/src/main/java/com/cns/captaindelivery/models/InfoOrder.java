package com.cns.captaindelivery.models;

public class InfoOrder {

    int ord_id;
    String src_name;
    long ord_created;
    int ord_status;         //0:Pending, 11: Accepted, 21:Completed, 22:Canceled
    int ord_price;
    String ord_payment;


    public int getOrd_id() {
        return ord_id;
    }

    public void setOrd_id(int ord_id) {
        this.ord_id = ord_id;
    }

    public String getSrc_name() {
        return src_name;
    }

    public void setSrc_name(String src_name) {
        this.src_name = src_name;
    }

    public long getOrd_created() {
        return ord_created;
    }

    public void setOrd_created(long ord_created) {
        this.ord_created = ord_created;
    }

    public int getOrd_status() {
        return ord_status;
    }

    public void setOrd_status(int ord_status) {
        this.ord_status = ord_status;
    }

    public int getOrd_price() {
        return ord_price;
    }

    public void setOrd_price(int ord_price) {
        this.ord_price = ord_price;
    }

    public String getOrd_payment() {
        return ord_payment;
    }

    public void setOrd_payment(String ord_payment) {
        this.ord_payment = ord_payment;
    }
}
