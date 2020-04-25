package com.cns.captaindelivery.models;

public class InfoNotification {
    int noti_id;
    int ord_id;
    int src_userid;
    int dst_userid;
    String noti_content;
    long noti_time;
    int noti_opened;
    String image;

    public int getNoti_id() {
        return noti_id;
    }

    public void setNoti_id(int noti_id) {
        this.noti_id = noti_id;
    }

    public int getOrd_id() {
        return ord_id;
    }

    public void setOrd_id(int ord_id) {
        this.ord_id = ord_id;
    }

    public int getSrc_userid() {
        return src_userid;
    }

    public void setSrc_userid(int src_userid) {
        this.src_userid = src_userid;
    }

    public int getDst_userid() {
        return dst_userid;
    }

    public void setDst_userid(int dst_userid) {
        this.dst_userid = dst_userid;
    }

    public String getNoti_content() {
        return noti_content;
    }

    public void setNoti_content(String noti_content) {
        this.noti_content = noti_content;
    }

    public long getNoti_time() {
        return noti_time;
    }

    public void setNoti_time(long noti_time) {
        this.noti_time = noti_time;
    }

    public int getNoti_opened() {
        return noti_opened;
    }

    public void setNoti_opened(int noti_opened) {
        this.noti_opened = noti_opened;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
