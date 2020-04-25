package com.cns.captaindelivery;

import android.os.Environment;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class GlobalConst {
    public final static int ROLE_DRIVER = 1;
    public final static int ROLE_CUSTOMER = 2;

    public final static String LOGIN_NORMAL = "normal";
    public final static String LOGIN_FACEBOOK = "facebook";
    public final static String LOGIN_GOOGLE = "google";


    public final static String KEY_EMAIL = "key_email";
    public final static String KEY_PHONE_NUMBER = "key_phone_number";
    public final static String KEY_DLG = "key_dlg";

    public final static String KEY_CODE = "key_code";
    public final static String KEY_NAME = "key_name";
    public final static String KEY_PHONE = "key_phone";
    public final static String KEY_COUNTRY = "key_country";
    public final static String KEY_CITY = "key_city";

    public final static String KEY_PLACE_ID = "key_place_id";
    public final static String KEY_PLACE_NAME = "key_place_name";
    public final static String KEY_PLACE_ADDR = "key_place_addr";
    public final static String KEY_PLACE_ICON = "key_place_icon";
    public final static String KEY_LAT = "key_lat";
    public final static String KEY_LNG = "key_lng";
    public final static String KEY_LAT_DEST = "key_lat_dest";
    public final static String KEY_LNG_DEST = "key_lng_dest";

    public final static String KEY_ORDER_ID = "key_order_id";

    public final static String KEY_FROM_PROFILE = "KEY_FROM_PROFILE";
    public final static String KEY_FROM_NOFIFICATION = "KEY_FROM_NOTIFICATION";

    public final static String OK = "OK";


    public static long  GPS_MIN_TIME = 1L*60L*1000L;			//1min
    public static float GPS_MIN_DISTANCE = 100;			//100m
    public static long  NETWORK_MIN_TIME = 1L*60L*1000L;			//1min
    public static float NETWORK_MIN_DISTANCE = 100;			//100m

    public static List<String> ALLOWED_PLACE_TYPES = new ArrayList<>();

}
