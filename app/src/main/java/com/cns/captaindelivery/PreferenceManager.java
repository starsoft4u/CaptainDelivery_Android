package com.cns.captaindelivery;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;


import com.cns.captaindelivery.services.MainService;
import com.cns.captaindelivery.utils.GPSTracker;
import com.cns.captaindelivery.utils.RealmMigrations;
import com.cns.captaindelivery.utils.Utils;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import io.realm.Realm;
import io.realm.RealmConfiguration;

@ReportsCrashes(formKey = "", // will not be used
        mailTo = "su0220@outlook.com",
        mode = ReportingInteractionMode.TOAST,
        resToastText = R.string.crash_toast_text)
public class PreferenceManager extends Application {

    public static final String TAG = PreferenceManager.class.getSimpleName();

    private static PreferenceManager mInstance;

    static SharedPreferences preferences;
    static SharedPreferences.Editor prefEditor;

    public static Realm realm;

    public static String PREF_KEY_USER_ID = "USER_ID";
    public static String PREF_KEY_USER_EMAIL = "USER_EMAIL";
    public static String PREF_KEY_PASSWORD = "PASSWORD";
    public static String PREF_KEY_USER_NAME = "USER_NAME";
    public static String PREF_KEY_COUNTRY_CODE = "COUNTRY_CODE";
    public static String PREF_KEY_PHONE = "PHONE";
    public static String PREF_KEY_USER_PHOTO = "USER_PHOTO";
    public static String PREF_KEY_ADDR_LAT = "ADDR_LAT";
    public static String PREF_KEY_ADDR_LNG = "ADDR_LNG";
    public static String PREF_KEY_COUNTRY = "COUNTRY";
    public static String PREF_KEY_CITY = "CITY";
    public static String PREF_KEY_REGION = "REGION";
    public static String PREF_KEY_CUSTOMER_ID = "CUSTOMER_ID";
    public static String PREF_KEY_DRIVER_ID = "DRIVER_ID";
    public static String PREF_KEY_LOGIN_METHOD = "LOGIN_METHOD";
    public static String PREF_KEY_USER_ROLE = "USER_ROLE";

    public static String PREF_KEY_FCM_TOKEN = "FCM_TOKEN";

    public static String PREF_KEY_RATE = "RATE";

    public static String PREF_KEY_CURRENT_LAT = "CurrentLat";                             // double
    public static String PREF_KEY_CURRENT_LNG = "CurrentLng";                             // double


    public static GPSTracker gpsTracker;

    private Intent serviceIntent = null;
    private ServiceConnection serviceConnection = null;
    public IPlaybackService myPlaybackService = null;

    public static void resetAll() {

        prefEditor.clear();
        prefEditor.commit();

    }

    public static void clear() {

        prefEditor.clear();
    }

    @Override
    public void onCreate() {
        ACRA.init(this);
        super.onCreate();

        mInstance = this;
        gpsTracker=new GPSTracker(this);
        preferences = getSharedPreferences("Ogrenciyim", Context.MODE_PRIVATE);
        prefEditor = preferences.edit();

        prefEditor.commit();

        // -----------------------------------------------------------------------------------------
        Realm.init(this);
        final RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .name("Flamingo.realm")
                .schemaVersion(2)
                .deleteRealmIfMigrationNeeded()
                .migration(new RealmMigrations())
                .build();

        Realm.setDefaultConfiguration(realmConfiguration);

        realm= Realm.getInstance(realmConfiguration);

        startMainService();

    }


    public static synchronized PreferenceManager getInstance() {
        return mInstance;
    }


    public void onTerminate() {
        // TODO Auto-generated method stub
        realm.close();
        realm.deleteAll();
        super.onTerminate();
    }

    public void startMainService(){

        if (Utils.isServiceExisted(this, MainService.class.getName()) != null){
            Log.i("LocationNotifier info", "service already exist...");
            if (myPlaybackService == null ){
                Log.i("LocationNotifier info", "service create...");
                serviceIntent = new Intent(this, MainService.class);
                serviceConnection = new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        Log.i("LocationNotifier info", "connected");
                        myPlaybackService = IPlaybackService.Stub.asInterface((IBinder) service);
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        Log.i("LocationNotifier info", "disconnected");
                        myPlaybackService = null;
                    }
                };
                bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            }else{

                Log.e("LocationNotifier info", "service real exist...");
            }
        }else{
            Log.e("LocationNotifier info", "service real create...");
            newServiceConnection();
        }
    }

    /**
     * Start new service connection.
     */
    public synchronized void newServiceConnection() {
        Log.i("LocationNotifier info", "new service ...");
        serviceIntent = new Intent(this, MainService.class);

        // Check if service is already running

        // Service is not running, start and bind it
        startService(serviceIntent);

        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                myPlaybackService = IPlaybackService.Stub.asInterface((IBinder) service);
                Log.i("LocationNotifier info", "success");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                myPlaybackService = null;
                Log.i("LocationNotifier info", "fail");
            }
        };

        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public static void setUserId(String UserId) {
        prefEditor.putString(PREF_KEY_USER_ID, UserId);
        prefEditor.commit();
    }

    public static String getUserId() {
        return preferences.getString(PREF_KEY_USER_ID, "");
    }

    public static void setUserEmail(String email)    {
        prefEditor.putString(PREF_KEY_USER_EMAIL,email);
        prefEditor.commit();
    }
    public static String getUserEmail(){
        return preferences.getString(PREF_KEY_USER_EMAIL," ");
    }

    public static void setPassword(String password)  {
        prefEditor.putString(PREF_KEY_PASSWORD,password);
        prefEditor.commit();
    }
    public static String getPassword(){
        return preferences.getString(PREF_KEY_PASSWORD,"");
    }

    public static void setUserName(String name)   {
        prefEditor.putString(PREF_KEY_USER_NAME,name);
        prefEditor.commit();
    }
    public static String getUserName(){
        return preferences.getString(PREF_KEY_USER_NAME,"");
    }

    public static void setCountryCode(String code)  {
        prefEditor.putString(PREF_KEY_COUNTRY_CODE,code);
        prefEditor.commit();
    }
    public static String getCountryCode(){
        return preferences.getString(PREF_KEY_COUNTRY_CODE,"");
    }

    public static void setPhone(String phone)   {
        prefEditor.putString(PREF_KEY_PHONE,phone);
        prefEditor.commit();
    }
    public static String getPhone(){
        return preferences.getString(PREF_KEY_PHONE,"");
    }
    public static void setUserPhoto(String userPhoto)   {
        prefEditor.putString(PREF_KEY_USER_PHOTO,userPhoto);
        prefEditor.commit();
    }
    public static String getUserPhoto(){
        return preferences.getString(PREF_KEY_USER_PHOTO,"");
    }

    public static void setAddrLat(String addrLat)   {
        prefEditor.putString(PREF_KEY_ADDR_LAT, addrLat);
        prefEditor.commit();
    }
    public static String getAddrLat(){
        return preferences.getString(PREF_KEY_ADDR_LAT,"");
    }
    public static void setAddrLng(String addrLng)   {
        prefEditor.putString(PREF_KEY_ADDR_LNG, addrLng);
        prefEditor.commit();
    }
    public static String getAddrLng(){
        return preferences.getString(PREF_KEY_ADDR_LNG,"");
    }
    public static void setCountry(String country)   {
        prefEditor.putString(PREF_KEY_COUNTRY, country);
        prefEditor.commit();
    }
    public static String getCountry(){
        return preferences.getString(PREF_KEY_COUNTRY,"");
    }
    public static void setCity(String city)   {
        prefEditor.putString(PREF_KEY_CITY, city);
        prefEditor.commit();
    }
    public static String getCity(){
        return preferences.getString(PREF_KEY_CITY,"");
    }
    public static void setRegion(String region)   {
        prefEditor.putString(PREF_KEY_REGION, region);
        prefEditor.commit();
    }
    public static String getRegion(){
        return preferences.getString(PREF_KEY_REGION,"");
    }
    public static void setCustomerId(String customerId)    {
        prefEditor.putString(PREF_KEY_CUSTOMER_ID,customerId);
        prefEditor.commit();
    }
    public static String getCustomerId(){
        return preferences.getString(PREF_KEY_CUSTOMER_ID,"");
    }
    public static void setDriverId(String driverId)    {
        prefEditor.putString(PREF_KEY_DRIVER_ID,driverId);
        prefEditor.commit();
    }
    public static String getDriverId() {
        return preferences.getString(PREF_KEY_DRIVER_ID,"");
    }
    public static void setLoginMethod(String loginMethod) {
        prefEditor.putString(PREF_KEY_LOGIN_METHOD, loginMethod);
        prefEditor.apply();
    }
    public static String getLoginMethod() {
        return preferences.getString(PREF_KEY_LOGIN_METHOD, "");
    }
    public static void setRole(int role)    {
        prefEditor.putInt(PREF_KEY_USER_ROLE,role);
        prefEditor.commit();
    }
    public static int getRole(){
        return preferences.getInt(PREF_KEY_USER_ROLE,-1);
    }

    public static void setRate(float rate)    {
        prefEditor.putFloat(PREF_KEY_RATE,rate);
        prefEditor.commit();
    }
    public static float getRate(){
        return preferences.getFloat(PREF_KEY_RATE,0);
    }

    public static void setCurrentLat(String currentLat)    {
        prefEditor.putString(PREF_KEY_CURRENT_LAT, currentLat);
        prefEditor.commit();
    }
    public static String getCurrentLat(){
        return preferences.getString(PREF_KEY_CURRENT_LAT,"");
    }

    public static void setCurrentLng(String currentLng)    {
        prefEditor.putString(PREF_KEY_CURRENT_LNG, currentLng);
        prefEditor.commit();
    }
    public static String getCurrentLng(){
        return preferences.getString(PREF_KEY_CURRENT_LNG,"");
    }

    public static void setFCMToken(String token)    {
        prefEditor.putString(PREF_KEY_FCM_TOKEN, token);
        prefEditor.commit();
    }
    public static String getFCMToken(){
        return preferences.getString(PREF_KEY_FCM_TOKEN,"");
    }



}
